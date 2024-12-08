package com.the.data.annotation

@Target(AnnotationTarget.VALUE_PARAMETER) // 只能用于方法参数
@Retention(AnnotationRetention.BINARY) // 编译期有效
annotation class TheDataParams(val value: String)
