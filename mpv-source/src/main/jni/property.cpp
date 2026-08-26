#include <jni.h>
#include <stdlib.h>
#include <mpv/client.h>
#include "jni_utils.h"
#include "log.h"
#include "globals.h"
#include "node.h"

extern "C" {
    jni_func(jint, setOptionString, jstring option, jstring value);
    jni_func(jobject, getPropertyInt, jstring property);
    jni_func(void, setPropertyInt, jstring property, jobject value);
    jni_func(jobject, getPropertyLong, jstring property);
    jni_func(void, setPropertyLong, jstring property, jobject value);
    jni_func(jobject, getPropertyDouble, jstring property);
    jni_func(void, setPropertyDouble, jstring property, jobject value);
    jni_func(jobject, getPropertyFloat, jstring property);
    jni_func(void, setPropertyFloat, jstring property, jobject value);
    jni_func(jobject, getPropertyBoolean, jstring property);
    jni_func(void, setPropertyBoolean, jstring property, jobject value);
    jni_func(jstring, getPropertyString, jstring property);
    jni_func(void, setPropertyString, jstring property, jstring value);
    jni_func(jobject, getPropertyNode, jstring property);
    jni_func(void, setPropertyNode, jstring property, jobject value);
    jni_func(void, observeProperty, jstring property, jint format);
}

jni_func(jint, setOptionString, jstring joption, jstring jvalue) {
    if (!g_mpv) {
        die("mpv is not initialized");
        return -1;
    }
    const char *option = env->GetStringUTFChars(joption, NULL);
    const char *value = env->GetStringUTFChars(jvalue, NULL);
    int result = mpv_set_option_string(g_mpv, option, value);
    env->ReleaseStringUTFChars(joption, option);
    env->ReleaseStringUTFChars(jvalue, value);
    return result;
}

static int common_get_property(JNIEnv *env, jstring jproperty, mpv_format format, void *output) {
    if (!g_mpv) {
        die("get_property called but mpv is not initialized");
        return -1;
    }
    const char *prop = env->GetStringUTFChars(jproperty, NULL);
    int result = mpv_get_property(g_mpv, prop, format, output);
    if (result < 0)
        ALOGE("mpv_get_property(%s) format %d returned error %s", prop, format, mpv_error_string(result));
    env->ReleaseStringUTFChars(jproperty, prop);
    return result;
}

static int common_set_property(JNIEnv *env, jstring jproperty, mpv_format format, void *value) {
    if (!g_mpv) {
        die("set_property called but mpv is not initialized");
        return -1;
    }
    const char *prop = env->GetStringUTFChars(jproperty, NULL);
    int result = mpv_set_property(g_mpv, prop, format, value);
    if (result < 0)
        ALOGE("mpv_set_property(%s) format %d returned error %s", prop, format, mpv_error_string(result));
    env->ReleaseStringUTFChars(jproperty, prop);
    return result;
}

jni_func(jobject, getPropertyInt, jstring jproperty) {
    int64_t value = 0;
    if (common_get_property(env, jproperty, MPV_FORMAT_INT64, &value) < 0) return NULL;
    return env->NewObject(java_Integer, java_Integer_init, (jint)value);
}

jni_func(void, setPropertyInt, jstring jproperty, jobject jvalue) {
    int64_t value = env->CallIntMethod(jvalue, java_Integer_intValue);
    common_set_property(env, jproperty, MPV_FORMAT_INT64, &value);
}

jni_func(jobject, getPropertyLong, jstring jproperty) {
    int64_t value = 0;
    if (common_get_property(env, jproperty, MPV_FORMAT_INT64, &value) < 0) return NULL;
    return env->NewObject(java_Long, java_Long_init, (jlong)value);
}

jni_func(void, setPropertyLong, jstring jproperty, jobject jvalue) {
    int64_t value = env->CallLongMethod(jvalue, java_Long_longValue);
    common_set_property(env, jproperty, MPV_FORMAT_INT64, &value);
}

jni_func(jobject, getPropertyDouble, jstring jproperty) {
    double value = 0;
    if (common_get_property(env, jproperty, MPV_FORMAT_DOUBLE, &value) < 0) return NULL;
    return env->NewObject(java_Double, java_Double_init, (jdouble)value);
}

jni_func(void, setPropertyDouble, jstring jproperty, jobject jvalue) {
    double value = env->CallDoubleMethod(jvalue, java_Double_doubleValue);
    common_set_property(env, jproperty, MPV_FORMAT_DOUBLE, &value);
}

jni_func(jobject, getPropertyFloat, jstring jproperty) {
    double value = 0;
    if (common_get_property(env, jproperty, MPV_FORMAT_DOUBLE, &value) < 0) return NULL;
    return env->NewObject(java_Float, java_Float_init, (jfloat)value);
}

jni_func(void, setPropertyFloat, jstring jproperty, jobject jvalue) {
    double value = (double)env->CallFloatMethod(jvalue, java_Float_floatValue);
    common_set_property(env, jproperty, MPV_FORMAT_DOUBLE, &value);
}

jni_func(jobject, getPropertyBoolean, jstring jproperty) {
    int value = 0;
    if (common_get_property(env, jproperty, MPV_FORMAT_FLAG, &value) < 0) return NULL;
    return env->NewObject(java_Boolean, java_Boolean_init, (jboolean)value);
}

jni_func(void, setPropertyBoolean, jstring jproperty, jobject jvalue) {
    int value = env->CallBooleanMethod(jvalue, java_Boolean_booleanValue);
    common_set_property(env, jproperty, MPV_FORMAT_FLAG, &value);
}

jni_func(jstring, getPropertyString, jstring jproperty) {
    char *value = NULL;
    if (common_get_property(env, jproperty, MPV_FORMAT_STRING, &value) < 0) return NULL;
    jstring jvalue = value ? env->NewStringUTF(value) : NULL;
    mpv_free(value);
    return jvalue;
}

jni_func(void, setPropertyString, jstring jproperty, jstring jvalue) {
    const char *value = env->GetStringUTFChars(jvalue, NULL);
    common_set_property(env, jproperty, MPV_FORMAT_STRING, (void *)&value);
    env->ReleaseStringUTFChars(jvalue, value);
}

jni_func(jobject, getPropertyNode, jstring jproperty) {
    mpv_node value;
    value.format = MPV_FORMAT_NONE;
    value.u.int64 = 0;
    if (common_get_property(env, jproperty, MPV_FORMAT_NODE, &value) < 0) return NULL;
    jobject result = mpv_node_to_jobject(env, &value);
    mpv_free_node_contents(&value);
    return result;
}

jni_func(void, setPropertyNode, jstring jproperty, jobject jvalue) {
    mpv_node value;
    value.format = MPV_FORMAT_NONE;
    value.u.int64 = 0;
    if (jobject_to_mpv_node(env, jvalue, &value) < 0) {
        ALOGE("setPropertyNode: failed to convert MPVNode");
        return;
    }
    common_set_property(env, jproperty, MPV_FORMAT_NODE, &value);
    mpv_free_node_contents(&value);
}

jni_func(void, observeProperty, jstring jproperty, jint format) {
    if (!g_mpv) {
        die("mpv is not initialized");
        return;
    }
    const char *prop = env->GetStringUTFChars(jproperty, NULL);
    int result = mpv_observe_property(g_mpv, 0, prop, (mpv_format)format);
    if (result < 0)
        ALOGE("mpv_observe_property(%s) format %d returned error %s", prop, format, mpv_error_string(result));
    env->ReleaseStringUTFChars(jproperty, prop);
}
