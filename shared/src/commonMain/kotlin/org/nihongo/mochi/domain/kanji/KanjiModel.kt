package org.nihongo.mochi.domain.kanji

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

@Serializable
data class KanjiDetailsRoot(
    @SerialName("kanji_details")
    val kanjiDetails: KanjiDetailsWrapper = KanjiDetailsWrapper(emptyList())
)

@Serializable
data class KanjiDetailsWrapper(
    val kanji: List<KanjiEntry> = emptyList()
)

@Serializable
data class KanjiEntry(
    val id: String = "",
    val character: String = "",
    
    // New structure replacing explicit school_grade/jlpt_level fields
    @Serializable(with = StringListSerializer::class)
    val category: List<String> = emptyList(),
    
    @Serializable(with = StringListSerializer::class)
    val level: List<String> = emptyList(),

    val frequency: String? = null,
    val strokes: String? = null,
    val readings: ReadingsWrapper? = null,
    val components: ComponentsWrapper? = null
) {
    // Helper properties for backward compatibility in code logic if needed
    // They derive values from the new lists.
    // Note: This assumes 1-to-1 mapping which might not always be true if a kanji is in multiple levels,
    // but usually a Kanji has a primary JLPT level and a primary School grade.
    
    val jlptLevel: String?
        get() {
            val index = category.indexOf("jlpt")
            return if (index != -1 && index < level.size) level[index].uppercase() else null
        }

    val schoolGrade: String?
        get() {
            val index = category.indexOf("school")
            // level[index] might be "grade1", we might want just "1". 
            // The old code expected just the number string for grades often, or "grade1" depending on usage.
            // Let's check KanjiRepository usage. It used `it.schoolGrade == levelValue`.
            // If levelValue passed to repo was "1", and JSON had "1", it matched.
            // Now JSON has "grade1". So we might need to normalize.
            return if (index != -1 && index < level.size) level[index] else null
        }
}

@Serializable
data class ReadingsWrapper(
    @Serializable(with = ReadingEntryListSerializer::class)
    val reading: List<ReadingEntry> = emptyList()
)

@Serializable
data class ReadingEntry(
    val type: String = "",
    val frequency: String? = null,
    @SerialName("#text")
    val value: String = ""
)

@Serializable
data class ComponentsWrapper(
    val structure: String? = null,
    @Serializable(with = ComponentEntryListSerializer::class)
    val component: List<ComponentEntry> = emptyList()
)

@Serializable
data class ComponentEntry(
    @SerialName("kanji_ref")
    val kanjiRef: String? = null,
    @SerialName("#text")
    val text: String? = null
)

// Serializer for fields that can be a single string or a list of strings (robustness)
object StringListSerializer : KSerializer<List<String>> {
    private val listSerializer = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: List<String>) {
        listSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<String> {
        val input = decoder as? JsonDecoder ?: throw IllegalStateException("This serializer can be used only with Json format")
        val element = input.decodeJsonElement()

        return if (element is JsonArray) {
            input.json.decodeFromJsonElement(listSerializer, element)
        } else if (element is kotlinx.serialization.json.JsonPrimitive) {
            if (element.isString) {
                listOf(element.content)
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
}

object ReadingEntryListSerializer : KSerializer<List<ReadingEntry>> {
    private val listSerializer = ListSerializer(ReadingEntry.serializer())
    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: List<ReadingEntry>) {
        listSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<ReadingEntry> {
        val input = decoder as? JsonDecoder ?: throw IllegalStateException("This serializer can be used only with Json format")
        val element = input.decodeJsonElement()

        return if (element is JsonArray) {
            input.json.decodeFromJsonElement(listSerializer, element)
        } else if (element is JsonObject) {
            listOf(input.json.decodeFromJsonElement(ReadingEntry.serializer(), element))
        } else {
            emptyList()
        }
    }
}

object ComponentEntryListSerializer : KSerializer<List<ComponentEntry>> {
    private val listSerializer = ListSerializer(ComponentEntry.serializer())
    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: List<ComponentEntry>) {
        listSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<ComponentEntry> {
        val input = decoder as? JsonDecoder ?: throw IllegalStateException("This serializer can be used only with Json format")
        val element = input.decodeJsonElement()

        return if (element is JsonArray) {
            input.json.decodeFromJsonElement(listSerializer, element)
        } else if (element is JsonObject) {
            listOf(input.json.decodeFromJsonElement(ComponentEntry.serializer(), element))
        } else {
            emptyList()
        }
    }
}
