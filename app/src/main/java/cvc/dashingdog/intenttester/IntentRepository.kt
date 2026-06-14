package cvc.dashingdog.intenttester

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

class IntentRepository(context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val file: File = File(context.filesDir, "intent_history.json")

    fun loadAll(): MutableList<SavedIntent> {
        if (!file.exists()) return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<SavedIntent>>() {}.type
            gson.fromJson(file.readText(), type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveAll(intents: List<SavedIntent>) {
        file.writeText(gson.toJson(intents))
    }

    fun upsert(intent: SavedIntent, intents: MutableList<SavedIntent>): MutableList<SavedIntent> {
        val index = intents.indexOfFirst { it.id == intent.id }
        if (index >= 0) {
            intents[index] = intent
        } else {
            intents.add(intent)
        }
        saveAll(intents)
        return intents
    }

    fun delete(id: String, intents: MutableList<SavedIntent>): MutableList<SavedIntent> {
        intents.removeAll { it.id == id }
        saveAll(intents)
        return intents
    }
}