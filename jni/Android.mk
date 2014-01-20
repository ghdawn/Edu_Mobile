LOCAL_PATH := $(call my-dir)
APP_ABI := armeabi-v7a
LOCAL_CPP_EXTENSION := .cpp
include $(CLEAR_VARS)

LOCAL_MODULE    := NavData
LOCAL_SRC_FILES := ppp.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
