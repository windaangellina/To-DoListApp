package com.dicoding.todoapp.data

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.dicoding.todoapp.utils.TASK_TABLE_NAME

//TODO 2 : Define data access object (DAO)
@Dao
interface TaskDao {

    @RawQuery(observedEntities = [Task::class])
    fun getTasks(query: SupportSQLiteQuery): DataSource.Factory<Int, Task>

    @Query("select * from $TASK_TABLE_NAME where id = :taskId")
    fun getTaskById(taskId: Int): LiveData<Task>

    @Query("select * from $TASK_TABLE_NAME where completed = 0 order by dueDateMillis ASC limit 1")
    fun getNearestActiveTask(): LiveData<Task>

    @Insert
    suspend fun insertTask(task: Task): Long

    @Insert
    fun insertAll(vararg tasks: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("update $TASK_TABLE_NAME set completed = :completed where id = :taskId")
    suspend fun updateCompleted(taskId: Int, completed: Boolean)
    
}
