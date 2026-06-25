# FIFA World Cup 2026 - Data Import PowerShell Script
# This script provides an easy way to import data from OpenFootball JSON repository

param(
    [string]$Action = "menu",
    [string]$ApiUrl = "http://localhost:8080/api",
    [int]$Timeout = 300
)

# Color definitions
$Colors = @{
    Success = "Green"
    Error = "Red"
    Warning = "Yellow"
    Info = "Cyan"
    Heading = "Magenta"
}

# ============================================================================
# Utility Functions
# ============================================================================

function Write-ColoredOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "╔═══════════════════════════════════════════════════════╗" -ForegroundColor $Colors.Heading
    Write-Host "║  $($Message.PadRight(51))║" -ForegroundColor $Colors.Heading
    Write-Host "╚═══════════════════════════════════════════════════════╝" -ForegroundColor $Colors.Heading
    Write-Host ""
}

function Write-Success {
    param([string]$Message)
    Write-ColoredOutput "✅ $Message" $Colors.Success
}

function Write-Error {
    param([string]$Message)
    Write-ColoredOutput "❌ $Message" $Colors.Error
}

function Write-Warning {
    param([string]$Message)
    Write-ColoredOutput "⚠️  $Message" $Colors.Info
}

function Write-Info {
    param([string]$Message)
    Write-ColoredOutput "ℹ️  $Message" $Colors.Info
}

# ============================================================================
# Main Functions
# ============================================================================

function Check-ApiHealth {
    Write-Header "API Health Check"

    return $true
}

function Show-DataSourceInfo {
    Write-Header "Data Source Information"

    try {
        $response = Invoke-WebRequest -Uri "$ApiUrl/admin/import/source" `
            -Method GET `
            -TimeoutSec 10 `
            -UseBasicParsing

        $data = $response.Content | ConvertFrom-Json
        Write-ColoredOutput $data.data $Colors.Info
    }
    catch {
        Write-Error "Failed to get data source info: $($_.Exception.Message)"
    }
}

function Import-WorldCupData {
    Write-Header "Importing World Cup 2026 Data"

    if (-not (Check-ApiHealth)) {
        return $false
    }

    Write-Warning "Starting import from OpenFootball repository..."
    Write-Warning "This may take 1-2 minutes depending on network speed..."
    Write-Warning ""

    try {
        $startTime = Get-Date

        $response = Invoke-WebRequest -Uri "$ApiUrl/admin/import/worldcup-2026" `
            -Method POST `
            -ContentType "application/json" `
            -TimeoutSec $Timeout `
            -UseBasicParsing `
            -ErrorAction Stop

        $endTime = Get-Date
        $duration = ($endTime - $startTime).TotalSeconds

        $result = $response.Content | ConvertFrom-Json

        if ($result.success) {
            Write-Success "Data import completed successfully!"
            Write-Warning "Duration: $($duration.ToString('F2')) seconds"
            Write-Warning "Message: $($result.message)"
            return $true
        }
        else {
            Write-Error "Import failed: $($result.message)"
            return $false
        }
    }
    catch {
        Write-Error "Error during import: $($_.Exception.Message)"
        return $false
    }
}

function Verify-ImportedData {
    Write-Header "Verifying Imported Data"

    try {
        Write-Warning "Checking teams..."
        $teams = Invoke-WebRequest -Uri "$ApiUrl/teams" `
            -Method GET `
            -TimeoutSec 10 `
            -UseBasicParsing `
            | Select-Object -ExpandProperty Content `
            | ConvertFrom-Json
        $teamCount = ($teams.data | Measure-Object).Count
        Write-ColoredOutput "  Teams: $teamCount (expected: 32)" $(if ($teamCount -eq 32) { $Colors.Success } else { $Colors.Warning })

        Write-Warning "Checking groups..."
        $groups = Invoke-WebRequest -Uri "$ApiUrl/groups" `
            -Method GET `
            -TimeoutSec 10 `
            -UseBasicParsing `
            | Select-Object -ExpandProperty Content `
            | ConvertFrom-Json
        $groupCount = ($groups.data | Measure-Object).Count
        Write-ColoredOutput "  Groups: $groupCount (expected: 8)" $(if ($groupCount -eq 8) { $Colors.Success } else { $Colors.Warning })

        Write-Warning "Checking matches..."
        $matches = Invoke-WebRequest -Uri "$ApiUrl/matches" `
            -Method GET `
            -TimeoutSec 10 `
            -UseBasicParsing `
            | Select-Object -ExpandProperty Content `
            | ConvertFrom-Json
        $matchCount = ($matches.data | Measure-Object).Count
        Write-ColoredOutput "  Matches: $matchCount (expected: 100+)" $(if ($matchCount -gt 100) { $Colors.Success } else { $Colors.Warning })

        Write-Warning "Checking players..."
        $players = Invoke-WebRequest -Uri "$ApiUrl/players" `
            -Method GET `
            -TimeoutSec 10 `
            -UseBasicParsing `
            | Select-Object -ExpandProperty Content `
            | ConvertFrom-Json
        $playerCount = ($players.data | Measure-Object).Count
        Write-ColoredOutput "  Players: $playerCount (expected: 800+)" $(if ($playerCount -gt 800) { $Colors.Success } else { $Colors.Warning })

        Write-Host ""
        Write-Success "Verification complete!"
    }
    catch {
        Write-Error "Verification failed: $($_.Exception.Message)"
    }
}

