package org.nihongo.mochi.presentation.models

data class LevelInfoState(
    val levelKey: String,
    val displayName: String,
    val percentage: Int
)

data class ReadingLevelInfoState(
    val id: String,
    val displayName: String,
    val percentage: Int
)

data class WritingLevelInfoState(
    val levelKey: String,
    val displayName: String,
    val percentage: Int
)
