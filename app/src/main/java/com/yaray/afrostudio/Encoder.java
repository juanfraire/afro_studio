package com.yaray.afrostudio;

//import com.android.cts.media.R;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;

// http://stackoverflow.com/questions/18862715/how-to-generate-the-aac-adts-elementary-stream-with-android-mediacodec
// http://developer.android.com/intl/es/reference/android/media/MediaCodec.html (Syncrhonous mode)
// audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
// Media Info AAC in Android http://stackoverflow.com/questions/6931180/how-to-detect-aac-audio-profile-and-ensure-android-compatibility
// Header: http://www.zytrax.com/tech/audio/formats.html

public class Encoder {

    private static final String TAG = "Encoder";
    FileOutputStream mFileStream;

    MediaFormat format;
    String componentName = null;
    MediaCodec codec;

    ByteBuffer[] codecInputBuffers;
    ByteBuffer[] codecOutputBuffers;

    Context activityContext1;

    public void init(final Context activityContext, final Ensemble ensemble, String user) {

        activityContext1=activityContext;

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) { //Media Ready to read and write
            user = user.substring(0, user.indexOf("@"));
            String fileName = ensemble.ensembleName + "_by_" + ensemble.ensembleAuthor + "_u_" + user + ".aac";
            File file = new File(activityContext.getExternalFilesDir(null), fileName);
            try {
                mFileStream = new FileOutputStream(file);
            } catch (IOException e) {
                Log.e("ExternalStorage", "Error writing " + file, e);
            }
        }

