package com.uillover.taskfire.tasks



import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import androidx.activity.viewModels
import android.view.View
import com.uillover.taskfire.R


import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.uillover.taskfire.databinding.ActivityMainBinding
import com.uillover.taskfire.profile.ProfileActivity
import com.uillover.taskfire.utils.FirebaseUtils

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        viewModel.loadCompletedTasks()
        binding.btnShowHistory.setOnClickListener {
            showCompletedTasks()
            binding.btnShowActiveTasks.visibility = View.VISIBLE
            binding.btnShowHistory.visibility = View.GONE
        }

        binding.btnShowActiveTasks.setOnClickListener {
            showActiveTasks()
            binding.btnShowActiveTasks.visibility = View.GONE
            binding.btnShowHistory.visibility = View.VISIBLE
        }


        binding.fabAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        val userId = FirebaseUtils.getCurrentUserId() ?: ""
        taskAdapter = TaskAdapter(emptyList(), userId,
            onTaskClick = { task ->
                val intent = Intent(this, TaskDetailActivity::class.java)
                intent.putExtra("task_id", task.id)
                startActivity(intent)
            },
            onTaskLongClick = { task ->
                viewModel.deleteTask(task)
            }
        )


        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }
    }

    private fun setupObservers() {
        viewModel.tasks.observe(this) { tasks ->
            taskAdapter.updateTasks(tasks)
            binding.emptyState.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }
    }
    private fun showActiveTasks() {
        viewModel.tasks.observe(this) { tasks ->
            taskAdapter.updateTasks(tasks.filter { !it.completed }) // Affiche les tâches non complétées
            binding.emptyState.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showCompletedTasks() {
        viewModel.completedTasks.observe(this) { tasks ->
            binding.rvTasks.post {
                taskAdapter.updateTasks(tasks)
                binding.emptyState.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.menu_logout -> {
                FirebaseUtils.logout()
                finish()
                true
            }
            R.id.menu_history -> {
                showCompletedTasks() // Affiche les tâches complétées
                true
            }R.id.menu_active_tasks -> {
                showActiveTasks() // Afficher les tâches non complétées
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}