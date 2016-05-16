#include <stdio.h>  
  
#ifdef __cplusplus  
extern "C" {  
#endif  
#include <libavcodec/avcodec.h>  
#include <libavformat/avformat.h>  
//#include <libswscale/swscale.h>  
  
#ifdef _MSC_VER  
int strcasecmp(const char *s1, const char *s2)  
{  
    while ((*s1 != '\0')  
        && (tolower(*(unsigned char *) s1) ==  
        tolower(*(unsigned char *) s2)))   
    {  
        s1++;  
        s2++;  
    }  
  
    return tolower(*(unsigned char *) s1) - tolower(*(unsigned char *) s2);  
}  
int strncasecmp(const char *s1, const char *s2, unsigned int n)  
{  
    if (n == 0)  
        return 0;  
  
    while ((n-- != 0)  
        && (tolower(*(unsigned char *) s1) ==  
        tolower(*(unsigned char *) s2))) {  
            if (n == 0 || *s1 == '\0' || *s2 == '\0')  
                return 0;  
            s1++;  
            s2++;  
    }  
  
    return tolower(*(unsigned char *) s1) - tolower(*(unsigned char *) s2);  
}  
#endif //_MSC_VER  
  
#ifdef __cplusplus  
}  
#endif  
  
/*********************************************************** 
'** stream_component_open 
*   Description:  
*            //open the stream component for video/audio 
* 
* 
*   Params:ic-pointer which contains the url and codec details  
:stream_index-index denoting video or audio stream 
***********************************************************/  
int stream_component_open(AVFormatContext *ic, int stream_index)  
{  
    AVCodecContext *enc;  
    AVCodec *codec;  
  
    if (stream_index < 0 || stream_index >= (int)ic->nb_streams)  
        return -1;  
    enc = ic->streams[stream_index]->codec;  
  
    /* prepare audio output */  
    if (enc->codec_type == CODEC_TYPE_AUDIO)  
    {  
        if (enc->channels > 0)  
            enc->request_channels = FFMIN(2, enc->channels);  
        else  
            enc->request_channels = 2;  
  
        /*Hardcoding the codec id to PCM_MULAW if the audio 
        codec id returned by the lib is CODEC_ID_NONE */  
  
        if(enc->codec_id == CODEC_ID_NONE)  
        {  
            enc->codec_id = CODEC_ID_PCM_MULAW;  
            enc->channels = 1;  
            enc->sample_rate = 16000;  
            enc->bit_rate = 128;  
        }  
    }  
  
    codec = avcodec_find_decoder(enc->codec_id);  
    enc->idct_algo           = FF_IDCT_AUTO;  
    enc->flags2   |= CODEC_FLAG2_FAST;  
    enc->skip_frame          = AVDISCARD_DEFAULT;  
    enc->skip_idct           = AVDISCARD_DEFAULT;  
    enc->skip_loop_filter    = AVDISCARD_DEFAULT;  
    enc->error_concealment   = 3;  
  
    if (!codec || avcodec_open(enc, codec) < 0)  
        return -1;  
    avcodec_thread_init(enc, 1);  
    enc->thread_count= 1;  
    ic->streams[stream_index]->discard = AVDISCARD_DEFAULT;  
  
    return 0;  
}  
//声明函数  
int enable_local_record(AVFormatContext *ic/*已经打开的视频文件上下文*/,  
                        int videostream,int audiostream,  
                        AVFormatContext **out_oc,const char * ofile,const char * ofileformat);  
  
