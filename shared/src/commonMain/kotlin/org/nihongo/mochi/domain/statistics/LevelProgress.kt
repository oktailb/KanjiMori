package org.nihongo.mochi.domain.statistics

data class LevelProgress(
    val title: String,
    val xmlName: String, // Keep this as identifier if needed for mapping back to UI specific resources if really necessary, or just as ID
    val percentage: Int,
    val type: StatisticsType
)

enum class StatisticsType {
    RECOGNITION,
    READING,
    WRITING
}
