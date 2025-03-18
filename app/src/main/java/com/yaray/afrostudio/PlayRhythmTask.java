package com.yaray.afrostudio;

import android.os.AsyncTask;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import com.yaray.afrostudio.databinding.FragmentMainBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

public class PlayRhythmTask extends AsyncTask<String, Integer, Void> { //<Params, Progress, Result>

    private Ensemble ensemble;
    private Encoder encoder;
    private ProgressDialog progressDialog;
    private MainActivity mainActivity;
    private FragmentMainBinding fragmentBinding;
    private SoundBank soundBank;

    public String params;
    public int byteBufferSizeInBytes; //this is the local size
    public byte[] byteBuffer;
    public Vector<Integer> djembeOffset = new Vector<>();

    public PlayRhythmTask(Ensemble ensemble, Encoder encoder, MainActivity mainActivity, FragmentMainBinding fragmentBinding) {
        this.ensemble = ensemble;
        this.encoder = encoder;
        this.mainActivity = mainActivity;
        this.fragmentBinding = fragmentBinding;
        soundBank = ensemble.getSoundBank();
    }

    @Override // Runs on UI
    protected void onPreExecute() {
        if (params.contains("record")) {
            progressDialog = ProgressDialog.show(mainActivity, mainActivity.getResources().getString(R.string.recording_audio), mainActivity.getResources().getString(R.string.recording_wait), true);
            encoder = new Encoder();
            encoder.init(mainActivity, ensemble, mainActivity.settings.getString("user", "undefined@undefined"));
        }
        byteBufferSizeInBytes = ensemble.byteBufferSizeInBytes;
        byteBuffer = new byte[byteBufferSizeInBytes];
        for (int i = 0; i < ensemble.djembeVector.size(); i++)
            djembeOffset.add(0);
        ensemble.onPlay = true;
        ensemble.flagEnsembleUpdate = false;
        EnsembleUtils.setEnsembleFromGui(mainActivity.ensembleLayout, ensemble);
    }

    @Override // Runs on separate Thread! No UI Updates
    protected Void doInBackground(String... strings) {

        ensemble.audioTrack.play();
        for (int currentBeat = 0; currentBeat < (ensemble.getBeats() + 4); currentBeat++) {

            publishProgress(currentBeat); // Color currentBeat bar in UI while doing this stuff

            // Update ensemble and gui if needed
            if (byteBufferSizeInBytes != ensemble.byteBufferSizeInBytes) {// Check if buffer size changed (tempo modification)
                byteBufferSizeInBytes = ensemble.byteBufferSizeInBytes;
                byteBuffer = new byte[byteBufferSizeInBytes];
            }

            this.clearBuffer();

            processInstrumentGroup("djembe", ensemble.djembeVector, ensemble.djembeStatus,
                    ensemble.djembeVolume, currentBeat);

            // TODO: why do we need to repeat this many times?
            if (!ensemble.onPlay) // Do nothing if play stopped
                break;

            // Dun // 0=empty, 1=snd_dun_bass_bell, 2=snd_dun_bell, 3=snd_dun_bass_bell_mute
            processInstrumentGroup("dun", ensemble.dunVector, ensemble.dunStatus,
                    ensemble.dunVolume, currentBeat);

            if (!ensemble.onPlay) // Do nothing if play stopped
                break;

            // Ken // 0=empty, 1=snd_ken_bass_bell, 2=snd_ken_bell, 3=snd_ken_bass_bell_mute
            processInstrumentGroup("ken", ensemble.kenVector, ensemble.kenStatus,
                    ensemble.kenVolume, currentBeat);

            if (!ensemble.onPlay) // Do nothing if play stopped
                break;

            // Sag // 0=empty, 1=snd_sag_bass_bell, 2=snd_sag_bell, 3=snd_sag_bass_bell_mute
            processInstrumentGroup("sag", ensemble.sagVector, ensemble.sagStatus,
                    ensemble.sagVolume, currentBeat);

            if (!ensemble.onPlay) // Do nothing if play stopped
                break;

            // Balet // 0=empty, 1=snd_dun_bass, 2=snd_sag_bass, 3=snd_ken_bass
            processInstrumentGroup("balet", ensemble.baletVector, ensemble.baletStatus,
                    ensemble.baletVolume, currentBeat);

            if (!ensemble.onPlay) // Do nothing if play stopped
                break;

            // Shek // 0=empty, 1=snd_shek
            processInstrumentGroup("shek", ensemble.shekVector, ensemble.shekStatus,
                    ensemble.shekVolume, currentBeat);

            if (!ensemble.onPlay) // Do nothing if play stopped
                break;

            if (params.contains("record")) { // Encode in file
                encoder.write(byteBuffer, 0, byteBufferSizeInBytes, false);
            } else { // Playback
                ensemble.audioTrack.write(byteBuffer, 0, byteBufferSizeInBytes);
            }

            // Repetitions TODO: Need to fix turnaround trail in loop/repetition
            if ((currentBeat + 1) % ensemble.getBeatsPerBar() == 0) { //[0]=barDst, [1]=total, [2]=current
                int currBar = (currentBeat + 1) / ensemble.getBeatsPerBar() - 1;
                if ((ensemble.repetitions.get(currBar)[1] > 1) && (ensemble.repetitions.get(currBar)[2] > 1)) {
                    ensemble.repetitions.get(currBar)[2]--; //Decrease Count
                    currentBeat = currentBeat - ensemble.getBeatsPerBar() * ensemble.repetitions.get(currBar)[0];
                    if (currentBeat < -1) {
                        //Log.e(TAG, "Illegal repetition from bar " + currBar + " back " + ensemble.getBeatsPerBar() * ensemble.repetitions.get(currBar)[0] + " impossible!");
                        currentBeat = -1;
                    }
                }
            }

            // Loop Ensemble
            if ((currentBeat == ensemble.getBeats() - 1) && ensemble.onLoop && (!params.contains("record"))) {
                //Reset ensemble repetitions
                for (int i = 0; i < ensemble.repetitions.size(); i++)
                    ensemble.repetitions.get(i)[2] = ensemble.repetitions.get(i)[1];
                currentBeat = -1;
            }
        }
        return null;
    }

