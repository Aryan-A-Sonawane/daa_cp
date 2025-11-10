# daa_cp

This repository contains implementations and a performance tester for dynamic range query data structures in Java (Segment Tree, Fenwick Tree, Range-Optimized BIT).

Quick commands (PowerShell on Windows):

Build and run:

```powershell
javac -d out $(Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java -cp out Main
```

Alternatively, use the included `run.ps1` script:

```powershell
.\run.ps1
```

Notes:
- The project requires a local JDK (javac/java) available on PATH.
- If you plan to push to GitHub, ensure your git credentials are set up (SSH key or Git credential manager).
