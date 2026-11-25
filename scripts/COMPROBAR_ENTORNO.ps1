# Verifica componentes locales SIN mostrar ni modificar credenciales ni datos.
$ErrorActionPreference = 'Stop'
$raiz = (git rev-parse --show-toplevel).Trim()
Write-Host "Proyecto: $raiz"
Write-Host "Gradle wrapper: $(Test-Path (Join-Path $raiz 'gradlew.bat'))"
Write-Host "Vue package.json: $(Test-Path (Join-Path $raiz 'frontend/package.json'))"
Write-Host "Variables de entorno en esta terminal:"
'DB_URL','DB_USERNAME','DB_PASSWORD','ADMIN_EMAIL','ADMIN_PASSWORD' | ForEach-Object { Write-Host "  $_ = $([bool][Environment]::GetEnvironmentVariable($_))" }
$puertos=8080,5173
foreach($p in $puertos){
  $s=Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue
  Write-Host "Puerto $p: $(if($s){'ESCUCHANDO'}else{'SIN SERVIDOR'})"
}
