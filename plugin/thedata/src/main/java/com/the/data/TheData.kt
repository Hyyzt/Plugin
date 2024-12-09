package com.the.data

import android.util.Log

object TheData {

    fun commit(event: String?, strategy: Int?, map: Map<String, String?>?) {
        android.util.Log.e("TAG", "commit: ${event},$strategy")
        map?.forEach { s, any ->
            android.util.Log.e("TAG", "commit:$s , $any ")
        }
    }

    fun commit(bean: TheDataBean?) {
        if (bean == null) {
            return
        }
        Log.e("TAG", "commitV2: ${bean.toString()}")
    }
}