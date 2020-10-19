package com.example.sipdemoapp

import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.example.sipdemoapp.ui.SipDemoAppTheme

@Composable
fun MainScreen(viewModel: MainViewModel = MainViewModel(), requestMic: () -> Unit = {}) {
    SipDemoAppTheme {
        Scaffold(topBar = {
            TopAppBar() {
                IconButton(onClick = requestMic) {
                    Icon(Icons.Filled.Phone)
                }
            }
        }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Current user: ${viewModel.currentUser.username}    Server status: ${viewModel.hasStarted}")
                MainButtons(
                    startServer = viewModel::startServer,
                    stopServer = viewModel::stopServer,
                    makeCall = viewModel::makeCall,
                    hangUp = viewModel::hangUp
                )

                Column(modifier = Modifier.padding(8.dp)) {
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
                }

                Text("Logs", modifier = Modifier.padding(16.dp))
                Divider(modifier = Modifier.fillMaxWidth(), color = Color.Black, thickness = 4.dp)
                Logs(logs = viewModel.logs, needsToScroll = viewModel.needsToScroll, resetNeedsToScroll = viewModel::resetNeedsToScroll)
            }
        }
    }
}

@Composable
fun MainButtons(
    startServer: () -> Unit,
    stopServer: () -> Unit,
    makeCall: () -> Unit,
    hangUp: () -> Unit
) {
    Column(modifier = Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = startServer) {
                Text(text = "Start")
            }
            Button(onClick = stopServer) {
                Text(text = "Stop")
            }
            Button(onClick = makeCall) {
                Text(text = "Call")
            }
            Button(onClick = hangUp) {
                Text(text = "Hangup")
            }
        }
    }
}

@Composable
fun Logs(logs: List<String>, needsToScroll: Boolean, resetNeedsToScroll: () -> Unit) {
    val scrollState = rememberScrollState()
    ScrollableColumn(scrollState = scrollState) {
        logs.forEach { log ->
            Text(log)
        }
    }
    if(needsToScroll) {
        scrollState.smoothScrollTo(logs.size.toFloat() * 100)
        resetNeedsToScroll()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainScreen()
}