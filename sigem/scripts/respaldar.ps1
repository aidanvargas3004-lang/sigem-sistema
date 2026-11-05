# Respaldo manual de la base de datos SIGEM con pg_dump (Windows / PowerShell)
# Uso:  .\scripts\respaldar.ps1                       (pide la contraseña)
#       .\scripts\respaldar.ps1 -Base getion_sigem -Usuario postgres
param(
    [string]$Base = "getion_sigem",
    [string]$Usuario = "postgres",
    [string]$Servidor = "localhost",
    [int]$Puerto = 5432,
    [string]$Carpeta = "backups"
)
$pgDump = (Get-Command pg_dump -ErrorAction SilentlyContinue).Source
if (-not $pgDump) {
    $pgDump = Get-ChildItem "C:\Program Files\PostgreSQL\*\bin\pg_dump.exe" -ErrorAction SilentlyContinue |
              Sort-Object FullName -Descending | Select-Object -First 1 -ExpandProperty FullName
}
if (-not $pgDump) { Write-Error "No se encontró pg_dump. Instale PostgreSQL o agregue su carpeta bin al PATH."; exit 1 }
if (-not $env:PGPASSWORD) {
    $seg = Read-Host "Contraseña de $Usuario" -AsSecureString
    $env:PGPASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($seg))
}
New-Item -ItemType Directory -Force -Path $Carpeta | Out-Null
$archivo = Join-Path $Carpeta ("sigem_db_{0}.sql" -f (Get-Date -Format "yyyyMMdd_HHmmss"))
& $pgDump -h $Servidor -p $Puerto -U $Usuario -d $Base --no-owner --no-privileges --encoding=UTF8 -f $archivo
if ($LASTEXITCODE -eq 0) {
    Write-Host "Respaldo generado: $archivo ($([math]::Round((Get-Item $archivo).Length / 1KB, 1)) KB)" -ForegroundColor Green
} else {
    Write-Error "pg_dump terminó con error."
}
