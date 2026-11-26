
$baseUrl = "https://agrogeorgia-soil-health-monitoring.onrender.com"

Write-Host "AGROGEORGIA API TESTING" -ForegroundColor Cyan
Write-Host "-----------------------"

# Test 1: Valid soil input
Write-Host "`nTest 1: Valid soil input"

try {
    $validUrl = "$baseUrl/api/predict?moisture=50&ph=6.5&nitrogen=90&phosphorus=42&potassium=43&temperature=25&humidity=80&rainfall=200"

    $result = Invoke-RestMethod -Uri $validUrl -Method Get

    Write-Host "PASS: Valid prediction request completed." -ForegroundColor Green
    Write-Host ($result | ConvertTo-Json -Depth 5)
}
catch {
    Write-Host "FAIL: Valid prediction request failed." -ForegroundColor Red
    Write-Host $_.Exception.Message
}

# Test 2: Invalid pH value
Write-Host "`nTest 2: Invalid pH value"

try {
    $invalidUrl = "$baseUrl/api/predict?moisture=50&ph=20&nitrogen=90&phosphorus=42&potassium=43&temperature=25&humidity=80&rainfall=200"

    Invoke-RestMethod -Uri $invalidUrl -Method Get

    Write-Host "FAIL: Invalid pH was accepted." -ForegroundColor Red
}
catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 400) {
        Write-Host "PASS: Invalid pH was rejected with HTTP 400." -ForegroundColor Green
    }
    else {
        Write-Host "FAIL: Unexpected response." -ForegroundColor Red
        Write-Host $_.Exception.Message
    }
}

# Test 3: Unsupported HTTP method
Write-Host "`nTest 3: Unsupported HTTP method"

try {
    Invoke-WebRequest -Uri "$baseUrl/api/history" -Method Post -UseBasicParsing

    Write-Host "FAIL: Unsupported method was accepted." -ForegroundColor Red
}
catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 405) {
        Write-Host "PASS: POST request was rejected with HTTP 405." -ForegroundColor Green
    }
    else {
        Write-Host "FAIL: Unexpected response." -ForegroundColor Red
        Write-Host $_.Exception.Message
    }
}

Write-Host "`nAPI testing completed." -ForegroundColor Cyan
