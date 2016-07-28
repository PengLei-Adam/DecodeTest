#include"record.h"
//#include <libavformat/avformat.h>
#include<stdio.h>
//#include<jni>
#include<android/log.h>
#define LOG_TAG "librecord"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

//初始化录像文件
int init_record(AVFormatContext *ic/*已经打开的视频文件上下文*/,int videostream,int audiostream,
                AVFormatContext **out_oc, AVBitStreamFilterContext** pbsfc, const char * ofile,const char * ofileformat, int num, int den)
{  
    AVFormatContext *oc     = NULL;//  
    // AVOutputFormat  *fmt    = NULL;
	
    // if( ofileformat )  
    // {  
        // fmt = av_guess_format(ofileformat, NULL, NULL);  
    // }  
    // if(!fmt)  
    // {  
        // LOGD("out file format \"%s\" invalidate.\n",ofileformat);  
        // return -1;  
    // }  
	/* allocate the output media context
	avformat_alloc_context2 contains av_guess_format
	*/  
    avformat_alloc_output_context2(&oc, NULL, ofileformat, ofile);
    if (!oc)   
    {  
        LOGD("out of memory.\n");  
        return -1;//内存分配失败  
    }  
  
    *out_oc = oc;

	//log
	AVCodecContext * codec_t;
	if( videostream >=0 )
    {  
        //AVCodecContext* pCodecCtx= ic->streams[videostream]->codec;;  
        //add video stream  
		
        AVStream * st = avformat_new_stream(oc, ic->streams[videostream]->codec->codec);  
        if(!st)  
        {  
            LOGD("can not add a video stream.\n");  
			avformat_free_context(oc);
			*out_oc = NULL;
            return -2;
        }  
		if(0 > avcodec_copy_context(st->codec, ic->streams[videostream]->codec)){
			LOGD("can not copy codec from input stream to output stream");
			avformat_free_context(oc);
			*out_oc = NULL;
			return -3;
		}
         st->codec->codec_id           =  ic ->streams[videostream] ->codec->codec_id;
         st->codec->codec_type     =  AVMEDIA_TYPE_VIDEO;
         st->codec->bit_rate           =  ic ->streams[videostream] ->codec->bit_rate;
         st->codec->width          =  ic ->streams[videostream] ->codec->width;
         st->codec->height         =  ic ->streams[videostream] ->codec->height;
//         st->codec->gop_size           =  ic ->streams[videostream] ->codec->gop_size;
         st->codec->pix_fmt            =  ic ->streams[videostream] ->codec->pix_fmt;
         st->codec->frame_size     =  ic ->streams[videostream] ->codec->frame_size;
         st->codec->has_b_frames       =  ic ->streams[videostream] ->codec->has_b_frames;
         st->codec->extradata      =  ic ->streams[videostream] ->codec->extradata;
         st->codec->extradata_size =  ic ->streams[videostream] ->codec->extradata_size;
//         st->codec->codec_tag      =  ic ->streams[videostream] ->codec->codec_tag;
         st->codec->bits_per_raw_sample        =  ic ->streams[videostream] ->codec->bits_per_raw_sample;
         st->codec->chroma_sample_location =  ic ->streams[videostream] ->codec->chroma_sample_location;
//        st->time_base.den            =  ic ->streams[videostream] ->time_base.den;
//        st->time_base.num            =  ic ->streams[videostream] ->time_base.num;
        st->cur_dts                  =  0;
          st->first_dts                =  0;
//        st->stream_copy              =  1;
        st->pts.den                  =  ic ->streams[videostream] ->time_base.den;
        st->pts.num                  =  ic ->streams[videostream] ->time_base.num;
        if( oc ->oformat->flags & AVFMT_GLOBALHEADER)  
            st ->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;  
         if(av_q2d(ic ->streams[videostream] ->codec->time_base)*ic ->streams[videostream] ->codec->ticks_per_frame > av_q2d(ic ->streams[videostream]->time_base) &&
             av_q2d(ic ->streams[videostream]->time_base) < 1.0/1000)
         {
             st->codec->time_base = ic ->streams[videostream] ->codec->time_base;
             st->codec->time_base.num *= ic ->streams[videostream] ->codec->ticks_per_frame;
         }
         else
         {
//                LOGI("ic->time_base:num=%d, den=%d",
//                ic ->streams[videostream] ->codec->time_base.num,
//                ic ->streams[videostream] ->codec->time_base.den);
//             st->codec->time_base = ic ->streams[videostream] ->time_base;
                st->codec->time_base = (AVRational){num, den};
//                st->r_frame_rate = (AVRational){den, num};
//                st->avg_frame_rate = (AVRational){den, num};
//                st->time_base = (AVRational){num, den};

         }
         st->disposition              =  ic ->streams[videostream] ->disposition;
         //log
         codec_t = st ->codec;
//         LOGI("avg_frame_rate=%d/%d, %d/%d", st->frame_rate.num, st->frame_rate.den,
//            ic->streams[videostream]->avg_frame_rate.num, ic->streams[videostream]->avg_frame_rate.den);
            LOGI("stream->first_dts=%lld", st->first_dts);

            //init bit stream filter
            if(*pbsfc){
                av_bitstream_filter_close(*pbsfc);
            }
            *pbsfc = av_bitstream_filter_init("h264_mp4toannexb");
    }
	if(audiostream >= 0 )  
    {  
        AVStream * st = avformat_new_stream(oc, ic->streams[audiostream]->codec->codec);
        if(!st)  
        {  
            LOGD("can not add a audio stream.\n");  
            avformat_free_context(oc);
			*out_oc = NULL;
			return -2;
        }
        if(0 > avcodec_copy_context(st->codec, ic->streams[audiostream]->codec)){
        			LOGD("can not copy codec from input stream to output stream");
        			avformat_free_context(oc);
        			*out_oc = NULL;
        			return -3;
        		}
//        st->codec->codec_id           =  ic ->streams[audiostream] ->codec->codec_id;
//        st->codec->codec_type     =  CODEC_TYPE_AUDIO;
//        st->codec->bit_rate           =  ic ->streams[audiostream] ->codec->bit_rate;
//        st->codec->gop_size           =  ic ->streams[audiostream] ->codec->gop_size;
//        st->codec->pix_fmt            =  ic ->streams[audiostream] ->codec->pix_fmt;
//        st->codec->bit_rate           =  ic ->streams[audiostream] ->codec->bit_rate;
//        st->codec->channel_layout =  ic ->streams[audiostream] ->codec->channel_layout;
//        st->codec->frame_size     =  ic ->streams[audiostream] ->codec->frame_size;
//        st->codec->sample_rate        =  ic ->streams[audiostream] ->codec->sample_rate;
//        st->codec->channels           =  ic ->streams[audiostream] ->codec->channels;
//        st->codec->block_align        =  ic ->streams[audiostream] ->codec->block_align;
//        st->time_base.den            =  ic ->streams[audiostream] ->time_base.den;
//        st->time_base.num            =  ic ->streams[audiostream] ->time_base.num;
//        st->stream_copy              =  1;
//        st->pts.den                  =  ic ->streams[audiostream] ->time_base.den;
//        st->pts.num                  =  ic ->streams[audiostream] ->time_base.num;
        if( oc->oformat->flags & AVFMT_GLOBALHEADER)  
            st ->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;  
        if(av_q2d(ic ->streams[audiostream] ->codec->time_base)*ic ->streams[audiostream] ->codec->ticks_per_frame > av_q2d(ic ->streams[audiostream]->time_base) &&   
            av_q2d(ic ->streams[audiostream]->time_base) < 1.0/1000)  
        {  
            st->codec->time_base = ic ->streams[audiostream] ->codec->time_base;
            st->codec->time_base.num *= ic ->streams[audiostream] ->codec->ticks_per_frame;
        }  
        else  
        {  
            st->codec->time_base = ic ->streams[audiostream] ->time_base;  
        }  
    }
	/* set the output parameters (must be done even if no parameters). */ 
	// if (av_set_parameters(oc, NULL/*ap*/) < 0)
    // {  
        // LOGD("invalid output format parameters.\n");  
        // return -5;  
    // }  
    oc ->flags |= AVFMT_FLAG_NONBLOCK;
	
	/* open the output file, if needed */  
    if (!(oc ->oformat ->flags & AVFMT_NOFILE))   
    {  
        
		/*暂定改为avio_open(&oc->pb, ofile, AVIO_FLAG_WRITE)
		原函数为url_fopen(&oc->pb, ofile, URL_WRONLY)*/
            if (avio_open(&oc->pb, ofile, AVIO_FLAG_WRITE) < 0)   
            {  
                LOGD("Could not open file.\n");  
                avformat_free_context(oc);
				*out_oc = NULL;  
				return -4;
            }  
        
    }  
	/* write the stream header, if any */  
    if( 0 > avformat_write_header(oc, NULL))  
    {  
        LOGD("write the stream header failed.\n"); 
		if (oc && !(oc->oformat->flags & AVFMT_NOFILE))
			avio_closep(&oc->pb);
        avformat_free_context(oc);
		*out_oc = NULL;   
		return -5;
    }  

    LOGI("time_base.num=%d\ttime_base.den=%d\tticks_per_frame=%d",
                    codec_t->time_base.num, codec_t->time_base.den, codec_t->ticks_per_frame);
    return 0;  
}  

