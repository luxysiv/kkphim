package com.kkphim.utils

object M3u8Filter {

    fun processM3u8(rawContent: String, originalUrl: String): String {
        val lines = rawContent.lines()
        val result = mutableListOf<String>()
        val baseUrl = originalUrl.substringBeforeLast("/") + "/"
        
        val convertRegex = Regex("convertv\\d+")

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) { i++; continue }

            if (line == "#EXT-X-DISCONTINUITY") {
                var j = i + 1
                var segmentCount = 0
                
                while (j < lines.size && lines[j].trim() != "#EXT-X-DISCONTINUITY") {
                    val current = lines[j].trim()
                    if (current.isNotEmpty()) {
                        if (!current.startsWith("#") && (current.contains(".ts") || current.contains("segment"))) {
                            segmentCount++
                        }
                    }
                    j++
                }

                if (segmentCount in 5..20) {
                    i = j + 1
                    continue
                } else {
                    i++ 
                    continue
                }
            }

            var processedLine = line.replace(convertRegex, "")

            when {
                !processedLine.startsWith("#") -> {
                    val fullUrl = if (processedLine.startsWith("http")) processedLine else baseUrl + processedLine
                    result.add(fullUrl)
                }
                processedLine.startsWith("#EXT-X-KEY") -> {
                    val fixedKey = if (processedLine.contains("URI=\"http")) processedLine 
                                  else processedLine.replace("URI=\"", "URI=\"$baseUrl")
                    result.add(fixedKey)
                }
                processedLine != "#EXT-X-DISCONTINUITY" -> {
                    result.add(processedLine)
                }
            }
            i++
        }
        return result.joinToString("\n")
    }
}
