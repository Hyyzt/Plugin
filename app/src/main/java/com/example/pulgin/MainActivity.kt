package com.example.pulgin

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.the.data.TheData.commit
import com.the.data.TheDataBean
import com.the.data.annotation.TheDataMethod
import com.the.data.annotation.TheDataParams


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        onTest(123, false, System.currentTimeMillis(), "2", TheDataBean())
    }

    fun onTest2(userId: Int, isTrue: Boolean) {
        val hashMap = HashMap<String, String?>()
        val mUserId = userId

        hashMap["userId"] = mUserId.toString()
        commit("12", 0, hashMap)
    }

    @TheDataMethod("nick")
    fun onTest(
        @TheDataParams("userId") userId: Int,
        @TheDataParams("isTrue") isTrue: Boolean,
        @TheDataParams("time") time: Long,
        @TheDataParams("age") age: String,
        bean: TheDataBean
    ) {
        val theDataBean = TheDataBean()
        theDataBean.strategy = 1
        Log.e("TAG", "onTest: ${theDataBean.toString()}")
    }
}