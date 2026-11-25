# Crea cinco cuentas de demostracion mediante la API real, solo en el servidor local.
# No modifica usuarios existentes. Las contrasenas aleatorias se muestran una vez.
param([string]$AdminEmail = 'admin@diplomaticfleet.co')
$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'
$secret = Read-Host 'Contrasena del administrador actual' -AsSecureString
$plain = [System.Net.NetworkCredential]::new('', $secret).Password
$headers = @{Authorization = 'Basic ' + [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("${AdminEmail}:$plain"))}
$plain = $null; $secret.Dispose()
$profile = Invoke-RestMethod -Uri "$base/api/auth/me" -Headers $headers
if ($profile.rol -ne 'ADMINISTRADOR') { throw 'Se requiere una cuenta administradora activa.' }
$users = @(Invoke-RestMethod -Uri "$base/api/admin/usuarios" -Headers $headers)
# Genera una clave criptograficamente aleatoria de 24 caracteres mas prefijo.
function Nueva-Clave { $bytes = New-Object byte[] 18; $rng = [Security.Cryptography.RandomNumberGenerator]::Create(); try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }; return ('Df!'+[Convert]::ToBase64String($bytes)) }
# Guarda las credenciales nuevas en un directorio local del usuario, fuera de Git.
$vault = Join-Path $env:LOCALAPPDATA 'DiplomaticFleet\CuentasDemoV10'
New-Item -Path $vault -ItemType Directory -Force | Out-Null
$created = @()
$definitions = @(
 @{nombre='Admin';apellido='Secundario';correo='admin.secundario@example.com';rol='ADMINISTRADOR'},
 @{nombre='Operador';apellido='Flota';correo='operaciones.df@example.com';rol='OPERACIONES'},
 @{nombre='Conductor';apellido='Demostracion';correo='conductor.df@example.com';rol='CONDUCTOR';numeroLicencia='LIC-DF-DEMO-V10';categoriaLicencia='B1';vencimientoLicencia=(Get-Date).AddYears(2).ToString('yyyy-MM-dd')},
 @{nombre='Contacto';apellido='Institucional A';correo='cliente.a.df@example.com';rol='CLIENTE';nombreEntidad='Institucion Demo DF A';tipoEntidad='EMBAJADA';ciudad='Bogota';direccion='Direccion ficticia DF A';numeroIdentificacionFiscal='DF-DEMO-A'},
 @{nombre='Contacto';apellido='Institucional B';correo='cliente.b.df@example.com';rol='CLIENTE';nombreEntidad='Institucion Demo DF B';tipoEntidad='CONSULADO';ciudad='Cali';direccion='Direccion ficticia DF B';numeroIdentificacionFiscal='DF-DEMO-B'}
)
foreach($account in $definitions) {
 if($users | Where-Object { $_.correo -eq $account.correo }) { Write-Host "Ya existe $($account.correo); no se cambia su clave."; continue }
 $password = Nueva-Clave
 $account.password = $password
 try {
  if($account.rol -eq 'CLIENTE') { $payload = @{}; foreach($key in $account.Keys) {if($key -ne 'rol'){$payload[$key]=$account[$key]}}; $null = Invoke-RestMethod -Method Post -Uri "$base/api/auth/registro" -ContentType 'application/json; charset=utf-8' -Body ($payload | ConvertTo-Json -Compress) }
  else { $null = Invoke-RestMethod -Method Post -Uri "$base/api/admin/usuarios" -Headers $headers -ContentType 'application/json; charset=utf-8' -Body ($account | ConvertTo-Json -Compress) }
  $credential = [PSCredential]::new($account.correo,(ConvertTo-SecureString $password -AsPlainText -Force))
  $fileName = ($account.correo -replace '[^A-Za-z0-9._-]','_') + '.clixml'
  $credential | Export-Clixml -LiteralPath (Join-Path $vault $fileName)
  $created += [PSCustomObject]@{Rol=$account.rol;Correo=$account.correo;Contrasena=$password}
  Write-Host "Creada: $($account.correo)" -ForegroundColor Green
 } catch { Write-Warning "No se creo $($account.correo): $($_.Exception.Message)" }
}
if($created.Count -gt 0){ Write-Host "Copias protegidas de Windows: $vault"; Write-Host 'No publique las credenciales en GitHub.'; $created | Format-Table -AutoSize | Out-Host }
else { Write-Host 'No se generaron nuevas credenciales.' }
