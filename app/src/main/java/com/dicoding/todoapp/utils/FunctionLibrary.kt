package com.dicoding.todoapp.utils

import android.content.Context
import android.widget.Toast

object FunctionLibrary {
    fun showToast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}