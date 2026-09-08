package com.anuj.graphsonic.data.history


import android.content.Context
import org.json.JSONArray

class EquationHistoryRepository(
    context: Context
) {
    companion object {
        private const val PREFS_NAME = "graphsonic_history"
        private const val KEY_EQUATIONS = "equations"
        private const val MAX_HISTORY = 100
    }

    private val preferences =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    fun getHistory(): List<String> {
        val raw =
            preferences.getString(
                KEY_EQUATIONS,
                null
            ) ?: return emptyList()

        return try {
            val json =
                JSONArray(raw)

            buildList {
                for (index in 0 until json.length()) {
                    add(json.getString(index))
                }
            }
        } catch (
            exception: Exception
        ) {
            emptyList()
        }
    }

    fun addEquation(
        expression: String
    ) {
        val cleaned =
            expression.trim()

        if (cleaned.isEmpty()) {
            return
        }

        val updated =
            buildList {
                add(cleaned)

                getHistory()
                    .filter {
                        it != cleaned
                    }
                    .forEach {
                        add(it)
                    }
            }
                .take(MAX_HISTORY)

        val json =
            JSONArray()

        updated.forEach {
            json.put(it)
        }

        preferences.edit()
            .putString(
                KEY_EQUATIONS,
                json.toString()
            )
            .apply()
    }

    fun removeEquation(
        expression: String
    ) {
        val updated =
            getHistory()
                .filter {
                    it != expression
                }

        val json =
            JSONArray()

        updated.forEach {
            json.put(it)
        }

        preferences.edit()
            .putString(
                KEY_EQUATIONS,
                json.toString()
            )
            .apply()
    }

    fun clearHistory() {
        preferences.edit()
            .remove(KEY_EQUATIONS)
            .apply()
    }
}