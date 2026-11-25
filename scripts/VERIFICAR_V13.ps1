# Verifica la API real sin crear reservas, usuarios ni modificar datos comerciales.
# Requiere Kotlin iniciado; crea una sesion temporal y la revoca al finalizar.
param([string]$BaseUrl='http://localhost:8080')
$ErrorActionPreference='Stop'
$BaseUrl=$BaseUrl.TrimEnd('/')
function Confirmar([bool]$Condicion,[string]$Nombre) {
    if (!$Condicion) { throw "FALLO: $Nombre" }
    Write-Host "OK: $Nombre" -ForegroundColor Green
}
function Obtener-Estado([string]$Ruta,[hashtable]$Headers=@{},[string]$Metodo='GET') {
    try {
        $r=Invoke-WebRequest -Uri ($BaseUrl+$Ruta) -Headers $Headers -Method $Metodo -UseBasicParsing -ErrorAction Stop
        return [pscustomobject]@{status=[int]$r.StatusCode; challenge=[string]$r.Headers['WWW-Authenticate']}
    } catch {
        $r=$_.Exception.Response
        if ($null -eq $r) { throw }
        return [pscustomobject]@{status=[int]$r.StatusCode; challenge=[string]$r.Headers['WWW-Authenticate']}
    }
}
function Entrar([string]$Correo,[Security.SecureString]$Clave) {
    $texto=[System.Net.NetworkCredential]::new('', $Clave).Password
    try {
        $basic=[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($Correo+':'+$texto))
        return Invoke-RestMethod -Uri ($BaseUrl+'/api/auth/sesion') -Method Post -Headers @{Authorization="Basic $basic";Accept='application/json'}
    } finally { $texto=$null; $basic=$null; $Clave.Dispose() }
}
$salud=Invoke-RestMethod -Uri ($BaseUrl+'/api/public/salud')
Confirmar ($salud.estado -eq 'OK') 'Backend y base de datos'
$flota=Invoke-RestMethod -Uri ($BaseUrl+'/api/public/vehiculos')
Write-Host "Vehiculos disponibles sin intervalo: $($flota.Count)"
if ($flota.Count -gt 0) { Confirmar ($null -ne $flota[0].nivelBlindaje) 'Catalogo devuelve nivel de blindaje' }
$sin=Obtener-Estado '/api/admin/dominio'
Confirmar ($sin.status -eq 401 -and [string]::IsNullOrEmpty($sin.challenge)) 'Sin sesion: 401 sin dialogo Basic'
$invalido=[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes('invalido@example.invalid:incorrecta'))
$mal=Obtener-Estado '/api/auth/sesion' @{Authorization="Basic $invalido"} 'POST'
Confirmar ($mal.status -eq 401 -and [string]::IsNullOrEmpty($mal.challenge)) 'Credenciales incorrectas: 401 sin reto'
$correo=Read-Host 'Correo de administrador'
$s=Entrar $correo (Read-Host 'Contrasena de administrador' -AsSecureString)
$h=@{Authorization=('Bearer '+$s.token);Accept='application/json'}
try {
    Confirmar ($s.perfil.rol -eq 'ADMINISTRADOR' -and $s.token.Length -eq 43) 'Sesion opaca administrativa'
    $me=Invoke-RestMethod -Uri ($BaseUrl+'/api/auth/me') -Headers $h
    Confirmar ($me.id -eq $s.perfil.id) 'Identidad por token'
    $d=Invoke-RestMethod -Uri ($BaseUrl+'/api/admin/dominio') -Headers $h
    Confirmar (($d.catalogos.PSObject.Properties | Measure-Object).Count -eq 18) '18 catalogos del diagrama'
    Confirmar ($null -ne $d.personas -and $null -ne $d.clientes -and $null -ne $d.vehiculos) 'Datos normalizados consultables'
    $null=Invoke-RestMethod -Uri ($BaseUrl+'/api/admin/reportes') -Headers $h
    Write-Host 'OK: reportes originales accesibles' -ForegroundColor Green
} finally {
    $null=Invoke-RestMethod -Uri ($BaseUrl+'/api/auth/salir') -Headers $h -Method Post
}
$revocado=Obtener-Estado '/api/auth/me' $h
Confirmar ($revocado.status -eq 401) 'Token revocado tras cierre de sesion'
$h=$null; $s=$null
$cliente=Read-Host 'Correo de cliente para probar separacion de roles (Enter para omitir)'
if (![string]::IsNullOrWhiteSpace($cliente)) {
    $c=Entrar $cliente (Read-Host 'Contrasena de cliente' -AsSecureString)
    $hc=@{Authorization=('Bearer '+$c.token);Accept='application/json'}
    try {
        Confirmar ($c.perfil.rol -eq 'CLIENTE') 'Rol cliente'
        $prohibido=Obtener-Estado '/api/admin/dominio' $hc
        Confirmar ($prohibido.status -eq 403) 'Cliente no puede consultar datos administrativos'
        $null=Invoke-RestMethod -Uri ($BaseUrl+'/api/solicitudes/mias') -Headers $hc
        Write-Host 'OK: consulta de reservas propias' -ForegroundColor Green
    } finally { $null=Invoke-RestMethod -Uri ($BaseUrl+'/api/auth/salir') -Headers $hc -Method Post }
}
Write-Host 'Verificacion HTTP terminada. Consulte docs/PRUEBAS_V13.md para pruebas de escritura.' -ForegroundColor Green
