package com.dicoding.todoapp.utils.reminder

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dicoding.todoapp.R
import com.dicoding.todoapp.setting.SettingsActivity
import com.dicoding.todoapp.ui.list.TaskActivity
import com.dicoding.todoapp.utils.NOTIFICATION_CHANNEL_ID
import com.dicoding.todoapp.utils.NOTIFICATION_CHANNEL_ID_NUM

class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private var taskTitle : String = ""

    companion object {
        const val EXTRA_TASK_TITLE = "extra-task-title"
    }

    private var resultStatus: Result? = null

    override fun doWork(): Result {
        taskTitle = inputData.getString(EXTRA_TASK_TITLE).toString()
        showAlarmNotification(applicationContext, "Nearest Task To-Do", taskTitle)
        resultStatus = Result.success()
        return resultStatus as Result
    }

    private fun showAlarmNotification(context: Context, title: String, message: String) {
        // waktu notif diklik, mau buka activity apa
        val notificationIntent = Intent(context, TaskActivity::class.java)

        // model task stack jadi kalo ada notif baru, modelnya bakalan numpuk jadi beberapa notif
        val taskStackBuilder : TaskStackBuilder = TaskStackBuilder.create(context)
        taskStackBuilder.addParentStack(SettingsActivity::class.java)
        taskStackBuilder.addNextIntent(notificationIntent)

        val pendingIntent : PendingIntent = taskStackBuilder.getPendingIntent(100, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationManagerCompat = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(ContextCompat.getColor(context, android.R.color.transparent))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setSound(alarmSound)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // Untuk android Oreo ke atas perlu menambahkan notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */
            val channelName = context.getString(R.string.notify_channel_name)
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT)

            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            builder.setChannelId(NOTIFICATION_CHANNEL_ID)
            notificationManagerCompat.createNotificationChannel(channel)
        }

        builder.setAutoCancel(true)
        val notification = builder.build()
        notification.flags = Notification.FLAG_AUTO_CANCEL or Notification.FLAG_ONGOING_EVENT
        notificationManagerCompat.notify(NOTIFICATION_CHANNEL_ID_NUM, notification)
    }
}