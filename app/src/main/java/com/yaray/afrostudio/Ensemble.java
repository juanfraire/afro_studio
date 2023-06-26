package com.yaray.afrostudio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

public class Ensemble {

    private static final String TAG = "AfroStudio.Ensemble";

    public String ensembleName;
    public String ensembleAuthor;

    // Time Measurements - Time Signature: beats (pulses) contained in each bar.
    //  3/4 -> *    *    *         ( 3 beats -quater- per bar) -> Not used
    //  6/8 -> **   **   **        ( 6 beats -eights- per bar) -> Not used
    // 16/8 -> **** **** **** **** (16 beats -eights- per bar) (*)
    // 12/8 -> ***  ***  ***  ***  (12 beats -eights- per bar) (*)
    //  9/8 -> ***  ***  ***       ( 9 beats -eights- per bar) (*)
    private int beatsPerBar; //16, 12, 9
    private int beats; // Total beats in ensemble (start from 1 and do not count trailing silence)
    private int bars; // Total bars in ensemble (start from 0 and do not count trailing silence)

    // Instruments
    @SuppressWarnings("unchecked")
    public Vector<Vector<Integer>> djembeVector = new Vector(); // 0=empty, 1=snd_djembe_bass, 2=snd_djembe_tone, 3=snd_djembe_slap
    @SuppressWarnings("unchecked")
    public Vector<Vector<Integer>> dunVector = new Vector(); // 0=empty, 1=snd_dun_bass_bell, 2=snd_dun_bell, 3=snd_dun_bass_bell_mute
    @SuppressWarnings("unchecked")
    public Vector<Vector<Integer>> kenVector = new Vector(); // 0=empty, 1=snd_ken_bass_bell, 2=snd_ken_bell, 3=snd_ken_bass_bell_mute
    @SuppressWarnings("unchecked")
    public Vector<Vector<Integer>> sagVector = new Vector(); // 0=empty, 1=snd_sag_bass_bell, 2=snd_sag_bell, 3=snd_sag_bass_bell_mute
    @SuppressWarnings("unchecked")
    public Vector<Vector<Integer>> shekVector = new Vector(); // 0=empty, 1=snd_shek
    @SuppressWarnings("unchecked")
    public Vector<Vector<Integer>> baletVector = new Vector(); // 0=empty, 1=dun_bass, 2=sag_bass, 3=ken_bass

    @SuppressWarnings("unchecked")
    public Vector<Integer> djembeStatus = new Vector(); // 0=inactive, 1=active
    @SuppressWarnings("unchecked")
    public Vector<Integer> dunStatus = new Vector(); // 0=inactive, 1=active
    @SuppressWarnings("unchecked")
    public Vector<Integer> kenStatus = new Vector(); // 0=inactive, 1=active
    @SuppressWarnings("unchecked")
    public Vector<Integer> sagStatus = new Vector(); // 0=inactive, 1=active
    @SuppressWarnings("unchecked")
    public Vector<Integer> shekStatus = new Vector(); // 0=inactive, 1=active
    @SuppressWarnings("unchecked")
    public Vector<Integer> baletStatus = new Vector(); // 0=inactive, 1=active

    @SuppressWarnings("unchecked")
    public Vector<Integer> djembeVolume = new Vector(); // 0=inactive, 1=active
    @SuppressWarnings("unchecked")
    public Vector<Integer> dunVolume = new Vector(); // 0=inactive, 1=active
    @SuppressWarnings("unchecked")
    public Vector<Integer> kenVolume = new Vector(); // 0=inactive, 1=active
    @SuppressWarnings("unchecked")
    public Vector<Integer> sagVolume = new Vector(); // 0=inactive, 1=active
    @SuppressWarnings("unchecked")
    public Vector<Integer> shekVolume = new Vector(); // 0=inactive, 1=active
    @SuppressWarnings("unchecked")
    public Vector<Integer> baletVolume = new Vector(); // 0=inactive, 1=active

    // Timeline (barRepetitions)
    public Vector<Integer[]> repetitions = new Vector(); //repetition [bar1, bar2, barN...].[barCount,countTotal,countCurrent]

    public Vector<String> barName = new Vector<String>();

    // Playback
    // 2 Channels: 16 bits left + 16 bits right = 32 bits per sample (4 multipliers)
    // 32 bits per sample @ 44100 samples per sec = 1411200 bits/sec = 176400 Bytes/sec
    // BPM to Buffer: byteBufferSizeInBytes=(60*176400)/(bpm);
    // Buffer to BPM: bpm = (60*176400) / byteBufferSizeInBytes
    //  60 bpm (buffer: 176400 Bytes) -  80 bpm (buffer: 132300 Bytes)
    // 240 bpm (buffer:  44100 Bytes) - 300 bpm (buffer: 35280 Bytes)

    AudioTrack audioTrack;
    int byteBufferSizeInBytes;
    int bpm;
    boolean onLoop;
    boolean onPlay;
    boolean flagEnsembleUpdate;
    byte[] snd_silence;
    byte[][] snd_djembe_bass;
    byte[][] snd_djembe_tone;
    byte[][] snd_djembe_slap;
    byte[][] snd_djembe_bass_flam;
    byte[][] snd_djembe_tone_flam;
    byte[][] snd_djembe_slap_flam;
    byte[] snd_dun_bass;
    byte[] snd_dun_bass_mute;
    byte[] snd_dun_bass_bell;
    byte[] snd_dun_bell;
    byte[] snd_dun_bass_bell_mute;
    byte[] snd_ken_bass;
    byte[] snd_ken_bass_mute;
    byte[] snd_ken_bass_bell;
    byte[] snd_ken_bell;
    byte[] snd_ken_bass_bell_mute;
    byte[] snd_sag_bass;
    byte[] snd_sag_bass_mute;
    byte[] snd_sag_bass_bell;
    byte[] snd_sag_bell;
    byte[] snd_sag_bass_bell_mute;
    byte[] snd_shek;
    byte[] snd_ring;

    public Ensemble(final android.content.Context activityContext) {
        // Instrument Init
        beatsPerBar = 16;
        beats = 0;
        bars = 0;

        // Playback Init
        byteBufferSizeInBytes = 35280;
        bpm = 300;
        onLoop = true;
        onPlay = false;
        this.loadSounds(activityContext);
    }

    public void clearEnsemble() {
        djembeVector.clear();
        kenVector.clear();
        sagVector.clear();
        dunVector.clear();
        shekVector.clear();
        baletVector.clear();

        djembeStatus.clear();
        kenStatus.clear();
        sagStatus.clear();
        dunStatus.clear();
        shekStatus.clear();
        baletStatus.clear();

        djembeVolume.clear(); // volume is updated on setEnsemblefromGui
        kenVolume.clear();
        sagVolume.clear();
        dunVolume.clear();
        shekVolume.clear();
        baletVolume.clear();

        beats = 0;
        bars = 0;

        barName.clear();

        //beatsPerBar = **; //beatsPerBar is updated from setVectorEmpty()
        //repetitions.clear(); //cleared in setVectorsFrom...
    }