int exit_onerr(const char * err_desc/*错误描述*/,int err_code/*错误码*/)  
{  
    printf("%s\n",err_desc);  
    system("pause");//暂停，查看错误描述  
    return err_code;  
}  
int main(int argc, char* argv[])  
{  
    //初始化ffmpeg链表结构  
    avcodec_register_all();                     // Register all formats and codecs  
    av_register_all();  
  
    AVPacket  packet;  
  
    //打开文件  
    AVFormatContext * ic = NULL;  
    const char * rtsp_url = "rtsp://192.168.0.168:8557/PSIA/Streaming/channels/2?videoCodecType=H.264";  
    if(av_open_input_file(&ic, rtsp_url, NULL, 0, NULL)!=0)  
    {                         
        return exit_onerr("can't open file.",-1);  
    }  
    if(!ic)  
    {  
        return exit_onerr("unknow error.",-2);  
    }  
    ic ->max_analyze_duration = 1000;  
  
    //get streams information  
    if(av_find_stream_info(ic)<0)  
    {  
        av_close_input_file(ic);//退出前，记得释放资源  
        ic = NULL;  
        return exit_onerr("con't init streams information.",-3);  
    }  
  
  
    //find stream  
    int videoStream=-1; // Didn't find a video stream  
    int audioStream=-1; // Didn't find a audio stream   
    // Find the first video stream  
    for(int i=0; i<ic ->nb_streams; i++)  
    {  
        if( ic ->streams[i]->codec->codec_type==CODEC_TYPE_VIDEO)  
        {   
            videoStream=i;  
            break;  
        }  
    }  
    // Find the first audio stream  
    for(int i=0; i<ic ->nb_streams; i++)  
    {  
        if( ic ->streams[i]->codec->codec_type==CODEC_TYPE_AUDIO)  
        {  
            audioStream=i;  
            break;  
        }  
    }  
    //判断视频文件中是否包含音视频流，如果没有，退出  
    if(audioStream<0 && videoStream<0)  
    {  
        av_close_input_file(ic);//退出前，记得释放资源  
        ic = NULL;  
        return exit_onerr("con't find a audio stream or video stream",-4);  
    }  
  
    //open the stream component for video  
    int videoComponent = -1;  
    if(videoStream >= 0)  
    {  
        videoComponent = stream_component_open(ic, videoStream);   
        if(videoComponent<0)  
        {  
            av_close_input_file(ic);//退出前，记得释放资源  
            return exit_onerr("not supported video stream",-5);//要求重新编译ffmpeg以支持该种编码格式  
        }  
    }  
    //open the stream component for audio  
    int audioComponent = -1;  
    if(audioStream >= 0)  
    {  
        audioComponent = stream_component_open(ic, audioStream);   
        if(audioComponent<0)  
        {  
            av_close_input_file(ic);//退出前，记得释放资源  
            return exit_onerr("not supported audio stream",-6);//要求重新编译ffmpeg以支持该种编码格式  
        }  
    }  
  
  
    //////////////////////////////////////////////////////  
    int ret = 0;  
    //初始化并打开录像  
    AVFormatContext * oc = NULL;  
    const char * out_file_name = "D:\\test.avi";  
  
    //获取一帧完整的图像用于初始化ffmpeg内部的一些结构体数据(这里使用什么数据尚未清楚，请自行查看)，否则会出现写文件头失败  
    int got_picture = 0;  
    AVFrame *frame = avcodec_alloc_frame();  
    while( 1 )  
    {  
        if(av_read_frame( ic, &packet)<0)  
        {  
            av_free_packet(&packet);  
            av_close_input_file(ic);//退出前，记得释放资源  
            return exit_onerr("read frame error.",-7);//读取视频帧失败  
        }  
        if(packet.stream_index == videoStream)  
        {  
            avcodec_decode_video(ic->streams[videoStream]->codec, frame, &got_picture, packet.data, packet.size);  
        }  
        av_free_packet(&packet);  
        if(got_picture)  
            break;  
    }  
    av_free(frame);  
  
    ret = enable_local_record(ic,videoStream,audioStream,&oc,out_file_name,"avi");  
  
    if(ret <0 || !oc)  
    {  
        //退出前，记得释放资源，现在又多了个oc参数需要释放  
        if(oc)  
        {  
            ///cleanup the output contents format  
            for(unsigned int i=0;i< oc->nb_streams;i++)  
            {  
                av_metadata_free(&oc ->streams[i]->metadata);  
                av_free(oc ->streams[i]->codec);  
                av_free(oc ->streams[i]);  
            }  
            for(unsigned int i=0;i<oc ->nb_programs;i++)   
            {  
                av_metadata_free(&oc ->programs[i]->metadata);  
            }  
            for(unsigned int i=0;i<oc ->nb_chapters;i++)   
            {  
                av_metadata_free(&oc ->chapters[i]->metadata);  
            }  
            av_metadata_free(&oc ->metadata);  
            av_free(oc);  
        }  
        av_close_input_file(ic);  
        return exit_onerr("can't init out file.",-8);  
    }  
  
    //开始录像  
    int video_dts = 0,audio_dts = 0;//时间戳  
    int total_frame = 300;//写300帧文件  
    while(total_frame--)  
    {  
        if( av_read_frame( ic, &packet) <0 )     //read the packet  
        {         
            //读取数据出错  
            av_free_packet(&packet);  
            break;  
        }  
        if(packet.data && (packet.stream_index == videoStream || packet.stream_index == audioStream) )  
        {  
            //计算时间视频戳，顺序+1，这里可以多研究几种编码的音视频文件，求其时间戳生成格式，h264编码顺序加1即可  
            if(packet.stream_index == videoStream)  
            {  
                packet.dts = video_dts++;  
                packet.pts = video_dts;  
            }  
            else if(packet.stream_index == audioStream)//计算音频时间戳  
            {  
                packet.dts = audio_dts++;  
                packet.pts = audio_dts * (1000 * packet.size /ic ->streams[packet.stream_index]->codec ->sample_rate);  
            }  
            packet.flags |= PKT_FLAG_KEY;  
            if(av_interleaved_write_frame(oc,&packet)<0)  
            {  
                printf("st:%d\twrite frame failed.\n",packet.stream_index);  
            }  
        }  
        av_free_packet(&packet);  
    }  
  
    //关闭录像文件和输入文件  
  
    //写文件尾  
    av_write_trailer(oc);  
  
    /* close the output file if need.*/  
    if (!(oc ->oformat->flags & AVFMT_NOFILE))   
    {  
        url_fclose(oc->pb);  
    }  
  
    //释放资源  
    /*cleanup the output contents format*/  
    for(unsigned int i=0;i< oc->nb_streams;i++)  
    {  
        av_metadata_free(&oc ->streams[i]->metadata);  
        av_free(oc ->streams[i]->codec);  
        av_free(oc ->streams[i]);  
    }  
    for(unsigned int i=0;i<oc ->nb_programs;i++)   
    {  
        av_metadata_free(&oc ->programs[i]->metadata);  
    }  
    for(unsigned int i=0;i<oc ->nb_chapters;i++)   
    {  
        av_metadata_free(&oc ->chapters[i]->metadata);  
    }  
    av_metadata_free(&oc ->metadata);  
    av_free(oc);  
  
    av_close_input_file(ic);  
  
    return 0;  
}  
  
