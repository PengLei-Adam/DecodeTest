/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class edu_tfnrc_rtp_codec_h264_NativeH264Decoder */

#ifndef _Included_edu_tfnrc_rtp_codec_h264_NativeH264Decoder
#define _Included_edu_tfnrc_rtp_codec_h264_NativeH264Decoder
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     edu_tfnrc_rtp_codec_h264_NativeH264Decoder
 * Method:    InitDecoder
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_InitDecoder
  (JNIEnv *, jclass);

/*
 * Class:     edu_tfnrc_rtp_codec_h264_NativeH264Decoder
 * Method:    DeinitDecoder
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_DeinitDecoder
  (JNIEnv *, jclass);

/*
 * Class:     edu_tfnrc_rtp_codec_h264_NativeH264Decoder
 * Method:    DecodeAndConvert
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_DecodeAndConvert
  (JNIEnv *, jclass, jstring, jclass);

/*
 * Class:     edu_tfnrc_rtp_codec_h264_NativeH264Decoder
 * Method:    SetDecoderStatus
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_SetDecoderState
  (JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif
