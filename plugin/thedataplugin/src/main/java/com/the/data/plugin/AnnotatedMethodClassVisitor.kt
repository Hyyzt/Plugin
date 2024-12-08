package com.the.data.plugin

import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter

class AnnotatedMethodClassVisitor(api: Int, classVisitor: ClassVisitor) :
    ClassVisitor(api, classVisitor) {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        return AnnotatedMethodVisitor(api, mv, access, name, descriptor)
    }

    private class AnnotatedMethodVisitor(
        api: Int,
        methodVisitor: MethodVisitor,
        access: Int,
        name: String?,
        val descriptor: String?
    ) : AdviceAdapter(api, methodVisitor, access, name, descriptor) {

        private var shouldPostEvent = false
        private var eventCode: String = ""
        private var eventStrategy: Int = 0
        private val eventParamsMap = mutableMapOf<Int, Any?>() // 存储参数索引与对应注解值

        private val parameterTypes = mutableListOf<Type>() // 存储方法参数的真实类型

        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
            if (descriptor == "Lcom/the/data/annotation/TheDataMethod;") {
                shouldPostEvent = true
                return object : AnnotationVisitor(api) {
                    override fun visit(name: String?, value: Any?) {
                        if (name == "event") {
                            eventCode = (value as? String) ?: ""
                        }
                        if (name == "strategy") {
                            eventStrategy = (value as? Int) ?: 0
                        }
                    }
                }
            } else {
                return super.visitAnnotation(descriptor, visible)
            }

        }

        override fun visitParameterAnnotation(
            parameter: Int,
            descriptor: String?,
            visible: Boolean
        ): AnnotationVisitor? {
            if (descriptor == "Lcom/the/data/annotation/TheDataParams;") {
                return object : AnnotationVisitor(api) {
                    override fun visit(name: String?, value: Any?) {
                        if (name == "value") {
                            eventParamsMap[parameter] = value
                        }
                    }
                }
            }
            return super.visitParameterAnnotation(parameter, descriptor, visible)
        }

        override fun visitLocalVariable(
            name: String?,
            descriptor: String?,
            signature: String?,
            start: Label?,
            end: Label?,
            index: Int
        ) {
            // 收集参数类型，跳过局部变量（index == 0 为 this 引用）
            if (index > 0 && name != null && descriptor != null) {
                parameterTypes.add(Type.getType(descriptor))
            }
            super.visitLocalVariable(name, descriptor, signature, start, end, index)
        }

        override fun onMethodEnter() {
            if (shouldPostEvent && eventCode.isNotEmpty()) {
                // 1. 创建 TheDataBean 对象
                mv.visitTypeInsn(NEW, "com/the/data/TheDataBean") // 创建 TheDataBean 对象
                mv.visitInsn(DUP) // 复制栈顶的对象引用，确保它在后续操作中有效
                mv.visitMethodInsn(
                    INVOKESPECIAL,
                    "com/the/data/TheDataBean",
                    "<init>",
                    "()V",
                    false
                ) // 调用构造函数

                // 2. 设置 eventCode 属性
                mv.visitInsn(DUP) // 保持 TheDataBean 对象在栈上
                mv.visitLdcInsn(eventCode) // 加载 eventCode
                mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "com/the/data/TheDataBean",
                    "setEventCode",
                    "(Ljava/lang/String;)V",
                    false
                ) // 设置 eventCode 属性

                // 3. 设置 strategy 属性
                mv.visitInsn(DUP) // 保持 TheDataBean 对象在栈上
                mv.visitLdcInsn(eventStrategy) // 加载 strategy
                mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "com/the/data/TheDataBean",
                    "setStrategy",
                    "(I)V",
                    false
                ) // 设置 strategy 属性

                // 4. 创建 HashMap<String, Any?> 对象
                mv.visitTypeInsn(NEW, "java/util/HashMap") // 创建 HashMap
                mv.visitInsn(DUP) // 保持 HashMap 在栈上
                mv.visitMethodInsn(
                    INVOKESPECIAL,
                    "java/util/HashMap",
                    "<init>",
                    "()V",
                    false
                ) // 调用 HashMap 构造函数

                // 遍历所有 @TheDataParams 参数，加入 Map
                eventParamsMap.forEach { (index, key) ->
                    mv.visitInsn(DUP) // 保持 Map 在栈上
                    mv.visitLdcInsn(key) // 加载 Map 的 key
                    loadArg(index) // 加载方法参数值
                    val paramType = Type.getArgumentTypes(descriptor)[index]

                    if (paramType.sort <= Type.DOUBLE) {
                        // 基本类型，装箱
                        box(paramType)
                    } else if (paramType.sort == Type.OBJECT
                        && paramType.descriptor == "Ljava/lang/String;") {
                        // 如果是 String 类型，直接使用
                        mv.visitTypeInsn(CHECKCAST, "java/lang/String")
                    } else {
                        // 如果参数类型不是基本类型或 String，抛出异常
                        throw Exception("TheDataParams can be described basic type or String type")
                    }

                    mv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        "java/util/HashMap",
                        "put",
                        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                        false
                    )
                    mv.visitInsn(POP) // 弹出 put 方法的返回值
                }

                // 5. 设置 params 属性
                // 当前栈：HashMap 在栈顶，TheDataBean 在栈底
                mv.visitInsn(DUP2); // 将栈中的 HashMap 插入到 TheDataBean 下方
                mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "com/the/data/TheDataBean",
                    "setParams",
                    "(Ljava/util/HashMap;)V",
                    false
                );
                mv.visitInsn(POP)
// 6. 调用 commitV2 方法
                mv.visitFieldInsn(
                    GETSTATIC,
                    "com/the/data/TheData",
                    "INSTANCE",
                    "Lcom/the/data/TheData;"
                ) // 获取 TheData 实例
                mv.visitInsn(SWAP) // 确保 TheDataBean 和 INSTANCE 顺序正确
                mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "com/the/data/TheData",
                    "commitV2",
                    "(Lcom/the/data/TheDataBean;)V",
                    false
                ) // 调用 commitV2 方法，传入 TheDataBean 对象
            }
        }

        override fun onMethodExit(opcode: Int) {
        }
    }
}