//初始化录像文件，代码较长，可写成函数  
/* 
*return <0  failed, 0 success 
*/  
int enable_local_record(AVFormatContext *ic/*已经打开的视频文件上下文*/,int videostream,int audiostream,AVFormatContext **out_oc,const char * ofile,const char * ofileformat)  
{  
    AVFormatContext *oc     = NULL;//  
    AVOutputFormat  *fmt    = NULL;  
  
    if( ofileformat )  
    {  
        fmt = av_guess_format(ofileformat, NULL, NULL);  
    }  
    if(!fmt)  
    {  
        printf("out file format \"%s\" invalidate.\n",ofileformat);  
        return -1;  
    }  
  
    /*******************************************init output contents begin**********************************************************/  
    /* allocate the output media context */  
    oc = avformat_alloc_context();  
    if (!oc)   
    {  
        printf("out of memory.\n");  
        return -2;//内存分配失败  
    }  
  
    *out_oc = oc;  
  
    oc ->oformat = fmt;  
    sprintf_s( oc ->filename, sizeof( oc ->filename), "%s", ofile);  
    av_metadata_conv(oc, fmt->metadata_conv, NULL);  
    if( videostream >=0 )  
    {  
        //AVCodecContext* pCodecCtx= ->streams[videostream]->codec;;  
        //add video stream  
        AVStream * st = av_new_stream(oc, videostream);  
        if(!st)  
        {  
            printf("can not add a video stream.\n");  
            return -3;  
        }  
        st->codec->codec_id           =  ic ->streams[videostream] ->codec->codec_id;  
        st->codec->codec_type     =  CODEC_TYPE_VIDEO;  
        st->codec->bit_rate           =  ic ->streams[videostream] ->codec->bit_rate;  
        st->codec->width          =  ic ->streams[videostream] ->codec->width;  
        st->codec->height         =  ic ->streams[videostream] ->codec->height;  
        st->codec->gop_size           =  ic ->streams[videostream] ->codec->gop_size;  
        st->codec->pix_fmt            =  ic ->streams[videostream] ->codec->pix_fmt;  
        st->codec->frame_size     =  ic ->streams[videostream] ->codec->frame_size;  
        st->codec->has_b_frames       =  ic ->streams[videostream] ->codec->has_b_frames;  
        st->codec->extradata      =  ic ->streams[videostream] ->codec->extradata;  
        st->codec->extradata_size =  ic ->streams[videostream] ->codec->extradata_size;  
        st->codec->codec_tag      =  ic ->streams[videostream] ->codec->codec_tag;  
        st->codec->bits_per_raw_sample        =  ic ->streams[videostream] ->codec->bits_per_raw_sample;  
        st->codec->chroma_sample_location =  ic ->streams[videostream] ->codec->chroma_sample_location;  
        st->time_base.den            =  ic ->streams[videostream] ->time_base.den;  
        st->time_base.num            =  ic ->streams[videostream] ->time_base.num;  
        st->cur_dts                  =  ic ->streams[videostream] ->cur_dts;  
        st->stream_copy              =  1;  
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
            st->codec->time_base = ic ->streams[videostream] ->time_base;  
        }  
        st->disposition              =  ic ->streams[videostream] ->disposition;  
    }  
    if(audiostream >= 0 )  
    {  
        AVStream * st = av_new_stream( oc, audiostream);  
        if(!st)  
        {  
            printf("can not add a audio stream.\n");  
            return -4;  
        }  
        st->codec->codec_id           =  ic ->streams[audiostream] ->codec->codec_id;  
        st->codec->codec_type     =  CODEC_TYPE_AUDIO;  
        st->codec->bit_rate           =  ic ->streams[audiostream] ->codec->bit_rate;  
        st->codec->gop_size           =  ic ->streams[audiostream] ->codec->gop_size;  
        st->codec->pix_fmt            =  ic ->streams[audiostream] ->codec->pix_fmt;  
        st->codec->bit_rate           =  ic ->streams[audiostream] ->codec->bit_rate;  
        st->codec->channel_layout =  ic ->streams[audiostream] ->codec->channel_layout;  
        st->codec->frame_size     =  ic ->streams[audiostream] ->codec->frame_size;  
        st->codec->sample_rate        =  ic ->streams[audiostream] ->codec->sample_rate;  
        st->codec->channels           =  ic ->streams[audiostream] ->codec->channels;      
        st->codec->block_align        =  ic ->streams[audiostream] ->codec->block_align;   
        st->time_base.den            =  ic ->streams[audiostream] ->time_base.den;  
        st->time_base.num            =  ic ->streams[audiostream] ->time_base.num;  
        st->stream_copy              =  1;  
        st->pts.den                  =  ic ->streams[audiostream] ->time_base.den;  
        st->pts.num                  =  ic ->streams[audiostream] ->time_base.num;  
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
    //AVFormatParameters params, *ap = ¶ms;  
    //memset(ap, 0, sizeof(*ap));  
    if (av_set_parameters(oc, NULL/*ap*/) < 0)  
    {  
        printf("invalid output format parameters.\n");  
        return -5;  
    }  
    oc ->flags |= AVFMT_FLAG_NONBLOCK;  
    /*******************************************init output contents end**********************************************************/  
  
    /* open the output file, if needed */  
    if (!(oc ->oformat ->flags & AVFMT_NOFILE))   
    {  
        try  
        {  
            if (url_fopen(&oc->pb, ofile, URL_WRONLY) < 0)   
            {  
                printf("Could not open file.\n");  
                return -6;  
            }  
        }  
        catch(...)  
        {  
            printf("Could not open file.\n");  
            return -6;  
        }  
    }  
  
    /* write the stream header, if any */  
    if( 0 != av_write_header(oc))  
    {  
        printf("write the stream header failed.\n");  
        return -7;  
    }  
  
    return 0;  
}  