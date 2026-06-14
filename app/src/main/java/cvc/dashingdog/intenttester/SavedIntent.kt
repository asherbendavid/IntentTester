package cvc.dashingdog.intenttester

import java.util.UUID

enum class DispatchType {
    START_ACTIVITY,
    SEND_BROADCAST,
    START_SERVICE,
    START_FOREGROUND_SERVICE
}

enum class ExtraType {
    STRING, INT, BOOLEAN, LONG, FLOAT
}

data class IntentExtra(
    val key: String = "",
    val type: ExtraType = ExtraType.STRING,
    val value: String = ""
)

data class SavedIntent(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "",
    val dispatchType: DispatchType = DispatchType.START_ACTIVITY,
    val action: String = "",
    val dataUri: String = "",
    val mimeType: String = "",
    val component: String = "",
    val packageName: String = "",
    val flags: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val extras: List<IntentExtra> = emptyList(),
    val lastUsed: Long = System.currentTimeMillis()
) {
    fun displayName(): String = when {
        label.isNotBlank() -> label
        action.isNotBlank() -> action
        component.isNotBlank() -> component
        else -> "(unnamed)"
    }
}