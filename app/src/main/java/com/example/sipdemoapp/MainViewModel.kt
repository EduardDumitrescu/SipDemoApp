package com.example.sipdemoapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mizuvoip.jvoip.SipStack

class MainViewModel : ViewModel() {
    var hasStarted: Boolean by mutableStateOf(false)
    val sipCLient: SipStack by lazy { SipStack() }
    var currentUser by mutableStateOf(AJVOIP_USER)
        private set

    var needsToScroll: Boolean by mutableStateOf(true)

    private val parameters by lazy {
        mutableMapOf(
            LOG_LEVEL to "5",
            SERVER_ADDRESS to "voip.mizu-voip.com",
            USERNAME to AJVOIP_USER.username,
            PASSWORD to AJVOIP_USER.password
        )
    }

    var logs by mutableStateOf(listOf<String>())
        private set

    fun addLog(log: String) {
        logs = logs + listOf(log)
        needsToScroll = true
    }

    private fun updateParams() {
        for ((k,v) in parameters) {
            sipCLient.SetParameter(k, v)
        }
    }

    fun startServer() {
        updateParams()
        sipCLient.Start()
        hasStarted = true
    }

    fun stopServer() {
        sipCLient.Stop()
        hasStarted = false
    }

    fun resetNeedsToScroll() {
        needsToScroll = false
    }

    fun restartServer() {
        updateParams()
        sipCLient.ReStart()
        hasStarted = true
    }

    fun makeCall(user: String? = null) {
        val otherUser =  user ?: if(currentUser == AJVOIP_USER) { SDK2_USER.username } else { AJVOIP_USER.username }
        if (!hasStarted) {
            addLog("ERROR, cannot initiate call because SipStack is not started");
        } else {
            sipCLient.Call(-1,otherUser)
        }
    }
    fun hangUp() {
        addLog("Hang up on click")
        if (!hasStarted) {
            addLog("ERROR, cannot hang up because SipStack is not started");
        } else {
            sipCLient.Hangup()
        }
    }

    fun changeUserToSDK2() {
        parameters[USERNAME] = SDK2_USER.username
        parameters[PASSWORD] = SDK2_USER.password
        currentUser = SDK2_USER
    }

    fun changeUserToAJVOIP() {
        parameters[USERNAME] = AJVOIP_USER.username
        parameters[PASSWORD] = AJVOIP_USER.password
        currentUser = AJVOIP_USER
    }

    companion object {
        private const val LOG_LEVEL = "loglevel"
        private const val SERVER_ADDRESS = "serveraddress"
        private const val USERNAME = "username"
        private const val PASSWORD = "password"

        private val SDK2_USER: User = User("sdktest2", "sdktest2")
        private val AJVOIP_USER: User = User("ajvoiptest", "ajvoip1234")
    }


//    val currentEditItem: TodoItem?
//        get() = todoItems.getOrNull(currentEditPosition)
//
//
//    fun removeItem(item: TodoItem) {
//        todoItems = todoItems.toMutableList().also { it.remove(item) }
//        onEditDone() // don't keep the editor open when removing items
//    }
//
//    fun onEditItemSelected(item: TodoItem) {
//        currentEditPosition = todoItems.indexOf(item)
//    }
//
//    fun onEditDone() {
//        currentEditPosition = -1
//    }
//
//    fun onEditItemChange(item: TodoItem) {
//        val currentItem = requireNotNull(currentEditItem)
//        require(currentItem.id == item.id) {
//            "You can only change an item with the same id as currentEditItem"
//        }
//
//        todoItems = todoItems.toMutableList().also {
//            it[currentEditPosition] = item
//        }
//    }
}

data class User(val username: String, val password: String)