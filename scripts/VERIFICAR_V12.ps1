# Comprobaciones de solo lectura con Kotlin en ejecucion. No solicita claves reales.
param([string]$BaseUrl = 'http://localhost:8080')
$ErrorActionPreference = 'Stop'
$archivo = [System.IO.Path]::GetTempFileName()
try {
    $invalida = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes('cuenta.inexistente@example.invalid:clave-de-prueba'))
    $codigo = curl.exe -sS -D $archivo -o NUL -w '%{http_code}' -H "Authorization: Basic $invalida" "$BaseUrl/api/auth/me"
    if ($LASTEXITCODE -ne 0 -or $codigo -ne '401') { throw "Se esperaba 401; recibido $codigo" }
    if (Select-String -Path $archivo -Pattern '^WWW-Authenticate:' -Quiet) { throw 'El servidor aun envia un reto HTTP Basic.' }
    Write-Host 'OK: credencial invalida -> 401 sin ventana de autenticacion.' -ForegroundColor Green
    $codigo = curl.exe -sS -D $archivo -o NUL -w '%{http_code}' "$BaseUrl/api/admin/usuarios"
    if ($LASTEXITCODE -ne 0 -or $codigo -ne '401') { throw "La ruta protegida debe rechazar acceso anonimo: $codigo" }
    if (Select-String -Path $archivo -Pattern '^WWW-Authenticate:' -Quiet) { throw 'El servidor aun envia un reto HTTP Basic.' }
    Write-Host 'OK: ruta administrativa protegida.' -ForegroundColor Green
    $codigo = curl.exe -sS -o NUL -w '%{http_code}' "$BaseUrl/api/public/vehiculos"
    if ($LASTEXITCODE -ne 0 -or $codigo -ne '200') { throw "Catalogo publico: se esperaba 200, recibido $codigo" }
    Write-Host 'OK: catalogo publico accesible.' -ForegroundColor Green
} finally { Remove-Item -LiteralPath $archivo -Force -ErrorAction SilentlyContinue }
