#include <jni.h>

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("graphsonic");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("graphsonic")
//      }
//    }


extern "C"
JNIEXPORT jstring JNICALL
Java_com_anuj_graphsonic_engine_NativeBridge_getEngineMessage(JNIEnv* env,jobject /* thiz */) {
    return env->NewStringUTF(
            "GraphSonic C++ Engine is working"
    );
}