        format = new MediaFormat(); // Configure Format
        //Log.e(TAG, "format: " + MediaFormat.MIMETYPE_AUDIO_AAC);
        format.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC); //"audio/mp4a-latm"
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, 2); // 2=OMX_AUDIO_AACObjectLC, 5=OMX_AUDIO_AACObjectHE, 39=OMX_AUDIO_AACObjectELD
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100); // {8000, 11025, 22050, 44100, 48000}
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2); // {1,2}
        format.setInteger(MediaFormat.KEY_BIT_RATE, 64000); // {64000, 128000} (desired bit rate)

        //MediaFormat.KEY_AAC_SBR_MODE

        LinkedList<String> componentNames = new LinkedList<String>(); // Configure Components
        int n = MediaCodecList.getCodecCount();
        for (int i = 0; i < n; ++i) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (!info.isEncoder() || !info.getName().startsWith("OMX."))
                continue;
            String[] supportedTypes = info.getSupportedTypes();
            for (int j = 0; j < supportedTypes.length; ++j)
                if (supportedTypes[j].equalsIgnoreCase("audio/mp4a-latm")) {
                    componentNames.push(info.getName());
                    //Log.e(TAG, "Component :" + info.getName());
                    break;
                }
        }
        if (componentNames.size() != 0) {
            componentName = componentNames.getFirst();
            try {
                codec = MediaCodec.createByCodecName(componentName);
                try {
                    codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "codec '" + componentName + "' failed configuration.");
                }
                codec.start();
            } catch (IOException e) {
                Log.e(TAG, "codec '" + componentName + "' failed IOException.");
            }
        } else
            Log.e(TAG, "Cannot find audio encoder!"); //componentName=null;
    }

    public void write(byte[] byteBuffer, int offset, int byteBufferSizeInBytes, boolean endOfStream) { //offset=0

        if (componentName == null) // no audio decoder, do nothing
            return;

        codecInputBuffers = codec.getInputBuffers();
        codecOutputBuffers = codec.getOutputBuffers();

        if (endOfStream) {
            int index = codec.dequeueInputBuffer(10000);  /* timeoutUs */
            if (index != MediaCodec.INFO_TRY_AGAIN_LATER)
                codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            else {
                Log.e(TAG, "Codec: Try Again Later");
            }

            // Output
            index=0;
            while(index>=0){
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                index = codec.dequeueOutputBuffer(info, 10000); //timeoutUs
                //Log.e(TAG, "last index=" + index);
                if (index >= 0) {
                    int outBitsSize = info.size;
                    int outPacketSize = outBitsSize + 7;    // 7 is ADTS size
                    ByteBuffer outBuf = codecOutputBuffers[index];

                    outBuf.position(info.offset);
                    outBuf.limit(info.offset + outBitsSize);
                    try {
                        byte[] data = new byte[outPacketSize];  //space for ADTS header included
                        addADTStoPacket(data, outPacketSize);
                        outBuf.get(data, 7, outBitsSize);
                        outBuf.position(info.offset);
                        mFileStream.write(data, 0, outPacketSize);  //open FileOutputStream beforehand
                    } catch (IOException e) {
                        //Log.e(TAG, "failed writing bitstream data to file");
                        e.printStackTrace();
                    }
                    outBuf.clear();
                    codec.releaseOutputBuffer(index, false);
                    //Log.e(TAG, "indexOut:" + index + ", dequeued:" + outBitsSize + ", wrote:" + outPacketSize);
                }
            }


            //Log.e(TAG, "End of Stream!");
        } else {
            int iniByte = 0;
            while (iniByte < byteBuffer.length) {
                // Input
                int index = codec.dequeueInputBuffer(10000);  //timeoutUs
                if (index != MediaCodec.INFO_TRY_AGAIN_LATER) {
                    codecInputBuffers[index].clear();
                    int endByte = Math.min(iniByte + codecInputBuffers[index].limit(), byteBuffer.length);
                    byte[] partialByteBuffer = Arrays.copyOfRange(byteBuffer, iniByte, endByte);
                    //Log.e(TAG, "indexInp:" + index + ", byteBufferLength:" + byteBuffer.length + ", partialByteBufferLenght:" + partialByteBuffer.length + ", range:[" + iniByte + "," + endByte + "]"); //bytes
                    iniByte = endByte;
                    codecInputBuffers[index].put(partialByteBuffer);
                    codec.queueInputBuffer(index, 0, partialByteBuffer.length, 0, 0);
                } else {
                    Log.e(TAG, "Codec: Try Again Later!");
                }

                // Output
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                index = codec.dequeueOutputBuffer(info, 10000); //timeoutUs
                if (index >= 0) {
                    //Log.e(TAG, "Valid Index!");
                    int outBitsSize = info.size;
                    int outPacketSize = outBitsSize + 7;    // 7 is ADTS size
                    ByteBuffer outBuf = codecOutputBuffers[index];
                    outBuf.position(info.offset);
                    outBuf.limit(info.offset + outBitsSize);
                    try {
                        byte[] data = new byte[outPacketSize];  //space for ADTS header included
                        addADTStoPacket(data, outPacketSize);
                        outBuf.get(data, 7, outBitsSize);
                        outBuf.position(info.offset);
                        mFileStream.write(data, 0, outPacketSize);  //open FileOutputStream beforehand
                    } catch (IOException e) {
                        Log.e(TAG, "failed writing bitstream data to file");
                        e.printStackTrace();
                    }
                    outBuf.clear();
                    codec.releaseOutputBuffer(index, false);
                    //Log.e(TAG, "indexOut:" + index + ", dequeued:" + outBitsSize + ", wrote:" + outPacketSize);

                } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    codecOutputBuffers = codec.getOutputBuffers();
                    //Log.e(TAG, "Wrong Index! (INFO_OUTPUT_BUFFERS_CHANGED)");
                } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    codecOutputBuffers = codec.getOutputBuffers();
                    //Log.e(TAG, "Wrong Index! (INFO_OUTPUT_FORMAT_CHANGED)");
                } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER){
                    //Log.e(TAG, "Wrong Index! (INFO_TRY_AGAIN_LATER)");
                }
            } // End While
        }
        //Log.e(TAG, "End of this packet!");
    }

    public void close() {
        codec.release();
        try {
            mFileStream.close();
            //Toast.makeText(activityContext1, "Audio stored in " + activityContext1.getExternalFilesDir(null), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("ExternalStorage", "Error closing file", e);
        }

    }

    /**
     * Add ADTS header at the beginning of each and every AAC packet.
     * This is needed as MediaCodec encoder generates a packet of raw
     * AAC data.
     * <p/>
     * Note the packetLen must count in the ADTS header itself.
     **/
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //=2 AAC LC //39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
        int freqIdx = 4;  //=4 44.1KHz, =7 22050 Hz //http://wiki.multimedia.cx/index.php?title=MPEG-4_Audio#Sampling_Frequencies
        int chanCfg = 2;  //=2 CPE

        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

}
