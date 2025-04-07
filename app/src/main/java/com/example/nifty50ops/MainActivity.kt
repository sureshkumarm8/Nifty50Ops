@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.nifty50ops

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.sharp.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.nifty50ops.network.ApiService
import com.example.nifty50ops.service.DataFetchService
import com.example.nifty50ops.ui.theme.Nifty50OpsTheme
import com.example.nifty50ops.utils.copyFromDownloadsToInternal
import com.example.nifty50ops.utils.readJwtToken
import com.example.nifty50ops.utils.readSecurityIdToSymbolMap
import com.example.nifty50ops.view.AboutScreen
import com.example.nifty50ops.view.MainScreen
import com.example.nifty50ops.view.OptionsScreen
import com.example.nifty50ops.view.SettingsScreen
import com.example.nifty50ops.view.StockScreen
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Nifty50OpsTheme {
                MainWithDrawer(applicationContext)
            }
        }

        createOptionDataFolderIfNeeded(this)

        val internalFile = File(filesDir, "option_data/NiftyScrips.txt")

        if (internalFile.exists()) {
            Log.d("Init", "File exists. Proceeding.")
        } else {
            Log.d("Init", "File doesn't exist. Asking user to pick.")
            launchFilePickerForNiftyFile()
        }
        readJwtToken(applicationContext)
        waitForTxtFileAndStartService()
    }

    @Composable
    fun AppDrawer(onItemSelected: (String) -> Unit, selectedItem: String = "main") {
        val menuItems = listOf(
            DrawerItem("main", "🏠 Home"),
            DrawerItem("stocks", "📈 Stocks"),
            DrawerItem("options", "📊 Options"),
            DrawerItem("settings", "⚙️ Settings"),
            DrawerItem("about", "ℹ️ About")
        )

        ModalDrawerSheet(
            modifier = Modifier.fillMaxHeight(),
            drawerContainerColor = Color(0xFF2196F3) // Sky blue background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2196F3)) // Apply sky blue to entire drawer content
            ) {
                // Header with image background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.fin_stockmarket),
//                        contentDescription = "Drawer Header Background",
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier.fillMaxSize()
//                    )

                    Text(
                        text = "\uD83D\uDCCA  Nifty50 Ops",
                        color = Color.Black,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 20.dp)
                    )

                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 1.dp)
                )
                // Menu items
                menuItems.forEach { item ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        },
                        selected = false,
                        onClick = { onItemSelected(item.key) },
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Divider(color = Color(0xFF81D4FA), thickness = 1.dp)

                Text(
                    text = "Made with 💙 in India",
                    color = Color(0xFF0277BD),
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
        }
    }

    data class DrawerItem(val key: String, val label: String)

    @Composable
    fun MainWithDrawer(context: Context) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()

        var selectedItem by remember { mutableStateOf("main") }
        var title by remember { mutableStateOf("Nifty 50 Ops") }

        val updateTitle: (String) -> String = {
            when (it) {
                "main" -> "🏠 Nifty 50 Ops"
                "stocks" -> "📈 Nifty 50 Stock Updates"
                "options" -> "📊 Weekly Nifty 50 Options"
                "settings" -> "⚙️ Settings"
                "about" -> "ℹ️ About"
                else -> "📊 Nifty 50 OPS"
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawer(
                    selectedItem = selectedItem,
                    onItemSelected = { item ->
                        selectedItem = item
                        title = updateTitle(item)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch { drawerState.open() }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF2196F3) // AppBar blue
                        )
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    when (selectedItem) {
                        "main" -> MainScreen(context)
                        "stocks" -> StockScreen(context)
                        "options" -> OptionsScreen(context)
                        "settings" -> SettingsScreen(context)
                        "about" -> AboutScreen(context)
                    }
                }
            }
        }
    }

    private fun requestForegroundServicePermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 102)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 14+ (API 34)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC),
                    101
                )
            } else {
                startMyService()
            }
        } else {
            startMyService()
        }
    }

    private fun startMyService() {
        val intent = Intent(this, DataFetchService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startMyService()
        }

        if (requestCode == 102 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Notification permission granted
        }
    }

    fun createOptionDataFolderIfNeeded(context: Context) {
        val folder = File(context.filesDir, "option_data")
        if (!folder.exists()) {
            folder.mkdirs()
            Log.d("AppInit", "Created folder: ${folder.absolutePath}")
        }
    }

    private fun waitForTxtFileAndStartService() {
        val handler = Handler(Looper.getMainLooper())
        val file = File(filesDir, "option_data/NiftyScrips.txt")

        val checkRunnable = object : Runnable {
            override fun run() {
                if (file.exists()) {
                    Log.d("InitFlow", "NiftyScrips.txt found. Proceeding...")

                    readJwtToken(this@MainActivity)
                    val map = readSecurityIdToSymbolMap(this@MainActivity)

                    requestForegroundServicePermissionIfNeeded() // Service starts after permission

                } else {
                    Log.d("InitFlow", "Waiting for NiftyScrips.txt...")
                    handler.postDelayed(this, 1000) // Check again in 1 second
                }
            }
        }

        handler.post(checkRunnable)
    }

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            copyFromUriToInternal(this, uri)
        } else {
            Toast.makeText(this, "File not selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchFilePickerForNiftyFile() {
        val mimeTypes = arrayOf("text/plain")
        filePickerLauncher.launch(mimeTypes)
    }

    fun copyFromUriToInternal(context: Context, uri: Uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val dir = File(context.filesDir, "option_data")
            if (!dir.exists()) dir.mkdirs()
            val internalFile = File(dir, "NiftyScrips.txt")

            inputStream?.use { input ->
                FileOutputStream(internalFile).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d("SAF", "File copied successfully to: ${internalFile.absolutePath}")
            Toast.makeText(context, "NiftyScrips.txt imported", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("SAF", "Failed to copy file: ${e.message}", e)
            Toast.makeText(context, "Failed to import NiftyScrips.txt", Toast.LENGTH_SHORT).show()
        }
    }


}
