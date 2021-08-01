package com.benjaminwan.composelocation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.afollestad.assent.isAllGranted
import com.benjaminwan.composelocation.ui.theme.ComposeLocationTheme
import com.benjaminwan.composelocation.utils.showToast

class MainActivity : AppCompatActivity() {
    private fun getPermissions() {
        val permissions = arrayOf(
            Permission.ACCESS_FINE_LOCATION,
            Permission.ACCESS_COARSE_LOCATION
        )
        if (!isAllGranted(*permissions)) {
            askForPermissions(*permissions) { result ->
                val permissionGranted: Boolean = result.isAllGranted(*permissions)
                if (!permissionGranted) {
                    showToast("权限获取错误！")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPermissions()
        setContent {
            ComposeLocationTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeLocationTheme {
        Greeting("Android")
    }
}