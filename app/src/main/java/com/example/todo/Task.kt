package com.example.todo

enum class Priority {
    LOW, MEDIUM, HIGH
}

data class Task(
    var title: String,
    var isCompleted: Boolean = false,
    var time: String = "",
    var priority: Priority = Priority.LOW
)