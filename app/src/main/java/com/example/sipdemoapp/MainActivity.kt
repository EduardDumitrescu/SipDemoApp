package com.example.sipdemoapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.setContent
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()

    var terminateNotifThread = false
    //var notifThread: GetNotificationsThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.sipCLient.Init(applicationContext)
        listenForEvents()
        setContent {
            MainScreen(viewModel = viewModel, requestMic = this::requestMicrophone)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestMicrophone() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        }
    }

    fun listenForEvents() {
        thread {
            while (!terminateNotifThread) {
                try {
                    var sipnotifications = ""
                    //get notifications from the SIP stack
                    sipnotifications = viewModel.sipCLient.GetNotifications()
                    if (sipnotifications != null && sipnotifications!!.length > 0) {
                        // send notifications to Main thread using a Handler
                        val messageToMainThread = Message()
                        val messageData = Bundle()
                        messageToMainThread.what = 0
                        messageData.putString("notifmessages", sipnotifications)
                        messageToMainThread.setData(messageData)
                        handleMessage(messageToMainThread)
                    }

                    if ((sipnotifications == null || sipnotifications!!.length < 1) && !terminateNotifThread) {
                        //some error occured. sleep a bit just to be sure to avoid busy loop
                        Thread.sleep(1)
                    }
                    continue
                } catch (e: Throwable) {
                    viewModel.addLog("ERROR, WorkerThread on run()intern ${e.message}")
                }
                if (!terminateNotifThread) {
                    Thread.sleep(10)
                }
            }
        }
    }
    fun handleMessage(msg: Message?) {
        if (msg == null || msg.data == null) return
        val resBundle = msg.data
        val receivedNotif = msg.data.getString("notifmessages")
        if (receivedNotif != null && receivedNotif.length > 0) {
            var notarray: Array<String?>? = null
                if (receivedNotif.length < 1) return
                notarray = receivedNotif.split("\r\n").toTypedArray()
                if (notarray.size < 1) return
                for (i in notarray.indices) {
                    if (notarray[i] != null && notarray!![i]!!.length > 0) {
                        if (notarray[i]!!.indexOf("WPNOTIFICATION,") == 0) notarray!![i] = notarray!![i]!!
                            .substring(15) //remove the WPNOTIFICATION, prefix
                        ProcessNotifications(notarray!![i])
                    }
                }

        }
    }

//    inner class GetNotificationsThread : Thread() {
//        var sipnotifications: String? = ""
//        override fun run() {
//            try {
//                try {
//                    currentThread().priority = 4
//                } catch (e: Throwable) {
//                } //we are lowering this thread priority a bit to give more chance for our main GUI thread
//                while (!terminateNotifThread) {
//                    try {
//                        sipnotifications = ""
//                        //get notifications from the SIP stack
//                        sipnotifications = viewModel.sipCLient.GetNotificationsSync()
//                        if (sipnotifications != null && sipnotifications!!.length > 0) {
//                            // send notifications to Main thread using a Handler
//                            val messageToMainThread = Message()
//                            val messageData = Bundle()
//                            messageToMainThread.what = 0
//                            messageData.putString("notifmessages", sipnotifications)
//                            messageToMainThread.setData(messageData)
//                            NotifThreadHandler.sendMessage(messageToMainThread)
//                        }
//
//                        if ((sipnotifications == null || sipnotifications!!.length < 1) && !terminateNotifThread) {
//                            //some error occured. sleep a bit just to be sure to avoid busy loop
//                            sleep(1)
//                        }
//                        continue
//                    } catch (e: Throwable) {
//                        Log.e(LOGTAG, "ERROR, WorkerThread on run()intern", e)
//                    }
//                    if (!terminateNotifThread) {
//                        sleep(10)
//                    }
//                }
//            } catch (e: Throwable) {
//                Log.e(LOGTAG, "WorkerThread on run()")
//            }
//        }
//    }

    //get the notifications from the GetNotificationsThread thread
//    companion object {
//        var NotifThreadHandler: Handler = object : Handler(Looper.myLooper()!!) {
//            override fun handleMessage(msg: Message?) {
//                try {
//                    if (msg == null || msg.data == null) return
//                    val resBundle = msg.data
//                    val receivedNotif = msg.data.getString("notifmessages")
//                    if (receivedNotif != null && receivedNotif.length > 0) instance.ReceiveNotifications(
//                        receivedNotif
//                    )
//                } catch (e: Throwable) {
//                    viewModel(LOGTAG, "NotifThreadHandler handle Message")
//                }
//            }
//        }
//    }

    //process notificatins phrase 1: split by line (we can receive multiple notifications separated by \r\n)
//    var notarray: Array<String?>? = null
//    fun ReceiveNotifications(notifs: String?) {
//        if (notifs == null || notifs.length < 1) return
//        notarray = notifs.split("\r\n").toTypedArray()
//        if (notarray == null || notarray!!.size < 1) return
//        for (i in notarray!!.indices) {
//            if (notarray!![i] != null && notarray!![i]!!.length > 0) {
//                if (notarray!![i]!!.indexOf("WPNOTIFICATION,") == 0) notarray!![i] = notarray!![i]!!
//                    .substring(15) //remove the WPNOTIFICATION, prefix
//                ProcessNotifications(notarray!![i])
//            }
//        }
//    }

    //process notificatins phrase 2: processing notification strings
    fun ProcessNotifications(notification: String?) {
        var notif = notification
        viewModel.addLog(notif ?: "") //we just display them in this simple test application
        //see the Notifications section in the documentation about the possible messages (parse the notification string and process them after your needs)


        //some example code for notification parsing:
        if (notification!!.indexOf("WPNOTIFICATION,") == 0) //remove WPNOTIFICATION prefix
        {
            notif = notification.substring("WPNOTIFICATION,".length)
        }
        val params = notification.split(",").toTypedArray()
        if (params.size < 2) return
        notif =
            notification.substring(notification.indexOf(',')) //keep only the rest in the notification variable
        if (params[5] == "2") {
            val line: Int = params[1].toIntOrNull() ?: 0
            //if(line != -1) return;  //we handle only the global state. See the "Multiple lines" FAQ point in the documentation if you wish to handle individual lines explicitely
            val endpointtype: Int = params[5].toIntOrNull() ?: 0
            if (endpointtype == 2) //incoming call
            {
                viewModel.addLog("Incoming call from " + params[3] + " " + params[6])
                viewModel.sipCLient.Accept(-1) //auto accept incoming call. you might disaplay ACCEPT / REJECT buttons instead
            }
        } else if (params[0] == "POPUP") {
            Toast.makeText(this, notification, Toast.LENGTH_LONG).show()
        }
        //else parse other parameters as needed. See the Notifications section in the documentation for the details.
    }
}


