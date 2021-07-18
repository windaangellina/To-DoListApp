package com.dicoding.todoapp.ui.list

import android.content.Context
import androidx.lifecycle.*
import androidx.paging.PagedList
import com.dicoding.todoapp.R
import com.dicoding.todoapp.data.Task
import com.dicoding.todoapp.data.TaskRepository
import com.dicoding.todoapp.utils.Event
import com.dicoding.todoapp.utils.TasksFilterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val _filter = MutableLiveData<TasksFilterType>()

    val tasks: LiveData<PagedList<Task>> = _filter.switchMap {
        taskRepository.getTasks(it)
    }

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    init {
        _filter.value = TasksFilterType.ALL_TASKS
    }

    fun filter(filterType: TasksFilterType) {
        _filter.value = filterType
    }

    fun completeTask(task: Task, completed: Boolean) = viewModelScope.launch {
        taskRepository.completeTask(task, completed)
        if (completed) {
            _snackbarText.value = Event(R.string.task_marked_complete)
        } else {
            _snackbarText.value = Event(R.string.task_marked_active)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.deleteTask(task)
        }
    }

    fun getTaskById(taskId: Int) : LiveData<Task>{
        return taskRepository.getTaskById(taskId = taskId)
    }

    fun getNearestActiveTask() : Task{
        return taskRepository.getNearestActiveTask()
    }

    fun insertTask(task: Task) : Long{
        var returnValue : Long = -1
        viewModelScope.launch(Dispatchers.IO) {
            returnValue = taskRepository.insertTask(task)
        }
        return returnValue
    }
}