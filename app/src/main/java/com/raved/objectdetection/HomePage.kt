package com.raved.objectdetection

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.rememberAsyncImagePainter
import com.raved.objectdetection.ui.theme.ObjectDetectionTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

@Composable
fun HomePage() {
    val context = LocalContext.current
    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        context.packageName + ".provider", file
    )

    var capturedImageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
        capturedImageUri = uri
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageFile: Uri? ->
        imageUri = imageFile
    }

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

                // poster image
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // display image
                    if (capturedImageUri.path?.isNotEmpty() == true || imageUri != null) {
                        val displayUri = imageUri ?: capturedImageUri
                        Image(
                            modifier = Modifier.fillMaxWidth(),
                            contentDescription = "Poster Image",
                            painter = rememberAsyncImagePainter(displayUri)
                        )
                    } else {
                        Image(
                            modifier = Modifier.fillMaxWidth(),
                            contentDescription = "Poster Image",
                            painter = painterResource(id = R.drawable.ic_launcher_background)
                        )
                    }
                }

                // button
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Button(
                        onClick = {
                            val permissionCheckResult =
                                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)

                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch(uri)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.black))
                    ) {
                        Text(text = "Take Picture", color = Color.White)
                    }
                    Button(
                        onClick = {
                            galleryLauncher.launch("image/*") // Launch gallery
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.black))
                    ) {
                        Text(text = "Import From Gallery", color = Color.White)
                    }
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

fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH:mm:ss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )

    return image
}