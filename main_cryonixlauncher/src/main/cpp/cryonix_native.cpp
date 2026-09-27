#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_cryonix_launcher_core_NativeBridge_status(
        JNIEnv* env,
        jobject /* thiz */) {
    return env->NewStringUTF("C/C++ native layer active");
}
