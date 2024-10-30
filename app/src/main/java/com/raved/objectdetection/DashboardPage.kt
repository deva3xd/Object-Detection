package com.raved.objectdetection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raved.objectdetection.ui.theme.ObjectDetectionTheme

@Composable
fun DashboardPage() {
    val context = LocalContext.current

    // handle alert
    var showAlert by remember { mutableStateOf(false) }
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text(text = "Quit") },
            text = { Text("Are you sure?") },
            shape = RectangleShape,
            confirmButton = {
                Button(
                    onClick = { (context as? MainActivity)?.finish() },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(colorResource(id = R.color.red))
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAlert = false },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(colorResource(id = R.color.black))
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    ObjectDetectionTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(colorResource(id = R.color.primary))
            ) {
                // header
                Card(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RectangleShape,
                    colors = CardDefaults.cardColors(colorResource(id = R.color.black))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Object Detection",
                            modifier = Modifier.padding(16.dp),
                            color = Color.White,
                            fontSize = 25.sp,
                        )
                    }
                }

                // button
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Button(
                        onClick = { showAlert = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.red))
                    ) {
                        Text(text = "Quit", color = Color.White)
                    }
                }
            }
        }
    }
}