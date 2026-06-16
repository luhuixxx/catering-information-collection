param(
  [string]$EsUrl = "http://localhost:9201",
  [string]$IndexName = "catering_post_v1",
  [string]$AliasName = "catering_post"
)

$ErrorActionPreference = "Stop"

# Usage:
#   powershell -ExecutionPolicy Bypass -File .\deploy\es\create-post-index.ps1 -EsUrl http://localhost:9201

$mappingPath = Join-Path $PSScriptRoot "post-index-v1.json"
if (!(Test-Path $mappingPath)) {
  throw "Mapping file not found: $mappingPath"
}

$body = Get-Content -Raw $mappingPath

Write-Host "Creating index $IndexName at $EsUrl ..."
try {
  Invoke-RestMethod -Method Put -Uri "$EsUrl/$IndexName" -ContentType "application/json" -Body $body | Out-Null
} catch {
  if ($_.Exception.Response -and $_.Exception.Response.StatusCode.Value__ -eq 400) {
    Write-Host "Index already exists, skip create." -ForegroundColor Yellow
  } else {
    throw
  }
}

Write-Host "Updating alias $AliasName -> $IndexName ..."
$aliasBody = @"
{
  "actions": [
    { "remove": { "alias": "$AliasName", "index": "*" } },
    { "add": { "alias": "$AliasName", "index": "$IndexName" } }
  ]
}
"@

Invoke-RestMethod -Method Post -Uri "$EsUrl/_aliases" -ContentType "application/json" -Body $aliasBody | Out-Null

Write-Host "Done."

