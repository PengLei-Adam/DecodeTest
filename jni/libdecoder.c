#include<stdio.h>
#include<jni.h>
#include<stdlib.h>
#include<pthread.h>
#include "edu_tfnrc_rtp_codec_h264_NativeH264Decoder.h"
#include "libavcodec/avcodec.h"
#include "libavutil/avutil.h"
#include "libavformat/avformat.h"
#include "libavutil/frame.h"
#include "libavutil/error.h"
#include "libswscale/swscale.h"
#include<android/log.h>
#include<string.h>
#include<malloc.h>

#define LOG_TAG "libdecoder"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#define STATE_OFF		0
#define STATE_READY		1
#define STATE_PLAY		2
#define STATE_PAUSE		3
#define STATE_CLOSE		4

//Global variables

AVCodec * codec;			/*CODEC*/
AVCodecContext * c;			/*CODEC Context*/
AVDictionary * opts;		/*Dictionary*/
//AVPacket * pkt;			/*AVPacket*/
int cnt;					/*解码计数*/
int  got_picture;			/*是否解码一帧图像*/
AVFrame * picture, * pictureARGB;		/*解码后的图像帧空间*/
FILE * out_file;						/*输出文件*/
AVFormatContext *pFormatCtx;			/*流文件*/
struct Swscontext * img_convert_ctx;	/*转换格式结构*/
char * url;					/*传入的视频流地址*/
int state;					/*设置解码器状态*/

//pthread variables
static pthread_mutex_t statelock = PTHREAD_MUTEX_INITIALIZER;
pthread_t thread;

/*设置解码器状态函数*/
int setState(int stateId){
	int error;
	if(error = pthread_mutex_lock(&statelock)){
		LOGD("failed to lock state: Error %d", error);
	}
	if(stateId < 0 || stateId > STATE_CLOSE)
      		return -1;
	state = stateId;
	if(error = pthread_mutex_unlock(&statelock)){
		LOGD("failed to unlock state: Error %d", error);
	}
	return state;
}

