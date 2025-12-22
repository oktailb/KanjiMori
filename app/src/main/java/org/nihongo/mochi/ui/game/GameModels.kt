package org.nihongo.mochi.ui.game

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reading(val value: String, val type: String, val frequency: Int) : Parcelable

@Parcelize
data class KanjiDetail(val id: String, val character: String, val meanings: List<String>, val readings: List<Reading>) : Parcelable

enum class GameStatus { NOT_ANSWERED, PARTIAL, CORRECT, INCORRECT }

@Parcelize
data class KanaCharacter(val kana: String, val romaji: String, val category: String) : Parcelable

data class KanaProgress(var normalSolved: Boolean = false, var reverseSolved: Boolean = false)

enum class KanaQuestionDirection { NORMAL, REVERSE } // NORMAL: Kana -> Romaji, REVERSE: Romaji -> Kana

@Parcelize
data class Word(val text: String, val phonetics: String) : Parcelable
