package com.the.data

public class TheDataBean {
    var eventCode: String = ""
    var strategy: Int = 0
    var params: HashMap<String, Any?>? = null

    override fun toString(): String {
        return "TheDataBean(eventCode=$eventCode, strategy=$strategy, params=$params)"
    }


}