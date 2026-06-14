package cvc.dashingdog.intenttester

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val repository by lazy { IntentRepository(this) }
    private val viewModel: IntentViewModel by viewModels {
        IntentViewModel.Factory(repository)
    }

    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
        setupFab()
        observeIntents()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            onClick = { savedIntent -> openEditDialog(savedIntent) },
            onLongClick = { savedIntent -> openDeleteDialog(savedIntent) }
        )

        findViewById<RecyclerView>(R.id.rvHistory).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            openNewIntentDialog()
        }
    }

    private fun observeIntents() {
        viewModel.intents.observe(this) { list ->
            adapter.submitList(list)
        }
    }

    private fun openNewIntentDialog() {
        IntentDialogFragment(
            savedIntent = null,
            onConfirm = { savedIntent -> saveAndDispatch(savedIntent) }
        ).show(supportFragmentManager, "new_intent")
    }

    private fun openEditDialog(savedIntent: SavedIntent) {
        IntentDialogFragment(
            savedIntent = savedIntent,
            onConfirm = { updated -> saveAndDispatch(updated) }
        ).show(supportFragmentManager, "edit_intent")
    }

    private fun openDeleteDialog(savedIntent: SavedIntent) {
        ConfirmDeleteDialog(
            displayName = savedIntent.displayName(),
            onConfirm = { viewModel.delete(savedIntent.id) }
        ).show(supportFragmentManager, "confirm_delete")
    }

    private fun saveAndDispatch(savedIntent: SavedIntent) {
        viewModel.upsert(savedIntent)
        when (val result = IntentDispatcher.dispatch(this, savedIntent)) {
            is IntentDispatcher.Result.Success ->
                toast("Sent: ${savedIntent.displayName()}")
            is IntentDispatcher.Result.Failure ->
                toast("Failed: ${result.message}")
        }
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}