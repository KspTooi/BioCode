$ErrorActionPreference = "Stop"

Write-Host "=== MCP Inspector 启动脚本 ===" -ForegroundColor Cyan
Write-Host ""

$nodeVersion = node --version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "错误: 未检测到 Node.js，请先安装 Node.js" -ForegroundColor Red
    pause
    exit 1
}
Write-Host "[OK] Node.js 版本: $nodeVersion" -ForegroundColor Green

$npmVersion = npm --version 2>&1
Write-Host "[OK] npm 版本: $npmVersion" -ForegroundColor Green
Write-Host ""

$packageName = "@modelcontextprotocol/inspector"
$globalNpmModules = npm root -g 2>&1
$globalNpmModules = $globalNpmModules.Trim()
$inspectorPath = Join-Path $globalNpmModules $packageName

if (Test-Path $inspectorPath) {
    Write-Host "[跳过] $packageName 已安装" -ForegroundColor Yellow
    Write-Host "  路径: $inspectorPath" -ForegroundColor Gray
} else {
    Write-Host "[安装] 正在全局安装 $packageName ..." -ForegroundColor Cyan
    npm install -g $packageName 2>&1 | ForEach-Object { Write-Host "  $_" }
    if ($LASTEXITCODE -ne 0) {
        Write-Host "错误: 安装失败" -ForegroundColor Red
        pause
        exit 1
    }
    Write-Host "[OK] 安装完成" -ForegroundColor Green
}

Write-Host ""
Write-Host "[启动] 正在启动 MCP Inspector ..." -ForegroundColor Cyan
Write-Host "  SSE 目标: http://127.0.0.1:27500/aacp/upstream/test" -ForegroundColor Gray
Write-Host ""

npx @modelcontextprotocol/inspector npx -y supergateway --sse http://127.0.0.1:27500/aacp/upstream/test

pause
