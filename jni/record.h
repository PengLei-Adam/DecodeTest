/*
* Function of recording video
*/
#ifndef _RECORD_H_
#define _RECORD_H_

#include <libavcodec/avcodec.h>  
#include <libavformat/avformat.h>  

/*
* Init AVFormatContext for recording
* 
* audiostream = -1, means do not decode and record audiostream
*/
int init_record(AVFormatContext *ic,int videostream,int audiostream,
			AVFormatContext **out_oc,const char * ofile,const char * ofileformat,
			int num, int den);

/*
* Free AVFormatContext after recording
*/
int deinit_record(AVFormatContext *oc);
/*
* Record a packet of data and write into a file.
* Should be called after init_record().
* Should be in a loop to record several frames.
* video_dts add one by one for each frame.
*/
int on_recording(AVFormatContext * ic, AVFormatContext * oc, AVPacket * packet,
				int videostream, int video_dts,
				int audiostream, int audio_dts);


#endif