package com.example.vkmessenger

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.vk.api.sdk.VKApiConfig
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import kotlinx.android.synthetic.main.activity_dialog.*
import kotlinx.android.synthetic.main.msglayout.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick

class Dialog : AppCompatActivity() {

    var accessToken: String = ""
    private lateinit var adapter: MsgAdapter
    private var userId: Int = 100

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
        Log.d("VKMSG DIALOG", msg.toString())
    }

    @SuppressLint("CheckResult", "RtlHardcoded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)

        val allMsg = arrayListOf<String>()

        recycleViewMessages.layoutManager = LinearLayoutManager(this)
        adapter = MsgAdapter(allMsg, this)
        recycleViewMessages.adapter = adapter

        userId = intent.getIntExtra("userId", 100)
        accessToken = intent.getStringExtra("accessToken")!!

        callMethod("messages.getHistory", VKApiResponseParser { res ->
            run {
                log("get response: $res")
                val answer = Klaxon().parseJsonObject(res.reader())
                val response = answer["response"] as JsonObject
                val items = response["items"] as JsonArray<JsonObject>
                items.sortBy { jsonObject -> jsonObject["date"] as Int }
                for (msg in items) {
                    log("curr msg: $msg")
                    val msgText = msg["text"] as String
                    allMsg.add(msgText)
                    val copyMsg = allMsg.subList(0, allMsg.size)
//                    allMsg.clear()
                    runOnUiThread {
                        adapter.notifyDataSetChanged()
//                        adapter.notifyItemInserted(allMsg.size - 1)
                    }
                }
            }
        }, mapOf(Pair("user_id", userId.toString())))

        sendMessageButton.onClick {
            val msgText = msgTextView.text
            callMethod("messages.send", VKApiResponseParser { res -> log(res) },
                mapOf(Pair("user_id", userId.toString()), Pair("message", msgText.toString())))
        }
    }
}
