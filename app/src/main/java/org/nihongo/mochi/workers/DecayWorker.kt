package org.nihongo.mochi.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.nihongo.mochi.MainActivity
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager

class DecayWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        ScoreManager.decayScores(applicationContext)
        
        // We only send a notification if the decay actually happened.
        // But ScoreManager.decayScores doesn't return info on whether something decayed.
        // For simplicity, we can assume this worker runs once a week, so we send the notification.
        // Or we could run it daily and check if we should notify.
        // Given the requirement "pusher une notification", let's send it.
        
        sendNotification()
        
        return Result.success()
    }

    private fun sendNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "mochi_decay_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Mochi Learning Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to keep your Mochi fresh!"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val messages = listOf(
            "Don't let your Mochi dry out! 🍡 Time for a quick review?",
            "Your Kanji missed you! Come back to refresh your memory! ✨",
            "A fresh Mochi is a happy Mochi! Let's practice! 🍵",
            "Mochi-Mochi! It's time to stretch your brain! 🧠"
        )
        
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback icon
            .setContentTitle("Nihongo Mochi")
            .setContentText(messages.random())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // ID 1 to update the same notification
        notificationManager.notify(1, notification)
    }
}
