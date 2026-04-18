package com.example.todo

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()
    private val filteredList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadData()
        filteredList.addAll(taskList)

        val etTask = findViewById<EditText>(R.id.etTask)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // FIX 3: Set LayoutManager
        recyclerView.layoutManager = LinearLayoutManager(this)

        taskAdapter = TaskAdapter(
            tasks = filteredList,
            onTaskChanged = {
                saveData()
            },
            onTaskDelete = { position ->
                deleteTask(position)
            },
            onTaskEdit = { position ->
                showEditDialog(position)
            }
        )

        recyclerView.adapter = taskAdapter

        // Swipe to Delete
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(r: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTask(viewHolder.bindingAdapterPosition)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Search Logic
        etTask.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnAdd.setOnClickListener {
            val title = etTask.text.toString()
            if (title.isNotEmpty()) {
                showPriorityDialog(title)
                etTask.text.clear()
            }
        }
    }

    private fun filter(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(taskList)
        } else {
            for (item in taskList) {
                if (item.title.lowercase().contains(query.lowercase())) {
                    filteredList.add(item)
                }
            }
        }
        // FIX 5: Refresh adapter
        taskAdapter.notifyDataSetChanged()
    }

    private fun showPriorityDialog(title: String) {
        val priorities = arrayOf("Low", "Medium", "High")
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Priority")
            .setItems(priorities) { _, which ->
                val priority = when (which) {
                    0 -> Priority.LOW
                    1 -> Priority.MEDIUM
                    else -> Priority.HIGH
                }
                addTask(title, priority)
            }
            .show()
    }

    private fun addTask(title: String, priority: Priority) {
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val newTask = Task(title, false, time, priority)
        taskList.add(0, newTask) // Add to top
        filter("") // Refresh filtered list
        saveData()
    }

    private fun deleteTask(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val taskToRemove = filteredList[position]
            // Safe removal from both lists
            taskList.remove(taskToRemove)
            filteredList.removeAt(position)
            taskAdapter.notifyItemRemoved(position)
            saveData()
        }
    }

    private fun showEditDialog(position: Int) {
        val task = filteredList[position]
        val etEdit = EditText(this)
        etEdit.setText(task.title)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Task")
            .setView(etEdit)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = etEdit.text.toString()
                if (newTitle.isNotEmpty()) {
                    // FIX 4: Use a safer way to find the index
                    val indexInMain = taskList.indexOfFirst { it.time == task.time && it.title == task.title }
                    if (indexInMain != -1) {
                        val updatedTask = task.copy(title = newTitle)
                        taskList[indexInMain] = updatedTask
                        filteredList[position] = updatedTask
                        taskAdapter.notifyItemChanged(position)
                        saveData()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("shared_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(taskList)
        editor.putString("tasks", json)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("shared_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("tasks", null)
        val type = object : TypeToken<MutableList<Task>>() {}.type

        if (json != null) {
            try {
                val loadedList: MutableList<Task> = gson.fromJson(json, type)
                taskList.clear()
                taskList.addAll(loadedList)
            } catch (e: Exception) {
                e.printStackTrace()
                taskList.clear() // Fallback if data format is incompatible
            }
        }
    }
}