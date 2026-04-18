package com.example.todo

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskChanged: () -> Unit,
    private val onTaskDelete: (Int) -> Unit,
    private val onTaskEdit: (Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbDone: CheckBox = itemView.findViewById(R.id.cbDone)
        val tvTaskTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.tvTaskTitle.text = task.title
        holder.tvTime.text = task.time
        holder.cbDone.isChecked = task.isCompleted
        
        // Handle potential null priority from older data
        val priority = task.priority ?: Priority.LOW
        holder.tvPriority.text = priority.name
        
        val (priorityColor, priorityTextColor) = when (priority) {
            Priority.HIGH -> 0xFFFFD7D7.toInt() to 0xFFB3261E.toInt()
            Priority.MEDIUM -> 0xFFFFF4D7.toInt() to 0xFF856404.toInt()
            Priority.LOW -> 0xFFD7FFD9.toInt() to 0xFF155724.toInt()
        }
        
        holder.tvPriority.setBackgroundColor(priorityColor)
        holder.tvPriority.setTextColor(priorityTextColor)

        updateStrikeThrough(holder.tvTaskTitle, task.isCompleted)

        // Prevent listener trigger during recycling
        holder.cbDone.setOnCheckedChangeListener(null)
        holder.cbDone.isChecked = task.isCompleted
        holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
            task.isCompleted = isChecked
            updateStrikeThrough(holder.tvTaskTitle, isChecked)
            onTaskChanged()
        }

        holder.btnDelete.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                onTaskDelete(currentPos)
            }
        }
        
        holder.itemView.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                onTaskEdit(currentPos)
            }
        }
    }

    override fun getItemCount(): Int = tasks.size

    private fun updateStrikeThrough(textView: TextView, isCompleted: Boolean) {
        if (isCompleted) {
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }
}