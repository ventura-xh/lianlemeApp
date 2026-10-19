#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

jstring
Java_com_example_helloandroid_MainActivity_getJniString(JNIEnv *env, jobject /* this */) {
    // 构造一个 std::string
    std::string message = "Hello from C++! 这是 JNI 返回的字符串。";

    // 转成 jstring 返回给 Kotlin
    return env->NewStringUTF(message.c_str());
}