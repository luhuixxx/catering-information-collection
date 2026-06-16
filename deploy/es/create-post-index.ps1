$ErrorActionPreference = "Stop"

# Usage:
#   powershell -ExecutionPolicy Bypass -File .\deploy\es\create-post-index.ps1 -EsUrl http://localhost:9200
param(
  [string]$EsUrl = "http://localhost:9200",
  [string]$IndexName = "catering_post_v1",
  [string]$AliasName = "catering_post"
)

$mappingPath = Join-Path $PSScriptRoot "post-index-v1.json"
if (!(Test-Path $mappingPath)) {
  throw "Mapping file not found: $mappingPath"
}

$body = Get-Content -Raw $mappingPath

Write-Host "Creating index $IndexName at $EsUrl ..."
Invoke-RestMethod -Method Put -Uri "$EsUrl/$IndexName" -ContentType "application/json" -Body $body | Out-Null

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

