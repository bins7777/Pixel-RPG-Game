param(
    [Parameter(Mandatory = $true)]
    [string]$InputPath,
    [Parameter(Mandatory = $true)]
    [string]$OutputPath
)

$ErrorActionPreference = "Stop"

function Parse-DoubleInvariant([string]$value) {
    return [double]::Parse($value, [System.Globalization.CultureInfo]::InvariantCulture)
}

function Format-DoubleInvariant([double]$value) {
    return $value.ToString("0.######", [System.Globalization.CultureInfo]::InvariantCulture)
}

function Shift-Attr([System.Xml.XmlElement]$node, [string]$attr, [double]$delta) {
    if (-not $node.HasAttribute($attr)) { return }
    $raw = $node.GetAttribute($attr)
    if ([string]::IsNullOrWhiteSpace($raw)) { return }
    $parsed = Parse-DoubleInvariant($raw)
    $node.SetAttribute($attr, (Format-DoubleInvariant($parsed + $delta)))
}

function Normalize-TilesetSources([System.Xml.XmlElement]$mapNode) {
    foreach ($tileset in $mapNode.SelectNodes(".//tileset[@source]")) {
        $src = $tileset.GetAttribute("source")
        if ([string]::IsNullOrWhiteSpace($src)) { continue }
        $normalized = $src.Replace('\', '/')

        $tileSetMatch = [System.Text.RegularExpressions.Regex]::Match(
            $normalized,
            "(?i)^(.*/)?DSA GAME/TILE SETS/(.+)$"
        )
        if ($tileSetMatch.Success) {
            $tileset.SetAttribute("source", "TILE SETS/" + $tileSetMatch.Groups[2].Value)
            continue
        }

        $questMatch = [System.Text.RegularExpressions.Regex]::Match(
            $normalized,
            "(?i)^(.*/)?DSA GAME/QUEST#1 Tilesets/(.+)$"
        )
        if ($questMatch.Success) {
            $tileset.SetAttribute("source", "QUEST#1 Tilesets/" + $questMatch.Groups[2].Value)
        }
    }
}

[xml]$xml = Get-Content $InputPath -Raw
$map = $xml.map
if ($null -eq $map) {
    throw "Invalid TMX file: missing <map> node"
}

$tileWidth = [int]$map.GetAttribute("tilewidth")
$tileHeight = [int]$map.GetAttribute("tileheight")

$layersWithChunks = @($map.SelectNodes("//layer[data/chunk]"))
if ($layersWithChunks.Count -eq 0) {
    throw "No chunked layers found. Map may already be finite."
}

$minX = [int]::MaxValue
$minY = [int]::MaxValue
$maxX = [int]::MinValue
$maxY = [int]::MinValue

foreach ($chunk in $map.SelectNodes("//layer/data/chunk")) {
    $cx = [int]$chunk.GetAttribute("x")
    $cy = [int]$chunk.GetAttribute("y")
    $cw = [int]$chunk.GetAttribute("width")
    $ch = [int]$chunk.GetAttribute("height")

    if ($cx -lt $minX) { $minX = $cx }
    if ($cy -lt $minY) { $minY = $cy }
    if (($cx + $cw) -gt $maxX) { $maxX = $cx + $cw }
    if (($cy + $ch) -gt $maxY) { $maxY = $cy + $ch }
}

$mapWidth = $maxX - $minX
$mapHeight = $maxY - $minY

if ($mapWidth -le 0 -or $mapHeight -le 0) {
    throw "Computed invalid map size: ${mapWidth}x${mapHeight}"
}

$layersRemoved = 0
$layersConverted = 0

foreach ($layer in @($map.SelectNodes("//layer"))) {
    $data = $layer.SelectSingleNode("data")
    if ($null -eq $data) { continue }

    $chunks = @($data.SelectNodes("chunk"))
    if ($chunks.Count -eq 0) {
        $txt = $data.InnerText
        if ([string]::IsNullOrWhiteSpace($txt)) {
            $null = $layer.ParentNode.RemoveChild($layer)
            $layersRemoved++
        } else {
            $layer.SetAttribute("width", "$mapWidth")
            $layer.SetAttribute("height", "$mapHeight")
        }
        continue
    }

    $cells = New-Object 'long[]' ($mapWidth * $mapHeight)

    foreach ($chunk in $chunks) {
        $cx = [int]$chunk.GetAttribute("x")
        $cy = [int]$chunk.GetAttribute("y")
        $cw = [int]$chunk.GetAttribute("width")
        $ch = [int]$chunk.GetAttribute("height")

        $values = @()
        foreach ($item in ($chunk.InnerText -split ",")) {
            $trimmed = $item.Trim()
            if (-not [string]::IsNullOrWhiteSpace($trimmed)) {
                $values += [int64]$trimmed
            }
        }

        $expected = $cw * $ch
        if ($values.Count -lt $expected) {
            $pad = $expected - $values.Count
            for ($i = 0; $i -lt $pad; $i++) { $values += 0 }
        } elseif ($values.Count -gt $expected) {
            $values = $values[0..($expected - 1)]
        }

        $baseX = $cx - $minX
        $baseY = $cy - $minY
        for ($yy = 0; $yy -lt $ch; $yy++) {
            for ($xx = 0; $xx -lt $cw; $xx++) {
                $src = $yy * $cw + $xx
                $dstX = $baseX + $xx
                $dstY = $baseY + $yy
                if ($dstX -ge 0 -and $dstX -lt $mapWidth -and $dstY -ge 0 -and $dstY -lt $mapHeight) {
                    $cells[$dstY * $mapWidth + $dstX] = [int64]$values[$src]
                }
            }
        }
    }

    $csv = New-Object System.Text.StringBuilder
    for ($y = 0; $y -lt $mapHeight; $y++) {
        for ($x = 0; $x -lt $mapWidth; $x++) {
            $idx = $y * $mapWidth + $x
            [void]$csv.Append($cells[$idx])
            if (-not ($y -eq $mapHeight - 1 -and $x -eq $mapWidth - 1)) {
                [void]$csv.Append(',')
            }
        }
        if ($y -lt $mapHeight - 1) {
            [void]$csv.Append("`n")
        }
    }

    while ($data.HasChildNodes) {
        $null = $data.RemoveChild($data.FirstChild)
    }
    $data.RemoveAttribute("compression")
    $data.SetAttribute("encoding", "csv")
    $null = $data.AppendChild($xml.CreateTextNode($csv.ToString()))

    $layer.SetAttribute("width", "$mapWidth")
    $layer.SetAttribute("height", "$mapHeight")
    $layersConverted++
}

$map.SetAttribute("infinite", "0")
$map.SetAttribute("width", "$mapWidth")
$map.SetAttribute("height", "$mapHeight")

$shiftX = -$minX * $tileWidth
$shiftY = -$minY * $tileHeight

foreach ($obj in $map.SelectNodes("//object")) {
    Shift-Attr $obj "x" $shiftX
    Shift-Attr $obj "y" $shiftY
}

foreach ($imgLayer in $map.SelectNodes("//imagelayer")) {
    Shift-Attr $imgLayer "x" $shiftX
    Shift-Attr $imgLayer "offsetx" $shiftX
    Shift-Attr $imgLayer "y" $shiftY
    Shift-Attr $imgLayer "offsety" $shiftY
}

Normalize-TilesetSources $map

$settings = New-Object System.Xml.XmlWriterSettings
$settings.Indent = $true
$settings.Encoding = New-Object System.Text.UTF8Encoding($false)
$settings.NewLineChars = "`n"
$settings.NewLineHandling = [System.Xml.NewLineHandling]::Replace

$outDir = Split-Path $OutputPath -Parent
if ($outDir -and -not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir -Force | Out-Null
}

$writer = [System.Xml.XmlWriter]::Create($OutputPath, $settings)
$xml.Save($writer)
$writer.Close()

Write-Output "Converted map to finite."
Write-Output "Input: $InputPath"
Write-Output "Output: $OutputPath"
Write-Output "Size: ${mapWidth}x${mapHeight} tiles"
Write-Output "Shift: (${shiftX}, ${shiftY}) pixels"
Write-Output "Layers converted: $layersConverted"
Write-Output "Empty layers removed: $layersRemoved"
