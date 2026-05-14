package com.flo.readinglog.domain.usecase

object SynopsisUseCase {

    fun sanitize(raw: String?): String {
        if (raw.isNullOrBlank()) return ""

        val stripped = raw
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<[^>]+>"), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val sentences = stripped.split(Regex("(?<=[.!?])\\s+"))
        return sentences.take(4).joinToString(" ").trim()
    }
}
