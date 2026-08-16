param(
    [string]$ApiBaseUrl = "http://localhost:8080",
    [string]$DeviceCode = "MOCK-AAS-001",
    [long]$TerminalUserId = 101,
    [string]$PunchTime,
    [string]$SourceRecordKey,
    [string]$DeviceApiKey = $env:ATTENDANCE_DEVICE_API_KEY
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($PunchTime)) {
    $punchDateTime = Get-Date
} else {
    $punchDateTime = [datetime]::Parse($PunchTime)
}

$punchTimeIso = $punchDateTime.ToString("yyyy-MM-ddTHH:mm:ss")

if ([string]::IsNullOrWhiteSpace($SourceRecordKey)) {
    $SourceRecordKey = "{0}:{1}:{2}:MOCK" -f `
        $DeviceCode, `
        $TerminalUserId, `
        $punchDateTime.ToString("yyyyMMddHHmmss")
}

$uri = $ApiBaseUrl.TrimEnd("/") + "/api/attendance/punches/batch"

$headers = @{
    "Content-Type" = "application/json"
}

if (-not [string]::IsNullOrWhiteSpace($DeviceApiKey)) {
    $headers["X-Device-Api-Key"] = $DeviceApiKey
}

$payload = @{
    deviceCode = $DeviceCode
    punches = @(
        @{
            terminalUserId = $TerminalUserId
            punchTime = $punchTimeIso
            sourceRecordKey = $SourceRecordKey
            rawPayload = (@{
                bridge = "mock-attendance-bridge.ps1"
                deviceUserId = $TerminalUserId
                generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
            } | ConvertTo-Json -Compress)
        }
    )
} | ConvertTo-Json -Depth 5

Invoke-RestMethod -Method Post -Uri $uri -Headers $headers -Body $payload
