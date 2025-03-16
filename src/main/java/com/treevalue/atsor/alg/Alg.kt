package com.treevalue.atsor.alg

import com.treevalue.atsor.TVRMusicApplication
import com.treevalue.atsor.hard.MusicMatch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class Alg(val musicMatch: MusicMatch) {
    private val logger = LoggerFactory.getLogger(TVRMusicApplication::class.java)

    fun getMusicUrl(stableV: Float): String {
        var url: String = GetInnerMusicUrl(stableV)
        url = webFilter(url)
        return url
    }

    private fun GetInnerMusicUrl(stableV: Float): String {
        var url: String
        if (stableV == 0f) {
            url = musicMatch.RandomPath()
        } else {
            url = musicMatch.RandomPath()
        }
        return url
    }

    private fun webFilter(url: String): String {
        if (url.startsWith(Mark.RES_BEG)) {
            return url.replaceFirst(Mark.RES_BEG, "/");
        }
        return url
    }
}
