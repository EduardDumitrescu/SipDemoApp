package com.example.sipdemoapp

import android.provider.MediaStore
import android.widget.FrameLayout
import androidx.compose.animation.animate
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.ui.tooling.preview.Preview
import com.example.sipdemoapp.ui.SipDemoAppTheme
import com.example.sipdemoapp.ui.darkGreen
import com.example.sipdemoapp.ui.darkRed

@Composable
fun MainScreen(viewModel: MainViewModel = MainViewModel(), requestMic: () -> Unit = {}, requestVideo: () -> Unit = {}) {
    val act = ContextAmbient.current as MainActivity

    SipDemoAppTheme {
        Scaffold(topBar = {
            TopAppBar() {
                IconButton(onClick = requestMic) {
                    Icon(Icons.Filled.Phone)
                }
                IconButton(onClick = requestVideo) {
                    Icon(Icons.Filled.Person)
                }
            }
        }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Current user: ${viewModel.currentUser.username} Server status: ${viewModel.hasStarted}")

                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = viewModel::changeUserToAJVOIP) {
                        Text(text = "Change to AJVOIP")
                    }
                    Button(onClick = viewModel::changeUserToSDK2) {
                        Text(text = "Change to SDK2")
                    }
                }

                ServerButtons(
                    startServer = viewModel::startServer,
                    stopServer = viewModel::stopServer,
                    restartServer = viewModel::restartServer,
                    hasStarted = viewModel.hasStarted
                )

                CallButtons(
                    makeCall = viewModel::makeCall,
                    hangUp = viewModel::hangUp,
                    isInCall = viewModel.isInCall
                )

                CallerZone(
                    answerCall = viewModel::answerCall,
                    rejectCall = viewModel::rejectCall,
                    otherCaller = viewModel.otherCaller,
                    isInCall = viewModel.isInCall
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {
                        viewModel.sipCLient.VideoCall(viewModel.secondUser.username, MainActivity.VIDEO_SCREEN_ID, act)
                    }) {
                        Text(text = "Call vid")
                    }
                    VideoScreen(
                        modifier = Modifier.width(128.dp).height(128.dp)
                            .background(color = Color.Blue)
                    )
                    Button(onClick = { viewModel.sipCLient.AcceptVideo(MainActivity.VIDEO_SCREEN_ID, act) }) {
                        Text(text = "Answer")
                    }
                }

                LogsZone(
                    logs = viewModel.logs,
                    needsToScroll = viewModel.needsToScroll,
                    resetNeedsToScroll = viewModel::resetNeedsToScroll,
                    modifier = Modifier.fillMaxHeight(fraction = 20f)
                )
            }
        }
    }
}

@Composable
fun CallerZone(
    answerCall: () -> Unit,
    rejectCall: () -> Unit,
    otherCaller: String?,
    isInCall: Boolean
) {
    val height = animate(if (otherCaller == null) 0.dp else 64.dp)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Other caller: ${otherCaller ?: "unknown"}")
        Row(
            modifier = Modifier.fillMaxWidth().height(height = height).padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = answerCall, backgroundColor = darkGreen, enabled = !isInCall) {
                Text(text = "Answer")
            }
            Button(onClick = rejectCall, backgroundColor = darkRed, enabled = !isInCall) {
                Text(text = "Reject")
            }
        }
    }
}

@Composable
fun LogsZone(
    logs: List<String>,
    needsToScroll: Boolean,
    resetNeedsToScroll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Logs", modifier = Modifier.padding(16.dp))
        Divider(modifier = Modifier.fillMaxWidth(), color = Color.Black, thickness = 4.dp)
        Logs(
            logs = logs,
            needsToScroll = needsToScroll,
            resetNeedsToScroll = resetNeedsToScroll,
        )
    }
}

@Composable
fun ServerButtons(
    startServer: () -> Unit,
    stopServer: () -> Unit,
    restartServer: () -> Unit,
    hasStarted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = startServer, enabled = !hasStarted) {
            Text(text = "Start")
        }
        Button(onClick = stopServer, enabled = hasStarted) {
            Text(text = "Stop")
        }
        Button(onClick = restartServer, enabled = hasStarted) {
            Text(text = "Restart")
        }
    }
}

@Composable
fun CallButtons(
    makeCall: () -> Unit,
    hangUp: () -> Unit,
    isInCall: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = makeCall,
            backgroundColor = if (!isInCall) darkGreen else Color.Gray,
            enabled = !isInCall
        ) {
            Text(text = "Call")
        }
        Button(
            onClick = hangUp,
            backgroundColor = if (isInCall) darkRed else Color.Gray,
            enabled = isInCall
        ) {
            Text(text = "Hangup")
        }
    }
}

@Composable
fun Logs(
    logs: List<String>,
    needsToScroll: Boolean,
    resetNeedsToScroll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    ScrollableColumn(scrollState = scrollState, modifier = modifier) {
        logs.forEach { log ->
            Text(log)
        }
    }
    if (needsToScroll) {
        scrollState.smoothScrollTo(logs.size.toFloat() * 100)
        resetNeedsToScroll()
    }
}

@Composable
fun VideoScreen(modifier: Modifier = Modifier) {
    val context = ContextAmbient.current
    val video = remember {
        FrameLayout(context).apply {
            id = MainActivity.VIDEO_SCREEN_ID
        }
    }
    AndroidView({ video }, modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainScreen()
}