    void addToBuffer(byte[] byteBufferIn, int byteBufferOffset, int volume) {

        Random r = new Random(); // Humanization in volume
        int newVolume = volume + r.nextInt(30);
        if (newVolume > 100)
            newVolume = 100;

        for (int i = 0; i < byteBufferSizeInBytes; i += 2) {
            // in 16 bit wav PCM, first byte is the low order byte
            if ((byteBufferOffset + i + 1) < byteBufferIn.length) { //Otherwise do not add nothing
                short currentVal1 = (short) (byteBuffer[i] & 0x000000ff);
                short currentVal2 = (short) ((byteBuffer[i + 1] & 0x000000ff) << 8);
                short currentVal = (short) (currentVal1 + currentVal2);
                short bufferInVal1 = (short) (byteBufferIn[byteBufferOffset + i] & 0x000000ff);
                short bufferInVal2 = (short) ((byteBufferIn[byteBufferOffset + i + 1] & 0x000000ff) << 8);
                short bufferInVal = (short) (bufferInVal1 + bufferInVal2);

                bufferInVal = (short) (bufferInVal * ((float) newVolume / 50)); //100 - //volume 0 to 100 (50% volume boost)

                // If Im am on the last part, copy new buffer,and attenuate last trailing zeroes
                // bufferInVal = bufferInVal * (int) (1 - (float) (i / (byteBufferSizeInBytes - 2))); //should be 0 at i=byteBufferSizeInBytes-2 (y=1-i/(size-2))
                if (byteBufferOffset == (byteBufferSizeInBytes * 4))  // last trailing zeroes
                    bufferInVal = (short) (bufferInVal - (int) (bufferInVal * (float) i / (float) (byteBufferSizeInBytes - 2))); //should be 0 at i=byteBufferSizeInBytes-2 (y=1-i/(size-2))
                //Log.e("TAG", "Im in! - " + ((float)i / (float)byteBufferSizeInBytes));
                //Log.e(TAG, Integer.toHexString(bufferInVal2) + " + " + Integer.toHexString(bufferInVal1) + " = " + Integer.toHexString(bufferInVal));

                short newVal = (short) (currentVal + bufferInVal);
                byteBuffer[i] = (byte) (newVal & 0x000000ff);
                byteBuffer[i + 1] = (byte) ((newVal & 0x0000ff00) >>> 8); //>>> unsigned (add zeroes)
            } else {
                break;
            }
        }
    }

    void clearBuffer() {
        for (int i = 0; i < byteBufferSizeInBytes; i++)
            byteBuffer[i] = 0;
    }

