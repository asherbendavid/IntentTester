package cvc.dashingdog.intenttester

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri

object IntentDispatcher {

    sealed class Result {
        object Success : Result()
        data class Failure(val message: String) : Result()
    }

    fun dispatch(context: Context, savedIntent: SavedIntent): Result {
        return try {
            val intent = buildIntent(savedIntent)
            when (savedIntent.dispatchType) {
                DispatchType.START_ACTIVITY          -> context.startActivity(intent)
                DispatchType.SEND_BROADCAST          -> context.sendBroadcast(intent)
                DispatchType.START_SERVICE           -> context.startService(intent)
                DispatchType.START_FOREGROUND_SERVICE -> context.startForegroundService(intent)
            }
            Result.Success
        } catch (e: Exception) {
            Result.Failure(e.message ?: "Unknown error")
        }
    }

    private fun buildIntent(saved: SavedIntent): Intent {
        val intent = Intent()

        if (saved.action.isNotBlank()) intent.action = saved.action

        when {
            saved.dataUri.isNotBlank() && saved.mimeType.isNotBlank() ->
                intent.setDataAndType(Uri.parse(saved.dataUri), saved.mimeType)
            saved.dataUri.isNotBlank() ->
                intent.data = Uri.parse(saved.dataUri)
            saved.mimeType.isNotBlank() ->
                intent.type = saved.mimeType
        }

        if (saved.component.isNotBlank()) {
            val parts = saved.component.split("/")
            if (parts.size == 2) {
                intent.component = ComponentName(parts[0], parts[1])
            }
        }

        if (saved.packageName.isNotBlank()) intent.`package` = saved.packageName

        var flagBits = 0
        for (flagName in saved.flags) {
            INTENT_FLAGS[flagName]?.let { flagBits = flagBits or it }
        }
        if (flagBits != 0) intent.flags = flagBits

        for (category in saved.categories) {
            if (category.isNotBlank()) intent.addCategory(category)
        }

        for (extra in saved.extras) {
            if (extra.key.isBlank()) continue
            when (extra.type) {
                ExtraType.STRING  -> intent.putExtra(extra.key, extra.value)
                ExtraType.INT     -> extra.value.toIntOrNull()
                    ?.let { intent.putExtra(extra.key, it) }
                ExtraType.BOOLEAN -> extra.value.toBooleanStrictOrNull()
                    ?.let { intent.putExtra(extra.key, it) }
                ExtraType.LONG    -> extra.value.toLongOrNull()
                    ?.let { intent.putExtra(extra.key, it) }
                ExtraType.FLOAT   -> extra.value.toFloatOrNull()
                    ?.let { intent.putExtra(extra.key, it) }
            }
        }

        return intent
    }
}