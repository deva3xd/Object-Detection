package com.raved.objectdetection

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.rememberAsyncImagePainter
import com.raved.objectdetection.ui.theme.ObjectDetectionTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

@Composable
fun HomePage() {
    val context = LocalContext.current
    val file = context.createImageFile()
    val uri = if (LocalInspectionMode.current) {
        Uri.parse("file://dummy/path")
    } else {
        FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            context.packageName + ".provider", file
        )
    }

    var capturedImageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var detectedClass by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            capturedImageUri = uri
            uploadImage(context, capturedImageUri) { result ->
                detectedClass = result
            }
        }
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
        imageUri?.let {
            uploadImage(context, it) { result ->
                detectedClass = result
            }
        }
    }

    // handle alert
    var showAlert by remember { mutableStateOf(false) }
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text(text = "Confirm Exit") },
            text = { Text("Are you sure you want to exit?") },
            shape = RectangleShape,
            confirmButton = {
                Button(
                    onClick = { (context as? MainActivity)?.finish() },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(colorResource(id = R.color.red))
                ) {
                    Text("Exit")
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
                Button(
                    onClick = { showAlert = true },
                    modifier = Modifier
                        .width(100.dp)
                        .padding(5.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(colorResource(id = R.color.red))
                ) {
                    Text(text = "Back", color = Color.White)
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(300.dp),
                                contentDescription = "Poster Image",
                                painter = rememberAsyncImagePainter(displayUri)
                            )
                            detectedClass?.let {
                                Text(
                                    text = "Detected: $it",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
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
                        Text(text = "Camera", color = Color.White)
                    }
                    Button(
                        onClick = {
                            galleryLauncher.launch("image/*") // Launch gallery
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.black))
                    ) {
                        Text(text = "Gallery", color = Color.White)
                    }
                }
            }
        }
    }
}

fun uploadImage(context: Context, imageUri: Uri, onResult: (String) -> Unit) {
    val file = getFileFromUri(context, imageUri) ?: run {
        Toast.makeText(context, "Failed to resolve file", Toast.LENGTH_SHORT).show()
        return
    }

    if (!file.exists()) {
        Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
        return
    }

    val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
    val body = MultipartBody.Part.createFormData("image", file.name, requestBody)

    val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.43.168:8080/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(ApiService::class.java)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = service.uploadImage(body)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string() ?: "{}"
                    val jsonResponse = JSONObject(responseBody)
                    val detections = jsonResponse.optJSONArray("detections")
                    val detectedClass = if (detections != null && detections.length() > 0) {
                        val firstDetection = detections.getJSONObject(0)
                        firstDetection.optString("class", "Unknown")
                    } else {
                        "Unknown"
                    }
                    onResult(detectedClass)
                } else {
                    Toast.makeText(context, "Failed to upload image: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val contentResolver = context.contentResolver
        val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
        contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        tempFile
    } catch (e: Exception) {
        Log.e("getFileFromUri", "Error resolving file: ${e.message}")
        null
    }
}

interface ApiService {
    @Multipart
    @POST("detect/")
    suspend fun uploadImage(@Part image: MultipartBody.Part): retrofit2.Response<ResponseBody>
}

@Preview
@Composable
fun HomePagePreview() {
    HomePage()
}