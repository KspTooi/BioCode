# 设置控制台编码为 UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "         ClaudeCode 启动脚本" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# 安装/更新 claude-code
Write-Host "[1/3] 正在安装/更新 claude-code..." -ForegroundColor Yellow
npm install -g @anthropic-ai/claude-code@latest
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[错误] 安装失败，请检查 Node.js 和 npm 是否已正确安装。" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}
Write-Host ""

# 设置固定环境变量
Write-Host "[2/3] 正在配置环境变量..." -ForegroundColor Yellow
$env:ANTHROPIC_BASE_URL = "https://api.deepseek.com/anthropic"
$env:ANTHROPIC_MODEL = "deepseek-v4-pro[1m]"
$env:ANTHROPIC_DEFAULT_OPUS_MODEL = "deepseek-v4-pro[1m]"
$env:ANTHROPIC_DEFAULT_SONNET_MODEL = "deepseek-v4-pro[1m]"
$env:ANTHROPIC_DEFAULT_HAIKU_MODEL = "deepseek-v4-flash"
$env:CLAUDE_CODE_SUBAGENT_MODEL = "deepseek-v4-flash"
$env:CLAUDE_CODE_EFFORT_LEVEL = "max"

# 处理 API Key
if ($env:ANTHROPIC_AUTH_TOKEN) {
    $masked = $env:ANTHROPIC_AUTH_TOKEN.Substring(0, [Math]::Min(8, $env:ANTHROPIC_AUTH_TOKEN.Length)) + "..."
    Write-Host "检测到已有 API Key: $masked（已隐藏）" -ForegroundColor Green
} else {
    Write-Host "未检测到 ANTHROPIC_AUTH_TOKEN 环境变量。" -ForegroundColor DarkYellow
}
Write-Host ""

$inputKey = Read-Host "请输入 DeepSeek API Key（直接回车使用现有配置）"

if ($inputKey -ne "") {
    $env:ANTHROPIC_AUTH_TOKEN = $inputKey
    Write-Host "API Key 已设置为本次会话环境变量。" -ForegroundColor Green
} elseif (-not $env:ANTHROPIC_AUTH_TOKEN) {
    Write-Host ""
    Write-Host "[错误] 未提供 API Key 且环境变量中也不存在，无法启动。" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
} else {
    Write-Host "使用现有 API Key。" -ForegroundColor Green
}
Write-Host ""

# 启动 claude
Write-Host "[3/3] 正在启动 ClaudeCode..." -ForegroundColor Yellow
Write-Host ""
claude
