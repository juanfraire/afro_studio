package com.yaray.afrostudio;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.yaray.afrostudio.databinding.ActivityMainBinding;
import com.yaray.afrostudio.databinding.FragmentMainBinding;

public class MainActivity extends AppCompatActivity
        implements
        DialogFragmentAddInstrument.AddInstrumentListener,
        DialogFragmentTempoSettings.TempoSettingsListener,
        DialogFragmentNewEnsemble.NewEnsembleListener,
        DialogFragmentInstrumentOptions.InstrumentOptionsListener,
        DialogFragmentSetRepetition.SetRepetitionListener,
        DialogFragmentSetName.SetNameListener,
        DialogFragmentLoad.LoadListener,
        DialogFragmentEqualizer.EqualizerListener,
        DialogFragmentBarOptions.BarOptionsListener,
        DialogFragmentSpecialStroke.SpecialStrokeListener,
        DialogFragmentEmail.EmailListener,
        DialogFragmentShare.ShareListener {

    private ActivityMainBinding binding;

    @SuppressWarnings("unused")
    private static final String TAG = "AfroStudio.MainActivity";
    final String PREFS_NAME = "MyPrefsFile";
    Random rnd = new Random(); //random source for ads

    SharedPreferences settings;
    LinearLayout ensembleLayout; // MasterLayout
    HorizontalScrollView horizontalScrollView; // Horizontal Scroll
    ViewAnimator viewAnimator;
    float iconScale = (float) 0.75; // 1 = 37.5 x 52.5 dp
    Ensemble ensemble;

    DialogFragmentLoad dialogFragmentLoad; // to access dialog from connectServerAsynctastk
    Encoder encoder;

    private class PlayRithmTask extends AsyncTask<String, Integer, Void> { //<Params, Progress, Result>

        String params;
        int byteBufferSizeInBytes; //this is the local size
        byte[] byteBuffer;
        public Vector<Integer> djembeOffset = new Vector();
        ProgressDialog progressDialog;

        @Override // Runs on UI
        protected void onPreExecute() {
            if (params.contains("record")) {
                progressDialog = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.recording_audio), getResources().getString(R.string.recording_wait), true);
                encoder = new Encoder();
                encoder.init(MainActivity.this, ensemble, settings.getString("user", "undefined@undefined"));
            }
            byteBufferSizeInBytes = ensemble.byteBufferSizeInBytes;
            byteBuffer = new byte[byteBufferSizeInBytes];
            for (int i = 0; i < ensemble.djembeVector.size(); i++)
                djembeOffset.add(0);
            ensemble.onPlay = true;
            ensemble.flagEnsembleUpdate = false;
            EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble);
        }

        @Override // Runs on separate Thread! No UI Updates
        protected Void doInBackground(String... strings) {

            ensemble.audioTrack.play();
            for (int j = 0; j < (ensemble.getBeats() + 4); j++) {

                publishProgress(j); // Color j bar in UI while doing this stuff

                // Update ensemble and gui if needed
                if (byteBufferSizeInBytes != ensemble.byteBufferSizeInBytes) {// Check if buffer size changed (tempo modification)
                    byteBufferSizeInBytes = ensemble.byteBufferSizeInBytes;
                    byteBuffer = new byte[byteBufferSizeInBytes];
                }

                this.clearBuffer();

                for (int i = 0; i < ensemble.djembeVector.size(); i++) { // All i Djembes at bar j
                    if ((ensemble.djembeVector.elementAt(i).elementAt(j) != 0) && (ensemble.djembeStatus.elementAt(i) == 1)) { // Set offset
                        Random r = new Random(); // Humanization on time
                        djembeOffset.setElementAt(r.nextInt(200) * 2, i);
                    }

                    if (ensemble.djembeStatus.elementAt(i) == 1) // instrument active
                        if (ensemble.djembeVector.elementAt(i).elementAt(j) == 1)
                            this.addToBuffer(ensemble.snd_djembe_bass[i % 3], djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                        else if (ensemble.djembeVector.elementAt(i).elementAt(j) == 2)
                            this.addToBuffer(ensemble.snd_djembe_tone[i % 3], djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                        else if (ensemble.djembeVector.elementAt(i).elementAt(j) == 3)
                            this.addToBuffer(ensemble.snd_djembe_slap[i % 3], djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                        else if (ensemble.djembeVector.elementAt(i).elementAt(j) == 4)
                            this.addToBuffer(ensemble.snd_djembe_bass_flam[i % 3], djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                        else if (ensemble.djembeVector.elementAt(i).elementAt(j) == 5)
                            this.addToBuffer(ensemble.snd_djembe_tone_flam[i % 3], djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                        else if (ensemble.djembeVector.elementAt(i).elementAt(j) == 6)
                            this.addToBuffer(ensemble.snd_djembe_slap_flam[i % 3], djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                        else if (ensemble.djembeVector.elementAt(i).elementAt(j) == 0) { // Silence, check previous to fill with continuing sound
                            if ((j - 1 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 1) == 1)
                                this.addToBuffer(ensemble.snd_djembe_bass[i % 3], byteBufferSizeInBytes + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 1) == 2)
                                this.addToBuffer(ensemble.snd_djembe_tone[i % 3], byteBufferSizeInBytes + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 1) == 3)
                                this.addToBuffer(ensemble.snd_djembe_slap[i % 3], byteBufferSizeInBytes + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 1) == 4)
                                this.addToBuffer(ensemble.snd_djembe_bass_flam[i % 3], byteBufferSizeInBytes + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 1) == 5)
                                this.addToBuffer(ensemble.snd_djembe_tone_flam[i % 3], byteBufferSizeInBytes + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 1) == 6)
                                this.addToBuffer(ensemble.snd_djembe_slap_flam[i % 3], byteBufferSizeInBytes + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                            else { // Silence, check previous to fill with continuing sound
                                if ((j - 2 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 2) == 1)
                                    this.addToBuffer(ensemble.snd_djembe_bass[i % 3], byteBufferSizeInBytes * 2 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 2) == 2)
                                    this.addToBuffer(ensemble.snd_djembe_tone[i % 3], byteBufferSizeInBytes * 2 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 2) == 3)
                                    this.addToBuffer(ensemble.snd_djembe_slap[i % 3], byteBufferSizeInBytes * 2 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 2) == 4)
                                    this.addToBuffer(ensemble.snd_djembe_bass_flam[i % 3], byteBufferSizeInBytes * 2 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 2) == 5)
                                    this.addToBuffer(ensemble.snd_djembe_tone_flam[i % 3], byteBufferSizeInBytes * 2 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 2) == 6)
                                    this.addToBuffer(ensemble.snd_djembe_slap_flam[i % 3], byteBufferSizeInBytes * 2 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                else {// Silence, check previous to fill with continuing sound
                                    if ((j - 3 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 3) == 1)
                                        this.addToBuffer(ensemble.snd_djembe_bass[i % 3], byteBufferSizeInBytes * 3 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 3) == 2)
                                        this.addToBuffer(ensemble.snd_djembe_tone[i % 3], byteBufferSizeInBytes * 3 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 3) == 3)
                                        this.addToBuffer(ensemble.snd_djembe_slap[i % 3], byteBufferSizeInBytes * 3 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 3) == 4)
                                        this.addToBuffer(ensemble.snd_djembe_bass_flam[i % 3], byteBufferSizeInBytes * 3 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 3) == 5)
                                        this.addToBuffer(ensemble.snd_djembe_tone_flam[i % 3], byteBufferSizeInBytes * 3 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 3) == 6)
                                        this.addToBuffer(ensemble.snd_djembe_slap_flam[i % 3], byteBufferSizeInBytes * 3 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                    else {// Silence, check previous to fill with continuing sound
                                        if ((j - 4 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 4) == 1)
                                            this.addToBuffer(ensemble.snd_djembe_bass[i % 3], byteBufferSizeInBytes * 4 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 4) == 2)
                                            this.addToBuffer(ensemble.snd_djembe_tone[i % 3], byteBufferSizeInBytes * 4 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 4) == 3)
                                            this.addToBuffer(ensemble.snd_djembe_slap[i % 3], byteBufferSizeInBytes * 4 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 4) == 4)
                                            this.addToBuffer(ensemble.snd_djembe_bass_flam[i % 3], byteBufferSizeInBytes * 4 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 4) == 5)
                                            this.addToBuffer(ensemble.snd_djembe_tone_flam[i % 3], byteBufferSizeInBytes * 4 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.djembeVector.elementAt(i).elementAt(j - 4) == 6)
                                            this.addToBuffer(ensemble.snd_djembe_slap_flam[i % 3], byteBufferSizeInBytes * 4 + djembeOffset.elementAt(i), ensemble.djembeVolume.elementAt(i));
                                        else
                                            this.addToBuffer(ensemble.snd_silence, 0, ensemble.djembeVolume.elementAt(i));
                                    }
                                }
                            }
                        }
                }

                if (!ensemble.onPlay) // Do nothing if play stopped
                    break;

                // Dun // 0=empty, 1=snd_dun_bass_bell, 2=snd_dun_bell, 3=snd_dun_bass_bell_mute
                for (int i = 0; i < ensemble.dunVector.size(); i++) { // All i Dun at bar j
                    if (ensemble.dunStatus.elementAt(i) == 1) // instrument active
                        if (ensemble.dunVector.elementAt(i).elementAt(j) == 1)
                            this.addToBuffer(ensemble.snd_dun_bass_bell, 0, ensemble.dunVolume.elementAt(i));
                        else if (ensemble.dunVector.elementAt(i).elementAt(j) == 2)
                            this.addToBuffer(ensemble.snd_dun_bell, 0, ensemble.dunVolume.elementAt(i));
                        else if (ensemble.dunVector.elementAt(i).elementAt(j) == 3)
                            this.addToBuffer(ensemble.snd_dun_bass_bell_mute, 0, ensemble.dunVolume.elementAt(i));
                        else if (ensemble.dunVector.elementAt(i).elementAt(j) == 0) { // Silence, check previous to fill with continuing sound
                            if ((j - 1 >= 0) && ensemble.dunVector.elementAt(i).elementAt(j - 1) == 1)
                                this.addToBuffer(ensemble.snd_dun_bass_bell, byteBufferSizeInBytes, ensemble.dunVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.dunVector.elementAt(i).elementAt(j - 1) == 2)
                                this.addToBuffer(ensemble.snd_dun_bell, byteBufferSizeInBytes, ensemble.dunVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.dunVector.elementAt(i).elementAt(j - 1) == 3)
                                this.addToBuffer(ensemble.snd_dun_bass_bell_mute, byteBufferSizeInBytes, ensemble.dunVolume.elementAt(i));
                            else { // Silence, check previous to fill with continuing sound
                                if ((j - 2 >= 0) && ensemble.dunVector.elementAt(i).elementAt(j - 2) == 1)
                                    this.addToBuffer(ensemble.snd_dun_bass_bell, byteBufferSizeInBytes * 2, ensemble.dunVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.dunVector.elementAt(i).elementAt(j - 2) == 2)
                                    this.addToBuffer(ensemble.snd_dun_bell, byteBufferSizeInBytes * 2, ensemble.dunVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.dunVector.elementAt(i).elementAt(j - 2) == 3)
                                    this.addToBuffer(ensemble.snd_dun_bass_bell_mute, byteBufferSizeInBytes * 2, ensemble.dunVolume.elementAt(i));
                                else {// Silence, check previous to fill with continuing sound
                                    if ((j - 3 >= 0) && ensemble.dunVector.elementAt(i).elementAt(j - 3) == 1)
                                        this.addToBuffer(ensemble.snd_dun_bass_bell, byteBufferSizeInBytes * 3, ensemble.dunVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.dunVector.elementAt(i).elementAt(j - 3) == 2)
                                        this.addToBuffer(ensemble.snd_dun_bell, byteBufferSizeInBytes * 3, ensemble.dunVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.dunVector.elementAt(i).elementAt(j - 3) == 3)
                                        this.addToBuffer(ensemble.snd_dun_bass_bell_mute, byteBufferSizeInBytes * 3, ensemble.dunVolume.elementAt(i));
                                    else {// Silence, check previous to fill with continuing sound
                                        if ((j - 4 >= 0) && ensemble.dunVector.elementAt(i).elementAt(j - 4) == 1)
                                            this.addToBuffer(ensemble.snd_dun_bass_bell, byteBufferSizeInBytes * 4, ensemble.dunVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.dunVector.elementAt(i).elementAt(j - 4) == 2)
                                            this.addToBuffer(ensemble.snd_dun_bell, byteBufferSizeInBytes * 4, ensemble.dunVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.dunVector.elementAt(i).elementAt(j - 4) == 3)
                                            this.addToBuffer(ensemble.snd_dun_bass_bell_mute, byteBufferSizeInBytes * 4, ensemble.dunVolume.elementAt(i));
                                        else
                                            this.addToBuffer(ensemble.snd_silence, 0, ensemble.dunVolume.elementAt(i));
                                    }
                                }
                            }
                        }
                }

                if (!ensemble.onPlay) // Do nothing if play stopped
                    break;

                // Ken // 0=empty, 1=snd_ken_bass_bell, 2=snd_ken_bell, 3=snd_ken_bass_bell_mute
                for (int i = 0; i < ensemble.kenVector.size(); i++) { // All i Kenkenis at bar j
                    if (ensemble.kenStatus.elementAt(i) == 1) // instrument active
                        if (ensemble.kenVector.elementAt(i).elementAt(j) == 1)
                            this.addToBuffer(ensemble.snd_ken_bass_bell, 0, ensemble.kenVolume.elementAt(i));
                        else if (ensemble.kenVector.elementAt(i).elementAt(j) == 2)
                            this.addToBuffer(ensemble.snd_ken_bell, 0, ensemble.kenVolume.elementAt(i));
                        else if (ensemble.kenVector.elementAt(i).elementAt(j) == 3)
                            this.addToBuffer(ensemble.snd_ken_bass_bell_mute, 0, ensemble.kenVolume.elementAt(i));
                        else if (ensemble.kenVector.elementAt(i).elementAt(j) == 0) { // Silence, check previous to fill with continuing sound
                            if ((j - 1 >= 0) && ensemble.kenVector.elementAt(i).elementAt(j - 1) == 1)
                                this.addToBuffer(ensemble.snd_ken_bass_bell, byteBufferSizeInBytes, ensemble.kenVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.kenVector.elementAt(i).elementAt(j - 1) == 2)
                                this.addToBuffer(ensemble.snd_ken_bell, byteBufferSizeInBytes, ensemble.kenVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.kenVector.elementAt(i).elementAt(j - 1) == 3)
                                this.addToBuffer(ensemble.snd_ken_bass_bell_mute, byteBufferSizeInBytes, ensemble.kenVolume.elementAt(i));
                            else { // Silence, check previous to fill with continuing sound
                                if ((j - 2 >= 0) && ensemble.kenVector.elementAt(i).elementAt(j - 2) == 1)
                                    this.addToBuffer(ensemble.snd_ken_bass_bell, byteBufferSizeInBytes * 2, ensemble.kenVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.kenVector.elementAt(i).elementAt(j - 2) == 2)
                                    this.addToBuffer(ensemble.snd_ken_bell, byteBufferSizeInBytes * 2, ensemble.kenVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.kenVector.elementAt(i).elementAt(j - 2) == 3)
                                    this.addToBuffer(ensemble.snd_ken_bass_bell_mute, byteBufferSizeInBytes * 2, ensemble.kenVolume.elementAt(i));
                                else {// Silence, check previous to fill with continuing sound
                                    if ((j - 3 >= 0) && ensemble.kenVector.elementAt(i).elementAt(j - 3) == 1)
                                        this.addToBuffer(ensemble.snd_ken_bass_bell, byteBufferSizeInBytes * 3, ensemble.kenVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.kenVector.elementAt(i).elementAt(j - 3) == 2)
                                        this.addToBuffer(ensemble.snd_ken_bell, byteBufferSizeInBytes * 3, ensemble.kenVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.kenVector.elementAt(i).elementAt(j - 3) == 3)
                                        this.addToBuffer(ensemble.snd_ken_bass_bell_mute, byteBufferSizeInBytes * 3, ensemble.kenVolume.elementAt(i));
                                    else {// Silence, check previous to fill with continuing sound
                                        if ((j - 4 >= 0) && ensemble.kenVector.elementAt(i).elementAt(j - 4) == 1)
                                            this.addToBuffer(ensemble.snd_ken_bass_bell, byteBufferSizeInBytes * 4, ensemble.kenVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.kenVector.elementAt(i).elementAt(j - 4) == 2)
                                            this.addToBuffer(ensemble.snd_ken_bell, byteBufferSizeInBytes * 4, ensemble.kenVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.kenVector.elementAt(i).elementAt(j - 4) == 3)
                                            this.addToBuffer(ensemble.snd_ken_bass_bell_mute, byteBufferSizeInBytes * 4, ensemble.kenVolume.elementAt(i));
                                        else
                                            this.addToBuffer(ensemble.snd_silence, 0, ensemble.kenVolume.elementAt(i));
                                    }
                                }
                            }
                        }
                }

                if (!ensemble.onPlay) // Do nothing if play stopped
                    break;

                // Sag // 0=empty, 1=snd_sag_bass_bell, 2=snd_sag_bell, 3=snd_sag_bass_bell_mute
                for (int i = 0; i < ensemble.sagVector.size(); i++) { // All i
                    if (ensemble.sagStatus.elementAt(i) == 1) // instrument active
                        if (ensemble.sagVector.elementAt(i).elementAt(j) == 1)
                            this.addToBuffer(ensemble.snd_sag_bass_bell, 0, ensemble.sagVolume.elementAt(i));
                        else if (ensemble.sagVector.elementAt(i).elementAt(j) == 2)
                            this.addToBuffer(ensemble.snd_sag_bell, 0, ensemble.sagVolume.elementAt(i));
                        else if (ensemble.sagVector.elementAt(i).elementAt(j) == 3)
                            this.addToBuffer(ensemble.snd_sag_bass_bell_mute, 0, ensemble.sagVolume.elementAt(i));
                        else if (ensemble.sagVector.elementAt(i).elementAt(j) == 0) { // Silence, check previous to fill with continuing sound
                            if ((j - 1 >= 0) && ensemble.sagVector.elementAt(i).elementAt(j - 1) == 1)
                                this.addToBuffer(ensemble.snd_sag_bass_bell, byteBufferSizeInBytes, ensemble.sagVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.sagVector.elementAt(i).elementAt(j - 1) == 2)
                                this.addToBuffer(ensemble.snd_sag_bell, byteBufferSizeInBytes, ensemble.sagVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.sagVector.elementAt(i).elementAt(j - 1) == 3)
                                this.addToBuffer(ensemble.snd_sag_bass_bell_mute, byteBufferSizeInBytes, ensemble.sagVolume.elementAt(i));
                            else { // Silence, check previous to fill with continuing sound
                                if ((j - 2 >= 0) && ensemble.sagVector.elementAt(i).elementAt(j - 2) == 1)
                                    this.addToBuffer(ensemble.snd_sag_bass_bell, byteBufferSizeInBytes * 2, ensemble.sagVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.sagVector.elementAt(i).elementAt(j - 2) == 2)
                                    this.addToBuffer(ensemble.snd_sag_bell, byteBufferSizeInBytes * 2, ensemble.sagVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.sagVector.elementAt(i).elementAt(j - 2) == 3)
                                    this.addToBuffer(ensemble.snd_sag_bass_bell_mute, byteBufferSizeInBytes * 2, ensemble.sagVolume.elementAt(i));
                                else {// Silence, check previous to fill with continuing sound
                                    if ((j - 3 >= 0) && ensemble.sagVector.elementAt(i).elementAt(j - 3) == 1)
                                        this.addToBuffer(ensemble.snd_sag_bass_bell, byteBufferSizeInBytes * 3, ensemble.sagVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.sagVector.elementAt(i).elementAt(j - 3) == 2)
                                        this.addToBuffer(ensemble.snd_sag_bell, byteBufferSizeInBytes * 3, ensemble.sagVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.sagVector.elementAt(i).elementAt(j - 3) == 3)
                                        this.addToBuffer(ensemble.snd_sag_bass_bell_mute, byteBufferSizeInBytes * 3, ensemble.sagVolume.elementAt(i));
                                    else {// Silence, check previous to fill with continuing sound
                                        if ((j - 4 >= 0) && ensemble.sagVector.elementAt(i).elementAt(j - 4) == 1)
                                            this.addToBuffer(ensemble.snd_sag_bass_bell, byteBufferSizeInBytes * 4, ensemble.sagVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.sagVector.elementAt(i).elementAt(j - 4) == 2)
                                            this.addToBuffer(ensemble.snd_sag_bell, byteBufferSizeInBytes * 4, ensemble.sagVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.sagVector.elementAt(i).elementAt(j - 4) == 3)
                                            this.addToBuffer(ensemble.snd_sag_bass_bell_mute, byteBufferSizeInBytes * 4, ensemble.sagVolume.elementAt(i));
                                        else
                                            this.addToBuffer(ensemble.snd_silence, 0, ensemble.sagVolume.elementAt(i));
                                    }
                                }
                            }
                        }
                }

                if (!ensemble.onPlay) // Do nothing if play stopped
                    break;

                // Balet // 0=empty, 1=snd_dun_bass, 2=snd_sag_bass, 3=snd_ken_bass
                for (int i = 0; i < ensemble.baletVector.size(); i++) { // All i
                    if (ensemble.baletStatus.elementAt(i) == 1) // instrument active
                        if (ensemble.baletVector.elementAt(i).elementAt(j) == 1)
                            this.addToBuffer(ensemble.snd_dun_bass, 0, ensemble.baletVolume.elementAt(i));
                        else if (ensemble.baletVector.elementAt(i).elementAt(j) == 2)
                            this.addToBuffer(ensemble.snd_sag_bass, 0, ensemble.baletVolume.elementAt(i));
                        else if (ensemble.baletVector.elementAt(i).elementAt(j) == 3)
                            this.addToBuffer(ensemble.snd_ken_bass, 0, ensemble.baletVolume.elementAt(i));
                        else if (ensemble.baletVector.elementAt(i).elementAt(j) == 4)
                            this.addToBuffer(ensemble.snd_dun_bass_mute, 0, ensemble.baletVolume.elementAt(i));
                        else if (ensemble.baletVector.elementAt(i).elementAt(j) == 5)
                            this.addToBuffer(ensemble.snd_sag_bass_mute, 0, ensemble.baletVolume.elementAt(i));
                        else if (ensemble.baletVector.elementAt(i).elementAt(j) == 6)
                            this.addToBuffer(ensemble.snd_ken_bass_mute, 0, ensemble.baletVolume.elementAt(i));
                        else if (ensemble.baletVector.elementAt(i).elementAt(j) == 7)
                            this.addToBuffer(ensemble.snd_ring, 0, ensemble.baletVolume.elementAt(i));
                        else if (ensemble.baletVector.elementAt(i).elementAt(j) == 0) { // Silence, check previous to fill with continuing sound
                            if ((j - 1 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 1) == 1)
                                this.addToBuffer(ensemble.snd_dun_bass, byteBufferSizeInBytes, ensemble.baletVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 1) == 2)
                                this.addToBuffer(ensemble.snd_sag_bass, byteBufferSizeInBytes, ensemble.baletVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 1) == 3)
                                this.addToBuffer(ensemble.snd_ken_bass, byteBufferSizeInBytes, ensemble.baletVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 1) == 4)
                                this.addToBuffer(ensemble.snd_dun_bass_mute, byteBufferSizeInBytes, ensemble.baletVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 1) == 5)
                                this.addToBuffer(ensemble.snd_sag_bass_mute, byteBufferSizeInBytes, ensemble.baletVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 1) == 6)
                                this.addToBuffer(ensemble.snd_ken_bass_mute, byteBufferSizeInBytes, ensemble.baletVolume.elementAt(i));
                            else if ((j - 1 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 1) == 7)
                                this.addToBuffer(ensemble.snd_ring, byteBufferSizeInBytes, ensemble.baletVolume.elementAt(i));
                            else { // Silence, check previous to fill with continuing sound
                                if ((j - 2 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 2) == 1)
                                    this.addToBuffer(ensemble.snd_dun_bass, byteBufferSizeInBytes * 2, ensemble.baletVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 2) == 2)
                                    this.addToBuffer(ensemble.snd_sag_bass, byteBufferSizeInBytes * 2, ensemble.baletVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 2) == 3)
                                    this.addToBuffer(ensemble.snd_ken_bass, byteBufferSizeInBytes * 2, ensemble.baletVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 2) == 4)
                                    this.addToBuffer(ensemble.snd_dun_bass_mute, byteBufferSizeInBytes * 2, ensemble.baletVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 2) == 5)
                                    this.addToBuffer(ensemble.snd_sag_bass_mute, byteBufferSizeInBytes * 2, ensemble.baletVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 2) == 6)
                                    this.addToBuffer(ensemble.snd_ken_bass_mute, byteBufferSizeInBytes * 2, ensemble.baletVolume.elementAt(i));
                                else if ((j - 2 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 2) == 7)
                                    this.addToBuffer(ensemble.snd_ring, byteBufferSizeInBytes * 2, ensemble.baletVolume.elementAt(i));
                                else {// Silence, check previous to fill with continuing sound
                                    if ((j - 3 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 3) == 1)
                                        this.addToBuffer(ensemble.snd_dun_bass, byteBufferSizeInBytes * 3, ensemble.baletVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 3) == 2)
                                        this.addToBuffer(ensemble.snd_sag_bass, byteBufferSizeInBytes * 3, ensemble.baletVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 3) == 3)
                                        this.addToBuffer(ensemble.snd_ken_bass, byteBufferSizeInBytes * 3, ensemble.baletVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 3) == 4)
                                        this.addToBuffer(ensemble.snd_dun_bass_mute, byteBufferSizeInBytes * 3, ensemble.baletVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 3) == 5)
                                        this.addToBuffer(ensemble.snd_sag_bass_mute, byteBufferSizeInBytes * 3, ensemble.baletVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 3) == 6)
                                        this.addToBuffer(ensemble.snd_ken_bass_mute, byteBufferSizeInBytes * 3, ensemble.baletVolume.elementAt(i));
                                    else if ((j - 3 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 3) == 7)
                                        this.addToBuffer(ensemble.snd_ring, byteBufferSizeInBytes * 3, ensemble.baletVolume.elementAt(i));
                                    else {// Silence, check previous to fill with continuing sound
                                        if ((j - 4 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 4) == 1)
                                            this.addToBuffer(ensemble.snd_dun_bass, byteBufferSizeInBytes * 4, ensemble.baletVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 4) == 2)
                                            this.addToBuffer(ensemble.snd_sag_bass, byteBufferSizeInBytes * 4, ensemble.baletVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 4) == 3)
                                            this.addToBuffer(ensemble.snd_ken_bass, byteBufferSizeInBytes * 4, ensemble.baletVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 4) == 4)
                                            this.addToBuffer(ensemble.snd_dun_bass_mute, byteBufferSizeInBytes * 4, ensemble.baletVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 4) == 5)
                                            this.addToBuffer(ensemble.snd_sag_bass_mute, byteBufferSizeInBytes * 4, ensemble.baletVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 4) == 6)
                                            this.addToBuffer(ensemble.snd_ken_bass_mute, byteBufferSizeInBytes * 4, ensemble.baletVolume.elementAt(i));
                                        else if ((j - 4 >= 0) && ensemble.baletVector.elementAt(i).elementAt(j - 4) == 7)
                                            this.addToBuffer(ensemble.snd_ring, byteBufferSizeInBytes * 4, ensemble.baletVolume.elementAt(i));
                                        else
                                            this.addToBuffer(ensemble.snd_silence, 0, ensemble.baletVolume.elementAt(i));
                                    }
                                }
                            }
                        }
                }

                if (!ensemble.onPlay) // Do nothing if play stopped
                    break;

                // Shek // 0=empty, 1=snd_shek
                for (int i = 0; i < ensemble.shekVector.size(); i++) { // All i Shek at bar j
                    if (ensemble.shekStatus.elementAt(i) == 1) // instrument active
                        if (ensemble.shekVector.elementAt(i).elementAt(j) == 1)
                            this.addToBuffer(ensemble.snd_shek, 0, ensemble.shekVolume.elementAt(i));
                        else if (ensemble.shekVector.elementAt(i).elementAt(j) == 0) { // Silence, check previous to fill with continuing sound
                            if ((j - 1 >= 0) && ensemble.shekVector.elementAt(i).elementAt(j - 1) == 1)
                                this.addToBuffer(ensemble.snd_shek, byteBufferSizeInBytes, ensemble.shekVolume.elementAt(i));
                            else { // Silence, check previous to fill with continuing sound
                                if ((j - 2 >= 0) && ensemble.shekVector.elementAt(i).elementAt(j - 2) == 1)
                                    this.addToBuffer(ensemble.snd_shek, byteBufferSizeInBytes * 2, ensemble.shekVolume.elementAt(i));
                                else {// Silence, check previous to fill with continuing sound
                                    if ((j - 3 >= 0) && ensemble.shekVector.elementAt(i).elementAt(j - 3) == 1)
                                        this.addToBuffer(ensemble.snd_shek, byteBufferSizeInBytes * 3, ensemble.shekVolume.elementAt(i));
                                    else {// Silence, check previous to fill with continuing sound
                                        if ((j - 4 >= 0) && ensemble.shekVector.elementAt(i).elementAt(j - 4) == 1)
                                            this.addToBuffer(ensemble.snd_shek, byteBufferSizeInBytes * 4, ensemble.shekVolume.elementAt(i));
                                        else
                                            this.addToBuffer(ensemble.snd_silence, 0, ensemble.shekVolume.elementAt(i));
                                    }
                                }
                            }
                        }
                }

                if (!ensemble.onPlay) // Do nothing if play stopped
                    break;

                if (params.contains("record")) { // Encode in file
                    encoder.write(byteBuffer, 0, byteBufferSizeInBytes, false);
                } else { // Playback
                    ensemble.audioTrack.write(byteBuffer, 0, byteBufferSizeInBytes);
                }

                // Repetitions TODO: Need to fix turnaround trail in loop/repetition
                if ((j + 1) % ensemble.getBeatsPerBar() == 0) { //[0]=barDst, [1]=total, [2]=current
                    int currBar = (j + 1) / ensemble.getBeatsPerBar() - 1;
                    if ((ensemble.repetitions.elementAt(currBar)[1] > 1) && (ensemble.repetitions.elementAt(currBar)[2] > 1)) {
                        ensemble.repetitions.elementAt(currBar)[2]--; //Decrease Count
                        j = j - ensemble.getBeatsPerBar() * ensemble.repetitions.elementAt(currBar)[0];
                        if (j < -1) {
                            //Log.e(TAG, "Illegal repetition from bar " + currBar + " back " + ensemble.getBeatsPerBar() * ensemble.repetitions.elementAt(currBar)[0] + " impossible!");
                            j = -1;
                        }
                    }
                }

                // Loop Ensemble
                if ((j == ensemble.getBeats() - 1) && ensemble.onLoop && (!params.contains("record"))) {
                    //Reset ensemble repetitions
                    for (int i = 0; i < ensemble.repetitions.size(); i++)
                        ensemble.repetitions.elementAt(i)[2] = ensemble.repetitions.elementAt(i)[1];
                    j = -1;
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
            for (int i = 1; i < ensembleLayout.getChildCount() - 1; i++) { //Color the playing Bar in UI
                LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
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
                            ((ImageView) (instrumentLayout.getChildAt(j))).setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.playback_afrostudio));
                        }
                        currentBar++;
                    }
                }
            }

            if (ensemble.flagEnsembleUpdate) { //update Ensemble
                EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble);
                EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                djembeOffset.clear();
                for (int i = 0; i < ensemble.djembeVector.size(); i++)
                    djembeOffset.add(0);
                ensemble.flagEnsembleUpdate = false;
            }
        }

        @Override // Runs on UI
        protected void onPostExecute(Void param) { //Runs on UI
            for (int i = 0; i < ensembleLayout.getChildCount(); i++) { //Clear Color in UI
                LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
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

            FragmentMainBinding fragmentBinding = getFragmentBinding();

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
                        String path = MainActivity.this.getExternalFilesDir(null).getPath();
                        String user = settings.getString("user", "undefined@undefined").substring(0, settings.getString("user", "undefined@undefined").indexOf("@"));
                        String fileName = ensemble.ensembleName + "_by_" + ensemble.ensembleAuthor + "_u_" + user + ".aac";
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path + "/" + fileName));
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    } catch (NullPointerException e) {
                        Toast.makeText(MainActivity.this, "Cannot access storage", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Cannot access storage", Toast.LENGTH_SHORT).show();
                }
            }

            if (params.contains("reproduce")) {//reproduce
                String path = MainActivity.this.getExternalFilesDir(null).getPath();
                String user = settings.getString("user", "undefined@undefined").substring(0, settings.getString("user", "undefined@undefined").indexOf("@"));
                String fileName = ensemble.ensembleName + "_by_" + ensemble.ensembleAuthor + "_u_" + user + ".aac";

                Intent reproduceIntent = new Intent(Intent.ACTION_VIEW);
                reproduceIntent.setDataAndType(Uri.parse("file://" + path + "/" + fileName), "audio/mp4a-latm");
                startActivity(reproduceIntent);

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
    }

    private class ConnectServerTask extends AsyncTask<String, Integer, Void> { //<Params, Progress, Result>

        String response = null;
        String username = null;
        String action = null;
        String parameters = null;
        String ensembleName;
        String ensembleAuthor;

        @Override // Runs on UI
        protected void onPreExecute() {

        }

        @Override // Runs on separate Thread! No UI Updates
        protected Void doInBackground(String... strings) {

            username = strings[0];
            action = strings[1]; //setEnsemble, getEnsemble, getEnsembleList

            switch (action) {
                case "register":
                    parameters = "username=" + username + "&action=" + action;
                    break;
                case "setEnsemble":
                    ensembleName = strings[2];
                    ensembleAuthor = strings[3];
                    String ensembleJSON = strings[4];
                    parameters = "username=" + username + "&action=" + action + "&ensemblename=" + ensembleName + "&ensembleauthor=" + ensembleAuthor + "&ensemblejson=" + ensembleJSON;
                    break;
                case "getEnsemble":
                    ensembleName = strings[2];
                    ensembleAuthor = strings[3];
                    String ensembleUser = strings[4];
                    parameters = "username=" + username + "&action=" + action + "&ensemblename=" + ensembleName + "&ensembleauthor=" + ensembleAuthor + "&ensembleuser=" + ensembleUser;
                    break;
                case "getEnsembleList":
                    parameters = "username=" + username + "&action=" + action;
                    break;
                case "deleteEnsemble":
                    ensembleName = strings[2];
                    ensembleAuthor = strings[3];
                    parameters = "username=" + username + "&action=" + action + "&ensemblename=" + ensembleName + "&ensembleauthor=" + ensembleAuthor;
                    break;
            }

//            //Log.e(TAG, "parameters.length: " + parameters.length());
//            if(parameters.length()>65500){
//                response = "PostTooLong";
//                return null;
//            }

            HttpURLConnection connection;
            OutputStreamWriter request;
            URL url; // init to null

            try {
                url = new URL("http://www.diligenciasley.com.ar/afro_studio/share.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestMethod("POST");
                request = new OutputStreamWriter(connection.getOutputStream());
                request.write(parameters);
                request.flush();
                request.close();

                String line = "";
                InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null)
                    sb.append(line).append("\n");
                response = sb.toString(); // Response from server after login process will be stored in response variable.
                isr.close();
                reader.close();

            } catch (IOException e) {
                Log.e(TAG, "Error generating url " + e);
            }
            return null;
        }

        @Override // Runs on UI
        protected void onPostExecute(Void param) { //Runs on UI

            if (response != null){
                if (response.contains("PostTooLong")) {
                    Toast.makeText(MainActivity.this, "Too long to share!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            switch (action) {
                case "register":
                    break;
                case "setEnsemble":
                    if (response != null)
                        Toast.makeText(MainActivity.this, R.string.toast_share_ok, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MainActivity.this, R.string.toast_share_error, Toast.LENGTH_SHORT).show();
                    break;
                case "getEnsemble":
                    if (response != null) { //if got a response (i.e. wifi connection)
                        ensemble.setVectorsFromJSON(response);
                        EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.toast_getensemble_error, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "getEnsembleList":
                    if (response != null) { //if got a response (i.e. wifi connection)
                        List<String> newRemoteFileListString = new ArrayList<String>();
                        try {
                            String[] splitArray = response.split("[+]");
                            for (int i = 0; i < splitArray.length - 1; i++)
                                newRemoteFileListString.add(splitArray[i]);
                        } catch (PatternSyntaxException ex) {
                            Log.e(TAG, "PatternSyntaxException in getEnsembleList " + ex);
                        }
                        dialogFragmentLoad.updateRemoteFileListString(newRemoteFileListString);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.toast_getensemble_error, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "deleteEnsemble":
                    if (response != null) { //if got a response (i.e. wifi connection)
                        List<String> newRemoteFileListString = new ArrayList<String>();
                        try {
                            String[] splitArray = response.split("[+]");
                            for (int i = 0; i < splitArray.length - 1; i++)
                                newRemoteFileListString.add(splitArray[i]);
                        } catch (PatternSyntaxException ex) {
                            Log.e(TAG, "PatternSyntaxException in getEnsembleList " + ex);
                        }
                        dialogFragmentLoad.updateRemoteFileListString(newRemoteFileListString);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.toast_getensemble_error, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    private FragmentMainBinding getFragmentBinding() {
        MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        return fragment != null ? fragment.getBinding() : null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Init Stuff
        settings = getSharedPreferences(PREFS_NAME, 0);

        FragmentMainBinding fragmentBinding = getFragmentBinding();

        ensembleLayout = fragmentBinding.ensembleLayout;
        horizontalScrollView = (HorizontalScrollView) ensembleLayout.getParent();
        viewAnimator = fragmentBinding.animatorIntro;
        ensemble = new Ensemble(MainActivity.this);

        // Init Rate Image (shown in help and randomly after several start ups
        ImageView rateImage = fragmentBinding.tutRateImage;
        rateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                settings.edit().putBoolean("show_rate", false).apply();
            }
        });

        // First Time Intro
        if (settings.getBoolean("first_time_2", true)) {

            Animation slide_in_left = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            Animation slide_out_right = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
            viewAnimator.setOutAnimation(slide_out_right);
            viewAnimator.setInAnimation(slide_in_left);
            viewAnimator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((ViewAnimator) v).getCurrentView().getId() == R.id.tut_welcome) {
                        Animation slide_out_right = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_out_right);
                        slide_out_right.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                viewAnimator.setVisibility(View.INVISIBLE);
                                if (settings.getString("user", "undefined@undefined").equals("undefined@undefined")) {
                                    DialogFragmentEmail dialogFragmentEmail = new DialogFragmentEmail();
                                    dialogFragmentEmail.show(getSupportFragmentManager(), "SetEmail");
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        v.startAnimation(slide_out_right);
                    } else
                        ((ViewAnimator) v).showNext();
                }
            });

            // Get current gmail address for username
            AccountManager am = AccountManager.get(MainActivity.this);
            Account[] accounts = am.getAccounts();
            String googleAccount = "undefined@undefined"; // if not account present use this one
            for (Account ac : accounts)
                if (ac.name.contains("@")) // use any email present
                    googleAccount = ac.name;
            for (Account ac : accounts)
                if (ac.type.equals("com.google"))  // use google account
                    googleAccount = ac.name;
            settings.edit().putString("user", googleAccount).apply();

            settings.edit().putBoolean("first_time_2", false).apply();

        } else { // Remove Animator from Master Layout
            viewAnimator.setVisibility(View.INVISIBLE);
            // Check account exists (?)
            if ((rnd.nextInt(3) < 1) && (settings.getBoolean("show_rate", true))) // 1 de cada 5 pop up support Afro...
                EnsembleUtils.popSupportAfroStudioMessage(viewAnimator, MainActivity.this);
        }

        // Verify filenames compatibility
        EnsembleUtils.correctFileNames(MainActivity.this, settings.getString("user", "undefined@undefined"));

        // Register User
        ConnectServerTask connectServerTask = new ConnectServerTask();
        connectServerTask.execute(settings.getString("user", "undefined@undefined"), "register");

        // Low Buttons init
        ImageView but_zoomIn = fragmentBinding.butZoomIn; // Init Buttons
        but_zoomIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                iconScale += 0.05;
                EnsembleUtils.setImageScale(ensembleLayout, horizontalScrollView, MainActivity.this, iconScale, ensemble, viewAnimator);
            }
        });
        ImageView but_zoomOut = fragmentBinding.butZoomOut;
        but_zoomOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                iconScale -= 0.05;
                EnsembleUtils.setImageScale(ensembleLayout, horizontalScrollView, MainActivity.this, iconScale, ensemble, viewAnimator);
            }
        });
        ImageView but_play = fragmentBinding.butPlay;
        but_play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!ensemble.onPlay) { // Play
                    ImageView but_play = fragmentBinding.butPlay;
                    but_play.setImageResource(R.drawable.but_stop);
                    PlayRithmTask playRithmTask = new PlayRithmTask();
                    playRithmTask.params = "";
                    playRithmTask.execute("");
                } else {  // Stop
                    ensemble.onPlay = false;
                    ImageView but_play = fragmentBinding.butPlay;
                    but_play.setImageResource(R.drawable.but_play);
                }
            }
        });
        but_play.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!ensemble.onPlay) { // Play
                    ImageView but_play = fragmentBinding.butPlay;
                    but_play.setImageResource(R.drawable.but_rec);
                    PlayRithmTask playRithmTask = new PlayRithmTask();
                    playRithmTask.params = "record reproduce";
                    playRithmTask.execute("");
                }
                return true;
            }
        });

        ImageView but_equ = fragmentBinding.butEqu;
        but_equ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragmentEqualizer dialogFragmentEqualizer = new DialogFragmentEqualizer();
                dialogFragmentEqualizer.activityContext = MainActivity.this;
                dialogFragmentEqualizer.ensemble = ensemble;
                dialogFragmentEqualizer.ensembleLayout = ensembleLayout;
                dialogFragmentEqualizer.show(getSupportFragmentManager(), "Equalizer");
            }
        });

        ImageView but_tempo = fragmentBinding.butTempo;
        but_tempo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragmentTempoSettings dialogFragmentTempoSettings = new DialogFragmentTempoSettings();
                dialogFragmentTempoSettings.ensemble = ensemble;
                dialogFragmentTempoSettings.show(getSupportFragmentManager(), "TempoSettings");
            }
        });
        TextView tempoText = fragmentBinding.tempoText;
        tempoText.setText(ensemble.bpm + " bpm");

        horizontalScrollView.setTag(R.string.tag0, "smallIconsUnSet");
        horizontalScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((v.getScrollX() > 120) && String.valueOf((v.getTag(R.string.tag0))).equals("smallIconsUnSet")) { // set small icons
                    v.setTag(R.string.tag0, "smallIconsSet");
                    final float displayDensity = getResources().getDisplayMetrics().density;
                    for (int i = 1; i < ensembleLayout.getChildCount() - 1; i++) { // only instruments icons
                        LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
                        ImageView smallIconImageView = new ImageView(MainActivity.this);
                        switch (String.valueOf(instrumentLayout.getTag(R.string.tag0))) {
                            case "ico_djembe":
                            case "ico_djembe_1":
                            case "ico_djembe_2":
                            case "ico_djembe_3":
                                smallIconImageView.setImageResource(R.drawable.ico_djembe);
                                break;
                            case "ico_dun":
                                smallIconImageView.setImageResource(R.drawable.ico_dun);
                                break;
                            case "ico_sag":
                                smallIconImageView.setImageResource(R.drawable.ico_sag);
                                break;
                            case "ico_ken":
                                smallIconImageView.setImageResource(R.drawable.ico_ken);
                                break;
                            case "ico_shek":
                                smallIconImageView.setImageResource(R.drawable.ico_shek);
                                break;
                            case "ico_balet":
                                smallIconImageView.setImageResource(R.drawable.ico_balet);
                                break;
                        }

                        smallIconImageView.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.green_afrostudio));
                        smallIconImageView.setAlpha((float) 0.3);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f));
                        params.topMargin = instrumentLayout.getTop();

                        RelativeLayout relativeLayout = new RelativeLayout(MainActivity.this);
                        relativeLayout.setTag(R.string.tag0, "ico_small");
                        relativeLayout.addView(smallIconImageView, params);
                        relativeLayout.setPadding(0, (int) (iconScale * 10 * displayDensity + 0.5f), 0, (int) (iconScale * 10 * displayDensity + 0.5f)); // (int left, int top, int right, int bottom) in pixels
                        ((FrameLayout) v.getParent()).addView(relativeLayout);

                        Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setInterpolator(new AccelerateInterpolator());
                        fadeIn.setDuration(1000);
                        smallIconImageView.startAnimation(fadeIn);
                    }

                } else if ((v.getScrollX() <= 120) && String.valueOf((v.getTag(R.string.tag0))).equals("smallIconsSet")) { // remove small icons
                    v.setTag(R.string.tag0, "smallIconsUnSet");
                    final FrameLayout frameLayout = (FrameLayout) v.getParent();
                    Vector<View> allView = new Vector<View>();
                    for (int i = 0; i < frameLayout.getChildCount(); i++)
                        if (String.valueOf(frameLayout.getChildAt(i).getTag(R.string.tag0)).equals("ico_small"))
                            allView.add(frameLayout.getChildAt(i));
                    for (int i = 0; i < allView.size(); i++) {
                        final View smallIconView = allView.elementAt(i);
                        Animation fadeOut = new AlphaAnimation(1, 0);
                        fadeOut.setInterpolator(new AccelerateInterpolator());
                        fadeOut.setDuration(10);
                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                            public void onAnimationEnd(Animation animation) {
                                frameLayout.removeView(smallIconView);
                            }

                            public void onAnimationRepeat(Animation animation) {
                            }

                            public void onAnimationStart(Animation animation) {
                            }
                        });
                        smallIconView.startAnimation(fadeOut);
                    }

                }
                return false;
            }
        });

        // Always generate this Demo Rithms and save them so they can be loaded
        ensemble.setVectorsFromRaw(R.raw.afr_sinte_demo, MainActivity.this);
        EnsembleUtils.saveEnsemble(ensemble, MainActivity.this, false, false, viewAnimator, settings.getString("user", "undefined@undefined"));
        ensemble.setVectorsFromRaw(R.raw.afr_dundunbe_demo, MainActivity.this);
        EnsembleUtils.saveEnsemble(ensemble, MainActivity.this, false, false, viewAnimator, settings.getString("user", "undefined@undefined"));
        ensemble.setVectorsFromRaw(R.raw.afr_kuku_demo, MainActivity.this);
        EnsembleUtils.saveEnsemble(ensemble, MainActivity.this, false, false, viewAnimator, settings.getString("user", "undefined@undefined"));
        ensemble.setVectorsFromRaw(R.raw.afr_soko_demo, MainActivity.this);
        EnsembleUtils.saveEnsemble(ensemble, MainActivity.this, false, false, viewAnimator, settings.getString("user", "undefined@undefined"));
    }

    // https://www.mobomo.com/2011/06/android-understanding-activity-launchmode/

    @Override
    protected void onResume() { // Called when the activity will start interacting with the user.
        ConnectServerTask connectServerTask = new ConnectServerTask();
        connectServerTask.execute(settings.getString("user", "undefined@undefined"), "register");

        File file = new File(getCacheDir(), this.hashCode() + ".tmp");
        if (file.exists()) { // there is a tem file for this hash id?, load it!
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) { // Media Ready to at least read
                try {
                    InputStream inputStream = new FileInputStream(file);
                    java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                    String jsonString = s.hasNext() ? s.next() : "";
                    ensemble.setVectorsFromJSON(jsonString);
                    EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                } catch (IOException e) { // Unable to create file, likely because external storage is not currently mounted.
                    Log.e(TAG, "Error loading from external mem: " + file, e);
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.toast_storage_not_ready, Toast.LENGTH_SHORT).show();
            }
        } else { // there is not a temp file, check if view intent or just put a new rythm on screen.
            Intent intent = getIntent();
            String action = intent.getAction();
            if (action.compareTo(Intent.ACTION_VIEW) == 0) {
                String scheme = intent.getScheme();
                Uri uri = intent.getData();
                ContentResolver resolver = getContentResolver();
                if ((scheme.compareTo(ContentResolver.SCHEME_FILE) == 0) || (scheme.compareTo(ContentResolver.SCHEME_CONTENT) == 0)) {

                    String state = Environment.getExternalStorageState();
                    if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) { // Media Ready to at least read
                        try {
                            InputStream inputStream = resolver.openInputStream(uri);
                            //InputStream inputStream = new FileInputStream(file);
                            java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                            String jsonString = s.hasNext() ? s.next() : "";
                            ensemble.setVectorsFromJSON(jsonString);
                            EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                        } catch (IOException e) {
                            Log.e(TAG, "Error generating inputStream for loading " + uri, e);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, R.string.toast_storage_not_ready, Toast.LENGTH_SHORT).show();
                    }
                }
            } else { // Set empty ensemble to begin
                ensemble.setVectorsFromEmpty(16, 1, true, MainActivity.this); //Empty Ensemble
                EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                // Set initial GUI
                ((TextView) ((LinearLayout) ensembleLayout.getChildAt(0)).getChildAt(0)).setText(R.string.init_string); // set custom ensebleName
            }
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        // commit unsaved changes to persistent data, stop animations and other things that may be consuming CPU
        // onResume takes the control once back
        if (ensemble.onPlay) { // Stop Playback and recording
            ensemble.onPlay = false;
            try {
                Thread.sleep(200); //wait async task to find a pause value
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            ensemble.audioTrack.stop();
        }

        // Store work in temp file with this app hash Id.
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) { //Media Ready to read and write
            String fileName = this.hashCode() + ".tmp";
            File file = new File(getCacheDir(), fileName);
            try {
                OutputStream outputStream = new FileOutputStream(file);
                outputStream.write(ensemble.saveVectorsInJSON().getBytes());
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error writing on cache: " + file, e);
            }
        }
        super.onPause();
    }

    // MENU FUNCTIONS
    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentMainBinding fragmentBinding = getFragmentBinding();

        int id = item.getItemId();

        if (id == R.id.action_new) {
            DialogFragmentNewEnsemble dialogFragmentNewEnsemble = new DialogFragmentNewEnsemble();
            dialogFragmentNewEnsemble.show(getSupportFragmentManager(), "NewEnsemble");
            return true;
        }

        if (id == R.id.action_load) {
            // Stop Playback if any
            if (ensemble.onPlay) {
                ImageView but_play = fragmentBinding.butPlay;
                but_play.callOnClick();
            }

            // Get Server File list
            ConnectServerTask connectServerTask = new ConnectServerTask();
            connectServerTask.execute(settings.getString("user", "undefined@undefined"), "getEnsembleList", ensemble.ensembleName, ensemble.ensembleAuthor, ensemble.saveVectorsInJSON());

            dialogFragmentLoad = new DialogFragmentLoad();
            dialogFragmentLoad.localUser = settings.getString("user", "undefined@undefined");
            dialogFragmentLoad.dir = this.getExternalFilesDir(null);
            dialogFragmentLoad.activityContext = MainActivity.this;
            dialogFragmentLoad.show(((FragmentActivity) MainActivity.this).getSupportFragmentManager(), "Load");
            return true;
        }

        if (id == R.id.action_save) {
            EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble); //creo que puedo hacer onPlay
            EnsembleUtils.saveEnsemble(ensemble, MainActivity.this, true, true, viewAnimator, settings.getString("user", "undefined@undefined"));
            return true;
        }

        if (id == R.id.action_upload) {
            if (EnsembleUtils.AfroStudioVersion.equals("free")) { // no sharing with free app
                EnsembleUtils.popGetFullAfroStudioMessage(viewAnimator, MainActivity.this);
                return true;
            }

            if (ensemble.ensembleName.equals("Unnamed") || ensemble.ensembleName.equals("Sin Nombre") || ensemble.ensembleAuthor.equals("Autor") || ensemble.ensembleAuthor.equals("Author")) {
                Toast.makeText(MainActivity.this, "Set name and author before sharing", Toast.LENGTH_SHORT).show();
                return true;
            }

            // Stop Playback if any
            if (ensemble.onPlay) {
                ImageView but_play = fragmentBinding.butPlay;
                but_play.callOnClick();
            }

            ConnectServerTask connectServerTask = new ConnectServerTask();
            connectServerTask.execute(settings.getString("user", "undefined@undefined"), "setEnsemble", ensemble.ensembleName, ensemble.ensembleAuthor, ensemble.saveVectorsInJSON());
            return true;
        }

        if (id == R.id.action_share) {
            DialogFragmentShare dialogFragmentShare = new DialogFragmentShare();
            dialogFragmentShare.show(getSupportFragmentManager(), "ShareDialog");
        }

        if (id == R.id.action_about) {
            DialogFragmentAbout dialogFragmentAbout = new DialogFragmentAbout();
            dialogFragmentAbout.activityContext = MainActivity.this;
            dialogFragmentAbout.AfroStudioVersion = EnsembleUtils.AfroStudioVersion;
            dialogFragmentAbout.show(getSupportFragmentManager(), "AboutDialog");
            return true;
        }

        if (id == R.id.action_help) {
            Animation slide_in_left = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            Animation slide_out_right = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
            viewAnimator.setOutAnimation(null);
            viewAnimator.setInAnimation(null);
            viewAnimator.setDisplayedChild(1);
            viewAnimator.setOutAnimation(slide_out_right);
            viewAnimator.setInAnimation(slide_in_left);
            viewAnimator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((ViewAnimator) v).getCurrentView().getId() == R.id.tut_rate) {
                        v.setOnClickListener(null); // avoid bouncing if several clicks
                        Animation slide_out_right = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_out_right);
                        slide_out_right.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                viewAnimator.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        v.startAnimation(slide_out_right);
                    } else
                        ((ViewAnimator) v).showNext();
                }
            });
            viewAnimator.startAnimation(slide_in_left);
            viewAnimator.setVisibility(View.VISIBLE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // INTERFACES
    public void addInstrumentPositiveClick(Vector<String> instrumentChoice) {

        //horizontalScrollView just remove the smallIcons, they are to be added as soon as new horizontal scrollbar is pushed
        horizontalScrollView.setTag(R.string.tag0, "smallIconsUnSet");
        final FrameLayout frameLayout = (FrameLayout) horizontalScrollView.getParent();
        Vector<View> allView = new Vector<View>();
        for (int i = 0; i < frameLayout.getChildCount(); i++)
            if (String.valueOf(frameLayout.getChildAt(i).getTag(R.string.tag0)).equals("ico_small"))
                allView.add(frameLayout.getChildAt(i));
        for (int i = 0; i < allView.size(); i++)
            frameLayout.removeView(allView.elementAt(i));
        horizontalScrollView.callOnClick();

        for (int i = 0; i < instrumentChoice.size(); i++)
            EnsembleUtils.addInstrumentInGui(instrumentChoice.elementAt(i), MainActivity.this, ensembleLayout, iconScale, ensemble, 100, 1, viewAnimator);
        if (ensemble.onPlay)
            ensemble.flagEnsembleUpdate = true;
        else {
            EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble); // To fix repetition ico_div issue
            EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
        }
    }

    public void tempoSettingsPositiveClick() {
        FragmentMainBinding fragmentBinding = getFragmentBinding();

        TextView tempoText = fragmentBinding.tempoText;
        tempoText.setText(ensemble.bpm + " bpm");
    }

    public void newEnsemblePositiveClick(int beatsPerBar, int numBar, boolean emptyInstruments) {
        FragmentMainBinding fragmentBinding = getFragmentBinding();

        if ((EnsembleUtils.AfroStudioVersion.equals("free")) && (numBar > 1)) {
            numBar = 1;
        }

        //horizontalScrollView just remove the smallIcons, they are to be added as soon as new horizontal scrollbar is pushed
        horizontalScrollView.setTag(R.string.tag0, "smallIconsUnSet");
        final FrameLayout frameLayout = (FrameLayout) horizontalScrollView.getParent();
        Vector<View> allView = new Vector<View>();
        for (int i = 0; i < frameLayout.getChildCount(); i++)
            if (String.valueOf(frameLayout.getChildAt(i).getTag(R.string.tag0)).equals("ico_small"))
                allView.add(frameLayout.getChildAt(i));
        for (int i = 0; i < allView.size(); i++)
            frameLayout.removeView(allView.elementAt(i));
        horizontalScrollView.callOnClick();

        if (ensemble.onPlay) {
            ensemble.onPlay = false;
            try {
                Thread.sleep(200); //wait async task to find a pause value
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ImageView but_play = fragmentBinding.butPlay;
            but_play.setImageResource(R.drawable.but_play);
        }

        ensemble.setVectorsFromEmpty(beatsPerBar, numBar, emptyInstruments, MainActivity.this);
        EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
    }

    public void instrumentOptionsPositiveClick(String option, int volume, ImageView instImage) {

        switch (option) { // duplicate, clear, remove when different than "" (onPlay)
            case "clear":
                LinearLayout instrumentLayout = (LinearLayout) instImage.getParent();
                for (int i = 0; i < instrumentLayout.getChildCount(); i++) {
                    if (String.valueOf(instrumentLayout.getChildAt(i).getTag(R.string.tag0)).contains("ico_ini")) {
                        ((ImageView) instrumentLayout.getChildAt(i)).setImageResource(R.drawable.ico_ini);
                        instrumentLayout.getChildAt(i).setTag(R.string.tag0, "ico_ini");
                    }
                    if (String.valueOf(instrumentLayout.getChildAt(i).getTag(R.string.tag0)).contains("ico_mid")) {
                        ((ImageView) instrumentLayout.getChildAt(i)).setImageResource(R.drawable.ico_mid);
                        instrumentLayout.getChildAt(i).setTag(R.string.tag0, "ico_mid");
                    }
                    if (String.valueOf(instrumentLayout.getChildAt(i).getTag(R.string.tag0)).contains("ico_end")) {
                        ((ImageView) instrumentLayout.getChildAt(i)).setImageResource(R.drawable.ico_end);
                        instrumentLayout.getChildAt(i).setTag(R.string.tag0, "ico_end");
                    }
                }
                if (ensemble.onPlay)
                    ensemble.flagEnsembleUpdate = true;
                else {
                    EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble);
                    EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                }
                break;
            case "remove":
                ((LinearLayout) instImage.getParent().getParent()).removeView((View) instImage.getParent());

                //horizontalScrollView just remove the smallIcons, they are to be added as soon as new horizontal scrollbar is pushed
                horizontalScrollView.setTag(R.string.tag0, "smallIconsUnSet");
                final FrameLayout frameLayout = (FrameLayout) horizontalScrollView.getParent();
                Vector<View> allView = new Vector<View>();
                for (int i = 0; i < frameLayout.getChildCount(); i++)
                    if (String.valueOf(frameLayout.getChildAt(i).getTag(R.string.tag0)).equals("ico_small"))
                        allView.add(frameLayout.getChildAt(i));
                for (int i = 0; i < allView.size(); i++)
                    frameLayout.removeView(allView.elementAt(i));
                horizontalScrollView.callOnClick();

                if (ensemble.onPlay)
                    ensemble.flagEnsembleUpdate = true;
                else {
                    EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble);
                    EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                }

                break;
        }
    }

    public void setRepetitionPositiveClick(int thisBar, int barCount, int countTotal) {

        ensemble.repetitions.elementAt(thisBar)[0] = barCount; //barCount
        ensemble.repetitions.elementAt(thisBar)[1] = countTotal; //total
        ensemble.repetitions.elementAt(thisBar)[2] = countTotal; //current

        if (ensemble.onPlay)
            ensemble.flagEnsembleUpdate = true;
        else {
            EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble);
            EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
        }
    }

    public void setNamePositiveClick(String name, String author) {
        ensemble.ensembleName = name;
        ensemble.ensembleAuthor = author;
        ((TextView) ((LinearLayout) ensembleLayout.getChildAt(0)).getChildAt(0)).setText(name + " - by " + author);
    }

    public void loadPositiveClick(String fileName) {
        FragmentMainBinding fragmentBinding = getFragmentBinding();

        //horizontalScrollView just remove the smallIcons, they are to be added as soon as new horizontal scrollbar is pushed
        horizontalScrollView.setTag(R.string.tag0, "smallIconsUnSet");
        final FrameLayout frameLayout = (FrameLayout) horizontalScrollView.getParent();
        Vector<View> allView = new Vector<View>();
        for (int i = 0; i < frameLayout.getChildCount(); i++)
            if (String.valueOf(frameLayout.getChildAt(i).getTag(R.string.tag0)).equals("ico_small"))
                allView.add(frameLayout.getChildAt(i));
        for (int i = 0; i < allView.size(); i++)
            frameLayout.removeView(allView.elementAt(i));
        horizontalScrollView.callOnClick();

        if (fileName != null) {
            if (ensemble.onPlay) {
                ensemble.onPlay = false;
                try {
                    Thread.sleep(200); //wait async task to find a pause value
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ImageView but_play = fragmentBinding.butPlay;
                but_play.setImageResource(R.drawable.but_play);
            }

            if (fileName.contains(".afr")) { // File is local
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) { // Media Ready to at least read
                    File file = new File(getExternalFilesDir(null), fileName);
                    try {
                        InputStream inputStream = new FileInputStream(file);
                        java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                        String jsonString = s.hasNext() ? s.next() : "";
                        ensemble.setVectorsFromJSON(jsonString);
                        EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                    } catch (IOException e) { // Unable to create file, likely because external storage is not currently mounted.
                        Log.e("ExternalStorage", "Error reading " + file, e);
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.toast_storage_not_ready, Toast.LENGTH_SHORT).show();
                }
            } else { // File in server
                String ensembleName = fileName.substring(0, fileName.indexOf("_by_"));
                String ensembleAuthor = fileName.substring(fileName.indexOf("_by_") + 4, fileName.indexOf("_u_"));
                String ensembleUser = fileName.substring(fileName.indexOf("_u_") + 3, fileName.length());

                ConnectServerTask connectServerTask = new ConnectServerTask();
                connectServerTask.execute(settings.getString("user", "undefined@undefined"), "getEnsemble", ensembleName, ensembleAuthor, ensembleUser);
                // wait for ensemble to load?
            }
        } else {
            Toast.makeText(MainActivity.this, R.string.toast_no_file_chosen, Toast.LENGTH_SHORT).show();
        }
    }

    public void loadDeleteEnsembleInServer(String fileName) {
        String name = fileName.substring(0, fileName.indexOf("_by_"));
        String author = fileName.substring(fileName.indexOf("_by_") + 4, fileName.indexOf("_u_"));

        // Delete (returns and updates the list in dialog once completed
        ConnectServerTask connectServerTask = new ConnectServerTask();
        connectServerTask.execute(settings.getString("user", "undefined@undefined"), "deleteEnsemble", name, author);
    }

    public void equalizerPositiveClick() {
        EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
    }

    public void barOptionsPositiveClick(int barPos, String option, String name) {

        ensemble.barName.setElementAt(name, barPos);// set Name
        EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);

        switch (option) { //browse option (options != ""
            case "clear":
                for (int i = 0; i < ensemble.djembeVector.size(); i++)
                    for (int j = ensemble.getBeatsPerBar() * barPos; j < ensemble.getBeatsPerBar() * (barPos + 1); j++)
                        ensemble.djembeVector.elementAt(i).setElementAt(0, j);
                for (int i = 0; i < ensemble.shekVector.size(); i++)
                    for (int j = ensemble.getBeatsPerBar() * barPos; j < ensemble.getBeatsPerBar() * (barPos + 1); j++)
                        ensemble.shekVector.elementAt(i).setElementAt(0, j);
                for (int i = 0; i < ensemble.dunVector.size(); i++)
                    for (int j = ensemble.getBeatsPerBar() * barPos; j < ensemble.getBeatsPerBar() * (barPos + 1); j++)
                        ensemble.dunVector.elementAt(i).setElementAt(0, j);
                for (int i = 0; i < ensemble.sagVector.size(); i++)
                    for (int j = ensemble.getBeatsPerBar() * barPos; j < ensemble.getBeatsPerBar() * (barPos + 1); j++)
                        ensemble.sagVector.elementAt(i).setElementAt(0, j);
                for (int i = 0; i < ensemble.kenVector.size(); i++)
                    for (int j = ensemble.getBeatsPerBar() * barPos; j < ensemble.getBeatsPerBar() * (barPos + 1); j++)
                        ensemble.kenVector.elementAt(i).setElementAt(0, j);
                for (int i = 0; i < ensemble.baletVector.size(); i++)
                    for (int j = ensemble.getBeatsPerBar() * barPos; j < ensemble.getBeatsPerBar() * (barPos + 1); j++)
                        ensemble.baletVector.elementAt(i).setElementAt(0, j);
                EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                break;
            case "remove": // do in gui so it can be done in real time
                if (EnsembleUtils.AfroStudioVersion.equals("free")) {
                    EnsembleUtils.popGetFullAfroStudioMessage(viewAnimator, MainActivity.this);
                    return;
                }
                for (int i = 0; i < ensembleLayout.getChildCount(); i++) {
                    LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
                    Vector<View> viewsToDelete = new Vector<>();
                    if ((String.valueOf(instrumentLayout.getTag(R.string.tag0)).contains("ico_djembe")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_shek")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_dun")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_sag")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_ken")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_balet")))
                        for (int j = (ensemble.getBeatsPerBar() + 1) * barPos + 1; j < (ensemble.getBeatsPerBar() + 1) * (barPos + 1) + 1; j++)
                            viewsToDelete.add(instrumentLayout.getChildAt(j));
                    if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("last_inst")) {
                        viewsToDelete.add(instrumentLayout.getChildAt(barPos + 1)); //first element is + icon
                    }
                    for (int j = 0; j < viewsToDelete.size(); j++)
                        instrumentLayout.removeView(viewsToDelete.elementAt(j));
                }
                //Also remove the repetitions in ensemble which is not updated from GUI
                ensemble.repetitions.remove(barPos);
                if (ensemble.onPlay)
                    ensemble.flagEnsembleUpdate = true;
                else {
                    EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble);
                    EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                }
                break;
            case "clone_bar": // do in gui so it can be done in real time
                if (EnsembleUtils.AfroStudioVersion.equals("free")) {
                    EnsembleUtils.popGetFullAfroStudioMessage(viewAnimator, MainActivity.this);
                    return;
                }
                for (int i = 0; i < ensembleLayout.getChildCount(); i++) {
                    LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
                    if ((String.valueOf(instrumentLayout.getTag(R.string.tag0)).contains("ico_djembe")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_shek")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_dun")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_sag")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_ken")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_balet")))
                        for (int j = (ensemble.getBeatsPerBar() + 1) * barPos + 2; j < (ensemble.getBeatsPerBar() + 1) * (barPos + 1) + 1; j++) {
                            //Log.e(TAG, "barPos=" + barPos + ", j = " + ((ensemble.getBeatsPerBar() + 1) * barPos + 2) + " - " + ((ensemble.getBeatsPerBar() + 1) * (barPos + 1) + 1));
                            ImageView clonedImage = new ImageView(this);
                            clonedImage.setImageDrawable(((ImageView) instrumentLayout.getChildAt(j)).getDrawable());
                            clonedImage.setScaleType(((ImageView) instrumentLayout.getChildAt(j)).getScaleType());
                            clonedImage.setTag(R.string.tag0, (instrumentLayout.getChildAt(j)).getTag(R.string.tag0)); //they wont have a second tag
                            try {
                                clonedImage.setLayoutParams((instrumentLayout.getChildAt(j)).getLayoutParams());
                            } catch (Exception e) {
                            }
                            instrumentLayout.addView(clonedImage, j + (ensemble.getBeatsPerBar() + 1));
                        }
                    // Bar Names
                    if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("last_inst")) {
                        TextView clonedText = new TextView(this);
                        clonedText.setTag(R.string.tag0, (instrumentLayout.getChildAt(barPos + 1)).getTag(R.string.tag0)); //they wont have a second tag
                        try {
                            clonedText.setLayoutParams((instrumentLayout.getChildAt(barPos + 1)).getLayoutParams());
                        } catch (Exception e) {
                        }
                        instrumentLayout.addView(clonedText, barPos + 1);
                    }
                }

                if (ensemble.onPlay)
                    ensemble.flagEnsembleUpdate = true;
                else {
                    EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble);
                    EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                }
                break;
        }
    }

    public void specialStrokePositiveClick() {
        EnsembleUtils.updateEnsembleFromGui(ensembleLayout, ensemble);
    }

    public void emailPositiveClick(String email) {
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            settings.edit().putString("user", email).apply();
        else
            Toast.makeText(MainActivity.this, "Email not valid, using 'undefined'.", Toast.LENGTH_SHORT).show();
    }

    public void sharePositiveClick(String option){
        FragmentMainBinding fragmentBinding = getFragmentBinding();

        Log.e(TAG, "option: " + option );

        // Save audio
        if (option.equals("audio")){
            if (ensemble.onPlay) { // Stop Playback if any
                ensemble.onPlay = false;
                try {
                    Thread.sleep(500); //wait async task to find a pause value
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ImageView but_play = fragmentBinding.butPlay;
            but_play.setImageResource(R.drawable.but_rec);
            PlayRithmTask playRithmTask = new PlayRithmTask();
            playRithmTask.params = "record share";
            playRithmTask.execute("");
        } else if (option.equals("ensemble")){
            if (EnsembleUtils.AfroStudioVersion.equals("free")) { // Do not save in free version
                EnsembleUtils.popGetFullAfroStudioMessage(viewAnimator, this);
            } else {
                // Save ensemble
                EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble); //creo que puedo hacer onPlay
                EnsembleUtils.saveEnsemble(ensemble, MainActivity.this, true, true, viewAnimator, settings.getString("user", "undefined@undefined"));

                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    //sharingIntent.setType("application/octet-stream");
                    sharingIntent.setType("application/json");
                    try {
                        String path = MainActivity.this.getExternalFilesDir(null).getPath();
                        String user = settings.getString("user", "undefined@undefined").substring(0, settings.getString("user", "undefined@undefined").indexOf("@"));
                        String fileName = ensemble.ensembleName + "_by_" + ensemble.ensembleAuthor + "_u_" + user + ".afr";
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path + "/" + fileName));
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    } catch (NullPointerException e) {
                        Toast.makeText(MainActivity.this, "Cannot access storage", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Cannot access storage", Toast.LENGTH_SHORT).show();
                }

            }
        }

    }
}
