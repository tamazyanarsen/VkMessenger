package com.example.vkmessenger

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.vk.api.sdk.VKApiConfig
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import kotlinx.android.synthetic.main.activity_dialog.*
import org.jetbrains.anko.*
import io.reactivex.rxkotlin.toObservable
import kotlinx.android.synthetic.main.activity_dialog.dialogLayout
import kotlinx.android.synthetic.main.activity_dialog.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick

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

    @SuppressLint("CheckResult", "RtlHardcoded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)

        recycleViewMessages.layoutManager = LinearLayoutManager(this)

        val list = arrayListOf("Alpha", "Beta", "Gamma", "Delta", "Epsilon")

//        list.toObservable() // extension function for Iterables
//            .filter { it.length >= 5 }
//            .subscribeBy(  // named arguments for lambda Subscribers
//                onNext = { println(it) },
//                onError =  { it.printStackTrace() },
//                onComplete = { println("Done!") }
//            )

        list.toObservable().subscribeBy { s ->
            log("get next: $s")
        }

        list[0] = "new element"

//        list.add("new element")

        val userId = intent.getIntExtra("userId", 100)
        accessToken = intent.getStringExtra("accessToken")!!

//        val parentWidth = messagesLayout.width
        val parentHeight = this@Dialog.window.decorView.height

        callMethod("messages.getHistory", VKApiResponseParser { res ->
            run {
                log("get response: $res")
                val answer = Klaxon().parseJsonObject(res.reader())
                val response = answer["response"] as JsonObject
                val items = response["items"] as JsonArray<JsonObject>
                items.sortBy { jsonObject -> jsonObject["date"] as Int }
//                val scroll = ScrollView(this)
//                val messagesLayout = LinearLayout(scroll.context)
//                messagesLayout.orientation = LinearLayout.VERTICAL
//                val formSend = LinearLayout(this)
//                formSend.orientation = LinearLayout.HORIZONTAL
//                runOnUiThread {
                    //                    verticalLayout {
//                        linearLayout {
//                            scrollView {
//                                messagesLayout = verticalLayout()
//                            }
//                        }
//                        linearLayout{
//                            button("sdfsdfsdf")
//                            button("sdfsdfsdf")
//                        }
//                    }

//                    dialogLayout.addView(scroll)
//                    dialogLayout.addView(formSend)
//                    scroll.addView(messagesLayout)
//                }
                for (msg in items) {
                    val msgText = msg["text"] as String
                    runOnUiThread {
//                        messagesLayout.run {
//                            linearLayout {
//                                val t = textView(msgText)
//                                t.setPadding(parentWidth / 100, 0, parentWidth / 2, 0)
//                                if (msg["from_id"] as Int != userId) {
//                                    t.setPadding(parentWidth / 2, 0, parentWidth / 98, 0)
//                                    t.gravity = Gravity.RIGHT
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                                        t.textAlignment = TextView.TEXT_ALIGNMENT_GRAVITY
//                                    }
//                                }
//                            }
//                            dividerPadding = 100
//                        }
                    }
                }
//                runOnUiThread {
//                    sendMessageButton.onClick {
//                        toast("отправляем сообщение: ${sendMessageEditText.text}")
//                    }
//                }
            }
        }, mapOf(Pair("user_id", userId.toString())))
    }
}