//C语言的工具方法：将Java端传过来的String类型转换为char数组类型
char* Jstring2CStr(JNIEnv* env, jstring jstr){
		char* rtn = NULL;
		jclass clsstring = (*env)->FindClass(env, "java/lang/String");
		jstring strencode = (*env)->NewStringUTF(env, "UTF-8");
		jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes", "(Ljava/lang/String;)[B");
		jbyteArray barr = (jbyteArray)(*env)->CallObjectMethod(env, jstr, mid, strencode);	//calling String .getByte("UTF-8")
		jsize alen = (*env)->GetArrayLength(env, barr);
		jbyte* ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
		if(alen > 0){
			rtn = (char*) malloc(alen + 1); //"\0"
			memcpy(rtn, ba, alen);
			rtn[alen] = 0;
		}
		(*env)->ReleaseByteArrayElements(env, barr, ba, 0);
		return rtn;
	}

JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_InitDecoder
  (JNIEnv * env, jclass clazz){

	if(state == STATE_OFF){
//  out_file = fopen("/sdcard/Pictures/output.yuv", "wb");
  /*CODEC的初始化，初始化一些常量表,在avcodec_register_all()中进行
	avcodec_init();*/

	/*av初始化*/
	av_register_all();
	/*注册CODEC*/
	avcodec_register_all();
	/*初始化网络*/
	avformat_network_init();

	setState(STATE_READY);
	}

  LOGD("init finished");
  return 0;
}

  JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_DeinitDecoder
    (JNIEnv * env, jclass clazz){
	if(state != STATE_PLAY) return state;
	setState(STATE_CLOSE);
//    if(out_file) fclose(out_file);
	int error;

	if(error = pthread_join(thread, NULL)){
		LOGD("failed to join decode thread: Error %d", error);
	}
    /*关闭CODEC，释放资源,调用decode_end本地函数*/
	if(c) {
		avcodec_close(c);
		c = NULL;
	}
	/*释放AVFrame空间*/
	if(picture) {
		av_frame_free(&picture);
		picture = NULL;
	}
	if(pictureARGB) {
    		av_frame_free(&pictureARGB);
    		pictureARGB = NULL;
    	}
	// Close the stream file
    avformat_close_input(&pFormatCtx);
    pFormatCtx = NULL;
	cnt = 0;
	sws_freeContext(img_convert_ctx);
    img_convert_ctx = NULL;
    setState(STATE_READY);
	LOGD("deinit");
	return 0;

}

  JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_DecodeAndConvert
    (JNIEnv *env, jclass clazz, jstring jurl, jobject jinput_object){

    	url = Jstring2CStr(env, jurl);
        av_dict_set(&opts, "rtsp_transport", "tcp", 0);
        int ret;
        char erbuf[256];
        // 打开流文件
        pFormatCtx = avformat_alloc_context();
        if(!pFormatCtx)
            LOGD("failed to alloc format context");

        if((ret = avformat_open_input(&pFormatCtx, url, NULL, &opts)) < 0){
        	LOGD("failed to open format context: %d", ret);
        	av_strerror(ret, erbuf, 256);
        	LOGD("%s", erbuf);
        	return 201;
        }
        // 获得流描述信息
        if (avformat_find_stream_info(pFormatCtx, NULL) < 0){
        	LOGD("failed to find stream info");
        	return 202;
        }
        // Dump information about file onto standard error
        av_dump_format(pFormatCtx, 0, url, 0);

        int i;

        // Find the first video stream
        int videoStream = -1;
        for (i = 0; i<pFormatCtx->nb_streams; i++)
        	if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) { //CODEC_TYPE_VIDEO(enum CodecType->AVMediaType)
        		videoStream = i;
        		break;
        	}
        if (videoStream == -1)
        	return -1;

        // Get a pointer to the codec context for the video stream
        c = pFormatCtx->streams[videoStream]->codec;
    	/*查找 流 CODEC(H264)*/
    	codec = avcodec_find_decoder(c->codec_id);

    	if(!codec) {
    			LOGD("failed to create CODEC");
    			return 101;
    	}
    	/*打开CODEC，这里初始化H.264解码器，调用decode_init本地函数*/
    	if (avcodec_open2(c, codec, &opts) < 0) 	{
    		LOGD("failed to open CODEC");
    		return -2;
    	}
    	/*为AVFrame申请空间，并清零*/
      picture = av_frame_alloc();
      pictureARGB = av_frame_alloc();
    	if(!picture) 	{
    		LOGD("failed to init AVFrame");
    		return -3;
    	}
    	if(!pictureARGB){
    		LOGD("failed to init pictureARGB");
    		return -3;
    	}
//    	/*为AVPacket申请空间*/
//    	pkt = (AVPacket*)malloc(sizeof(AVPacket));
//    	if(!pkt){
//    		LOGD("failed to get AVPacket");
//    	}
//
//    	av_init_packet(pkt);
//    	//java byte数组转换为 char 数组
//    	arrayLen = (*env)->GetArrayLength(env, ByteArray);
//    	Buf = (*env)->GetByteArrayElements(env, ByteArray, JNI_FALSE);
//    	if(arrayLen > 0){
//    		pkt->data = (uint8_t *)av_malloc(arrayLen);
//    		memcpy(pkt->data, Buf, arrayLen);
//    		pkt->size = arrayLen;
//    		pkt->dts = AV_NOPTS_VALUE;
//    		pkt->pts = AV_NOPTS_VALUE;
//    	}
    	//释放内存
//    	(*env)->ReleaseByteArrayElements(env, ByteArray, Buf, 0);

//    	if(!pkt->size){
//    		LOGD("size is 0");
//    		return -4;
//    	}


		//Call Java class VideoSurfaceView

		jclass jclass_surfaceView = (*env)->GetObjectClass(env, jinput_object);
		if(!jclass_surfaceView){
			LOGD("failed to find surfaceView");
			return -4;
		}
		jmethodID jdraw = (*env)->GetMethodID(env, jclass_surfaceView, "drawPicture", "(II)V");
		if(!jdraw){
			LOGD("can't find draw method");
			return -5;
		}

		jfieldID fid_frameData = (*env)->GetFieldID(env, jclass_surfaceView, "frameData", "[I");
		if(!fid_frameData){
			LOGD("failed to find fid_frameData");
			return -6;
		}
		//获取frameData的intArray对象
        jintArray jframeData = (jintArray)(*env)->GetObjectField(env, jinput_object, fid_frameData);
        if(!jframeData){
           	LOGD("failed to get array of framData");
            return -7;
        }
        jint *bufferARGB = (*env)->GetIntArrayElements(env, jframeData, NULL);

    	/**即使我们申请了一帧的内存，当转换的时候，我们仍然需要一个地方来放置原始的数据。我们使用
        		avpicture_get_size来获得我们需要的大小，然后手工申请内存空间：**/
        uint8_t *bufferYUV;
        int numBytes;
        // Determine required buffer size and allocate buffer
        numBytes = avpicture_get_size(AV_PIX_FMT_YUV420P, c->width, c->height);//AV_PIX_FMT_YUV420P
        bufferYUV = (uint8_t *)av_malloc(numBytes*sizeof(uint8_t));

        numBytes = avpicture_get_size(AV_PIX_FMT_RGB32, c->width, c->height);
//        bufferARGB = (uint8_t *)av_malloc(numBytes * sizeof(uint8_t));

   		/**现在我们使用 avpicture_fill 来把帧和我们新申请的内存来结合。关于 AVPicture 的结成：AVPicture 结构体是
    		AVFrame结构体的子集——AVFrame结构体的开始部分与 AVPicture 结构体是一样的。**/
    	// Assign appropriate parts of buffer to image planes in pFrameRGB
    	// Note that pFrameRGB is an AVFrame, but AVFrame is a superset
    	// of AVPicture
    	avpicture_fill((AVPicture *)picture, bufferYUV, AV_PIX_FMT_YUV420P, c->width, c->height);//AV_PIX_FMT_YUV420P
		avpicture_fill((AVPicture *)pictureARGB, (uint8_t *)bufferARGB, AV_PIX_FMT_RGB32, c->width, c->height);

        img_convert_ctx = sws_getContext(c->width, c->height, AV_PIX_FMT_YUV420P, c->width, c->height,
                        AV_PIX_FMT_RGB32, SWS_BICUBIC, NULL, NULL, NULL);
        if(!img_convert_ctx)
            LOGD("failed to get SwsContext");


		AVPacket packet;
		int consumed_bytes;

		setState(STATE_PLAY);
		thread = pthread_self();
		int numFrames = 0;
    	//开始解码
		while(av_read_frame(pFormatCtx, &packet) >= 0){
			if (packet.stream_index == videoStream) {

    		if(state == STATE_READY){
    			state = STATE_PLAY;
    		}else if(state == STATE_PAUSE){
    			continue;
    		}else if(state == STATE_CLOSE){
    			//TODO:Close the decoder
    			break;
    		}else if(state == STATE_OFF){
    			return -1;
    		}
    		//NAL 解码, 返回消耗的码流长度
  			consumed_bytes= avcodec_decode_video2(c, picture, &got_picture, &packet);
    		cnt++;

    		/*输出当前的解码信息*/
    		LOGI("No:=%4d, length=%4d\n",cnt,consumed_bytes);

			/*返回<0 表示解码数据头，返回>0，表示解码一帧图像*/
			if(consumed_bytes > 0 && got_picture)
			{
				LOGD("decoded success: %d", ++numFrames);
				/*从二维空间中提取解码后的图像*/
//				for(i=0; i<c->height; i++)
//					fwrite(picture->data[0] + i * picture->linesize[0], 1, c->width, out_file);
//				for(i=0; i<c->height/2; i++)
//					fwrite(picture->data[1] + i * picture->linesize[1], 1, c->width/2, out_file);
//				for(i=0; i<c->height/2; i++)
//					fwrite(picture->data[2] + i * picture->linesize[2], 1, c->width/2, out_file);

				/*解码后得到YUV格式图像转换为RGB32格式*/
				int sws_ret = sws_scale(img_convert_ctx, (const uint8_t* const*)picture->data, picture->linesize,
                				 	0, c->height, pictureARGB->data, pictureARGB->linesize);
                if(0 > sws_ret)
                	LOGD("failed to scale:%d", sws_ret);

				//Copy buffer of pictureARGB to Java field frameData
//                (*env)->SetIntArrayRegion(env, jframeData, 0, numBytes >> 2, (int*)bufferARGB);
                //Call surfaceView's method -- drawPicture(width, height)
                (*env)->CallVoidMethod(env, jinput_object, jdraw, c->width, c->height);


			}
		}
	}
//		(*env)->ReleaseIntArrayElements(env, jframeData, (jint*)bufferARGB, 0);
		// Free the packet that was allocated by av_read_frame
    av_free_packet(&packet);

	(*env)->ReleaseIntArrayElements(env, jframeData, bufferARGB, 0);
//	(*env)->DeleteLocalRef(env, jframeData);
	av_free(bufferYUV);
	av_free(bufferARGB);
	free(url);
    	return 0;

}


  JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_getVideoWidth
    (JNIEnv *env, jclass clazz){
    	if(!c)
    		return c->width;
    	else
    		return 0;
    }


  JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_getVideoHeight
    (JNIEnv *env, jclass clazz){
    	if(!c)
    		return c->height;
    	else
    		return 0;
    }


JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_SetDecoderState
  (JNIEnv *env, jclass clazz, jint stateId){

  	return setState(stateId);
  }
