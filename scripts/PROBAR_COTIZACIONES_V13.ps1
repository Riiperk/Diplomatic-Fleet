# Prueba de escritura controlada. Windows PowerShell 5.1.
# Usar un backend LOCAL conectado a PostgreSQL LOCAL con datos ficticios.
$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'
$confirmar = Read-Host 'Confirme que el backend local apunta a una base LOCAL de prueba (escriba LOCAL)'
if ($confirmar -cne 'LOCAL') { throw 'Prueba cancelada.' }
function Abrir-Sesion([string]$rol) {
    $correo = Read-Host "Correo de $rol"
    $segura = Read-Host "Contrasena de $rol" -AsSecureString
    $clave = [System.Net.NetworkCredential]::new('', $segura).Password
    $basic = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes("${correo}:$clave"))
    try {
        $sesion = Invoke-RestMethod -Uri "$base/api/auth/sesion" -Method Post -Headers @{Authorization="Basic $basic";Accept='application/json'}
        if ($sesion.perfil.rol -ne $rol) { throw "Rol incorrecto: se esperaba $rol." }
        return @{Authorization="Bearer $($sesion.token)";Accept='application/json'}
    } finally { Remove-Variable segura,clave,basic -ErrorAction SilentlyContinue }
}
function Invocar([string]$metodo,[string]$ruta,[hashtable]$cabeceras,$datos=$null) {
    $params = @{Uri="$base$ruta";Method=$metodo;Headers=$cabeceras}
    if ($null -ne $datos) { $params.ContentType='application/json';$params.Body=($datos | ConvertTo-Json -Depth 6 -Compress) }
    $respuesta=Invoke-RestMethod @params
    if ($respuesta -is [Array]) { foreach ($elemento in $respuesta) { Write-Output $elemento } } else { return $respuesta }
}
function Comprobar([bool]$condicion,[string]$mensaje) {
    if (!$condicion) { throw "FALLO: $mensaje" }
    Write-Host "OK: $mensaje" -ForegroundColor Green
}
function Esperar-Conflicto([scriptblock]$accion,[string]$mensaje) {
    try { & $accion | Out-Null } catch {
        $respuesta=$_.Exception.Response
        if ($respuesta -and [int]$respuesta.StatusCode -eq 409) { Write-Host "OK: $mensaje" -ForegroundColor Green;return }
        throw
    }
    throw "FALLO: $mensaje (se esperaba HTTP 409)"
}
$cliente=$null;$admin=$null;$r=$null;$cerrada=$false
try {
    $cliente=Abrir-Sesion 'CLIENTE'
    $admin=Abrir-Sesion 'ADMINISTRADOR'
    $inicio=[DateTime]::Today.AddDays(14).ToString('yyyy-MM-dd')+'T09:00:00-05:00'
    $fin=[DateTime]::Today.AddDays(16).ToString('yyyy-MM-dd')+'T09:00:00-05:00'
    $consulta='?inicio='+[Uri]::EscapeDataString($inicio)+'&fin='+[Uri]::EscapeDataString($fin)
    $flota=@(Invocar 'Get' "/api/public/vehiculos$consulta" @{})
    $vehiculo=$flota | Where-Object {$_.pasajeros -ge 1 -and $_.tarifaDiaria -gt 0} | Select-Object -First 1
    if (!$vehiculo) { throw 'No hay vehiculo disponible para el periodo de prueba.' }
    $datos=@{vehiculoId=$vehiculo.id;inicio=$inicio;fin=$fin;recogida='Bogota, Colombia';devolucion='Bogota, Colombia';pasajeros=1;observaciones='Prueba de regresion de cotizaciones';metodoPago='TRANSFERENCIA';modalidad='SIN_CONDUCTOR';requiereBlindaje=$false;solicitarCotizacion=$true}
    $r=Invocar 'Post' '/api/solicitudes' $cliente $datos
    Comprobar ($r.estado -eq 'COTIZADA' -and $r.estadoCotizacion -eq 'BORRADOR') 'Solicitud y borrador creados'
    $q=@(Invocar 'Get' '/api/admin/cotizaciones' $admin) | Where-Object {$_.solicitud_id -eq $r.id} | Select-Object -First 1
    Comprobar ($null -ne $q -and $q.estado -eq 'BORRADOR' -and [decimal]$q.valor_total -eq [decimal]$r.valorEstimado) 'Borrador visible para administracion y precio coherente'
    $publicadas=@(Invocar 'Get' '/api/cliente/cotizaciones' $cliente)
    Comprobar (-not ($publicadas | Where-Object {$_.id -eq $q.id})) 'Borrador interno no visible como propuesta publicada'
    Esperar-Conflicto {Invocar 'Patch' "/api/admin/solicitudes/$($r.id)/estado" $admin @{estado='CONFIRMADA'}} 'No se permite confirmar sin aceptacion'
    Esperar-Conflicto {Invocar 'Post' "/api/solicitudes/$($r.id)/pago/reportar" $cliente @{referencia='PRUEBA-0001'}} 'No se permite informar el pago sin aceptacion'
    $editada=Invocar 'Patch' "/api/admin/cotizaciones/$($q.id)/borrador" $admin @{validaHasta=[DateTime]::Today.AddDays(7).ToString('yyyy-MM-dd');observaciones='Propuesta de prueba sin cobro'}
    Comprobar ($editada.estado -eq 'BORRADOR') 'Edicion del borrador'
    $publicada=Invocar 'Patch' "/api/admin/cotizaciones/$($q.id)/estado" $admin @{estado='ENVIADA'}
    Comprobar ($publicada.estado -eq 'ENVIADA') 'Publicacion de propuesta'
    $propia=@(Invocar 'Get' '/api/cliente/cotizaciones' $cliente) | Where-Object {$_.id -eq $q.id} | Select-Object -First 1
    Comprobar ($null -ne $propia -and $propia.decidible -eq $true) 'Cliente puede decidir la propuesta publicada'
    $aceptada=Invocar 'Patch' "/api/cliente/cotizaciones/$($q.id)/estado" $cliente @{estado='ACEPTADA'}
    Comprobar ($aceptada.estado -eq 'ACEPTADA') 'Aceptacion registrada'
    Esperar-Conflicto {Invocar 'Patch' "/api/cliente/cotizaciones/$($q.id)/estado" $cliente @{estado='ACEPTADA'}} 'Se impide una segunda decision'
    $confirmada=Invocar 'Patch' "/api/admin/solicitudes/$($r.id)/estado" $admin @{estado='CONFIRMADA'}
    Comprobar ($confirmada.estado -eq 'CONFIRMADA') 'Administrador confirma tras aceptacion'
    $monto=[decimal]$r.valorEstimado
    $refLocal="LOCAL-$($r.codigo)"
    Esperar-Conflicto {Invocar 'Post' "/api/solicitudes/$($r.id)/pago/reportar" $cliente @{referencia=$refLocal;monto=($monto+1)}} 'Rechaza importe distinto al total'
    $reportada=Invocar 'Post' "/api/solicitudes/$($r.id)/pago/reportar" $cliente @{referencia=$refLocal;monto=$monto}
    Comprobar ($reportada.estadoPago -eq 'REPORTADO' -and [decimal]$reportada.montoReportado -eq $monto) 'Importe declarado persistido'
    Esperar-Conflicto {Invocar 'Patch' "/api/admin/solicitudes/$($r.id)/pago" $admin @{estado='VERIFICADO';referencia="REC-$refLocal";monto=($monto+1)}} 'No verifica importe incorrecto'
    $verificada=Invocar 'Patch' "/api/admin/solicitudes/$($r.id)/pago" $admin @{estado='VERIFICADO';referencia="REC-$refLocal";monto=$monto;observacion='Prueba local sin movimiento de dinero'}
    Comprobar ($verificada.estadoPago -eq 'VERIFICADO' -and [decimal]$verificada.montoVerificado -eq $monto) 'Verificacion con importe y referencia'
    $cancelada=Invocar 'Post' "/api/solicitudes/$($r.id)/cancelar" $cliente @{motivo='Prueba local de cancelacion y reembolso'}
    $cerrada=($cancelada.estado -eq 'CANCELADA')
    Comprobar ($cerrada -and $cancelada.estadoPago -eq 'REEMBOLSO_PENDIENTE') 'Cancelacion conserva reembolso pendiente'
    $devuelta=Invocar 'Patch' "/api/admin/solicitudes/$($r.id)/pago" $admin @{estado='REEMBOLSADO';referencia="DEV-$refLocal";monto=$monto;observacion='Prueba local sin movimiento de dinero'}
    Comprobar ($devuelta.estadoPago -eq 'REEMBOLSADO' -and [decimal]$devuelta.montoReembolsado -eq $monto) 'Reembolso externo registrado'
    Write-Host 'Los registros de esta prueba son exclusivamente ficticios; no se movio dinero.'
    Write-Host "PRUEBA COMPLETADA: $($r.codigo)" -ForegroundColor Green
} finally {
    if ($r -and !$cerrada -and $cliente) { try { Invocar 'Post' "/api/solicitudes/$($r.id)/cancelar" $cliente @{motivo='Limpieza de prueba interrumpida'} | Out-Null;Write-Host 'Solicitud de prueba cancelada.' } catch { Write-Warning "Revise y cancele manualmente la solicitud $($r.codigo)." } }
    foreach ($cabeceras in @($cliente,$admin)) { if ($cabeceras) { try { Invocar 'Post' '/api/auth/salir' $cabeceras | Out-Null } catch {} } }
}