int deinit_record(AVFormatContext * oc, AVBitStreamFilterContext** pbsfc)
{

    //close bit stream filter
    av_bitstream_filter_close(*pbsfc);
    *pbsfc = NULL;

	//写文件尾
    av_write_trailer(oc);
  
    /* close the output file if need.*/  
    if (!(oc ->oformat->flags & AVFMT_NOFILE))   
    {  
        avio_closep(oc->pb);  
    } 
	avformat_free_context(oc);
	
	return 0;
}

int on_recording(AVFormatContext * ic, AVFormatContext * oc, AVPacket * packet, AVBitStreamFilterContext* bsfc,
				int videostream, int video_dts,
				int audiostream, int audio_dts)
{
	if(packet->data)  
    {  
            //计算时间视频戳，顺序+1
        if(packet->stream_index == videostream)
            {  
//                packet->dts = ic->streams[videostream]->codec->ticks_per_frame * av_rescale_q(packet->pts, ic->streams[videostream]->codec->time_base, oc->streams[videostream]->time_base);
//                packet->pts = ic->streams[videostream]->codec->ticks_per_frame * av_rescale_q(packet->pts, ic->streams[videostream]->codec->time_base, oc->streams[videostream]->time_base);

                packet->dts = video_dts;
                packet->pts = video_dts;
//                LOGI("packet->dts=%lld", packet->dts);

                //stream filter filter the h264 stream
                av_bitstream_filter_filter(bsfc, ic->streams[videostream]->codec,
                                            NULL, &packet->data, &packet->size,
                                            packet->data, packet->size, 0);
            }  
            else if(packet->stream_index == audiostream)//计算音频时间戳
            {  
                packet->dts = audio_dts;  
                packet->pts = audio_dts;// (1000 * packet->size /oc ->streams[audiostream]->codec ->sample_rate)
            }  
            packet->flags |= AV_PKT_FLAG_KEY;
            if(av_write_frame(oc,packet)<0)
            {  
                LOGD("st:%d\twrite frame failed.\n",packet->stream_index);  
				return -1;
            }

			return 0;
        } else{
			LOGD("packet data is empty");
			return -2;
	} 
			
}
