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

# 从用户环境变量读取并设置到当前会话，若不存在则使用默认值并写回用户环境变量
Write-Host "[2/3] 正在配置环境变量..." -ForegroundColor Yellow

$envVars = @{
    "ANTHROPIC_BASE_URL"              = "https://api.deepseek.com/anthropic"
    "ANTHROPIC_MODEL"                 = "deepseek-v4-pro[1m]"
    "ANTHROPIC_DEFAULT_OPUS_MODEL"    = "deepseek-v4-pro[1m]"
    "ANTHROPIC_DEFAULT_SONNET_MODEL"  = "deepseek-v4-pro[1m]"
    "ANTHROPIC_DEFAULT_HAIKU_MODEL"   = "deepseek-v4-flash"
    "CLAUDE_CODE_SUBAGENT_MODEL"      = "deepseek-v4-flash"
}

foreach ($key in $envVars.Keys) {
    $userVal = [System.Environment]::GetEnvironmentVariable($key, "User")
    if ($userVal) {
        Set-Item -Path "Env:$key" -Value $userVal
    } else {
        $default = $envVars[$key]
        [System.Environment]::SetEnvironmentVariable($key, $default, "User")
        Set-Item -Path "Env:$key" -Value $default
        Write-Host "已初始化用户环境变量 $key = $default" -ForegroundColor DarkGray
    }
}

# 处理 API Key
$storedKey = [System.Environment]::GetEnvironmentVariable("ANTHROPIC_AUTH_TOKEN", "User")
if ($storedKey) {
    $masked = $storedKey.Substring(0, [Math]::Min(8, $storedKey.Length)) + "..."
    Write-Host "检测到已有 API Key: $masked（已隐藏）" -ForegroundColor Green
    $env:ANTHROPIC_AUTH_TOKEN = $storedKey
} else {
    Write-Host "未检测到 ANTHROPIC_AUTH_TOKEN 用户环境变量。" -ForegroundColor DarkYellow
}
Write-Host ""

$inputKey = Read-Host "请输入 DeepSeek API Key（直接回车使用现有配置）"

if ($inputKey -ne "") {
    [System.Environment]::SetEnvironmentVariable("ANTHROPIC_AUTH_TOKEN", $inputKey, "User")
    $env:ANTHROPIC_AUTH_TOKEN = $inputKey
    Write-Host "API Key 已保存到用户环境变量。" -ForegroundColor Green
} elseif (-not $env:ANTHROPIC_AUTH_TOKEN) {
    Write-Host ""
    Write-Host "[错误] 未提供 API Key 且用户环境变量中也不存在，无法启动。" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
} else {
    Write-Host "使用现有 API Key。" -ForegroundColor Green
}
Write-Host ""

# 处理思考强度
$storedEffort = [System.Environment]::GetEnvironmentVariable("CLAUDE_CODE_EFFORT_LEVEL", "User")
$effortDisplay = if ($storedEffort) { $storedEffort } else { "unset" }
Write-Host "思考强度（CLAUDE_CODE_EFFORT_LEVEL）：上次：$effortDisplay，回车保持" -ForegroundColor DarkYellow
Write-Host "可选：low / medium / high / xhigh / max / unset（回车保持上次选择）" -ForegroundColor DarkGray
$inputEffort = Read-Host "请输入思考强度"

if ($inputEffort -ne "") {
    if ($inputEffort -eq "unset") {
        [System.Environment]::SetEnvironmentVariable("CLAUDE_CODE_EFFORT_LEVEL", $null, "User")
        Remove-Item -Path "Env:CLAUDE_CODE_EFFORT_LEVEL" -ErrorAction SilentlyContinue
        Write-Host "思考强度已取消设置（unset）。" -ForegroundColor Green
    } elseif ($inputEffort -in @("low", "medium", "high", "xhigh", "max")) {
        [System.Environment]::SetEnvironmentVariable("CLAUDE_CODE_EFFORT_LEVEL", $inputEffort, "User")
        $env:CLAUDE_CODE_EFFORT_LEVEL = $inputEffort
        Write-Host "思考强度已设置为：$inputEffort" -ForegroundColor Green
    } else {
        Write-Host "无效输入，保持上次选择：$effortDisplay" -ForegroundColor DarkYellow
        if ($storedEffort) { $env:CLAUDE_CODE_EFFORT_LEVEL = $storedEffort }
    }
} else {
    if ($storedEffort) {
        $env:CLAUDE_CODE_EFFORT_LEVEL = $storedEffort
        Write-Host "保持上次选择：$storedEffort" -ForegroundColor Green
    } else {
        Remove-Item -Path "Env:CLAUDE_CODE_EFFORT_LEVEL" -ErrorAction SilentlyContinue
        Write-Host "保持上次选择：unset" -ForegroundColor Green
    }
}
Write-Host ""

# 处理全自动模式
$storedDangerously = [System.Environment]::GetEnvironmentVariable("CLAUDE_CODE_DANGEROUSLY_SKIP_PERMISSIONS", "User")
$defaultPrompt = if ($storedDangerously -eq "1") { "上次：开启，回车保持" } else { "上次：关闭，回车保持" }
Write-Host "全自动模式（--dangerously-skip-permissions）：$defaultPrompt" -ForegroundColor DarkYellow
$inputDangerously = Read-Host "是否开启全自动模式？(y=开启 / n=关闭 / 回车保持上次选择)"

if ($inputDangerously -eq "y") {
    [System.Environment]::SetEnvironmentVariable("CLAUDE_CODE_DANGEROUSLY_SKIP_PERMISSIONS", "1", "User")
    $dangerouslyMode = $true
    Write-Host "全自动模式已开启。" -ForegroundColor Green
} elseif ($inputDangerously -eq "n") {
    [System.Environment]::SetEnvironmentVariable("CLAUDE_CODE_DANGEROUSLY_SKIP_PERMISSIONS", "0", "User")
    $dangerouslyMode = $false
    Write-Host "全自动模式已关闭。" -ForegroundColor Green
} else {
    $dangerouslyMode = ($storedDangerously -eq "1")
    Write-Host "保持上次选择：$(if ($dangerouslyMode) { '开启' } else { '关闭' })" -ForegroundColor Green
}
Write-Host ""

# 启动 claude
Write-Host "[3/3] 正在启动 ClaudeCode..." -ForegroundColor Yellow
Write-Host ""
if ($dangerouslyMode) {
    claude --dangerously-skip-permissions
} else {
    claude
}
