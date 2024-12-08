package com.the.data.annotation


//默认策略
const val STRATEGY_DEFAULT = 0
//立即上传
const val STRATEGY_NOW = 1

@Target(AnnotationTarget.FUNCTION) // 只能用于方法参数
@Retention(AnnotationRetention.BINARY) // 编译期有效
annotation class TheDataMethod(
    val event: String, val strategy: Int = STRATEGY_DEFAULT
)
