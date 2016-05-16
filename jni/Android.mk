# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE :=avcodec-57-prebuilt
LOCAL_SRC_FILES :=./prebuilt/libavcodec-57.so
include $(PREBUILT_SHARED_LIBRARY)

#include $(CLEAR_VARS)
#LOCAL_MODULE:=avdevice-57-prebuilt
#LOCAL_SRC_FILES:=./prebuilt/libavdevice-57.so
#include $(PREBUILT_SHARED_LIBRARY)

#include $(CLEAR_VARS)
#LOCAL_MODULE :=avfilter-6-prebuilt
#LOCAL_SRC_FILES :=./prebuilt/libavfilter-6.so
#include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE :=avformat-57-prebuilt
LOCAL_SRC_FILES :=./prebuilt/libavformat-57.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE :=  avutil-55-prebuilt
LOCAL_SRC_FILES :=./prebuilt/libavutil-55.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE :=  avswresample-2-prebuilt
LOCAL_SRC_FILES :=./prebuilt/libswresample-2.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE :=  swscale-4-prebuilt
LOCAL_SRC_FILES :=./prebuilt/libswscale-4.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE :=  postproc-54-prebuilt
LOCAL_SRC_FILES :=./prebuilt/libpostproc-54.so
include $(PREBUILT_SHARED_LIBRARY)

TARGET_PRELINK_MODULES := false

include $(CLEAR_VARS)

LOCAL_MODULE    := decoder
LOCAL_SRC_FILES := libdecoder.c record.c

LOCAL_LDLIBS := -llog -ljnigraphics -lz -landroid -pthread
LOCAL_SHARED_LIBRARIES:=avcodec-57-prebuilt  avformat-57-prebuilt avutil-55-prebuilt avswresample-2-prebuilt swscale-4-prebuilt postproc-54-prebuilt
include $(BUILD_SHARED_LIBRARY)
