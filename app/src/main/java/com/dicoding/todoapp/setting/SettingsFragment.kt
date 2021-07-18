package com.dicoding.todoapp.setting

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.work.*
import com.dicoding.todoapp.R
import com.dicoding.todoapp.ui.ViewModelFactory
import com.dicoding.todoapp.ui.list.TaskViewModel
import com.dicoding.todoapp.utils.FunctionLibrary
import com.dicoding.todoapp.utils.reminder.MyWorker
import java.util.concurrent.TimeUnit

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var workManager: WorkManager
    private lateinit var periodicWorkRequest: PeriodicWorkRequest
    private lateinit var taskViewModel: TaskViewModel

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
            val channelName = getString(R.string.notify_channel_name)
            //TODO 13 : Schedule and cancel daily reminder using WorkManager with data channelName


            true
        }
    }

    private fun updateTheme(mode: Int): Boolean {
        AppCompatDelegate.setDefaultNightMode(mode)
        requireActivity().recreate()
        return true
    }

    private fun startPeriodicTask() {
//            binding.textStatus.text = getString(R.string.status)
        val task = taskViewModel.getNearestActiveTask()
        val data = Data.Builder()
            .putString(MyWorker.EXTRA_TASK_TITLE, task.title)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        periodicWorkRequest = PeriodicWorkRequest.Builder(MyWorker::class.java, 1, TimeUnit.DAYS)
            .setInputData(data)
            .setConstraints(constraints)
            .build()

        workManager.enqueue(periodicWorkRequest)
        workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id)
            .observe(viewLifecycleOwner, { workInfo ->
                val status = workInfo.state.name
                FunctionLibrary.showToast(requireContext(), "status : $status")

                if (workInfo.state == WorkInfo.State.ENQUEUED) {
//                    binding.btnCancelTask.isEnabled = true
                }
            })
    }
    private fun cancelPeriodicTask() {
        workManager.cancelWorkById(periodicWorkRequest.id)
    }
}