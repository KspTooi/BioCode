param(
    [switch]$NoPause
)

function Show-Menu {
    Clear-Host
    Write-Host "==================== CodeGraph 工具 ====================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  1. 安装 CodeGraph (npx @colbymchenry/codegraph)" -ForegroundColor Yellow
    Write-Host "  2. 初始化索引 (codegraph init -i)" -ForegroundColor Yellow
    Write-Host "  3. 修复索引 (codegraph index)" -ForegroundColor Yellow
    Write-Host "  Q. 退出" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "=======================================================" -ForegroundColor Cyan
    Write-Host ""
}

function Install-CodeGraph {
    Write-Host "正在安装 CodeGraph..." -ForegroundColor Green
    npx @colbymchenry/codegraph
}

function Init-Index {
    Write-Host "正在初始化索引..." -ForegroundColor Green
    codegraph init -i
}

function Fix-Index {
    Write-Host "正在修复索引..." -ForegroundColor Green
    codegraph index
}

do {
    Show-Menu
    $choice = Read-Host "请选择操作"

    switch ($choice) {
        "1" {
            Install-CodeGraph
            if (-not $NoPause) {
                Write-Host ""
                Write-Host "按任意键返回菜单..." -ForegroundColor Gray
                $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
            }
        }
        "2" {
            Init-Index
            if (-not $NoPause) {
                Write-Host ""
                Write-Host "按任意键返回菜单..." -ForegroundColor Gray
                $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
            }
        }
        "3" {
            Fix-Index
            if (-not $NoPause) {
                Write-Host ""
                Write-Host "按任意键返回菜单..." -ForegroundColor Gray
                $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
            }
        }
        "q" { break }
        "Q" { break }
        default {
            Write-Host "无效选项，请重新选择" -ForegroundColor Red
            if (-not $NoPause) {
                Start-Sleep -Milliseconds 1500
            }
        }
    }
} while ($true)

Write-Host "已退出 CodeGraph 工具" -ForegroundColor Cyan
