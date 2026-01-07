package org.nihongo.mochi.data

interface LearningScore {
    val successes: Int
    val failures: Int
    val lastReviewDate: Long
}
