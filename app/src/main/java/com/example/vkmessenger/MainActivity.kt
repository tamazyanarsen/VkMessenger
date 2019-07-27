package com.example.vkmessenger

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.ScrollView
import com.beust.klaxon.*
import com.vk.api.sdk.*
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.InputStream
import java.net.URL

class MainActivity : AppCompatActivity() {

    var accessToken: VKAccessToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        mainGridLayout.columnCount = 1
//        setContentView(mainGridLayout)
        VK.login(this, arrayListOf(VKScope.WALL, VKScope.FRIENDS, VKScope.PHOTOS, VKScope.MESSAGES))
    }

    fun callMethod(
        methodName: String,
        callback: VKApiResponseParser<Any>? = null,
        params: Map<String, String>? = null
    ) {
        val config = VKApiConfig(
            this@MainActivity,
            7047198,
            version = VKApiConfig.DEFAULT_API_VERSION,
            accessToken = lazyOf(accessToken!!.accessToken),
            validationHandler = null
        )
        val manager = VKApiManager(config)
        val createMethod = VKMethodCall.Builder().method(methodName)
        params?.forEach { entry -> createMethod.args(entry.key, entry.value) }
        val call = createMethod.version(VKApiConfig.DEFAULT_API_VERSION).build()
        doAsync {
            manager.execute(
                call,
                callback ?: VKApiResponseParser { response -> log("get response: $response") })
        }
    }

    private fun log(msg: Any) {
        Log.d("VKMSG", msg.toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                // User passed authorization
                accessToken = token
                callMethod("users.get")
                callMethod("messages.getConversations", VKApiResponseParser { res ->
                    run {
                        log(res)
                        val result = Klaxon().parseJsonObject(res.reader())
                        val items = (result["response"] as JsonObject)["items"] as JsonArray<JsonObject>
                        log(items)
                        val scroll = ScrollView(mainLayout.context)
                        val listDialogsLayout = LinearLayout(scroll.context)
                        listDialogsLayout.orientation = LinearLayout.VERTICAL
                        runOnUiThread {
                            mainLayout.addView(scroll)
                            scroll.addView(listDialogsLayout)
                        }
                        log("items size: ${items.size}")
                        items.sortByDescending { jsonObject -> (jsonObject["last_message"] as JsonObject)["date"] as Int }
                        for (item in items) {
                            val dialog = item["conversation"] as JsonObject
                            val lastMessage = item["last_message"] as JsonObject
                            val lastMsgText = lastMessage["text"] as String
                            val fromId = (dialog["peer"] as JsonObject)["id"] as Int
                            val avatarSize = "photo_200"
                            callMethod("users.get", VKApiResponseParser { userInfo ->
                                run {
                                    log("try get user info: $fromId")
                                    val answer = Klaxon().parseJsonObject(userInfo.reader())
                                    val usersArray = answer["response"] as JsonArray<JsonObject>
                                    log(usersArray)
                                    val user = usersArray[0]
                                    log(user)
                                    val firstName = user["first_name"] as String
                                    val lastName = user["last_name"] as String
                                    var d: Drawable? = null
                                    if (user.containsKey(avatarSize)) {
                                        val photo50URL = user[avatarSize] as String
                                        val inputStream = URL(photo50URL).content as InputStream
                                        d = Drawable.createFromStream(inputStream, firstName)
                                    }
                                    runOnUiThread {
                                        listDialogsLayout.run {
                                            linearLayout {
                                                if (d != null) imageView(d)
                                                verticalLayout {
                                                    textView("$firstName $lastName").textColor = Color.DKGRAY
                                                    textView(lastMsgText)
                                                }
                                                this.onClick { view ->
                                                    run {
                                                        toast("click $firstName")
                                                        intent = Intent(this@MainActivity, Dialog().javaClass)
                                                        intent.putExtra("userId", fromId)
                                                        intent.putExtra("accessToken", accessToken!!.accessToken)
                                                        startActivity(intent)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }, mapOf(Pair("user_ids", fromId.toString()), Pair("fields", avatarSize)))
                        }
                        val horizontalLayout = LinearLayout(this@MainActivity)
                        horizontalLayout.orientation = LinearLayout.HORIZONTAL
                    }
                }) // , mapOf(Pair("count", "40"))
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