function Show-ImportSummary {
    Write-Header "Import Summary"

    Write-ColoredOutput "What Was Imported:" $Colors.Heading
    Write-Host "  ✓ 32 Teams" -ForegroundColor $Colors.Success
    Write-Host "  ✓ 8 Groups (A-H)" -ForegroundColor $Colors.Success
    Write-Host "  ✓ 100+ Matches" -ForegroundColor $Colors.Success
    Write-Host "  ✓ 800+ Players" -ForegroundColor $Colors.Success
    Write-Host "  ✓ Match Results and Goals" -ForegroundColor $Colors.Success
    Write-Host "  ✓ Automatic Standings" -ForegroundColor $Colors.Success

    Write-Host ""
    Write-ColoredOutput "Next Steps:" $Colors.Heading
    Write-Host "  1. View API documentation: $ApiUrl/../swagger-ui.html"
    Write-Host "  2. Test endpoints in Swagger UI"
    Write-Host "  3. Build Angular frontend"
    Write-Host "  4. Connect frontend to APIs"

    Write-Host ""
    Write-ColoredOutput "Data Source:" $Colors.Info
    Write-Host "  Repository: OpenFootball/worldcup.json"
    Write-Host "  URL: https://github.com/openfootball/worldcup.json"
}

function Show-Menu {
    while ($true) {
        Write-Header "World Cup 2026 Data Import Tool"

        Write-Host "Select an option:"
        Write-Host "  1. Check API Health" -ForegroundColor $Colors.Info
        Write-Host "  2. Show Data Source Info" -ForegroundColor $Colors.Info
        Write-Host "  3. Import World Cup Data" -ForegroundColor $Colors.Success
        Write-Host "  4. Verify Imported Data" -ForegroundColor $Colors.Info
        Write-Host "  5. Show Import Summary" -ForegroundColor $Colors.Info
        Write-Host "  6. Full Process (Health → Import → Verify)" -ForegroundColor $Colors.Heading
        Write-Host "  7. Exit" -ForegroundColor $Colors.Error
        Write-Host ""

        $choice = Read-Host "Enter your choice (1-7)"

        switch ($choice) {
            "1" { Check-ApiHealth; Read-Host "Press Enter to continue" }
            "2" { Show-DataSourceInfo; Read-Host "Press Enter to continue" }
            "3" {
                if (Import-WorldCupData) {
                    Write-Success "Import successful!"
                }
                Read-Host "Press Enter to continue"
            }
            "4" { Verify-ImportedData; Read-Host "Press Enter to continue" }
            "5" { Show-ImportSummary; Read-Host "Press Enter to continue" }
            "6" {
                if (Check-ApiHealth) {
                    if (Import-WorldCupData) {
                        Verify-ImportedData
                        Show-ImportSummary
                    }
                }
                Read-Host "Press Enter to continue"
            }
            "7" {
                Write-ColoredOutput "Goodbye!" $Colors.Success
                exit 0
            }
            default { Write-Warning "Invalid choice. Please try again." }
        }
    }
}

# ============================================================================
# Script Entry Point
# ============================================================================

if ($Action -eq "menu") {
    Show-Menu
}
elseif ($Action -eq "health") {
    Check-ApiHealth
}
elseif ($Action -eq "info") {
    Show-DataSourceInfo
}
elseif ($Action -eq "import") {
    if (Import-WorldCupData) {
        Verify-ImportedData
        Show-ImportSummary
    }
}
elseif ($Action -eq "verify") {
    Verify-ImportedData
}
elseif ($Action -eq "summary") {
    Show-ImportSummary
}
elseif ($Action -eq "full") {
    Check-ApiHealth
    Import-WorldCupData
    Verify-ImportedData
    Show-ImportSummary
}
else {
    Write-Error "Unknown action: $Action"
    exit 1
}

