package com.example.helloandroid.jni

object NativeLoader {
    init {
        System.loadLibrary("native-lib")
    }

    external fun getJniString(): String
}