LOCAL_PATH:= $(call my-dir)
# The CI/F-Droid build copies reviewed native prefix outputs into this ABI-local directory.
# Keeping LOCAL_SRC_FILES relative avoids NDK prebuilt-library path issues with symlinked prefixes.
PREFIX = ../libs/$(TARGET_ARCH_ABI)
HEADER_PREFIX = ../native-headers/$(TARGET_ARCH_ABI)

include $(CLEAR_VARS)
LOCAL_MODULE := libswresample
LOCAL_SRC_FILES := $(PREFIX)/$(LOCAL_MODULE).so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libavutil
LOCAL_SRC_FILES := $(PREFIX)/$(LOCAL_MODULE).so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libavcodec
LOCAL_SRC_FILES := $(PREFIX)/$(LOCAL_MODULE).so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libavformat
LOCAL_SRC_FILES := $(PREFIX)/$(LOCAL_MODULE).so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libswscale
LOCAL_SRC_FILES := $(PREFIX)/$(LOCAL_MODULE).so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libavfilter
LOCAL_SRC_FILES := $(PREFIX)/$(LOCAL_MODULE).so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libavdevice
LOCAL_SRC_FILES := $(PREFIX)/$(LOCAL_MODULE).so
LOCAL_EXPORT_C_INCLUDES := $(HEADER_PREFIX)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libmpv
LOCAL_SRC_FILES := $(PREFIX)/libmpv.so
LOCAL_EXPORT_C_INCLUDES := $(HEADER_PREFIX)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := libplayer
LOCAL_CFLAGS    := -Werror
LOCAL_CPPFLAGS  += -std=c++11
LOCAL_SRC_FILES := \
	main.cpp \
	render.cpp \
	log.cpp \
	jni_utils.cpp \
	property.cpp \
	event.cpp \
	thumbnail.cpp \
	node.cpp
LOCAL_LDLIBS    := -llog -lGLESv3 -lEGL -latomic
LOCAL_SHARED_LIBRARIES := swscale avcodec mpv

include $(BUILD_SHARED_LIBRARY)
