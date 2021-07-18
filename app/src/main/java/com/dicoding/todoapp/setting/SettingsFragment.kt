package com.dicoding.todoapp.setting

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.dicoding.todoapp.R
import com.dicoding.todoapp.notification.NotificationWorker
import com.dicoding.todoapp.ui.ViewModelFactory
import com.dicoding.todoapp.ui.list.TaskViewModel
import com.dicoding.todoapp.utils.FunctionLibrary
import com.dicoding.todoapp.utils.NOTIFICATION_CHANNEL_ID
import java.util.concurrent.TimeUnit

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var workManager: WorkManager
    private lateinit var periodicWorkRequest: PeriodicWorkRequest
    private lateinit var taskViewModel: TaskViewModel

    companion object{
        const val TAG = "SettingsFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workManager = WorkManager.getInstance(requireContext())

        val factory = ViewModelFactory.getInstance(requireContext())
        taskViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val prefNotification = findPreference<SwitchPreference>(getString(R.string.pref_key_notify))
        prefNotification?.setOnPreferenceChangeListener { preference, newValue ->
            //TODO 13 : Schedule and cancel daily reminder using WorkManager with data channelName
            if(newValue == true){
                startPeriodicTask()
            }
            else{
                try {
                    workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id)
                        .observe(viewLifecycleOwner, { workInfo ->
                            val status = workInfo.state.name
                            Log.d(TAG, "WorkManager Status : $status")
                            if (workInfo.state == WorkInfo.State.ENQUEUED) {
                                cancelPeriodicTask()
                                FunctionLibrary.showToast(
                                    requireContext(),
                                    "Task reminder has been cancelled"
                                )
                            }
                        })
                }catch (e: Exception){
                    Log.e(TAG, "Cancelling Reminder Failed : ${e.message}")
                }
            }
            true
        }
    }

    private fun updateTheme(mode: Int): Boolean {
        AppCompatDelegate.setDefaultNightMode(mode)
        requireActivity().recreate()
        return true
    }

    private fun startPeriodicTask() {
        val data = Data.Builder()
            .putString(NOTIFICATION_CHANNEL_ID, "notification_id_reminder")
            .build()

        // daily task
        periodicWorkRequest = PeriodicWorkRequest.Builder(
            NotificationWorker::class.java,
            1, TimeUnit.DAYS
        ).setInputData(data).build()

        workManager.enqueue(periodicWorkRequest)
        workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id)
            .observe(viewLifecycleOwner, { workInfo ->
                val status = workInfo.state.name
                Log.d(TAG, "WorkManager Status : $status")
                if (workInfo.state == WorkInfo.State.ENQUEUED) {
                    Log.d(TAG, "Reminder has been enqueued")
                }
            })
    }

    private fun cancelPeriodicTask() {
        try {
            workManager.cancelWorkById(periodicWorkRequest.id)
        }catch (e : Exception){
            Log.e(TAG, "Cancel Periodic Work Failed : ${e.message}")
        }
    }
}