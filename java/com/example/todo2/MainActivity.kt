package com.example.todo2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import java.util.UUID
import androidx.compose.ui.graphics.Color

// Timothy Hyde
// 2025
// Project 1, TO-DO APP
// 4 October 2025

@Composable
private fun Theme(content: @Composable () -> Unit) {
// black and magenta color theme
    val blackTheme = darkColorScheme(
        background = Color.Black,
        surface = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        primary = Color.Magenta,
        onPrimary = Color.Black
    )

    MaterialTheme(

        colorScheme = blackTheme,
        typography = Typography(),
        content = content
    )
}

data class TodoItem(

    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val done: Boolean = false
)

// ---------------------------------------------------------------------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Theme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TodoApp()
                }
            }
        }
    }
}

@Composable
fun TodoApp() {
    var active by remember { mutableStateOf(listOf<TodoItem>()) }
    var completed by remember { mutableStateOf(listOf<TodoItem>()) }

    val onAdd: (String) -> Unit = { raw ->
        val t = raw.trim()
        if (t.isNotEmpty()) active = active + TodoItem(label = t)
    }
    val onToggle: (String, Boolean) -> Unit = { id, checked ->
        if (checked) {
            active.firstOrNull { it.id == id }?.let { found ->
                active = active.filterNot { it.id == id }
                completed = completed + found.copy(done = true)
            }
        } else {
            completed.firstOrNull { it.id == id }?.let { found ->
                completed = completed.filterNot { it.id == id }
                active = active + found.copy(done = false)
            }
        }
    }

    val onDelete: (String, Boolean) -> Unit = { id, fromCompleted ->
        if (fromCompleted) completed = completed.filterNot { it.id == id }
        else active = active.filterNot { it.id == id }
    }

    TodoScreen(

        // active =
        active = active,
        completed = completed,
        onAdd = onAdd,
        onToggle = onToggle,
        onDelete = onDelete
    )
}

@Composable
private fun TodoScreen(

    active: List<TodoItem>,
    completed: List<TodoItem>,
    onAdd: (String) -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onDelete: (String, Boolean) -> Unit,


) {
    Column(Modifier.padding(16.dp).fillMaxSize()) {

        // -----------------------
        Text("TODO List", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        var text by remember { mutableStateOf("") }
        var showError by remember { mutableStateOf(false) }

        Row(verticalAlignment = Alignment.CenterVertically) {

            Column(Modifier.weight(1f)) {

                OutlinedTextField(
                    value = text,

                    onValueChange = {
                        text = it
                        if (showError && it.isNotBlank()) showError = false
                    },
                    singleLine = true,
                    label = { Text("Enter the task name") },

                    isError = showError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (text.trim().isBlank()) showError = true
                        else { onAdd(text); text = ""; showError = false }
                    }),
                    modifier = Modifier.fillMaxWidth()
                )
                if (showError) {
                    Text(
                        "erorr",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Button(onClick = {
                if (text.trim().isBlank()) showError = true
                else { onAdd(text); text = ""; showError = false }
            }) { Text("Add") }
        }

        Spacer(Modifier.height(16.dp))

        if (active.isNotEmpty()) Text("Items", style = MaterialTheme.typography.titleMedium)
        if (active.isEmpty()) EmptyState("empty")

        else TodoList(active, false, onToggle) { id -> onDelete(id, false) }

        Spacer(Modifier.height(16.dp))

        if (completed.isNotEmpty()) Text("Completed Items", style = MaterialTheme.typography.titleMedium)
        if (completed.isEmpty()) EmptyState("No completed items")
        else TodoList(completed, true, onToggle) { id -> onDelete(id, true) }
    }
}

@Composable
private fun EmptyState(msg: String) {
    Text(

        msg,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 6.dp)

    )
}

@Composable
private fun TodoList(



    items: List<TodoItem>,
    completed: Boolean,

    onToggle: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit

    // lazy column to expand, no crashing
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items, key = { it.id }) { item ->
            TodoRow(item, completed, onToggle, onDelete)
        }
    }
}

@Composable
private fun TodoRow(
    item: TodoItem,
    isCompletedList: Boolean,
    onToggle: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {

    Surface(tonalElevation = 2.dp, shape = MaterialTheme.shapes.medium) {
        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.label, modifier = Modifier.weight(1f))
            Checkbox(checked = item.done, onCheckedChange = { onToggle(item.id, it) })
            IconButton(onClick = { onDelete(item.id) }) {
                Icon(Icons.Filled.Close, contentDescription = "Delete")
            }
        }
    }
}