LOCAL_PATH := $(call my-dir)
# The CI/F-Droid build stages reviewed native libraries per ABI in ../libs.
# Only modules directly linked by libplayer are declared here; libmpv and FFmpeg
# carry their own runtime DT_NEEDED dependencies, which are packaged alongside them.
PREFIX := ../native-libs/$(TARGET_ARCH_ABI)
HEADER_PREFIX := ../native-headers/$(TARGET_ARCH_ABI)

include $(CLEAR_VARS)
LOCAL_MODULE := libavcodec
LOCAL_SRC_FILES := $(PREFIX)/$(LOCAL_MODULE).so
LOCAL_EXPORT_C_INCLUDES := $(HEADER_PREFIX)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libswscale
LOCAL_SRC_FILES := $(PREFIX)/$(LOCAL_MODULE).so
LOCAL_EXPORT_C_INCLUDES := $(HEADER_PREFIX)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libmpv
LOCAL_SRC_FILES := $(PREFIX)/$(LOCAL_MODULE).so
LOCAL_EXPORT_C_INCLUDES := $(HEADER_PREFIX)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libplayer
LOCAL_CFLAGS := -Werror
LOCAL_CPPFLAGS += -std=c++11
LOCAL_C_INCLUDES := $(HEADER_PREFIX)/include
LOCAL_SRC_FILES := \
    main.cpp \
    render.cpp \
    log.cpp \
    jni_utils.cpp \
    property.cpp \
    event.cpp \
    thumbnail.cpp \
    node.cpp
LOCAL_LDLIBS := -llog -lGLESv3 -lEGL -latomic
LOCAL_SHARED_LIBRARIES := swscale avcodec mpv

include $(BUILD_SHARED_LIBRARY)
