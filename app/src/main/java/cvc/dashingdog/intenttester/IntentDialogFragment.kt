package cvc.dashingdog.intenttester

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.util.UUID

class IntentDialogFragment(
    private val savedIntent: SavedIntent? = null,
    private val onConfirm: (SavedIntent) -> Unit
) : DialogFragment() {

    private lateinit var etLabel: EditText
    private lateinit var spinnerDispatch: Spinner
    private lateinit var etAction: EditText
    private lateinit var etDataUri: EditText
    private lateinit var etMimeType: EditText
    private lateinit var etComponent: EditText
    private lateinit var etPackageName: EditText
    private lateinit var tvSelectedFlags: TextView
    private lateinit var llCategories: LinearLayout
    private lateinit var llExtras: LinearLayout

    private val selectedFlags = mutableSetOf<String>()
    private val flagKeys = INTENT_FLAGS.keys.toList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_intent, null)

        bindViews(view)
        setupSpinner()
        setupFlagChooser(view)
        setupAddCategory(view)
        setupAddExtra(view)
        populateIfEditing()

        return AlertDialog.Builder(requireContext())
            .setTitle(if (savedIntent == null) "New intent" else "Edit intent")
            .setView(view)
            .setPositiveButton("Send") { _, _ -> buildAndConfirm() }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun bindViews(view: View) {
        etLabel        = view.findViewById(R.id.etLabel)
        spinnerDispatch = view.findViewById(R.id.spinnerDispatch)
        etAction       = view.findViewById(R.id.etAction)
        etDataUri      = view.findViewById(R.id.etDataUri)
        etMimeType     = view.findViewById(R.id.etMimeType)
        etComponent    = view.findViewById(R.id.etComponent)
        etPackageName  = view.findViewById(R.id.etPackageName)
        tvSelectedFlags = view.findViewById(R.id.tvSelectedFlags)
        llCategories   = view.findViewById(R.id.llCategories)
        llExtras       = view.findViewById(R.id.llExtras)
    }

    private fun setupSpinner() {
        val labels = DispatchType.values().map { type ->
            type.name.replace("_", " ")
                .lowercase()
                .replaceFirstChar { it.uppercase() }
        }
        spinnerDispatch.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            labels
        )
    }

    private fun setupFlagChooser(view: View) {
        view.findViewById<Button>(R.id.btnChooseFlags).setOnClickListener {
            // Rebuilt fresh each open so pre-populated flags show as checked
            val checkedState = BooleanArray(flagKeys.size) { i -> flagKeys[i] in selectedFlags }
            AlertDialog.Builder(requireContext())
                .setTitle("Select flags")
                .setMultiChoiceItems(flagKeys.toTypedArray(), checkedState) { _, index, isChecked ->
                    if (isChecked) selectedFlags.add(flagKeys[index])
                    else selectedFlags.remove(flagKeys[index])
                    updateFlagsDisplay()
                }
                .setPositiveButton("Done", null)
                .show()
        }
    }

    private fun updateFlagsDisplay() {
        tvSelectedFlags.text = if (selectedFlags.isEmpty()) "None selected"
        else selectedFlags.joinToString("\n")
    }

    private fun setupAddCategory(view: View) {
        view.findViewById<Button>(R.id.btnAddCategory).setOnClickListener {
            addCategoryRow("")
        }
    }

    private fun setupAddExtra(view: View) {
        view.findViewById<Button>(R.id.btnAddExtra).setOnClickListener {
            addExtraRow(IntentExtra())
        }
    }

    private fun addCategoryRow(value: String) {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val et = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            hint = "android.intent.category.BROWSABLE"
            setText(value)
        }
        val btnRemove = Button(requireContext()).apply {
            text = "✕"
            setOnClickListener { llCategories.removeView(row) }
        }
        row.addView(et)
        row.addView(btnRemove)
        llCategories.addView(row)
    }

    private fun addExtraRow(extra: IntentExtra) {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val etKey = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
            hint = "key"
            setText(extra.key)
        }
        val typeSpinner = Spinner(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                ExtraType.values().map { it.name.lowercase() }
            )
            setSelection(extra.type.ordinal)
        }
        val etValue = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
            hint = "value"
            setText(extra.value)
        }
        val btnRemove = Button(requireContext()).apply {
            text = "✕"
            setOnClickListener { llExtras.removeView(row) }
        }
        row.addView(etKey)
        row.addView(typeSpinner)
        row.addView(etValue)
        row.addView(btnRemove)
        llExtras.addView(row)
    }

    private fun populateIfEditing() {
        val s = savedIntent ?: return
        etLabel.setText(s.label)
        spinnerDispatch.setSelection(s.dispatchType.ordinal)
        etAction.setText(s.action)
        etDataUri.setText(s.dataUri)
        etMimeType.setText(s.mimeType)
        etComponent.setText(s.component)
        etPackageName.setText(s.packageName)
        selectedFlags.addAll(s.flags)
        updateFlagsDisplay()
        s.categories.forEach { addCategoryRow(it) }
        s.extras.forEach { addExtraRow(it) }
    }

    private fun getCategories(): List<String> =
        (0 until llCategories.childCount)
            .map { llCategories.getChildAt(it) as LinearLayout }
            .mapNotNull { (it.getChildAt(0) as? EditText)?.text?.toString()?.trim() }
            .filter { it.isNotBlank() }

    private fun getExtras(): List<IntentExtra> =
        (0 until llExtras.childCount)
            .map { llExtras.getChildAt(it) as LinearLayout }
            .mapNotNull { row ->
                val key   = (row.getChildAt(0) as? EditText)?.text?.toString()?.trim()
                    ?: return@mapNotNull null
                val type  = ExtraType.values()[
                    (row.getChildAt(1) as? Spinner)?.selectedItemPosition ?: 0]
                val value = (row.getChildAt(2) as? EditText)?.text?.toString()?.trim() ?: ""
                if (key.isBlank()) null else IntentExtra(key, type, value)
            }

    private fun buildAndConfirm() {
        onConfirm(SavedIntent(
            id           = savedIntent?.id ?: UUID.randomUUID().toString(),
            label        = etLabel.text.toString().trim(),
            dispatchType = DispatchType.values()[spinnerDispatch.selectedItemPosition],
            action       = etAction.text.toString().trim(),
            dataUri      = etDataUri.text.toString().trim(),
            mimeType     = etMimeType.text.toString().trim(),
            component    = etComponent.text.toString().trim(),
            packageName  = etPackageName.text.toString().trim(),
            flags        = selectedFlags.toList(),
            categories   = getCategories(),
            extras       = getExtras(),
            lastUsed     = System.currentTimeMillis()
        ))
    }
}