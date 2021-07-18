package com.dicoding.todoapp.ui.detail

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.todoapp.R
import com.dicoding.todoapp.data.Task
import com.dicoding.todoapp.ui.ViewModelFactory
import com.dicoding.todoapp.ui.list.TaskViewModel
import com.dicoding.todoapp.utils.DateConverter
import com.dicoding.todoapp.utils.FunctionLibrary
import com.dicoding.todoapp.utils.TASK_ID
import com.google.android.material.textfield.TextInputEditText

class DetailTaskActivity : AppCompatActivity() {

    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        //TODO 11 : Show detail task and implement delete action
        val factory = ViewModelFactory.getInstance(this)
        taskViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)

        var taskId : Int? = null
        if (intent.hasExtra(TASK_ID)){
            taskId = intent.getIntExtra(TASK_ID, -1)
            val task = taskViewModel.getTaskById(taskId = taskId)
            task.observe(this, {
                if (it == null){
                    finish()
                }
                else{
                    bind(task = it)
                }
            })
        }
    }

    private fun bind(task: Task){
        val tvTitle : TextInputEditText = findViewById(R.id.detail_ed_title)
        val tvDueDate : TextInputEditText = findViewById(R.id.detail_ed_due_date)
        val tvDescription : TextInputEditText = findViewById(R.id.detail_ed_description)
        val btnDelete : Button = findViewById(R.id.btn_delete_task)

        tvTitle.setText(task.title)
        tvDueDate.setText(DateConverter.convertMillisToString(task.dueDateMillis))
        tvDescription.setText(task.description)

        btnDelete.setOnClickListener {
            taskViewModel.deleteTask(task)
            finish()
            FunctionLibrary.showToast(context = applicationContext, "Task has been deleted")
        }
    }
}