    public void setBeats(int beatsIn) { // Only called by setVectorsFromGui()
        if ((beatsPerBar == 16) && (beatsIn % 16 == 0)) {
            beats = beatsIn;
            bars = beats / beatsPerBar; //0,1,2,
        } else if ((beatsPerBar == 12) && (beatsIn % 12 == 0)) {
            beats = beatsIn;
            bars = beats / beatsPerBar;
        } else if ((beatsPerBar == 9) && (beatsIn % 9 == 0)) {
            beats = beatsIn;
            bars = beats / beatsPerBar;
        }

        for (int i = repetitions.size(); i < bars; i++) { //Increase repetitions if needed
            Integer[] repetition = new Integer[3];
            repetition[0] = 1; //barCount
            repetition[1] = 1; //countTotal (is 1 = no repetition set)
            repetition[2] = 1; //countCurrent
            repetitions.add(repetition);
        }

        for (int i = barName.size(); i < bars; i++) { //Increase repetitions if needed
            barName.add("");
        }

    }

    public int getBeatsPerBar() {
        return beatsPerBar;
    }

    public int getBeats() {
        return beats;
    }

    public int getBars() {
        return bars;
    }

    // Playback Functions
    private void loadSounds(final android.content.Context activityContext) {
        // Init AudioTrack
        int minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
//        Log.d(TAG, "AudioTrack minBufferSize: " + minBufferSize);
//        Log.d(TAG, "AudioTrack sampleRate (Hz): " + audioTrack.getSampleRate());
//        Log.d(TAG, "AudioTrack nativeOutputSampleRate (Hz): " + AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC));
//        Log.d(TAG, "AudioTrack playbackRate (Hz): " + audioTrack.getPlaybackRate());

        // Load Wav Files
        InputStream snd_djembe_bass_stream = activityContext.getResources().openRawResource(R.raw.snd_djembe_bass);
        InputStream snd_djembe_tone_stream = activityContext.getResources().openRawResource(R.raw.snd_djembe_tone);
        InputStream snd_djembe_slap_stream = activityContext.getResources().openRawResource(R.raw.snd_djembe_slap);
        InputStream snd_djembe_bass_stream_flam = activityContext.getResources().openRawResource(R.raw.snd_djembe_bass_flam);
        InputStream snd_djembe_tone_stream_flam = activityContext.getResources().openRawResource(R.raw.snd_djembe_tone_flam);
        InputStream snd_djembe_slap_stream_flam = activityContext.getResources().openRawResource(R.raw.snd_djembe_slap_flam);
        InputStream snd_djembe2_bass_stream = activityContext.getResources().openRawResource(R.raw.snd_djembe2_bass);
        InputStream snd_djembe2_tone_stream = activityContext.getResources().openRawResource(R.raw.snd_djembe2_tone);
        InputStream snd_djembe2_slap_stream = activityContext.getResources().openRawResource(R.raw.snd_djembe2_slap);
        InputStream snd_djembe2_bass_stream_flam = activityContext.getResources().openRawResource(R.raw.snd_djembe2_bass_flam);
        InputStream snd_djembe2_tone_stream_flam = activityContext.getResources().openRawResource(R.raw.snd_djembe2_tone_flam);
        InputStream snd_djembe2_slap_stream_flam = activityContext.getResources().openRawResource(R.raw.snd_djembe2_slap_flam);
        InputStream snd_djembe3_bass_stream = activityContext.getResources().openRawResource(R.raw.snd_djembe3_bass);
        InputStream snd_djembe3_tone_stream = activityContext.getResources().openRawResource(R.raw.snd_djembe3_tone);
        InputStream snd_djembe3_slap_stream = activityContext.getResources().openRawResource(R.raw.snd_djembe3_slap);
        InputStream snd_djembe3_bass_stream_flam = activityContext.getResources().openRawResource(R.raw.snd_djembe3_bass_flam);
        InputStream snd_djembe3_tone_stream_flam = activityContext.getResources().openRawResource(R.raw.snd_djembe3_tone_flam);
        InputStream snd_djembe3_slap_stream_flam = activityContext.getResources().openRawResource(R.raw.snd_djembe3_slap_flam);
        InputStream snd_dun_bass_stream = activityContext.getResources().openRawResource(R.raw.snd_dun_bass);
        InputStream snd_dun_bass_mute_stream = activityContext.getResources().openRawResource(R.raw.snd_dun_bass_mute);
        InputStream snd_dun_bass_bell_stream = activityContext.getResources().openRawResource(R.raw.snd_dun_bass_bell);
        InputStream snd_dun_bell_stream = activityContext.getResources().openRawResource(R.raw.snd_dun_bell);
        InputStream snd_dun_bass_bell_mute_stream = activityContext.getResources().openRawResource(R.raw.snd_dun_bass_bell_mute);
        InputStream snd_ken_bass_stream = activityContext.getResources().openRawResource(R.raw.snd_ken_bass);
        InputStream snd_ken_bass_mute_stream = activityContext.getResources().openRawResource(R.raw.snd_ken_bass_mute);
        InputStream snd_ken_bass_bell_stream = activityContext.getResources().openRawResource(R.raw.snd_ken_bass_bell);
        InputStream snd_ken_bell_stream = activityContext.getResources().openRawResource(R.raw.snd_ken_bell);
        InputStream snd_ken_bass_bell_mute_stream = activityContext.getResources().openRawResource(R.raw.snd_ken_bass_bell_mute);
        InputStream snd_sag_bass_stream = activityContext.getResources().openRawResource(R.raw.snd_sag_bass);
        InputStream snd_sag_bass_mute_stream = activityContext.getResources().openRawResource(R.raw.snd_sag_bass_mute);
        InputStream snd_sag_bass_bell_stream = activityContext.getResources().openRawResource(R.raw.snd_sag_bass_bell);
        InputStream snd_sag_bell_stream = activityContext.getResources().openRawResource(R.raw.snd_sag_bell);
        InputStream snd_sag_bass_bell_mute_stream = activityContext.getResources().openRawResource(R.raw.snd_sag_bass_bell_mute);
        InputStream snd_shek_stream = activityContext.getResources().openRawResource(R.raw.snd_shek);
        InputStream snd_ring_stream = activityContext.getResources().openRawResource(R.raw.snd_ring);

        int snd_djembe_bass_dataSize = readWavHeader(snd_djembe_bass_stream);
        int snd_djembe_tone_dataSize = readWavHeader(snd_djembe_tone_stream);
        int snd_djembe_slap_dataSize = readWavHeader(snd_djembe_slap_stream);
        int snd_djembe_bass_dataSize_flam = readWavHeader(snd_djembe_bass_stream_flam);
        int snd_djembe_tone_dataSize_flam = readWavHeader(snd_djembe_tone_stream_flam);
        int snd_djembe_slap_dataSize_flam = readWavHeader(snd_djembe_slap_stream_flam);
        int snd_djembe2_bass_dataSize = readWavHeader(snd_djembe2_bass_stream);
        int snd_djembe2_tone_dataSize = readWavHeader(snd_djembe2_tone_stream);
        int snd_djembe2_slap_dataSize = readWavHeader(snd_djembe2_slap_stream);
        int snd_djembe2_bass_dataSize_flam = readWavHeader(snd_djembe2_bass_stream_flam);
        int snd_djembe2_tone_dataSize_flam = readWavHeader(snd_djembe2_tone_stream_flam);
        int snd_djembe2_slap_dataSize_flam = readWavHeader(snd_djembe2_slap_stream_flam);
        int snd_djembe3_bass_dataSize = readWavHeader(snd_djembe3_bass_stream);
        int snd_djembe3_tone_dataSize = readWavHeader(snd_djembe3_tone_stream);
        int snd_djembe3_slap_dataSize = readWavHeader(snd_djembe3_slap_stream);
        int snd_djembe3_bass_dataSize_flam = readWavHeader(snd_djembe3_bass_stream_flam);
        int snd_djembe3_tone_dataSize_flam = readWavHeader(snd_djembe3_tone_stream_flam);
        int snd_djembe3_slap_dataSize_flam = readWavHeader(snd_djembe3_slap_stream_flam);
        int snd_dun_bass_dataSize = readWavHeader(snd_dun_bass_stream);
        int snd_dun_bass_mute_dataSize = readWavHeader(snd_dun_bass_mute_stream);
        int snd_dun_bass_bell_dataSize = readWavHeader(snd_dun_bass_bell_stream);
        int snd_dun_bell_dataSize = readWavHeader(snd_dun_bell_stream);
        int snd_dun_bass_bell_mute_dataSize = readWavHeader(snd_dun_bass_bell_mute_stream);
        int snd_ken_bass_dataSize = readWavHeader(snd_ken_bass_stream);
        int snd_ken_bass_mute_dataSize = readWavHeader(snd_ken_bass_mute_stream);
        int snd_ken_bass_bell_dataSize = readWavHeader(snd_ken_bass_bell_stream);
        int snd_ken_bell_dataSize = readWavHeader(snd_ken_bell_stream);
        int snd_ken_bass_bell_mute_dataSize = readWavHeader(snd_ken_bass_bell_mute_stream);
        int snd_sag_bass_dataSize = readWavHeader(snd_sag_bass_stream);
        int snd_sag_bass_mute_dataSize = readWavHeader(snd_sag_bass_mute_stream);
        int snd_sag_bass_bell_dataSize = readWavHeader(snd_sag_bass_bell_stream);
        int snd_sag_bell_dataSize = readWavHeader(snd_sag_bell_stream);
        int snd_sag_bass_bell_mute_dataSize = readWavHeader(snd_sag_bass_bell_mute_stream);
        int snd_shek_dataSize = readWavHeader(snd_shek_stream);
        int snd_ring_dataSize = readWavHeader(snd_ring_stream);

        snd_djembe_bass = new byte[3][];
        snd_djembe_tone = new byte[3][];
        snd_djembe_slap = new byte[3][];
        snd_djembe_bass_flam = new byte[3][];
        snd_djembe_tone_flam = new byte[3][];
        snd_djembe_slap_flam = new byte[3][];

        snd_djembe_bass[0] = new byte[snd_djembe_bass_dataSize];
        snd_djembe_tone[0] = new byte[snd_djembe_tone_dataSize];
        snd_djembe_slap[0] = new byte[snd_djembe_slap_dataSize];
        snd_djembe_bass_flam[0] = new byte[snd_djembe_bass_dataSize_flam];
        snd_djembe_tone_flam[0] = new byte[snd_djembe_tone_dataSize_flam];
        snd_djembe_slap_flam[0] = new byte[snd_djembe_slap_dataSize_flam];
        snd_djembe_bass[1] = new byte[snd_djembe2_bass_dataSize];
        snd_djembe_tone[1] = new byte[snd_djembe2_tone_dataSize];
        snd_djembe_slap[1] = new byte[snd_djembe2_slap_dataSize];
        snd_djembe_bass_flam[1] = new byte[snd_djembe2_bass_dataSize_flam];
        snd_djembe_tone_flam[1] = new byte[snd_djembe2_tone_dataSize_flam];
        snd_djembe_slap_flam[1] = new byte[snd_djembe2_slap_dataSize_flam];
        snd_djembe_bass[2] = new byte[snd_djembe3_bass_dataSize];
        snd_djembe_tone[2] = new byte[snd_djembe3_tone_dataSize];
        snd_djembe_slap[2] = new byte[snd_djembe3_slap_dataSize];
        snd_djembe_bass_flam[2] = new byte[snd_djembe3_bass_dataSize_flam];
        snd_djembe_tone_flam[2] = new byte[snd_djembe3_tone_dataSize_flam];
        snd_djembe_slap_flam[2] = new byte[snd_djembe3_slap_dataSize_flam];
        snd_dun_bass = new byte[snd_dun_bass_dataSize];
        snd_dun_bass_mute = new byte[snd_dun_bass_mute_dataSize];
        snd_dun_bass_bell = new byte[snd_dun_bass_bell_dataSize];
        snd_dun_bell = new byte[snd_dun_bell_dataSize];
        snd_dun_bass_bell_mute = new byte[snd_dun_bass_bell_mute_dataSize];
        snd_ken_bass = new byte[snd_ken_bass_dataSize];
        snd_ken_bass_mute = new byte[snd_ken_bass_mute_dataSize];
        snd_ken_bass_bell = new byte[snd_ken_bass_bell_dataSize];
        snd_ken_bell = new byte[snd_ken_bell_dataSize];
        snd_ken_bass_bell_mute = new byte[snd_ken_bass_bell_mute_dataSize];
        snd_sag_bass = new byte[snd_sag_bass_dataSize];
        snd_sag_bass_mute = new byte[snd_sag_bass_mute_dataSize];
        snd_sag_bass_bell = new byte[snd_sag_bass_bell_dataSize];
        snd_sag_bell = new byte[snd_sag_bell_dataSize];
        snd_sag_bass_bell_mute = new byte[snd_sag_bass_bell_mute_dataSize];
        snd_shek = new byte[snd_shek_dataSize];
        snd_ring = new byte[snd_ring_dataSize];
        try {
            boolean err=false;

            if (snd_djembe_bass_stream_flam.read(snd_djembe_bass_flam[0], 0, snd_djembe_bass_flam[0].length) != snd_djembe_bass_flam[0].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            if (snd_djembe_tone_stream_flam.read(snd_djembe_tone_flam[0], 0, snd_djembe_tone_flam[0].length) != snd_djembe_tone_flam[0].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            if (snd_djembe_slap_stream_flam.read(snd_djembe_slap_flam[0], 0, snd_djembe_slap_flam[0].length) != snd_djembe_slap_flam[0].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");

            if (snd_djembe_bass_stream.read(snd_djembe_bass[0], 0, snd_djembe_bass[0].length) != snd_djembe_bass[0].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Log.v(TAG, "WavReader snd_djembe_bass Length: " + snd_djembe_bass.length + " Bytes (" + snd_djembe_bass.length * 1000 / 176400 + " ms)");
            if (snd_djembe_tone_stream.read(snd_djembe_tone[0], 0, snd_djembe_tone[0].length) != snd_djembe_tone[0].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_djembe_tone Length: " + snd_djembe_tone.length + " Bytes (" + snd_djembe_tone.length * 1000 / 176400 + " ms)");
            if (snd_djembe_slap_stream.read(snd_djembe_slap[0], 0, snd_djembe_slap[0].length) != snd_djembe_slap[0].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_djembe_slap Length: " + snd_djembe_slap.length + " Bytes (" + snd_djembe_slap.length * 1000 / 176400 + " ms)");

            if (snd_djembe2_bass_stream_flam.read(snd_djembe_bass_flam[1], 0, snd_djembe_bass_flam[1].length) != snd_djembe_bass_flam[1].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            if (snd_djembe2_tone_stream_flam.read(snd_djembe_tone_flam[1], 0, snd_djembe_tone_flam[1].length) != snd_djembe_tone_flam[1].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            if (snd_djembe2_slap_stream_flam.read(snd_djembe_slap_flam[1], 0, snd_djembe_slap_flam[1].length) != snd_djembe_slap_flam[1].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");

            if (snd_djembe2_bass_stream.read(snd_djembe_bass[1], 0, snd_djembe_bass[1].length) != snd_djembe_bass[1].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Log.v(TAG, "WavReader snd_djembe_bass Length: " + snd_djembe_bass.length + " Bytes (" + snd_djembe_bass.length * 1000 / 176400 + " ms)");
            if (snd_djembe2_tone_stream.read(snd_djembe_tone[1], 0, snd_djembe_tone[1].length) != snd_djembe_tone[1].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_djembe_tone Length: " + snd_djembe_tone.length + " Bytes (" + snd_djembe_tone.length * 1000 / 176400 + " ms)");
            if (snd_djembe2_slap_stream.read(snd_djembe_slap[1], 0, snd_djembe_slap[1].length) != snd_djembe_slap[1].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_djembe_slap Length: " + snd_djembe_slap.length + " Bytes (" + snd_djembe_slap.length * 1000 / 176400 + " ms)");

            if (snd_djembe3_bass_stream_flam.read(snd_djembe_bass_flam[2], 0, snd_djembe_bass_flam[2].length) != snd_djembe_bass_flam[2].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            if (snd_djembe3_tone_stream_flam.read(snd_djembe_tone_flam[2], 0, snd_djembe_tone_flam[2].length) != snd_djembe_tone_flam[2].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            if (snd_djembe3_slap_stream_flam.read(snd_djembe_slap_flam[2], 0, snd_djembe_slap_flam[2].length) != snd_djembe_slap_flam[2].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");

            if (snd_djembe3_bass_stream.read(snd_djembe_bass[2], 0, snd_djembe_bass[2].length) != snd_djembe_bass[2].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Log.v(TAG, "WavReader snd_djembe_bass Length: " + snd_djembe_bass.length + " Bytes (" + snd_djembe_bass.length * 1000 / 176400 + " ms)");
            if (snd_djembe3_tone_stream.read(snd_djembe_tone[2], 0, snd_djembe_tone[2].length) != snd_djembe_tone[2].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_djembe_tone Length: " + snd_djembe_tone.length + " Bytes (" + snd_djembe_tone.length * 1000 / 176400 + " ms)");
            if (snd_djembe3_slap_stream.read(snd_djembe_slap[2], 0, snd_djembe_slap[2].length) != snd_djembe_slap[2].length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_djembe_slap Length: " + snd_djembe_slap.length + " Bytes (" + snd_djembe_slap.length * 1000 / 176400 + " ms)");

            if (snd_dun_bass_stream.read(snd_dun_bass, 0, snd_dun_bass.length) != snd_dun_bass.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            if (snd_dun_bass_mute_stream.read(snd_dun_bass_mute, 0, snd_dun_bass_mute.length) != snd_dun_bass_mute.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            if (snd_dun_bass_bell_stream.read(snd_dun_bass_bell, 0, snd_dun_bass_bell.length) != snd_dun_bass_bell.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_dun_bass_bell Length: " + snd_dun_bass_bell.length + " Bytes (" + snd_dun_bass_bell.length * 1000 / 176400 + " ms)");
            if (snd_dun_bell_stream.read(snd_dun_bell, 0, snd_dun_bell.length) != snd_dun_bell.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_dun_bell Length: " + snd_dun_bell.length + " Bytes (" + snd_dun_bell.length * 1000 / 176400 + " ms)");
            if (snd_dun_bass_bell_mute_stream.read(snd_dun_bass_bell_mute, 0, snd_dun_bass_bell_mute.length) != snd_dun_bass_bell_mute.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_dun_bass_mute_bell Length: " + snd_dun_bass_bell_mute.length + " Bytes (" + snd_dun_bass_bell_mute.length * 1000 / 176400 + " ms)");

            if (snd_ken_bass_stream.read(snd_ken_bass, 0, snd_ken_bass.length) != snd_ken_bass.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            if (snd_ken_bass_mute_stream.read(snd_ken_bass_mute, 0, snd_ken_bass_mute.length) != snd_ken_bass_mute.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            if (snd_ken_bass_bell_stream.read(snd_ken_bass_bell, 0, snd_ken_bass_bell.length) != snd_ken_bass_bell.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_ken_bass_bell Length: " + snd_ken_bass_bell.length + " Bytes (" + snd_ken_bass_bell.length * 1000 / 176400 + " ms)");
            if (snd_ken_bell_stream.read(snd_ken_bell, 0, snd_ken_bell.length) != snd_ken_bell.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_ken_bell Length: " + snd_ken_bell.length + " Bytes (" + snd_ken_bell.length * 1000 / 176400 + " ms)");
            if (snd_ken_bass_bell_mute_stream.read(snd_ken_bass_bell_mute, 0, snd_ken_bass_bell_mute.length) != snd_ken_bass_bell_mute.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_ken_bass_mute_bell Length: " + snd_ken_bass_bell_mute.length + " Bytes (" + snd_ken_bass_bell_mute.length * 1000 / 176400 + " ms)");

            if (snd_sag_bass_stream.read(snd_sag_bass, 0, snd_sag_bass.length) != snd_sag_bass.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            if (snd_sag_bass_mute_stream.read(snd_sag_bass_mute, 0, snd_sag_bass_mute.length) != snd_sag_bass_mute.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            if (snd_sag_bass_bell_stream.read(snd_sag_bass_bell, 0, snd_sag_bass_bell.length) != snd_sag_bass_bell.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_sag_bass_bell Length: " + snd_sag_bass_bell.length + " Bytes (" + snd_sag_bass_bell.length * 1000 / 176400 + " ms)");
            if (snd_sag_bell_stream.read(snd_sag_bell, 0, snd_sag_bell.length) != snd_sag_bell.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_sag_bell Length: " + snd_sag_bell.length + " Bytes (" + snd_sag_bell.length * 1000 / 176400 + " ms)");
            if (snd_sag_bass_bell_mute_stream.read(snd_sag_bass_bell_mute, 0, snd_sag_bass_bell_mute.length) != snd_sag_bass_bell_mute.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_sag_bass_mute_bell Length: " + snd_sag_bass_bell_mute.length + " Bytes (" + snd_sag_bass_bell_mute.length * 1000 / 176400 + " ms)");

            if (snd_shek_stream.read(snd_shek, 0, snd_shek.length) != snd_shek.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            if (snd_ring_stream.read(snd_ring, 0, snd_ring.length) != snd_ring.length)
                err=true; //Log.e(TAG, "Not enough bytes read from snd file!");
            //Logv(TAG, "WavReader snd_shek Length: " + snd_shek.length + " Bytes (" + snd_shek.length * 1000 / 176400 + " ms)");

            snd_djembe_bass_stream.close();
            snd_djembe_tone_stream.close();
            snd_djembe_slap_stream.close();
            snd_djembe_bass_stream_flam.close();
            snd_djembe_tone_stream_flam.close();
            snd_djembe_slap_stream_flam.close();
            snd_djembe2_bass_stream.close();
            snd_djembe2_tone_stream.close();
            snd_djembe2_slap_stream.close();
            snd_djembe2_bass_stream_flam.close();
            snd_djembe2_tone_stream_flam.close();
            snd_djembe2_slap_stream_flam.close();
            snd_djembe3_bass_stream.close();
            snd_djembe3_tone_stream.close();
            snd_djembe3_slap_stream.close();
            snd_djembe3_bass_stream_flam.close();
            snd_djembe3_tone_stream_flam.close();
            snd_djembe3_slap_stream_flam.close();
            snd_dun_bass_stream.close();
            snd_dun_bass_mute_stream.close();
            snd_dun_bass_bell_stream.close();
            snd_dun_bell_stream.close();
            snd_dun_bass_bell_mute_stream.close();
            snd_ken_bass_stream.close();
            snd_ken_bass_mute_stream.close();
            snd_ken_bass_bell_stream.close();
            snd_ken_bell_stream.close();
            snd_ken_bass_bell_mute_stream.close();
            snd_sag_bass_stream.close();
            snd_sag_bass_mute_stream.close();
            snd_sag_bass_bell_stream.close();
            snd_sag_bell_stream.close();
            snd_sag_bass_bell_mute_stream.close();
            snd_shek_stream.close();
            snd_ring_stream.close();

        } catch (IOException ie) {
            ie.printStackTrace();
        }
        // Init silence
        snd_silence = new byte[176400]; // 500ms -> @binary = 2 sec
        for (int i = 0; i < 176400; i++)
            snd_silence[i] = 0;
    }

    private int readWavHeader(InputStream wavStream) {     // Read WAV Header
        final int HEADER_SIZE = 44;
        int dataSize = 0;
        try {
            boolean err=false;
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            if (wavStream.read(buffer.array(), buffer.arrayOffset(), buffer.capacity()) != buffer.capacity())
                err=true; //Log.e(TAG, "Not enough bytes read from snd file header!");
            buffer.rewind();
            buffer.position(buffer.position() + 20);
            int format = buffer.getShort();
            //Logd(TAG, "WavReader encoding (1 expected): " + format); // 1 = Linear PCM
            int channels = buffer.getShort();
            //Logd(TAG, "WavReader channels (2 expected): " + channels);
            int rate = buffer.getInt();
            //Logd(TAG, "WavReader rate (44100 expected): " + rate); //44100
            buffer.position(buffer.position() + 6);
            int bits = buffer.getShort();
            //Logd(TAG, "WavReader bits (16 expected): " + bits); // 16 bits
            while (buffer.getInt() != 0x61746164) { // "data" marker
                //Logd(TAG, "WavReader Skipping non-data chunk");
                int size = buffer.getInt();
                if (wavStream.skip(size) != size)
                    err=true;//Log.e(TAG, "Not enough bytes skip from snd file header!");
                buffer.rewind();
                if (wavStream.read(buffer.array(), buffer.arrayOffset(), 8) != 8)
                    err=true;//Log.e(TAG, "Not enough bytes read from snd file header!");
                buffer.rewind();
            }
            dataSize = buffer.getInt();
            //Logd(TAG, "WavReader dataSize (0+ expected): " + dataSize + " Bytes"); // 16 bits
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        return dataSize; //  total number of bytes in the chunk data
    }

    public String saveVectorsInJSON() {
        try { //http://www.javacodegeeks.com/2013/10/android-json-tutorial-create-and-parse-json-data.html

            JSONObject jsonEnsemble = new JSONObject();

            jsonEnsemble.put("ensembleName", ensembleName); // Ensemble Name
            jsonEnsemble.put("ensembleAuthor", ensembleAuthor);
            jsonEnsemble.put("beatsPerBar", beatsPerBar); // beatsPerBar (16,12,9)
            jsonEnsemble.put("tempoInBPM", this.bpm); // tempo
            jsonEnsemble.put("beats", this.getBeats());

            // Shek Vector
            JSONArray jsonShekVector = new JSONArray();
            JSONArray jsonShekStatus = new JSONArray();
            JSONArray jsonShekVolume = new JSONArray();
            for (int i = 0; i < shekVector.size(); i++) {
                JSONObject jsonShekStatusN = new JSONObject();
                jsonShekStatusN.put("shekStatus"+String.valueOf(i), shekStatus.elementAt(i));
                jsonShekStatus.put(jsonShekStatusN);

                JSONObject jsonShekVolumeN = new JSONObject();
                jsonShekVolumeN.put("shekVolume"+String.valueOf(i), shekVolume.elementAt(i));
                jsonShekVolume.put(jsonShekVolumeN);

                JSONArray jsonShekVectorN = new JSONArray();
                for (int j = 0; j < getBeats(); j++) {
                    JSONObject thisBar = new JSONObject();
                    thisBar.put("bar" + String.valueOf(j), shekVector.elementAt(i).elementAt(j));
                    jsonShekVectorN.put(thisBar);
                }
                jsonShekVector.put(jsonShekVectorN);
            }
            jsonEnsemble.put("shekVector", jsonShekVector);
            jsonEnsemble.put("shekStatus", jsonShekStatus);
            jsonEnsemble.put("shekVolume", jsonShekVolume);

            // Balet Vector
            JSONArray jsonBaletVector = new JSONArray();
            JSONArray jsonBaletStatus = new JSONArray();
            JSONArray jsonBaletVolume = new JSONArray();
            for (int i = 0; i < baletVector.size(); i++) {
                JSONObject jsonBaletStatusN = new JSONObject();
                jsonBaletStatusN.put("baletStatus"+String.valueOf(i), baletStatus.elementAt(i));
                jsonBaletStatus.put(jsonBaletStatusN);

                JSONObject jsonBaletVolumeN = new JSONObject();
                jsonBaletVolumeN.put("baletVolume"+String.valueOf(i), baletVolume.elementAt(i));
                jsonBaletVolume.put(jsonBaletVolumeN);

                JSONArray jsonBaletVectorN = new JSONArray();
                for (int j = 0; j < getBeats(); j++) {
                    JSONObject thisBar = new JSONObject();
                    thisBar.put("bar" + String.valueOf(j), baletVector.elementAt(i).elementAt(j));
                    jsonBaletVectorN.put(thisBar);
                }
                jsonBaletVector.put(jsonBaletVectorN);
            }
            jsonEnsemble.put("baletVector", jsonBaletVector);
            jsonEnsemble.put("baletStatus", jsonBaletStatus);
            jsonEnsemble.put("baletVolume", jsonBaletVolume);

            // Djembe Vector
            JSONArray jsonDjembeVector = new JSONArray();
            JSONArray jsonDjembeStatus = new JSONArray();
            JSONArray jsonDjembeVolume = new JSONArray();
            for (int i = 0; i < djembeVector.size(); i++) {

                JSONObject jsonDjembeStatusN = new JSONObject();
                jsonDjembeStatusN.put("djembeStatus"+String.valueOf(i), djembeStatus.elementAt(i));
                jsonDjembeStatus.put(jsonDjembeStatusN);

                JSONObject jsonDjembeVolumeN = new JSONObject();
                jsonDjembeVolumeN.put("djembeVolume"+String.valueOf(i), djembeVolume.elementAt(i));
                jsonDjembeVolume.put(jsonDjembeVolumeN);

                JSONArray jsonDjembeVectorN = new JSONArray();
                for (int j = 0; j < getBeats(); j++) {
                    JSONObject thisBar = new JSONObject();
                    thisBar.put("bar" + String.valueOf(j), djembeVector.elementAt(i).elementAt(j));
                    jsonDjembeVectorN.put(thisBar);
                }
                jsonDjembeVector.put(jsonDjembeVectorN);
            }
            jsonEnsemble.put("djembeVector", jsonDjembeVector);
            jsonEnsemble.put("djembeStatus", jsonDjembeStatus);
            jsonEnsemble.put("djembeVolume", jsonDjembeVolume);

            // Dun Vector
            JSONArray jsonDunVector = new JSONArray();
            JSONArray jsonDunStatus = new JSONArray();
            JSONArray jsonDunVolume = new JSONArray();
            for (int i = 0; i < dunVector.size(); i++) {

                JSONObject jsonDunStatusN = new JSONObject();
                jsonDunStatusN.put("dunStatus"+String.valueOf(i), dunStatus.elementAt(i));
                jsonDunStatus.put(jsonDunStatusN);

                JSONObject jsonDunVolumeN = new JSONObject();
                jsonDunVolumeN.put("dunVolume"+String.valueOf(i), dunVolume.elementAt(i));
                jsonDunVolume.put(jsonDunVolumeN);

                JSONArray jsonDunVectorN = new JSONArray();
                for (int j = 0; j < getBeats(); j++) {
                    JSONObject thisBar = new JSONObject();
                    thisBar.put("bar" + String.valueOf(j), dunVector.elementAt(i).elementAt(j));
                    jsonDunVectorN.put(thisBar);
                }
                jsonDunVector.put(jsonDunVectorN);
            }
            jsonEnsemble.put("dunVector", jsonDunVector);
            jsonEnsemble.put("dunStatus", jsonDunStatus);
            jsonEnsemble.put("dunVolume", jsonDunVolume);

            // Sag Vector
            JSONArray jsonSagVector = new JSONArray();
            JSONArray jsonSagStatus = new JSONArray();
            JSONArray jsonSagVolume = new JSONArray();
            for (int i = 0; i < sagVector.size(); i++) {

                JSONObject jsonSagStatusN = new JSONObject();
                jsonSagStatusN.put("sagStatus"+String.valueOf(i), sagStatus.elementAt(i));
                jsonSagStatus.put(jsonSagStatusN);

                JSONObject jsonSagVolumeN = new JSONObject();
                jsonSagVolumeN.put("sagVolume"+String.valueOf(i), sagVolume.elementAt(i));
                jsonSagVolume.put(jsonSagVolumeN);

                JSONArray jsonSagVectorN = new JSONArray();
                for (int j = 0; j < getBeats(); j++) {
                    JSONObject thisBar = new JSONObject();
                    thisBar.put("bar" + String.valueOf(j), sagVector.elementAt(i).elementAt(j));
                    jsonSagVectorN.put(thisBar);
                }
                jsonSagVector.put(jsonSagVectorN);
            }
            jsonEnsemble.put("sagVector", jsonSagVector);
            jsonEnsemble.put("sagStatus", jsonSagStatus);
            jsonEnsemble.put("sagVolume", jsonSagVolume);

            // Ken Vector
            JSONArray jsonKenVector = new JSONArray();
            JSONArray jsonKenStatus = new JSONArray();
            JSONArray jsonKenVolume = new JSONArray();
            for (int i = 0; i < kenVector.size(); i++) {

                JSONObject jsonKenStatusN = new JSONObject();
                jsonKenStatusN.put("kenStatus"+String.valueOf(i), kenStatus.elementAt(i));
                jsonKenStatus.put(jsonKenStatusN);

                JSONObject jsonKenVolumeN = new JSONObject();
                jsonKenVolumeN.put("kenVolume"+String.valueOf(i), kenVolume.elementAt(i));
                jsonKenVolume.put(jsonKenVolumeN);

                JSONArray jsonKenVectorN = new JSONArray();
                for (int j = 0; j < getBeats(); j++) {
                    JSONObject thisBar = new JSONObject();
                    thisBar.put("bar" + String.valueOf(j), kenVector.elementAt(i).elementAt(j));
                    jsonKenVectorN.put(thisBar);
                }
                jsonKenVector.put(jsonKenVectorN);
            }
            jsonEnsemble.put("kenVector", jsonKenVector);
            jsonEnsemble.put("kenStatus", jsonKenStatus);
            jsonEnsemble.put("kenVolume", jsonKenVolume);

            // Repetitions
            JSONArray jsonRepetitions = new JSONArray();
            for (int i = 0; i < repetitions.size(); i++) {
                JSONObject jsonRepetitionN = new JSONObject();
                jsonRepetitionN.put("barCount", repetitions.elementAt(i)[0]);
                jsonRepetitionN.put("countTotal", repetitions.elementAt(i)[1]); //countCurrent not necesary
                jsonRepetitions.put(jsonRepetitionN);
            }
            jsonEnsemble.put("repetitions", jsonRepetitions);

            // BarNames
            JSONArray jsonBarName = new JSONArray();
            for (int i = 0; i < barName.size(); i++) {
                JSONObject jsonBarNameN = new JSONObject();
                jsonBarNameN.put("barName"+i, barName.elementAt(i));
                jsonBarName.put(jsonBarNameN);
            }
            jsonEnsemble.put("barName", jsonBarName);

            //Log.e(TAG, "Saved: " + jsonEnsemble.toString());

            return jsonEnsemble.toString();

        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public void setVectorsFromJSON(String jsonString) {

        clearEnsemble();
        repetitions.clear();
        onLoop = true;
        onPlay = false;

        //Log.e(TAG, "Loaded: " + jsonString);

        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            // Ensemble Name
            this.ensembleName = jsonObject.getString("ensembleName");
            this.ensembleAuthor = jsonObject.getString("ensembleAuthor");
            this.beatsPerBar = jsonObject.getInt("beatsPerBar");
            this.bpm = jsonObject.getInt("tempoInBPM");
            this.byteBufferSizeInBytes = (60 * 176400) / bpm;
            // check bpm are multiple of 4 and set byteBufferSize
            if ((byteBufferSizeInBytes % 4) != 0)
                byteBufferSizeInBytes = byteBufferSizeInBytes + 4 - byteBufferSizeInBytes % 4;

            // set repetitions and barName vectors
            this.setBeats(jsonObject.getInt("beats"));

//            // Balet
            if(jsonObject.has("baletVector")) { //older versions do not have baletVector
                JSONArray jsonBaletVector = jsonObject.getJSONArray("baletVector");
                JSONArray jsonBaletStatus = jsonObject.getJSONArray("baletStatus");
                JSONArray jsonBaletVolume = jsonObject.getJSONArray("baletVolume");
                for (int i = 0; i < jsonBaletVector.length(); i++) {
                    try {
                        JSONObject jsonBaletStatusN = jsonBaletStatus.getJSONObject(i);
                        this.baletStatus.add(jsonBaletStatusN.getInt("baletStatus" + i));

                        JSONObject jsonBaletVolumeN = jsonBaletVolume.getJSONObject(i);
                        this.baletVolume.add(jsonBaletVolumeN.getInt("baletVolume" + i));

                        JSONArray jsonBaletVectorN = jsonBaletVector.getJSONArray(i);
                        Vector<Integer> balet = new Vector();
                        for (int j = 0; j < jsonBaletVectorN.length(); j++) {
                            JSONObject thisBar = jsonBaletVectorN.getJSONObject(j);
                            balet.add(thisBar.getInt("bar" + j));
                            //Log.e(TAG, "test: " + thisBar.getInt("bar" + j));
                        }
                        this.baletVector.add(balet);
                    } catch (JSONException e) {
                        // Oops
                    }
                }
            }

            // Shekere
            JSONArray jsonShekVector = jsonObject.getJSONArray("shekVector");
            JSONArray jsonShekStatus = jsonObject.getJSONArray("shekStatus");
            JSONArray jsonShekVolume = jsonObject.getJSONArray("shekVolume");
            for (int i = 0; i < jsonShekVector.length(); i++) {
                try {
                    JSONObject jsonSheckStatusN = jsonShekStatus.getJSONObject(i);
                    this.shekStatus.add(jsonSheckStatusN.getInt("shekStatus"+i));

                    JSONObject jsonSheckVolumeN = jsonShekVolume.getJSONObject(i);
                    this.shekVolume.add(jsonSheckVolumeN.getInt("shekVolume"+i));

                    JSONArray jsonShekVectorN = jsonShekVector.getJSONArray(i);
                    Vector<Integer> shek = new Vector();
                    for (int j = 0; j < jsonShekVectorN.length(); j++) {
                        JSONObject thisBar = jsonShekVectorN.getJSONObject(j);
                        shek.add(thisBar.getInt("bar" + j));
                        //Log.e(TAG, "test: " + thisBar.getInt("bar" + j));
                    }
                    this.shekVector.add(shek);
                } catch (JSONException e) {
                    // Oops
                }
            }

            // Djembe
            JSONArray jsonDjembeVector = jsonObject.getJSONArray("djembeVector");
            JSONArray jsonDjembeStatus = jsonObject.getJSONArray("djembeStatus");
            JSONArray jsonDjembeVolume = jsonObject.getJSONArray("djembeVolume");
            for (int i = 0; i < jsonDjembeVector.length(); i++) {
                try {

                    JSONObject jsonSheckStatusN = jsonDjembeStatus.getJSONObject(i);
                    this.djembeStatus.add(jsonSheckStatusN.getInt("djembeStatus"+i));

                    JSONObject jsonSheckVolumeN = jsonDjembeVolume.getJSONObject(i);
                    this.djembeVolume.add(jsonSheckVolumeN.getInt("djembeVolume"+i));

                    JSONArray jsonDjembeVectorN = jsonDjembeVector.getJSONArray(i);
                    Vector<Integer> djembe = new Vector();
                    for (int j = 0; j < jsonDjembeVectorN.length(); j++) {
                        JSONObject thisBar = jsonDjembeVectorN.getJSONObject(j);
                        djembe.add(thisBar.getInt("bar" + j));
                    }
                    this.djembeVector.add(djembe);
                } catch (JSONException e) {
                }
            }

            // Dun
            JSONArray jsonDunVector = jsonObject.getJSONArray("dunVector");
            JSONArray jsonDunStatus = jsonObject.getJSONArray("dunStatus");
            JSONArray jsonDunVolume = jsonObject.getJSONArray("dunVolume");
            for (int i = 0; i < jsonDunVector.length(); i++) {
                try {
                    JSONObject jsonSheckStatusN = jsonDunStatus.getJSONObject(i);
                    this.dunStatus.add(jsonSheckStatusN.getInt("dunStatus"+i));

                    JSONObject jsonSheckVolumeN = jsonDunVolume.getJSONObject(i);
                    this.dunVolume.add(jsonSheckVolumeN.getInt("dunVolume"+i));

                    JSONArray jsonDunVectorN = jsonDunVector.getJSONArray(i);
                    Vector<Integer> dun = new Vector();
                    for (int j = 0; j < jsonDunVectorN.length(); j++) {
                        JSONObject thisBar = jsonDunVectorN.getJSONObject(j);
                        dun.add(thisBar.getInt("bar" + j));
                    }
                    this.dunVector.add(dun);
                } catch (JSONException e) {
                }
            }

            // Sag
            JSONArray jsonSagVector = jsonObject.getJSONArray("sagVector");
            JSONArray jsonSagStatus = jsonObject.getJSONArray("sagStatus");
            JSONArray jsonSagVolume = jsonObject.getJSONArray("sagVolume");
            for (int i = 0; i < jsonSagVector.length(); i++) {
                try {

                    JSONObject jsonSheckStatusN = jsonSagStatus.getJSONObject(i);
                    this.sagStatus.add(jsonSheckStatusN.getInt("sagStatus"+i));

                    JSONObject jsonSheckVolumeN = jsonSagVolume.getJSONObject(i);
                    this.sagVolume.add(jsonSheckVolumeN.getInt("sagVolume"+i));

                    JSONArray jsonSagVectorN = jsonSagVector.getJSONArray(i);
                    Vector<Integer> sag = new Vector();
                    for (int j = 0; j < jsonSagVectorN.length(); j++) {
                        JSONObject thisBar = jsonSagVectorN.getJSONObject(j);
                        sag.add(thisBar.getInt("bar" + j));
                    }
                    this.sagVector.add(sag);
                } catch (JSONException e) {
                }
            }

            // Ken
            JSONArray jsonKenVector = jsonObject.getJSONArray("kenVector");
            JSONArray jsonKenStatus = jsonObject.getJSONArray("kenStatus");
            JSONArray jsonKenVolume = jsonObject.getJSONArray("kenVolume");
            for (int i = 0; i < jsonKenVector.length(); i++) {
                try {

                    JSONObject jsonSheckStatusN = jsonKenStatus.getJSONObject(i);
                    this.kenStatus.add(jsonSheckStatusN.getInt("kenStatus"+i));

                    JSONObject jsonSheckVolumeN = jsonKenVolume.getJSONObject(i);
                    this.kenVolume.add(jsonSheckVolumeN.getInt("kenVolume"+i));

                    JSONArray jsonKenVectorN = jsonKenVector.getJSONArray(i);
                    Vector<Integer> ken = new Vector();
                    for (int j = 0; j < jsonKenVectorN.length(); j++) {
                        JSONObject thisBar = jsonKenVectorN.getJSONObject(j);
                        ken.add(thisBar.getInt("bar" + j));
                    }
                    this.kenVector.add(ken);
                } catch (JSONException e) {
                }
            }

            // Repetitions
            JSONArray jsonRepetitions = jsonObject.getJSONArray("repetitions");
            for (int i = 0; i < jsonRepetitions.length(); i++) {
                try {
                    JSONObject jsonRepetitionN = jsonRepetitions.getJSONObject(i);
                        repetitions.elementAt(i)[0] = jsonRepetitionN.getInt("barCount");
                        repetitions.elementAt(i)[1] = jsonRepetitionN.getInt("countTotal");
                        repetitions.elementAt(i)[2] = jsonRepetitionN.getInt("countTotal");
                } catch (JSONException e) {
                }
            }

            // BarNames
            JSONArray jsonBarName = jsonObject.getJSONArray("barName");
            for (int i = 0; i < barName.size(); i++) {
                JSONObject jsonBarNameN = jsonBarName.getJSONObject(i);
                barName.setElementAt(jsonBarNameN.getString("barName" + i),i);
            }

        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void setVectorsFromEmpty(int newBeatsPerBar, int newBarNum, boolean emptyInstruments, final android.content.Context activityContext) { //beatsPerBar; //16, 12, 9

        if ((newBeatsPerBar == 16) || (newBeatsPerBar == 12) || (newBeatsPerBar == 9)) {
            this.clearEnsemble();
            repetitions.clear();
            beatsPerBar = newBeatsPerBar;
            this.setBeats(newBarNum * beatsPerBar);
        } //else
            //Log.e(TAG, "Incorrect beatsPerBar" + newBeatsPerBar + "in setVectorEmpty");

        // Playback Init
        byteBufferSizeInBytes = 35280;
        bpm = 300;
        onLoop = true;
        onPlay = false;

        this.ensembleName = activityContext.getResources().getString(R.string.unnamed);
        this.ensembleAuthor = activityContext.getResources().getString(R.string.you);

        if (!emptyInstruments) {
            @SuppressWarnings("unchecked")
            Vector<Integer> djembe1 = new Vector();
            for (int i = 0; i < beats + 4; i++)
                djembe1.add(0);
            this.djembeVector.add(djembe1);
            this.djembeStatus.add(1);
            this.djembeVolume.add(100);
            @SuppressWarnings("unchecked")
            Vector<Integer> dundun1 = new Vector();
            for (int i = 0; i < beats + 4; i++)
                dundun1.add(0);
            this.dunVector.add(dundun1);
            this.dunStatus.add(1);
            this.dunVolume.add(100);
            @SuppressWarnings("unchecked")
            Vector<Integer> sagbang1 = new Vector();
            for (int i = 0; i < beats + 4; i++)
                sagbang1.add(0);
            this.sagVector.add(sagbang1);
            this.sagStatus.add(1);
            this.sagVolume.add(100);
            @SuppressWarnings("unchecked")
            Vector<Integer> kenkeni1 = new Vector();
            for (int i = 0; i < beats + 4; i++)
                kenkeni1.add(0);
            this.kenVector.add(kenkeni1);
            this.kenStatus.add(1);
            this.kenVolume.add(100);
            @SuppressWarnings("unchecked")
            Vector<Integer> shekere1 = new Vector();
            for (int i = 0; i < beats + 4; i++)
                shekere1.add(0);
            this.shekVector.add(shekere1);
            this.shekStatus.add(1);
            this.shekVolume.add(100);
            @SuppressWarnings("unchecked")
            Vector<Integer> balet1 = new Vector();
            for (int i = 0; i < beats + 4; i++)
                balet1.add(0);
            this.baletVector.add(balet1);
            this.baletStatus.add(1);
            this.baletVolume.add(100);
        }
    }

    public void setVectorsFromRaw(int resourceId, final android.content.Context activityContext) {
        InputStream inputStream = activityContext.getResources().openRawResource(resourceId);
        java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
        String jsonString = s.hasNext() ? s.next() : "";
        this.setVectorsFromJSON(jsonString);
    }

}
