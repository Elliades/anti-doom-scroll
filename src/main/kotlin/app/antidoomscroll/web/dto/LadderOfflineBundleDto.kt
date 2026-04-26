package app.antidoomscroll.web.dto

import app.antidoomscroll.config.LadderConfigProperties

/**
 * Full ladder definitions for offline Android / bundled JSON export.
 * Serialized by [app.antidoomscroll.web.LadderController.offlineBundle].
 */
data class LadderOfflineBundleDto(
    val ladders: List<LadderOfflineSummaryDto>,
    val mixes: List<LadderOfflineMixDto>,
    val configs: Map<String, LadderConfigProperties.LadderDef>
)

data class LadderOfflineSummaryDto(
    val code: String,
    val name: String?,
    val levelCount: Int
)

data class LadderOfflineMixDto(
    val code: String,
    val name: String?,
    val ladderCodes: List<String>
)
