# Restaura un respaldo de SIGEM en una base nueva o vacía (Windows / PowerShell)
# Detenga la aplicación antes de restaurar.
# Uso:  .\scripts\restaurar.ps1 -Archivo backups\sigem_db_20261012_020000.sql [-Base getion_sigem]
param(
    [Parameter(Mandatory = $true)][string]$Archivo,
    [string]$Base = "getion_sigem",
    [string]$Usuario = "postgres",
    [string]$Servidor = "localhost",
    [int]$Puerto = 5432
)
$psql = (Get-Command psql -ErrorAction SilentlyContinue).Source
if (-not $psql) {
    $psql = Get-ChildItem "C:\Program Files\PostgreSQL\*\bin\psql.exe" -ErrorAction SilentlyContinue |
            Sort-Object FullName -Descending | Select-Object -First 1 -ExpandProperty FullName
}
if (-not $psql) { Write-Error "No se encontró psql."; exit 1 }
if (-not (Test-Path $Archivo)) { Write-Error "No existe el archivo $Archivo"; exit 1 }
if (-not $env:PGPASSWORD) {
    $seg = Read-Host "Contraseña de $Usuario" -AsSecureString
    $env:PGPASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($seg))
}
$conf = Read-Host "Se BORRARÁ y recreará la base '$Base'. Escriba SI para continuar"
if ($conf -ne "SI") { Write-Host "Cancelado."; exit 0 }
& $psql -h $Servidor -p $Puerto -U $Usuario -d postgres -c "DROP DATABASE IF EXISTS $Base WITH (FORCE);" -c "CREATE DATABASE $Base ENCODING 'UTF8' TEMPLATE template0;"
& $psql -h $Servidor -p $Puerto -U $Usuario -d $Base -v ON_ERROR_STOP=1 -q -f $Archivo
if ($LASTEXITCODE -eq 0) { Write-Host "Base '$Base' restaurada desde $Archivo" -ForegroundColor Green }
else { Write-Error "La restauración terminó con errores." }
