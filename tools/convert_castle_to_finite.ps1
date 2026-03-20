$ErrorActionPreference = "Stop"

function Convert-InfiniteTmxToFiniteCsv {
    param([Parameter(Mandatory = $true)][string]$Path)

    [xml]$xml = Get-Content -LiteralPath $Path
    $map = $xml.SelectSingleNode("/map")
    if ($null -eq $map) {
        throw "No <map> root in ${Path}"
    }

    $tileWidth = [int]$map.GetAttribute("tilewidth")
    $tileHeight = [int]$map.GetAttribute("tileheight")

    $layers = $xml.SelectNodes("//layer")
    if ($layers.Count -eq 0) {
        throw "No <layer> nodes in ${Path}"
    }

    $minX = [int]::MaxValue
    $minY = [int]::MaxValue
    $maxX = [int]::MinValue
    $maxY = [int]::MinValue
    $hasChunks = $false

    foreach ($layer in $layers) {
        $chunks = $layer.SelectNodes("data/chunk")
        foreach ($chunk in $chunks) {
            $hasChunks = $true
            $x = [int]$chunk.GetAttribute("x")
            $y = [int]$chunk.GetAttribute("y")
            $w = [int]$chunk.GetAttribute("width")
            $h = [int]$chunk.GetAttribute("height")
            if ($x -lt $minX) { $minX = $x }
            if ($y -lt $minY) { $minY = $y }
            if (($x + $w) -gt $maxX) { $maxX = $x + $w }
            if (($y + $h) -gt $maxY) { $maxY = $y + $h }
        }
    }

    if (-not $hasChunks) {
        Write-Output "NO_CHUNKS|${Path}"
        return
    }

    $newW = $maxX - $minX
    $newH = $maxY - $minY
    if ($newW -le 0 -or $newH -le 0) {
        throw "Invalid computed bounds for ${Path}: min($minX,$minY) max($maxX,$maxY)"
    }

    foreach ($layer in $layers) {
        $data = $layer.SelectSingleNode("data")
        if ($null -eq $data) { continue }

        $encoding = [string]$data.GetAttribute("encoding")
        if ($encoding -ne "csv") {
            throw "Unsupported non-csv layer '$($layer.GetAttribute("name"))' in ${Path}"
        }

        $newCells = New-Object long[] ($newW * $newH)
        $chunks = $layer.SelectNodes("data/chunk")

        if ($chunks.Count -gt 0) {
            foreach ($chunk in $chunks) {
                $cx = [int]$chunk.GetAttribute("x")
                $cy = [int]$chunk.GetAttribute("y")
                $cw = [int]$chunk.GetAttribute("width")
                $ch = [int]$chunk.GetAttribute("height")
                $raw = [string]$chunk.InnerText

                $vals = New-Object "System.Collections.Generic.List[long]"
                foreach ($p in ($raw -split ",")) {
                    $t = $p.Trim()
                    if ($t.Length -gt 0) { $vals.Add([long]$t) }
                }

                $expected = $cw * $ch
                if ($vals.Count -ne $expected) {
                    throw "Chunk value count mismatch in ${Path} layer '$($layer.GetAttribute("name"))': got $($vals.Count), expected $expected"
                }

                $k = 0
                for ($yy = 0; $yy -lt $ch; $yy++) {
                    for ($xx = 0; $xx -lt $cw; $xx++) {
                        $gx = $cx + $xx - $minX
                        $gy = $cy + $yy - $minY
                        if ($gx -ge 0 -and $gx -lt $newW -and $gy -ge 0 -and $gy -lt $newH) {
                            $idx = $gy * $newW + $gx
                            $newCells[$idx] = $vals[$k]
                        }
                        $k++
                    }
                }
            }
        } else {
            $oldW = [int]$layer.GetAttribute("width")
            $oldH = [int]$layer.GetAttribute("height")
            $raw = [string]$data.InnerText
            $vals = New-Object "System.Collections.Generic.List[long]"
            foreach ($p in ($raw -split ",")) {
                $t = $p.Trim()
                if ($t.Length -gt 0) { $vals.Add([long]$t) }
            }

            $expected = $oldW * $oldH
            if ($expected -gt 0 -and $vals.Count -eq $expected) {
                $k = 0
                for ($yy = 0; $yy -lt $oldH; $yy++) {
                    for ($xx = 0; $xx -lt $oldW; $xx++) {
                        $gx = $xx - $minX
                        $gy = $yy - $minY
                        if ($gx -ge 0 -and $gx -lt $newW -and $gy -ge 0 -and $gy -lt $newH) {
                            $idx = $gy * $newW + $gx
                            $newCells[$idx] = $vals[$k]
                        }
                        $k++
                    }
                }
            }
        }

        $layer.SetAttribute("width", [string]$newW)
        $layer.SetAttribute("height", [string]$newH)

        while ($data.Attributes.Count -gt 0) { $null = $data.Attributes.RemoveAt(0) }
        $data.SetAttribute("encoding", "csv")
        while ($data.HasChildNodes) { $null = $data.RemoveChild($data.FirstChild) }

        $sb = New-Object System.Text.StringBuilder
        for ($i = 0; $i -lt $newCells.Length; $i++) {
            if ($i -gt 0) { $null = $sb.Append(",") }
            $null = $sb.Append($newCells[$i])
        }
        $data.InnerText = $sb.ToString()
    }

    $shiftXPx = -$minX * $tileWidth
    $shiftYPx = -$minY * $tileHeight

    foreach ($obj in $xml.SelectNodes("//objectgroup/object")) {
        if ($obj.HasAttribute("x")) {
            $x = [double]$obj.GetAttribute("x")
            $obj.SetAttribute("x", [string]($x + $shiftXPx))
        }
        if ($obj.HasAttribute("y")) {
            $y = [double]$obj.GetAttribute("y")
            $obj.SetAttribute("y", [string]($y + $shiftYPx))
        }
    }

    $map.SetAttribute("width", [string]$newW)
    $map.SetAttribute("height", [string]$newH)
    $map.SetAttribute("infinite", "0")

    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    $xmlText = $xml.OuterXml
    [System.IO.File]::WriteAllText((Resolve-Path $Path), $xmlText, $utf8NoBom)

    Write-Output "CONVERTED|${Path}|min=($minX,$minY)|max=($maxX,$maxY)|size=${newW}x${newH}|shiftPx=($shiftXPx,$shiftYPx)"
}

$targets = @(
    "core/assets/maps/Demon-Castle_finite.tmx",
    "core/assets/maps/Demon-Castle.tmx",
    "DSA GAME/Demon-Castle.tmx"
)

foreach ($t in $targets) {
    Convert-InfiniteTmxToFiniteCsv -Path $t
}
