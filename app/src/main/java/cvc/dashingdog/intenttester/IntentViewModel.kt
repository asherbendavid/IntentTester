package cvc.dashingdog.intenttester

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class IntentViewModel(private val repository: IntentRepository) : ViewModel() {

    private val _intents = MutableLiveData<List<SavedIntent>>()
    val intents: LiveData<List<SavedIntent>> = _intents

    init {
        load()
    }

    private fun load() {
        _intents.value = repository.loadAll().sortedByDescending { it.lastUsed }
    }

    fun upsert(intent: SavedIntent) {
        val current = _intents.value?.toMutableList() ?: mutableListOf()
        val updated = repository.upsert(intent, current)
        _intents.value = updated.sortedByDescending { it.lastUsed }
    }

    fun delete(id: String) {
        val current = _intents.value?.toMutableList() ?: mutableListOf()
        val updated = repository.delete(id, current)
        _intents.value = updated.sortedByDescending { it.lastUsed }
    }

    class Factory(private val repository: IntentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return IntentViewModel(repository) as T
        }
    }
}