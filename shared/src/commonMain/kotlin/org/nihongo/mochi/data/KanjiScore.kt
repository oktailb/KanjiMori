package org.nihongo.mochi.data

import kotlinx.serialization.Serializable

@Serializable
data class KanjiScore(val successes: Int, val failures: Int, val lastReviewDate: Long = 0)
