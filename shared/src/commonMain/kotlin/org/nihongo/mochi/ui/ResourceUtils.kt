package org.nihongo.mochi.ui

import org.jetbrains.compose.resources.StringResource
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.*

object ResourceUtils {
    /**
     * Attempts to find a string resource dynamically by name.
     * This is a workaround until Compose Multiplatform supports dynamic resource lookup better.
     * It maps known keys to their generated resource IDs.
     */
    fun resolveStringResource(key: String): StringResource? {
        // This is a manual mapping or reflection-based lookup if possible (reflection is limited in KMP/Native)
        // For now, we can use a generated map or huge when statement if we want to be safe.
        // Or we can rely on specific naming conventions if we generate this file.
        
        // However, since KMP resources are static properties of Res.string, 
        // we can't easily look them up by string name without a registry.
        
        return when(key) {
            "jlpt" -> Res.string.section_jlpt
            "joyo" -> null // No direct match in provided strings, maybe section_school?
            "kana" -> Res.string.results_section_kanas
            "hiragana" -> Res.string.level_hiragana
            "katakana" -> Res.string.level_katakana
            "n5" -> Res.string.level_n5
            "n4" -> Res.string.level_n4
            "n3" -> Res.string.level_n3
            "n2" -> Res.string.level_n2
            "n1" -> Res.string.level_n1
            "grade_1" -> Res.string.level_grade_1
            "grade_2" -> Res.string.level_grade_2
            "grade_3" -> Res.string.level_grade_3
            "grade_4" -> Res.string.level_grade_4
            "grade_5" -> Res.string.level_grade_5
            "grade_6" -> Res.string.level_grade_6
            "secondary_school" -> Res.string.results_section_school // Approximated
            "frequency" -> Res.string.results_section_frequency
            "user_list" -> Res.string.reading_user_list
            "writing_user_lists" -> Res.string.writing_user_lists
            "my_list" -> Res.string.reading_user_list
            // Mappings for Dictionary Screen Spinner (Native Challenge, School, etc.)
            "school" -> Res.string.section_school
            "challenges" -> Res.string.section_challenges
            "challenge" -> Res.string.section_challenges
            "section_jlpt" -> Res.string.section_jlpt
            "section_school" -> Res.string.section_school
            "section_challenges" -> Res.string.section_challenges
            "section_challenge" -> Res.string.section_challenges
            "section_fundamentals" -> Res.string.section_fundamentals
            "native_challenge" -> Res.string.level_native_challenge
            "no_reading" -> Res.string.level_no_reading
            "no_meaning" -> Res.string.level_no_meaning
            else -> null
        }
    }
}
