package org.nihongo.mochi.data

import kotlinx.serialization.Serializable

@Serializable
data class KanjiScore(override val successes: Int, override val failures: Int, override val lastReviewDate: Long = 0) : LearningScore
