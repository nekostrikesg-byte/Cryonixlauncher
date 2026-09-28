#include <jni.h>
extern "C" JNIEXPORT jstring JNICALL Java_com_cryonix_launcher_runtime_NativeMinecraftEngine_nativeGetStatus(JNIEnv* env,jobject){return env->NewStringUTF("native bridge loaded; Android LWJGL/GLFW engine package required");}
extern "C" JNIEXPORT jboolean JNICALL Java_com_cryonix_launcher_runtime_NativeMinecraftEngine_nativeIsReady(JNIEnv*,jobject){return JNI_FALSE;}
