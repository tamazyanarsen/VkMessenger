package com.example.vkmessenger

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.vk.api.sdk.VKApiConfig
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import kotlinx.android.synthetic.main.activity_dialog.*
import org.jetbrains.anko.*

class Dialog : AppCompatActivity() {

    var accessToken: String = ""

    private fun callMethod(
        methodName: String,
        callback: VKApiResponseParser<Any>? = null,
        params: Map<String, String>? = null
    ) {
        val config = VKApiConfig(
            this,
            7047198,
            version = VKApiConfig.DEFAULT_API_VERSION,
            accessToken = lazyOf(accessToken),
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
        Log.d("DIALOG VKMSG", msg.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)

        val userId = intent.getIntExtra("userId", 100)
        accessToken = intent.getStringExtra("accessToken")!!
        callMethod("messages.getHistory", VKApiResponseParser { res ->
            run {
                log("get response: $res")
                val answer = Klaxon().parseJsonObject(res.reader())
                val response = answer["response"] as JsonObject
                val items = response["items"] as JsonArray<JsonObject>
                items.sortBy { jsonObject -> jsonObject["date"] as Int }
                val scroll = ScrollView(this)
                val messagesLayout = LinearLayout(scroll.context)
                messagesLayout.orientation = LinearLayout.VERTICAL
                runOnUiThread {
                    dialogLayout.addView(scroll)
                    scroll.addView(messagesLayout)
                }
                for (msg in items) {
                    val msgText = msg["text"] as String
                    runOnUiThread {
                        messagesLayout.run {
                            linearLayout {
                                textView(msgText)
                            }
                            dividerPadding = 10
                        }
                    }
                }
            }
        }, mapOf(Pair("user_id", userId.toString())))
    }
}
