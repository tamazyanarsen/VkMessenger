package com.example.vkmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.GridLayout
import android.widget.LinearLayout
import com.vk.api.sdk.*
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync

class MainActivity : AppCompatActivity() {

    var accessToken: VKAccessToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        mainGridLayout.columnCount = 1
//        setContentView(mainGridLayout)
        VK.login(this, arrayListOf(VKScope.WALL, VKScope.FRIENDS, VKScope.PHOTOS, VKScope.MESSAGES))
    }

    fun callMethod(methodName: String, callback: VKApiResponseParser<Any>? = null) {
        val config = VKApiConfig(
            this@MainActivity,
            7047198,
            version = VKApiConfig.DEFAULT_API_VERSION,
            accessToken = lazyOf(accessToken!!.accessToken),
            validationHandler = null
        )
        val manager = VKApiManager(config)
        val call = VKMethodCall.Builder().method(methodName).version(VKApiConfig.DEFAULT_API_VERSION).build()
        doAsync {
            manager.execute(
                call,
                callback ?: VKApiResponseParser { response -> println("get response: $response") })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                // User passed authorization
                accessToken = token
                callMethod("users.get")
                callMethod("messages.getConversations", VKApiResponseParser { res ->
                    {
                        println(res)
                        val horizontalLayout = LinearLayout(this@MainActivity)
                        horizontalLayout.orientation = LinearLayout.HORIZONTAL
                    }
                })
            }

            override fun onLoginFailed(errorCode: Int) {
                // User didn't pass authorization
            }
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
