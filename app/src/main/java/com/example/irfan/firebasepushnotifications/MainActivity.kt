package com.example.irfan.firebasepushnotifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import com.example.irfan.firebasepushnotifications.config.Config
import com.example.irfan.firebasepushnotifications.util.NotificationUtils
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private var mRegistrationBroadcastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRegistrationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                // checking for type intent filter
                if (intent.action == Config.REGISTRATION_COMPLETE) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL)

                    displayFirebaseRegId()

                } else if (intent.action == Config.PUSH_NOTIFICATION) {
                    // new push notification is received

                    val message = intent.getStringExtra("message")

                    Toast.makeText(applicationContext, "Push notification: $message", Toast.LENGTH_LONG).show()

                    txt_push_message.setText(message)
                }
            }
        }

        displayFirebaseRegId()
    }

    private fun displayFirebaseRegId() {
        val pref = applicationContext.getSharedPreferences(Config.SHARED_PREF, 0)
        val regId = pref.getString("regId", null)

        // Log.e(FragmentActivity.TAG, "Firebase reg id: " + regId!!)

        if (!TextUtils.isEmpty(regId))
            txt_reg_id.setText("Firebase Reg Id: $regId")
        else
            txt_reg_id.setText("Firebase Reg Id is not received yet!")
    }

    override fun onResume() {
        super.onResume()

        // register GCM registration complete receiver
        mRegistrationBroadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).registerReceiver(it,
                    IntentFilter(Config.REGISTRATION_COMPLETE))

            // register new push message receiver
            // by doing this, the activity will be notified each time a new message arrives
            LocalBroadcastManager.getInstance(this).registerReceiver(it,
                    IntentFilter(Config.PUSH_NOTIFICATION))
        }
        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(applicationContext)
    }

    override fun onPause() {
        mRegistrationBroadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
        }
        super.onPause()
    }
}
