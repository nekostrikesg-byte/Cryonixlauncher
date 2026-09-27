#include <jni.h>

extern "C" const char* cryonix_native_status();

extern "C"
JNIEXPORT jstring JNICALL
Java_com_cryonix_launcher_core_NativeBridge_status(
        JNIEnv* env,
        jobject /* thiz */) {
    return env->NewStringUTF(cryonix_native_status());
}
