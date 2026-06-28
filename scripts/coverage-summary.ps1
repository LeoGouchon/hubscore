$reportPath = Join-Path $PSScriptRoot "..\target\site\jacoco\jacoco.csv"

if (!(Test-Path $reportPath)) {
    Write-Host "Coverage summary unavailable: $reportPath was not found."
    exit 1
}

$rows = Import-Csv $reportPath

function Format-Coverage {
    param(
        [int] $Missed,
        [int] $Covered
    )

    $total = $Missed + $Covered
    if ($total -eq 0) {
        return "n/a"
    }

    return "{0:N2}%" -f (($Covered / $total) * 100)
}

function Sum-Column {
    param(
        [string] $Name
    )

    return ($rows | Measure-Object -Property $Name -Sum).Sum
}

$instructionMissed = Sum-Column "INSTRUCTION_MISSED"
$instructionCovered = Sum-Column "INSTRUCTION_COVERED"
$branchMissed = Sum-Column "BRANCH_MISSED"
$branchCovered = Sum-Column "BRANCH_COVERED"
$lineMissed = Sum-Column "LINE_MISSED"
$lineCovered = Sum-Column "LINE_COVERED"
$methodMissed = Sum-Column "METHOD_MISSED"
$methodCovered = Sum-Column "METHOD_COVERED"

Write-Host ""
Write-Host "JaCoCo coverage summary"
Write-Host ("Instructions: {0}" -f (Format-Coverage $instructionMissed $instructionCovered))
Write-Host ("Branches:     {0}" -f (Format-Coverage $branchMissed $branchCovered))
Write-Host ("Lines:        {0}" -f (Format-Coverage $lineMissed $lineCovered))
Write-Host ("Methods:      {0}" -f (Format-Coverage $methodMissed $methodCovered))
Write-Host ""
Write-Host "Full report: target/site/jacoco/index.html"
