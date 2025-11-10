#!/usr/bin/env pwsh
# Compile all Java sources and run Main
Write-Host "Compiling Java sources..."
javac -d out $(Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
if ($LASTEXITCODE -ne 0) { Write-Error "Compilation failed."; exit $LASTEXITCODE }

Write-Host "Running Main..."
java -cp out Main