    @Override // Runs on UI
    protected void onProgressUpdate(Integer... progress) {
        //int cursorPosition=0;
        for (int i = 1; i < mainActivity.ensembleLayout.getChildCount() - 1; i++) { //Color the playing Bar in UI
            LinearLayout instrumentLayout = (LinearLayout) mainActivity.ensembleLayout.getChildAt(i);
            int currentBar = 0;
            for (int j = 0; j < instrumentLayout.getChildCount(); j++) {
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).contains("ico_ini") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).contains("ico_mid") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).contains("ico_end")) {
                    if (((currentBar + 1) % ensemble.getBeatsPerBar()) == 0) { //Just clear this bar for the repetition issue
                        ((ImageView) (instrumentLayout.getChildAt(j))).clearColorFilter();
                    }
                    if ((currentBar == progress[0] - 1) || (currentBar == ensemble.getBeats() - 1)) {//Remove Color!
                        ((ImageView) (instrumentLayout.getChildAt(j))).clearColorFilter();
                    }
                    if (currentBar == progress[0]) { //Color this Bar!
                        ((ImageView) (instrumentLayout.getChildAt(j))).setColorFilter(ContextCompat.getColor(mainActivity, R.color.playback_afrostudio));
                    }
                    currentBar++;
                }
            }
        }

        if (ensemble.flagEnsembleUpdate) { //update Ensemble
            EnsembleUtils.setEnsembleFromGui(mainActivity.ensembleLayout, ensemble);
            EnsembleUtils.setGuiFromEnsemble(mainActivity.ensembleLayout, ensemble, mainActivity, mainActivity.iconScale, fragmentBinding.animatorIntro);
            djembeOffset.clear();
            for (int i = 0; i < ensemble.djembeVector.size(); i++)
                djembeOffset.add(0);
            ensemble.flagEnsembleUpdate = false;
        }
    }

    @Override // Runs on UI
    protected void onPostExecute(Void param) { //Runs on UI
        for (int i = 0; i < mainActivity.ensembleLayout.getChildCount(); i++) { //Clear Color in UI
            LinearLayout instrumentLayout = (LinearLayout) mainActivity.ensembleLayout.getChildAt(i);
            for (int j = 0; j < instrumentLayout.getChildCount(); j++) {
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).contains("_ini") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).contains("_mid") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).contains("_end")) {
                    ((ImageView) (instrumentLayout.getChildAt(j))).clearColorFilter();
                }
            }
        }

        if (params.contains("record")) {
            encoder.write(byteBuffer, 0, byteBufferSizeInBytes, true); //End of Stream
            encoder.close();
            progressDialog.dismiss();
        }

        ensemble.audioTrack.flush();
        ensemble.audioTrack.stop();
        ensemble.onPlay = false;
        ImageView but_play = fragmentBinding.butPlay;
        but_play.setImageResource(R.drawable.but_play);

        if (params.contains("share")) { // Share file
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                //sharingIntent.setPackage("com.whatsapp"); // Directly via WhatsApp
                sharingIntent.setType("audio/*");//mp4a-latm acc
                try {
                    String path = mainActivity.getExternalFilesDir(null).getPath();
                    String user = mainActivity.settings.getString("user", "undefined@undefined").substring(0, mainActivity.settings.getString("user", "undefined@undefined").indexOf("@"));
                    String fileName = ensemble.ensembleName + "_by_" + ensemble.ensembleAuthor + "_u_" + user + ".aac";
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path + "/" + fileName));
                    mainActivity.startActivity(Intent.createChooser(sharingIntent, "Share via"));
                } catch (NullPointerException e) {
                    Toast.makeText(mainActivity, "Cannot access storage", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mainActivity, "Cannot access storage", Toast.LENGTH_SHORT).show();
            }
        }

        if (params.contains("reproduce")) {//reproduce
            String path = mainActivity.getExternalFilesDir(null).getPath();
            String user = mainActivity.settings.getString("user", "undefined@undefined").substring(0, mainActivity.settings.getString("user", "undefined@undefined").indexOf("@"));
            String fileName = ensemble.ensembleName + "_by_" + ensemble.ensembleAuthor + "_u_" + user + ".aac";

            Intent reproduceIntent = new Intent(Intent.ACTION_VIEW);
            reproduceIntent.setDataAndType(Uri.parse("file://" + path + "/" + fileName), "audio/mp4a-latm");
            mainActivity.startActivity(reproduceIntent);

//                mediaPlayer = MediaPlayer.create(MainActivity.this, Uri.parse("file://" + path + "/" + fileName));
//                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//                        ImageView but_play = fragmentBinding.butPlay;
//                        but_play.setImageResource(R.drawable.but_play);
//                        Log.e(TAG, "MediaPlayer error!!!");
//                    }
//                });
//                mediaPlayer.setLooping(ensemble.onLoop);
//                mediaPlayer.start();
//                but_play = fragmentBinding.butPlay;
//                but_play.setImageResource(R.drawable.but_stop);

        }
    }

    private String getSoundNameForCode(String family, int code) {
        Map<String, Map<Integer, String>> soundMap = new HashMap<>();

        // Define mappings for djembe
        Map<Integer, String> djembeMap = new HashMap<>();
        djembeMap.put(1, "bass");
        djembeMap.put(2, "tone");
        djembeMap.put(3, "slap");
        djembeMap.put(4, "bass_flam");
        djembeMap.put(5, "tone_flam");
        djembeMap.put(6, "slap_flam");

        // Define mappings for dun
        Map<Integer, String> dunMap = new HashMap<>();
        dunMap.put(1, "bass_bell");
        dunMap.put(2, "bell");
        dunMap.put(3, "bass_bell_mute");

        // Define mappings for ken
        Map<Integer, String> kenMap = new HashMap<>();
        kenMap.put(1, "bass_bell");
        kenMap.put(2, "bell");
        kenMap.put(3, "bass_bell_mute");

        // Define mappings for sag
        Map<Integer, String> sagMap = new HashMap<>();
        sagMap.put(1, "bass_bell");
        sagMap.put(2, "bell");
        sagMap.put(3, "bass_bell_mute");

        // Define mappings for balet
        Map<Integer, String> baletMap = new HashMap<>();
        baletMap.put(1, "dun");       // uses dun bass
        baletMap.put(2, "sag");       // uses sag bass
        baletMap.put(3, "ken");       // uses ken bass
        baletMap.put(4, "dun_mute");  // uses dun bass_mute
        baletMap.put(5, "sag_mute");  // uses sag bass_mute
        baletMap.put(6, "ken_mute");  // uses ken bass_mute
        baletMap.put(7, "ring");      // special ring sound

        // Define mappings for shek
        Map<Integer, String> shekMap = new HashMap<>();
        shekMap.put(1, "standard");

        // Add all instrument families to the main map
        soundMap.put("djembe", djembeMap);
        soundMap.put("dun", dunMap);
        soundMap.put("ken", kenMap);
        soundMap.put("sag", sagMap);
        soundMap.put("balet", baletMap);
        soundMap.put("shek", shekMap);

        // Special sounds
        Map<Integer, String> specialMap = new HashMap<>();
        specialMap.put(1, "silence");
        specialMap.put(2, "ring");
        soundMap.put("special", specialMap);

        return soundMap.getOrDefault(family, new HashMap<>()).getOrDefault(code, "silence");
    }

    private void processInstrumentGroup(String family, List<Vector<Integer>> patterns,
                                        List<Integer> status, List<Integer> volumes, int currentBeat) {
        for (int i = 0; i < patterns.size(); i++) {
            if (status.get(i) == 1) {  // instrument is active
                int variant = family.equals("djembe") ? i % 3 : 0;
                int offset = 0;

                // Apply humanization only for djembe instruments
                if (family.equals("djembe")) {
                    Vector<Integer> pattern = patterns.get(i);
                    // Set new offset when a sound plays
                    if (pattern.get(currentBeat) != 0) {
                        Random r = new Random();
                        djembeOffset.setElementAt(r.nextInt(200) * 2, i);
                    }
                    offset = djembeOffset.get(i);
                }

                processInstrumentSound(family, null, variant, currentBeat, patterns.get(i), volumes.get(i), offset);
            }
        }
    }

    private void processInstrumentSound(String family, String soundType, int variant, int currentBeat,
                                        Vector<Integer> instrumentPattern, int volume, int offset) {
        int soundCode = instrumentPattern.get(currentBeat);

        if (soundCode > 0) {
            // Play the specific sound based on code with offset (offset = 0 for non-djembe instruments)
            String soundName = getSoundNameForCode(family, soundCode);
            addToBuffer(soundBank.getSound(family, soundName, variant), offset, volume);
        } else {
            // Check previous beats for trailing sounds with offset
            checkPreviousBeats(family, instrumentPattern, currentBeat, volume, variant, offset);
        }
    }

    private void checkPreviousBeats(String family, Vector<Integer> pattern, int currentBeat,
                                    int volume, int variant, int offset) {
        // Check up to MAX_BEATS_BACK for trailing sound
        final int MAX_BEATS_BACK = 4;

        for (int beatsBack = 1; beatsBack <= MAX_BEATS_BACK; beatsBack++) {
            int prevBeat = currentBeat - beatsBack;
            if (prevBeat >= 0) {
                int prevSoundCode = pattern.get(prevBeat);
                if (prevSoundCode > 0) {
                    String soundName = getSoundNameForCode(family, prevSoundCode);
                    // Add offset to the buffer timing
                    addToBuffer(soundBank.getSound(family, soundName, variant),
                            byteBufferSizeInBytes * beatsBack + offset, volume);
                    return;
                }
            }
        }

        // No previous sound found, play silence
        addToBuffer(soundBank.getSound("special", "silence"), 0, volume);
    }
}