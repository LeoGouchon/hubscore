$ErrorActionPreference = 'Stop'

# Prepares the git-ignored local files of a worktree (used by `make install`).
# Each file is copied from the main checkout when available, otherwise created from a default.

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$gitCommonDir = git -C $repoRoot rev-parse --path-format=absolute --git-common-dir
if ($LASTEXITCODE -ne 0)
{
    throw "Unable to locate the git common directory."
}
$mainCheckout = Split-Path $gitCommonDir -Parent

function Install-LocalFile
{
    param(
        [string] $RelativePath,
        [scriptblock] $Fallback
    )

    $target = Join-Path $repoRoot $RelativePath
    if (Test-Path $target)
    {
        Write-Host "Using existing $RelativePath."
        return
    }

    $targetDir = Split-Path $target -Parent
    if (!(Test-Path $targetDir))
    {
        New-Item -ItemType Directory -Path $targetDir | Out-Null
    }

    $source = Join-Path $mainCheckout $RelativePath
    if (Test-Path $source)
    {
        Copy-Item $source $target
        Write-Host "Copied $RelativePath from main checkout ($mainCheckout)."
        return
    }

    & $Fallback $target
}

Install-LocalFile ".env" {
    param($target)
    New-Item -ItemType File -Path $target | Out-Null
    Write-Host "Created empty .env (no .env found in main checkout)."
}

Install-LocalFile "config/application.yml" {
    param($target)
    Copy-Item (Join-Path $repoRoot "config/application.example.yml") $target
    Write-Host "Created config/application.yml from config/application.example.yml. Review it before starting."
}
