package com.yaray.afrostudio;

import android.app.Activity;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class EnsembleUtils {

    //public static final String AfroStudioVersion = "free";
    public static final String AfroStudioVersion = "premium";

    @SuppressWarnings("unused")
    private static final String TAG = "EnsembleUtils";

    // This functions chances ensemble vectors without modifying it length for real time changes while playback
    public static synchronized void updateEnsembleFromGui(LinearLayout ensembleLayout, Ensemble ensemble) {

        int djembeVectorPos = 0;
        int dunVectorPos = 0;
        int kenVectorPos = 0;
        int sagVectorPos = 0;
        int shekVectorPos = 0;
        int baletVectorPos = 0;
        for (int i = 0; i < ensembleLayout.getChildCount(); i++) {
            LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
            if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).contains("ico_djembe")) { // Got a Djembe
                int barPos = 0;
                for (int j = 0; j < instrumentLayout.getChildCount(); j++) { // Always in order!
                    if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end")) {
                        ensemble.djembeVector.elementAt(djembeVectorPos).set(barPos, 0);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bass") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bass") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bass")) {
                        ensemble.djembeVector.elementAt(djembeVectorPos).set(barPos, 1);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone")) {
                        ensemble.djembeVector.elementAt(djembeVectorPos).set(barPos, 2);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_slap") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_slap") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_slap")) {
                        ensemble.djembeVector.elementAt(djembeVectorPos).set(barPos, 3);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bass_flam") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bass_flam") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bass_flam")) {
                        ensemble.djembeVector.elementAt(djembeVectorPos).set(barPos, 4);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_flam") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_flam") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_flam")) {
                        ensemble.djembeVector.elementAt(djembeVectorPos).set(barPos, 5);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_slap_flam") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_slap_flam") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_slap_flam")) {
                        ensemble.djembeVector.elementAt(djembeVectorPos).set(barPos, 6);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("enabled")) {
                        ensemble.djembeStatus.set(djembeVectorPos, 1); // Enable
                        ensemble.djembeVolume.set(djembeVectorPos, (Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("disabled"))
                        ensemble.djembeStatus.set(djembeVectorPos, 0); // Disable
                }
                djembeVectorPos++;
            }
            if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_dun")) { // Got a Dun
                int barPos = 0;
                for (int j = 0; j < instrumentLayout.getChildCount(); j++) {
                    if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end")) {
                        ensemble.dunVector.elementAt(dunVectorPos).set(barPos, 0);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bass_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bass_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bass_bell")) {
                        ensemble.dunVector.elementAt(dunVectorPos).set(barPos, 1);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bell")) {
                        ensemble.dunVector.elementAt(dunVectorPos).set(barPos, 2);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bass_bell_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bass_bell_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bass_bell_mute")) {
                        ensemble.dunVector.elementAt(dunVectorPos).set(barPos, 3);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("enabled")) {
                        ensemble.dunStatus.set(dunVectorPos, 1);
                        ensemble.dunVolume.set(dunVectorPos, (Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } // Enable
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("disabled"))
                        ensemble.dunStatus.set(dunVectorPos, 0); // Disable
                }
                dunVectorPos++;
            }
            if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_ken")) { // Got a Ken
                int barPos = 0;
                for (int j = 0; j < instrumentLayout.getChildCount(); j++) {
                    if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end")) {
                        ensemble.kenVector.elementAt(kenVectorPos).set(barPos, 0);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_bell_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_bell_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_bell_white")) {
                        ensemble.kenVector.elementAt(kenVectorPos).set(barPos, 1);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bell")) {
                        ensemble.kenVector.elementAt(kenVectorPos).set(barPos, 2);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_bell_mute_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_bell_mute_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_bell_mute_white")) {
                        ensemble.kenVector.elementAt(kenVectorPos).set(barPos, 3);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("enabled")) {
                        ensemble.kenStatus.set(kenVectorPos, 1);
                        ensemble.kenVolume.set(kenVectorPos, (Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } // Enable
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("disabled"))
                        ensemble.kenStatus.set(kenVectorPos, 0); // Disable
                }
                kenVectorPos++;
            }
            if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_sag")) { // Got a Sag
                int barPos = 0;
                for (int j = 0; j < instrumentLayout.getChildCount(); j++) {
                    if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end")) {
                        ensemble.sagVector.elementAt(sagVectorPos).set(barPos, 0);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_bell")) {
                        ensemble.sagVector.elementAt(sagVectorPos).set(barPos, 1);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bell")) {
                        ensemble.sagVector.elementAt(sagVectorPos).set(barPos, 2);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_bell_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_bell_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_bell_mute")) {
                        ensemble.sagVector.elementAt(sagVectorPos).set(barPos, 3);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("enabled")) {
                        ensemble.sagStatus.set(sagVectorPos, 1);
                        ensemble.sagVolume.set(sagVectorPos, (Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } // Enable
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("disabled"))
                        ensemble.sagStatus.set(sagVectorPos, 0); // Disable
                }
                sagVectorPos++;
            }
            if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_shek")) { // Got a Sag
                int barPos = 0;
                for (int j = 0; j < instrumentLayout.getChildCount(); j++) {
                    if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end")) {
                        ensemble.shekVector.elementAt(shekVectorPos).set(barPos, 0);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_slap") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_slap") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_slap")) {
                        ensemble.shekVector.elementAt(shekVectorPos).set(barPos, 1);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("enabled")) {
                        ensemble.shekStatus.set(shekVectorPos, 1);
                        ensemble.shekVolume.set(shekVectorPos, (Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } // Enable
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("disabled"))
                        ensemble.shekStatus.set(shekVectorPos, 0); // Disable
                }
                shekVectorPos++;
            }
            if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_balet")) { // Got a Sag
                int barPos = 0;
                for (int j = 0; j < instrumentLayout.getChildCount(); j++) {
                    if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end")) {
                        ensemble.baletVector.elementAt(baletVectorPos).set(barPos, 0);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bass") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bass") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bass")) {
                        ensemble.baletVector.elementAt(baletVectorPos).set(barPos, 1);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone")) {
                        ensemble.baletVector.elementAt(baletVectorPos).set(barPos, 2);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_white")) {
                        ensemble.baletVector.elementAt(baletVectorPos).set(barPos, 3);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bass_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bass_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bass_mute")) {
                        ensemble.baletVector.elementAt(baletVectorPos).set(barPos, 4);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_mute")) {
                        ensemble.baletVector.elementAt(baletVectorPos).set(barPos, 5);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_mute_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_mute_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_mute_white")) {
                        ensemble.baletVector.elementAt(baletVectorPos).set(barPos, 6);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_ring") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_ring") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_ring")) {
                        ensemble.baletVector.elementAt(baletVectorPos).set(barPos, 7);
                        barPos++;
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("enabled")) {
                        ensemble.baletStatus.set(baletVectorPos, 1);
                        ensemble.baletVolume.set(baletVectorPos, (Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } // Enable
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("disabled")) {
                        // TODO: Not sure why the app hags when this is enabled. However it works this way!
                        //ensemble.baletStatus.set(baletVectorPos, 0); // Disable
                    }
                }
                baletVectorPos++;
            }
        }
    }

    public static synchronized void setEnsembleFromGui(LinearLayout ensembleLayout, Ensemble ensemble) {

        int previousBeats = ensemble.getBeats(); // if ensemble stays empty, mantain beats count

        ensemble.clearEnsemble();

        for (int i = 0; i < ensembleLayout.getChildCount(); i++) {
            LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
            if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).contains("ico_djembe")) { // Got a Djembe
                @SuppressWarnings("unchecked")
                Vector<Integer> djembe = new Vector();
                for (int j = 0; j < instrumentLayout.getChildCount(); j++) { // Always in order!
                    if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end"))
                        djembe.add(0);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bass") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bass") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bass"))
                        djembe.add(1);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone"))
                        djembe.add(2);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_slap") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_slap") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_slap"))
                        djembe.add(3);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bass_flam") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bass_flam") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bass_flam"))
                        djembe.add(4);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_flam") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_flam") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_flam"))
                        djembe.add(5);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_slap_flam") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_slap_flam") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_slap_flam"))
                        djembe.add(6);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("enabled")) {
                        ensemble.djembeStatus.add(1); // Enable
                        ensemble.djembeVolume.add((Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("disabled")) {
                        ensemble.djembeStatus.add(0); // Disable
                        ensemble.djembeVolume.add((Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    }
                }
                if (djembe.size() > ensemble.getBeats()) // Get largest bar size in all ensemble
                    ensemble.setBeats(djembe.size());
                ensemble.djembeVector.add(djembe);
                djembe.add(0); //4 Trailing silence to finish drums wavfile
                djembe.add(0);
                djembe.add(0);
                djembe.add(0);
            }
            if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_dun")) { // Got a Dun
                @SuppressWarnings("unchecked")
                Vector<Integer> dun = new Vector();
                for (int j = 0; j < instrumentLayout.getChildCount(); j++) {
                    if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end"))
                        dun.add(0);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bass_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bass_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bass_bell"))
                        dun.add(1);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bell"))
                        dun.add(2);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bass_bell_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bass_bell_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bass_bell_mute"))
                        dun.add(3);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("enabled")) {
                        ensemble.dunStatus.add(1);
                        ensemble.dunVolume.add((Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } // Enable
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("disabled")) {
                        ensemble.dunStatus.add(0);
                        ensemble.dunVolume.add((Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } // Disable
                }
                if (dun.size() > ensemble.getBeats()) // Get largets bar size in all ensemble
                    ensemble.setBeats(dun.size());
                ensemble.dunVector.add(dun);
                dun.add(0); //4 Trailing silence to finish drums wavfile
                dun.add(0);
                dun.add(0);
                dun.add(0);
            }
            if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_ken")) { // Got a Ken
                @SuppressWarnings("unchecked")
                Vector<Integer> ken = new Vector();
                for (int j = 0; j < instrumentLayout.getChildCount(); j++) {
                    if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end"))
                        ken.add(0);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_bell_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_bell_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_bell_white"))
                        ken.add(1);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bell"))
                        ken.add(2);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_bell_mute_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_bell_mute_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_bell_mute_white"))
                        ken.add(3);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("enabled")) {
                        ensemble.kenStatus.add(1);
                        ensemble.kenVolume.add((Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } // Enable
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("disabled")) {
                        ensemble.kenStatus.add(0);
                        ensemble.kenVolume.add((Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    }  // Disable
                }
                if (ken.size() > ensemble.getBeats()) // Get largets bar size in all ensemble
                    ensemble.setBeats(ken.size());
                ensemble.kenVector.add(ken);
                ken.add(0); //4 Trailing silence to finish drums wavfile
                ken.add(0);
                ken.add(0);
                ken.add(0);
            }
            if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_sag")) { // Got a Sag
                @SuppressWarnings("unchecked")
                Vector<Integer> sag = new Vector();
                for (int j = 0; j < instrumentLayout.getChildCount(); j++) {
                    if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end"))
                        sag.add(0);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_bell"))
                        sag.add(1);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bell") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bell"))
                        sag.add(2);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_bell_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_bell_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_bell_mute"))
                        sag.add(3);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("enabled")) {
                        ensemble.sagStatus.add(1);
                        ensemble.sagVolume.add((Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } // Enable
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("disabled")) {
                        ensemble.sagStatus.add(0);
                        ensemble.sagVolume.add((Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } // Disable
                }
                if (sag.size() > ensemble.getBeats()) // Get largets bar size in all ensemble
                    ensemble.setBeats(sag.size());
                ensemble.sagVector.add(sag);
                sag.add(0); //4 Trailing silence to finish drums wavfile
                sag.add(0);
                sag.add(0);
                sag.add(0);
            }
            if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_shek")) { // Got a Shek
                @SuppressWarnings("unchecked")
                Vector<Integer> shek = new Vector();
                for (int j = 0; j < instrumentLayout.getChildCount(); j++) {
                    if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end"))
                        shek.add(0);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_slap") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_slap") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_slap"))
                        shek.add(1);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("enabled")) {
                        ensemble.shekStatus.add(1);
                        ensemble.shekVolume.add((Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } // Enable
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("disabled")) {
                        ensemble.shekStatus.add(0);
                        ensemble.shekVolume.add((Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } // Disable
                }
                if (shek.size() > ensemble.getBeats()) // Get largets bar size in all ensemble
                    ensemble.setBeats(shek.size());
                ensemble.shekVector.add(shek);
                shek.add(0); //4 Trailing silence to finish drums wavfile
                shek.add(0);
                shek.add(0);
                shek.add(0);
            }
            if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_balet")) { // Got a Shek
                @SuppressWarnings("unchecked")
                Vector<Integer> balet = new Vector();
                for (int j = 0; j < instrumentLayout.getChildCount(); j++) {
                    if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end"))
                        balet.add(0);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bass") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bass") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bass"))
                        balet.add(1);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone"))
                        balet.add(2);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_white"))
                        balet.add(3);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_bass_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_bass_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_bass_mute"))
                        balet.add(4);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_mute") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_mute"))
                        balet.add(5);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_tone_mute_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_tone_mute_white") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_tone_mute_white"))
                        balet.add(6);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini_ring") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid_ring") || String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end_ring"))
                        balet.add(7);
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("enabled")) {
                        ensemble.baletStatus.add(1);
                        ensemble.baletVolume.add((Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } // Enable
                    else if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("disabled")) {
                        ensemble.baletStatus.add(0);
                        ensemble.baletVolume.add((Integer) instrumentLayout.getChildAt(0).getTag(R.string.tag1));
                    } // Disable
                }
                if (balet.size() > ensemble.getBeats()) // Get largets bar size in all ensemble
                    ensemble.setBeats(balet.size());
                ensemble.baletVector.add(balet);
                balet.add(0); //4 Trailing silence to finish drums wavfile
                balet.add(0);
                balet.add(0);
                balet.add(0);
            }
        }

        for (int i = 0; i < ensemble.djembeVector.size(); i++) // Include bars to balance empty spaces
            if (ensemble.djembeVector.elementAt(i).size() < ensemble.getBeats())
                for (int j = ensemble.djembeVector.elementAt(i).size() - 1; j < ensemble.getBeats(); j++)
                    ensemble.djembeVector.elementAt(i).add(0);

        for (int i = 0; i < ensemble.dunVector.size(); i++) // Include bars to balance empty spaces
            if (ensemble.dunVector.elementAt(i).size() < ensemble.getBeats())
                for (int j = ensemble.dunVector.elementAt(i).size() - 1; j < ensemble.getBeats(); j++)
                    ensemble.dunVector.elementAt(i).add(0);

        for (int i = 0; i < ensemble.kenVector.size(); i++) // Include bars to balance empty spaces
            if (ensemble.kenVector.elementAt(i).size() < ensemble.getBeats())
                for (int j = ensemble.kenVector.elementAt(i).size() - 1; j < ensemble.getBeats(); j++)
                    ensemble.kenVector.elementAt(i).add(0);

        for (int i = 0; i < ensemble.sagVector.size(); i++) // Include bars to balance empty spaces
            if (ensemble.sagVector.elementAt(i).size() < ensemble.getBeats())
                for (int j = ensemble.sagVector.elementAt(i).size() - 1; j < ensemble.getBeats(); j++)
                    ensemble.sagVector.elementAt(i).add(0);

        for (int i = 0; i < ensemble.shekVector.size(); i++) // Include bars to balance empty spaces
            if (ensemble.shekVector.elementAt(i).size() < ensemble.getBeats())
                for (int j = ensemble.shekVector.elementAt(i).size() - 1; j < ensemble.getBeats(); j++)
                    ensemble.shekVector.elementAt(i).add(0);

        for (int i = 0; i < ensemble.baletVector.size(); i++) // Include bars to balance empty spaces
            if (ensemble.baletVector.elementAt(i).size() < ensemble.getBeats())
                for (int j = ensemble.baletVector.elementAt(i).size() - 1; j < ensemble.getBeats(); j++)
                    ensemble.baletVector.elementAt(i).add(0);

        // Reset Repetitions Status
        for (int i = 0; i < ensemble.repetitions.size(); i++)
            ensemble.repetitions.elementAt(i)[2] = ensemble.repetitions.elementAt(i)[1];

        // If no bars (no instruments), set 1 bar in ensemble
        if (ensemble.getBars() == 0)
            ensemble.setBeats(previousBeats);

        // Get Bar Names from gui
        LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(ensembleLayout.getChildCount() - 1);
        for (int i = 1; i < instrumentLayout.getChildCount(); i++) { //first element is (+) button
            TextView text = (TextView) instrumentLayout.getChildAt(i);
            ensemble.barName.setElementAt(String.valueOf(text.getTag(R.string.tag0)), (i - 1));
        }
    }

    public static void setGuiFromEnsemble(LinearLayout ensembleLayout, final Ensemble ensemble, final android.content.Context activityContext, float iconScale, final ViewAnimator viewAnimator) {

        ensembleLayout.removeAllViews();

        // set Tempo number - id: but_tempo
        TextView tempoText = (TextView) ((Activity) activityContext).findViewById(R.id.tempoText);
        tempoText.setText(String.valueOf(ensemble.bpm));

        // Add ensemble name
        final TextView ensembleName = new TextView(activityContext);
        ensembleName.setText(ensemble.ensembleName + " - " + activityContext.getResources().getString(R.string.by) + " " + ensemble.ensembleAuthor);
        ensembleName.setTag(R.string.tag0, "ens_name");
        ensembleName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragmentSetName dialogFragmentSetName = new DialogFragmentSetName();
                dialogFragmentSetName.name = ensemble.ensembleName;
                dialogFragmentSetName.author = ensemble.ensembleAuthor;
                dialogFragmentSetName.show(((FragmentActivity) activityContext).getSupportFragmentManager(), "SetName");
            }
        });
        LinearLayout nameLayout = new LinearLayout(activityContext);
        nameLayout.setTag(R.string.tag0, "ens_name");
        nameLayout.addView(ensembleName);
        ensembleLayout.addView(nameLayout);

        for (int i = 0; i < ensemble.shekVector.size(); i++) { // 1 - Shekere
            LinearLayout instrumentLayout = EnsembleUtils.addInstrumentInGui("shek", activityContext, ensembleLayout, iconScale, ensemble, ensemble.shekVolume.elementAt(i), ensemble.shekStatus.elementAt(i), viewAnimator); // Includes 4 compass
            int vectorPosition = 0;
            for (int j = 0; j < instrumentLayout.getChildCount(); j++) { // Go over all views in layout
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini")) {
                    if (ensemble.shekVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_slap);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_slap");
                    }
                    vectorPosition++;
                }
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid")) {
                    if (ensemble.shekVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_slap);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_slap");
                    }
                    vectorPosition++;
                }
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end")) {
                    if (ensemble.shekVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_slap);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_slap");
                    }
                    vectorPosition++;
                }
            }
        }

        for (int i = 0; i < ensemble.baletVector.size(); i++) { // 1A - Balet
            LinearLayout instrumentLayout = EnsembleUtils.addInstrumentInGui("balet", activityContext, ensembleLayout, iconScale, ensemble, ensemble.baletVolume.elementAt(i), ensemble.baletStatus.elementAt(i), viewAnimator); // Includes 4 compass
            int vectorPosition = 0;
            for (int j = 0; j < instrumentLayout.getChildCount(); j++) { // Go over all views in layout
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini")) {
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_bass);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_bass");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_tone);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_tone");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_tone_white);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_tone_white");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 4) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_bass_mute);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_bass_mute");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 5) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_tone_mute);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_tone_mute");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 6) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_tone_mute_white);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_tone_mute_white");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 7) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_ring);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_ring");
                    }
                    vectorPosition++;
                }
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid")) {
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_bass);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_bass");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_tone);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_tone");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_tone_white);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_tone_white");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 4) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_bass_mute);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_bass_mute");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 5) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_tone_mute);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_tone_mute");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 6) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_tone_mute_white);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_tone_mute_white");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 7) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_ring);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_ring");
                    }
                    vectorPosition++;
                }
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end")) {
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_bass);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_bass");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_tone);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_tone");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_tone_white);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_tone_white");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 4) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_bass_mute);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_bass_mute");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 5) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_tone_mute);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_tone_mute");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 6) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_tone_mute_white);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_tone_mute_white");
                    }
                    if (ensemble.baletVector.elementAt(i).elementAt(vectorPosition) == 7) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_ring);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_ring");
                    }
                    vectorPosition++;
                }
            }
        }

        for (int i = 0; i < ensemble.dunVector.size(); i++) { // 2 - Dundun
            LinearLayout instrumentLayout = EnsembleUtils.addInstrumentInGui("dun", activityContext, ensembleLayout, iconScale, ensemble, ensemble.dunVolume.elementAt(i), ensemble.dunStatus.elementAt(i), viewAnimator); // Includes 4 compass
            int vectorPosition = 0;
            for (int j = 0; j < instrumentLayout.getChildCount(); j++) { // Go over all views in layout
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini")) {
                    if (ensemble.dunVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_bass_bell_mute);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_bass_bell_mute");
                    }
                    if (ensemble.dunVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_bell");
                    }
                    if (ensemble.dunVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_bass_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_bass_bell");
                    }
                    vectorPosition++;
                }
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid")) {
                    if (ensemble.dunVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_bass_bell_mute);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_bass_bell_mute");
                    }
                    if (ensemble.dunVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_bell");
                    }
                    if (ensemble.dunVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_bass_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_bass_bell");
                    }
                    vectorPosition++;
                }
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end")) {
                    if (ensemble.dunVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_bass_bell_mute);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_bass_bell_mute");
                    }
                    if (ensemble.dunVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_bell");
                    }
                    if (ensemble.dunVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_bass_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_bass_bell");
                    }
                    vectorPosition++;
                }
            }
        }

        for (int i = 0; i < ensemble.sagVector.size(); i++) { // 3 - Sagbang
            LinearLayout instrumentLayout = EnsembleUtils.addInstrumentInGui("sag", activityContext, ensembleLayout, iconScale, ensemble, ensemble.sagVolume.elementAt(i), ensemble.sagStatus.elementAt(i), viewAnimator); // Includes 4 compass
            int vectorPosition = 0;
            for (int j = 0; j < instrumentLayout.getChildCount(); j++) { // Go over all views in layout
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini")) {
                    if (ensemble.sagVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_tone_bell_mute);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_tone_bell_mute");
                    }
                    if (ensemble.sagVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_bell");
                    }
                    if (ensemble.sagVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_tone_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_tone_bell");
                    }
                    vectorPosition++;
                }
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid")) {
                    if (ensemble.sagVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_tone_bell_mute);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_tone_bell_mute");
                    }
                    if (ensemble.sagVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_bell");
                    }
                    if (ensemble.sagVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_tone_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_tone_bell");
                    }
                    vectorPosition++;
                }
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end")) {
                    if (ensemble.sagVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_tone_bell_mute);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_tone_bell_mute");
                    }
                    if (ensemble.sagVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_bell");
                    }
                    if (ensemble.sagVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_tone_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_tone_bell");
                    }
                    vectorPosition++;
                }
            }
        }

        for (int i = 0; i < ensemble.kenVector.size(); i++) { // 3 - Kenkeni
            LinearLayout instrumentLayout = EnsembleUtils.addInstrumentInGui("ken", activityContext, ensembleLayout, iconScale, ensemble, ensemble.kenVolume.elementAt(i), ensemble.kenStatus.elementAt(i), viewAnimator); // Includes 4 compass
            int vectorPosition = 0;
            for (int j = 0; j < instrumentLayout.getChildCount(); j++) { // Go over all views in layout
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini")) {
                    if (ensemble.kenVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_tone_bell_mute_white);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_tone_bell_mute_white");
                    }
                    if (ensemble.kenVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_bell");
                    }
                    if (ensemble.kenVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_tone_bell_white);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_tone_bell_white");
                    }
                    vectorPosition++;
                }
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid")) {
                    if (ensemble.kenVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_tone_bell_mute_white);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_tone_bell_mute_white");
                    }
                    if (ensemble.kenVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_bell");
                    }
                    if (ensemble.kenVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_tone_bell_white);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_tone_bell_white");
                    }
                    vectorPosition++;
                }
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end")) {
                    if (ensemble.kenVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_tone_bell_mute_white);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_tone_bell_mute_white");
                    }
                    if (ensemble.kenVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_bell);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_bell");
                    }
                    if (ensemble.kenVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_tone_bell_white);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_tone_bell_white");
                    }
                    vectorPosition++;
                }
            }
        }

        for (int i = 0; i < ensemble.djembeVector.size(); i++) { // 4 - Djembe
            LinearLayout instrumentLayout = EnsembleUtils.addInstrumentInGui("djembe", activityContext, ensembleLayout, iconScale, ensemble, ensemble.djembeVolume.elementAt(i), ensemble.djembeStatus.elementAt(i), viewAnimator); // Includes 4 compass
            int vectorPosition = 0;
            for (int j = 0; j < instrumentLayout.getChildCount(); j++) { // Go over all views in layout
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_ini")) {
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_slap);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_slap");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_tone);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_tone");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_bass);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_bass");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 6) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_slap_flam);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_slap_flam");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 5) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_tone_flam);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_tone_flam");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 4) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_ini_bass_flam);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_ini_bass_flam");
                    }
                    vectorPosition++;
                }
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_mid")) {
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_slap);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_slap");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_tone);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_tone");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_bass);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_bass");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 6) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_slap_flam);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_slap_flam");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 5) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_tone_flam);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_tone_flam");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 4) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_mid_bass_flam);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_mid_bass_flam");
                    }
                    vectorPosition++;
                }
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).equals("ico_end")) {
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 3) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_slap);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_slap");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 2) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_tone);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_tone");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 1) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_bass);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_bass");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 6) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_slap_flam);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_slap_flam");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 5) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_tone_flam);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_tone_flam");
                    }
                    if (ensemble.djembeVector.elementAt(i).elementAt(vectorPosition) == 4) {
                        ((ImageView) instrumentLayout.getChildAt(j)).setImageResource(R.drawable.ico_end_bass_flam);
                        instrumentLayout.getChildAt(j).setTag(R.string.tag0, "ico_end_bass_flam");
                    }
                    vectorPosition++;
                }
            }
        }

        // set Repetitions icons
//        repetitions.elementAt(i)[0] = jsonRepetitionN.getInt("barCount");
//        repetitions.elementAt(i)[1] = jsonRepetitionN.getInt("countTotal");
//        repetitions.elementAt(i)[2] = jsonRepetitionN.getInt("countTotal");
        for (int i = 0; i < ensemble.repetitions.size(); i++) {
            if (ensemble.repetitions.elementAt(i)[1] > 1) { //We have a repetition at bar i
                for (int j = 1; j < ensembleLayout.getChildCount() - 1; j++) {
                    int totalBars = 0;
                    LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(j);
                    for (int k = 0; k < instrumentLayout.getChildCount(); k++) {
                        if (String.valueOf(instrumentLayout.getChildAt(k).getTag(R.string.tag0)).contains("ico_div")) {
                            if (String.valueOf(instrumentLayout.getChildAt(k).getTag(R.string.tag0)).equals("ico_div")) { // es palito normal
                                if (totalBars == i) {
                                    ((ImageView) instrumentLayout.getChildAt(k)).setImageResource(R.drawable.ico_div_r_end);
                                    (instrumentLayout.getChildAt(k)).setTag(R.string.tag0, "ico_div_r_end");
                                }
                                if (totalBars == (i - ensemble.repetitions.elementAt(i)[0])) {
                                    ((ImageView) instrumentLayout.getChildAt(k)).setImageResource(R.drawable.ico_div_r_ini);
                                    (instrumentLayout.getChildAt(k)).setTag(R.string.tag0, "ico_div_r_ini");
                                }
                            }
                            if (String.valueOf(instrumentLayout.getChildAt(k).getTag(R.string.tag0)).equals("ico_div_r_ini")) { // es palito normal
                                if (totalBars == i) {
                                    ((ImageView) instrumentLayout.getChildAt(k)).setImageResource(R.drawable.ico_div_r_both);
                                    (instrumentLayout.getChildAt(k)).setTag(R.string.tag0, "ico_div_r_both");
                                }
                                if (totalBars == (i - ensemble.repetitions.elementAt(i)[0])) {
                                    ((ImageView) instrumentLayout.getChildAt(k)).setImageResource(R.drawable.ico_div_r_ini);
                                    (instrumentLayout.getChildAt(k)).setTag(R.string.tag0, "ico_div_r_ini");
                                }
                            }
                            if (String.valueOf(instrumentLayout.getChildAt(k).getTag(R.string.tag0)).equals("ico_div_r_end")) { // es palito normal
                                if (totalBars == i) {
                                    ((ImageView) instrumentLayout.getChildAt(k)).setImageResource(R.drawable.ico_div_r_end);
                                    (instrumentLayout.getChildAt(k)).setTag(R.string.tag0, "ico_div_r_end");
                                }
                                if (totalBars == (i - ensemble.repetitions.elementAt(i)[0])) {
                                    ((ImageView) instrumentLayout.getChildAt(k)).setImageResource(R.drawable.ico_div_r_both);
                                    (instrumentLayout.getChildAt(k)).setTag(R.string.tag0, "ico_div_r_both");
                                }
                            }
                            if (String.valueOf(instrumentLayout.getChildAt(k).getTag(R.string.tag0)).equals("ico_div_r_both")) { // es palito normal
                                if (totalBars == i) {
                                    ((ImageView) instrumentLayout.getChildAt(k)).setImageResource(R.drawable.ico_div_r_both);
                                    (instrumentLayout.getChildAt(k)).setTag(R.string.tag0, "ico_div_r_both");
                                }
                                if (totalBars == (i - ensemble.repetitions.elementAt(i)[0])) {
                                    ((ImageView) instrumentLayout.getChildAt(k)).setImageResource(R.drawable.ico_div_r_both);
                                    (instrumentLayout.getChildAt(k)).setTag(R.string.tag0, "ico_div_r_both");
                                }
                            }
                            totalBars++;
                        }
                    }
                }
            }
        }

        // If ensemble is empty, add instrument and remove it to leave only the add button (+)
        if ((ensemble.shekVector.size() == 0) && (ensemble.dunVector.size() == 0) && (ensemble.sagVector.size() == 0) && (ensemble.kenVector.size() == 0) && (ensemble.djembeVector.size() == 0) && (ensemble.baletVector.size() == 0)) {
            addInstrumentInGui("djembe", activityContext, ensembleLayout, iconScale, ensemble, 100, 1, viewAnimator);
            ensembleLayout.removeView(ensembleLayout.getChildAt(1)); // child 0 = ensembleName
            //remove bar names from gui if empty
            LinearLayout lastLineLayout = (LinearLayout) ensembleLayout.getChildAt(1);
            Vector<View> viewsToRemove = new Vector();
            for (int i = 1; i < lastLineLayout.getChildCount(); i++) // 1st item is add instrument (+)
                viewsToRemove.add(lastLineLayout.getChildAt(i));
            for (int i = 0; i < viewsToRemove.size(); i++)
                lastLineLayout.removeView(viewsToRemove.elementAt(i));
        }

        // Floating icons update

    }

    public static LinearLayout addInstrumentInGui(final String instr, final android.content.Context activityContext, final LinearLayout ensembleLayout, final float iconScale, final Ensemble ensemble, int volume, int enabled, final ViewAnimator viewAnimator) {

        final float displayDensity = activityContext.getResources().getDisplayMetrics().density;

        // General LinearLayout (Instrument Logo + Compasses)
        LinearLayout instrumentLayout = new LinearLayout(activityContext);
        instrumentLayout.setOrientation(LinearLayout.HORIZONTAL);
        instrumentLayout.setPadding(0, (int) (iconScale * 10 * displayDensity + 0.5f), 0, (int) (iconScale * 10 * displayDensity + 0.5f)); // (int left, int top, int right, int bottom) in pixels
        //instrumentLayout.setId(currentID++);

        // Instrument Logo
        ImageView instImage = new ImageView(activityContext);
        instImage.setPadding(0, 0, 0, 0);
        instImage.setLayoutParams(new ViewGroup.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f)));

        if (enabled == 1) {
            instImage.setTag(R.string.tag0, "enabled");
            instImage.setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
        } else {
            instImage.setTag(R.string.tag0, "disabled");
            instImage.setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
        }
        instImage.setTag(R.string.tag1, volume); //Volume

        instImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragmentInstrumentOptions dialogFragmentInstrumentOptions = new DialogFragmentInstrumentOptions();
                dialogFragmentInstrumentOptions.onPlay = ensemble.onPlay;
                dialogFragmentInstrumentOptions.activityContext = activityContext;
                dialogFragmentInstrumentOptions.instImage = (ImageView) v;
                dialogFragmentInstrumentOptions.ensemble = ensemble;
                dialogFragmentInstrumentOptions.ensembleLayout = ensembleLayout;
                dialogFragmentInstrumentOptions.show(((FragmentActivity) activityContext).getSupportFragmentManager(), "InstrumentOptions");
            }
        });
        instImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (String.valueOf(v.getTag(R.string.tag0)).equals("enabled")) { //Disable
                    v.setTag(R.string.tag0, "disabled");
                    ((ImageView) v).setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
                } else {
                    v.setTag(R.string.tag0, "enabled");
                    ((ImageView) v).setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
                }
                updateEnsembleFromGui(ensembleLayout, ensemble); //Update Vectors
                return true; // return true to let others know the event was handled
            }
        });
        switch (instr) {
            case "djembe":
                int djembesInGui=0;
                for (int i = 0; i < ensembleLayout.getChildCount(); i++)
                    if (String.valueOf(((LinearLayout) ensembleLayout.getChildAt(i)).getTag(R.string.tag0)).contains("ico_djembe"))
                        djembesInGui++;
                if(djembesInGui%3==0){
                    instImage.setImageResource(R.drawable.ico_djembe_1);
                    instrumentLayout.setTag(R.string.tag0, "ico_djembe_1");
                } else if(djembesInGui%3==1){
                    instImage.setImageResource(R.drawable.ico_djembe_2);
                    instrumentLayout.setTag(R.string.tag0, "ico_djembe_2");
                } else {
                    instImage.setImageResource(R.drawable.ico_djembe_3);
                    instrumentLayout.setTag(R.string.tag0, "ico_djembe_3");
                }
                break;
            case "dun":
                instImage.setImageResource(R.drawable.ico_dun);
                instrumentLayout.setTag(R.string.tag0, "ico_dun");
                break;
            case "sag":
                instImage.setImageResource(R.drawable.ico_sag);
                instrumentLayout.setTag(R.string.tag0, "ico_sag");
                break;
            case "ken":
                instImage.setImageResource(R.drawable.ico_ken);
                instrumentLayout.setTag(R.string.tag0, "ico_ken");
                break;
            case "shek":
                instImage.setImageResource(R.drawable.ico_shek);
                instrumentLayout.setTag(R.string.tag0, "ico_shek");
                break;
            case "balet":
                instImage.setImageResource(R.drawable.ico_balet);
                instrumentLayout.setTag(R.string.tag0, "ico_balet");
                break;
        }
        instrumentLayout.addView(instImage);

        // Initial Division (no repeat listener to this one, no tag either! use trailing one!)
        ImageView icoDivImage = new ImageView(activityContext);
        icoDivImage.setPadding(0, 0, 0, 0);
        icoDivImage.setImageResource(R.drawable.ico_div_ini);
        icoDivImage.setLayoutParams(new ViewGroup.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f)));

        //icoDivImage.setId(currentID++);
        instrumentLayout.addView(icoDivImage);

        // Set Bars as per ensemble properties
        for (int i = 0; i < ensemble.getBars(); i++) {
            if ((ensemble.getBeatsPerBar() == 16) || (ensemble.getBeatsPerBar() == 12))
                addBarInGui(instrumentLayout, 4, activityContext, iconScale, ensemble, viewAnimator);
            if (ensemble.getBeatsPerBar() == 9)
                addBarInGui(instrumentLayout, 3, activityContext, iconScale, ensemble, viewAnimator);
        }

        // Add Button for adding bars
        ImageView addImage1 = new ImageView(activityContext);
        addImage1.setPadding(0, 0, (int) (iconScale * 37.5 * displayDensity + 0.5f), 0);
        addImage1.setTag(R.string.tag0, "ico_add");
        addImage1.setLayoutParams(new ViewGroup.LayoutParams((int) (2 * iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f)));
        addImage1.setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
        addImage1.setImageResource(R.drawable.ico_add);
        //addImage1.setId(currentID++);
        instrumentLayout.addView(addImage1);
        addImage1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (AfroStudioVersion.equals("free")) {
                    popGetFullAfroStudioMessage(viewAnimator, activityContext);
                } else {
                    addAllBarInGui(ensembleLayout, activityContext, iconScale, ensemble, viewAnimator);
                    if (ensemble.onPlay)
                        ensemble.flagEnsembleUpdate = true;
                    else {
                        setEnsembleFromGui(ensembleLayout, ensemble); //Update Bars in Vectors (in case instruments are added)
                        setGuiFromEnsemble(ensembleLayout, ensemble, activityContext, iconScale, viewAnimator);
                    }
                }
            }
        });

        // Add las line, but first remove existing one!
        for (int i = 0; i < ensembleLayout.getChildCount(); i++)
            if (String.valueOf(ensembleLayout.getChildAt(i).getTag(R.string.tag0)).equals("last_inst")) // aca van los bars names?
                ensembleLayout.removeView(ensembleLayout.getChildAt(i));
        ensembleLayout.addView(instrumentLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout instrumentLayout2 = new LinearLayout(activityContext);
        instrumentLayout2.setOrientation(LinearLayout.HORIZONTAL);
        instrumentLayout2.setPadding(0, (int) (iconScale * 10 * displayDensity + 0.5f), 0, (int) (iconScale * 10 * displayDensity + 0.5f)); // (int left, int top, int right, int bottom) in pixels
        instrumentLayout2.setTag(R.string.tag0, "last_inst");

        ImageView addImage2 = new ImageView(activityContext); //Add new instrument button (+)
        addImage2.setPadding(0, 0, 0, (int) (iconScale * 52.5 * displayDensity + 0.5f)); // 1 to have some margin below for payback buttons
        addImage2.setTag(R.string.tag0, "ico_add");
        addImage2.setLayoutParams(new ViewGroup.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (2 * iconScale * 52.5 * displayDensity + 0.5f))); // 2 to have some margin below for payback buttons
        addImage2.setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
        addImage2.setImageResource(R.drawable.ico_add);
        addImage2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragmentAddInstrument dialogFragmentAddInstrument = new DialogFragmentAddInstrument();
                dialogFragmentAddInstrument.show(((FragmentActivity) activityContext).getSupportFragmentManager(), "Select Instrument");
            }
        });
        instrumentLayout2.addView(addImage2);

        // Add Bar Names below each bar
        for (int i = 0; i < ensemble.getBars(); i++) {
            TextView barName = new TextView(activityContext);
            barName.setTag(R.string.tag0, ensemble.barName.elementAt(i));
            if (ensemble.barName.elementAt(i).equals(""))
                barName.setText(activityContext.getResources().getString(R.string.bar) + "  " + (i + 1));
            else
                barName.setText(activityContext.getResources().getString(R.string.bar) + "  " + (i + 1) + ": " + ensemble.barName.elementAt(i));
            if (i % 2 == 0) // Back Colour
                barName.setBackgroundColor(ContextCompat.getColor(activityContext, R.color.green_afrostudio_transp_1));
            else
                barName.setBackgroundColor(ContextCompat.getColor(activityContext, R.color.green_afrostudio_transp_2));

            barName.setLayoutParams(new ViewGroup.LayoutParams((int) ((ensemble.getBeatsPerBar() + 1) * iconScale * 37.5 * displayDensity + 0.5f), (int) (0.4 * 52.5 * displayDensity + 0.5f))); //1 beat per ico_div // fixed height!
            barName.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            final int barPos = i;
            barName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragmentBarOptions dialogFragmentBarOptions = new DialogFragmentBarOptions();
                    dialogFragmentBarOptions.ensemble = ensemble;
                    dialogFragmentBarOptions.barPos = barPos;
                    dialogFragmentBarOptions.show(((FragmentActivity) activityContext).getSupportFragmentManager(), "BarOptions");
                }
            });
            instrumentLayout2.addView(barName);
        }

        ensembleLayout.addView(instrumentLayout2, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return instrumentLayout;
    }

    // Add compass to all instrumentLayout in ensembleLayout
    private static void addAllBarInGui(final LinearLayout ensembleLayout, final android.content.Context activityContext, float iconScale, Ensemble ensemble, final ViewAnimator viewAnimator) {
        for (int j = 0; j < ensembleLayout.getChildCount(); j++) { // ensembleLayout only has instrumentLayout child
            LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(j);
            if ((!(String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("last_inst"))) && !((String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ens_name")))) { // only add compass to instrument layouts (not add image)
                View addImage2 = null;
                for (int i = 0; i < instrumentLayout.getChildCount(); i++) {
                    if (String.valueOf(instrumentLayout.getChildAt(i).getTag(R.string.tag0)).equals("ico_add")) {
                        addImage2 = instrumentLayout.getChildAt(i);
                        instrumentLayout.removeView(addImage2);
                    }
                }
                if ((ensemble.getBeatsPerBar() == 12) || ensemble.getBeatsPerBar() == 16)
                    addBarInGui(instrumentLayout, 4, activityContext, iconScale, ensemble, viewAnimator);
                else if (ensemble.getBeatsPerBar() == 9)
                    addBarInGui(instrumentLayout, 3, activityContext, iconScale, ensemble, viewAnimator);
                if (addImage2 != null)
                    instrumentLayout.addView(addImage2); //+
            }
        }
    }

    // Add individual compass to instrumentLayout
    private static void addBarInGui(final LinearLayout instrumentLayout, int numGroups, final android.content.Context activityContext, float iconScale, final Ensemble ensemble, final ViewAnimator viewAnimator) {

        final float displayDensity = activityContext.getResources().getDisplayMetrics().density;

        for (int i = 1; i <= numGroups; i++) {

            ImageView icoIniImage = new ImageView(activityContext);
            icoIniImage.setPadding(0, 0, 0, 0);
            icoIniImage.setImageResource(R.drawable.ico_ini);
            icoIniImage.setLayoutParams(new ViewGroup.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f)));
            icoIniImage.setTag(R.string.tag0, "ico_ini");
            icoIniImage.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (AfroStudioVersion.equals("free") && (ensemble.getBars() > 1)) {
                        popGetFullAfroStudioMessage(viewAnimator, activityContext);
                    } else {
                        ImageView icoImage = (ImageView) v;
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).contains("ico_djembe"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini")) {
                                icoImage.setImageResource(R.drawable.ico_ini_slap);
                                icoImage.setTag(R.string.tag0, "ico_ini_slap");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini_slap")) {
                                icoImage.setImageResource(R.drawable.ico_ini_tone);
                                icoImage.setTag(R.string.tag0, "ico_ini_tone");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini_tone")) {
                                icoImage.setImageResource(R.drawable.ico_ini_bass);
                                icoImage.setTag(R.string.tag0, "ico_ini_bass");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_ini);
                                icoImage.setTag(R.string.tag0, "ico_ini");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_dun"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini")) {
                                icoImage.setImageResource(R.drawable.ico_ini_bass_bell);
                                icoImage.setTag(R.string.tag0, "ico_ini_bass_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini_bass_bell")) {
                                icoImage.setImageResource(R.drawable.ico_ini_bell);
                                icoImage.setTag(R.string.tag0, "ico_ini_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini_bell")) {
                                icoImage.setImageResource(R.drawable.ico_ini_bass_bell_mute);
                                icoImage.setTag(R.string.tag0, "ico_ini_bass_bell_mute");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini_bass_bell_mute")) {
                                icoImage.setImageResource(R.drawable.ico_ini);
                                icoImage.setTag(R.string.tag0, "ico_ini");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_sag"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini")) {
                                icoImage.setImageResource(R.drawable.ico_ini_tone_bell);
                                icoImage.setTag(R.string.tag0, "ico_ini_tone_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini_tone_bell")) {
                                icoImage.setImageResource(R.drawable.ico_ini_bell);
                                icoImage.setTag(R.string.tag0, "ico_ini_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini_bell")) {
                                icoImage.setImageResource(R.drawable.ico_ini_tone_bell_mute);
                                icoImage.setTag(R.string.tag0, "ico_ini_tone_bell_mute");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_ini);
                                icoImage.setTag(R.string.tag0, "ico_ini");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_ken"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini")) {
                                icoImage.setImageResource(R.drawable.ico_ini_tone_bell_white);
                                icoImage.setTag(R.string.tag0, "ico_ini_tone_bell_white");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini_tone_bell_white")) {
                                icoImage.setImageResource(R.drawable.ico_ini_bell);
                                icoImage.setTag(R.string.tag0, "ico_ini_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini_bell")) {
                                icoImage.setImageResource(R.drawable.ico_ini_tone_bell_mute_white);
                                icoImage.setTag(R.string.tag0, "ico_ini_tone_bell_mute_white");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_ini);
                                icoImage.setTag(R.string.tag0, "ico_ini");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_shek"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini")) {
                                icoImage.setImageResource(R.drawable.ico_ini_slap);
                                icoImage.setTag(R.string.tag0, "ico_ini_slap");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_ini);
                                icoImage.setTag(R.string.tag0, "ico_ini");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_balet"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini")) {
                                icoImage.setImageResource(R.drawable.ico_ini_bass);
                                icoImage.setTag(R.string.tag0, "ico_ini_bass");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini_bass")) {
                                icoImage.setImageResource(R.drawable.ico_ini_tone);
                                icoImage.setTag(R.string.tag0, "ico_ini_tone");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_ini_tone")) {
                                icoImage.setImageResource(R.drawable.ico_ini_tone_white);
                                icoImage.setTag(R.string.tag0, "ico_ini_tone_white");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_ini);
                                icoImage.setTag(R.string.tag0, "ico_ini");
                            }
                        updateEnsembleFromGui((LinearLayout) instrumentLayout.getParent(), ensemble);
                    }
                }
            });
            icoIniImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (AfroStudioVersion.equals("free") && (ensemble.getBars() > 1)) {
                        popGetFullAfroStudioMessage(viewAnimator, activityContext);
                    } else {
                        ImageView icoImage = (ImageView) v;
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).contains("ico_djembe") || String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_balet")) {
                            DialogFragmentSpecialStroke dialogFragmentSpecialStroke = new DialogFragmentSpecialStroke();
                            dialogFragmentSpecialStroke.icoImage = icoImage;
                            dialogFragmentSpecialStroke.ensemble = ensemble;
                            dialogFragmentSpecialStroke.show(((FragmentActivity) activityContext).getSupportFragmentManager(), "Select Instrument");
                        }
                    }
                    return false;
                }
            });
            //Log.v(TAG, "AddInstrument: instrumentImageCompass - id:" + currentID);
            //icoIniImage.setId(currentID++);
            instrumentLayout.addView(icoIniImage);

            ImageView icoMidImage1 = new ImageView(activityContext);
            icoMidImage1.setPadding(0, 0, 0, 0);
            icoMidImage1.setImageResource(R.drawable.ico_mid);
            icoMidImage1.setLayoutParams(new ViewGroup.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f)));
            icoMidImage1.setTag(R.string.tag0, "ico_mid");
            icoMidImage1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (AfroStudioVersion.equals("free") && (ensemble.getBars() > 1)) {
                        popGetFullAfroStudioMessage(viewAnimator, activityContext);
                    } else {
                        ImageView icoImage = (ImageView) v;
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).contains("ico_djembe"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid")) {
                                icoImage.setImageResource(R.drawable.ico_mid_slap);
                                icoImage.setTag(R.string.tag0, "ico_mid_slap");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_slap")) {
                                icoImage.setImageResource(R.drawable.ico_mid_tone);
                                icoImage.setTag(R.string.tag0, "ico_mid_tone");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_tone")) {
                                icoImage.setImageResource(R.drawable.ico_mid_bass);
                                icoImage.setTag(R.string.tag0, "ico_mid_bass");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_mid);
                                icoImage.setTag(R.string.tag0, "ico_mid");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_dun"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid")) {
                                icoImage.setImageResource(R.drawable.ico_mid_bass_bell);
                                icoImage.setTag(R.string.tag0, "ico_mid_bass_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_bass_bell")) {
                                icoImage.setImageResource(R.drawable.ico_mid_bell);
                                icoImage.setTag(R.string.tag0, "ico_mid_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_bell")) {
                                icoImage.setImageResource(R.drawable.ico_mid_bass_bell_mute);
                                icoImage.setTag(R.string.tag0, "ico_mid_bass_bell_mute");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_mid);
                                icoImage.setTag(R.string.tag0, "ico_mid");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_sag"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid")) {
                                icoImage.setImageResource(R.drawable.ico_mid_tone_bell);
                                icoImage.setTag(R.string.tag0, "ico_mid_tone_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_tone_bell")) {
                                icoImage.setImageResource(R.drawable.ico_mid_bell);
                                icoImage.setTag(R.string.tag0, "ico_mid_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_bell")) {
                                icoImage.setImageResource(R.drawable.ico_mid_tone_bell_mute);
                                icoImage.setTag(R.string.tag0, "ico_mid_tone_bell_mute");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_mid);
                                icoImage.setTag(R.string.tag0, "ico_mid");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_ken"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid")) {
                                icoImage.setImageResource(R.drawable.ico_mid_tone_bell_white);
                                icoImage.setTag(R.string.tag0, "ico_mid_tone_bell_white");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_tone_bell_white")) {
                                icoImage.setImageResource(R.drawable.ico_mid_bell);
                                icoImage.setTag(R.string.tag0, "ico_mid_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_bell")) {
                                icoImage.setImageResource(R.drawable.ico_mid_tone_bell_mute_white);
                                icoImage.setTag(R.string.tag0, "ico_mid_tone_bell_mute_white");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_mid);
                                icoImage.setTag(R.string.tag0, "ico_mid");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_shek"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid")) {
                                icoImage.setImageResource(R.drawable.ico_mid_slap);
                                icoImage.setTag(R.string.tag0, "ico_mid_slap");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_mid);
                                icoImage.setTag(R.string.tag0, "ico_mid");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_balet"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid")) {
                                icoImage.setImageResource(R.drawable.ico_mid_bass);
                                icoImage.setTag(R.string.tag0, "ico_mid_bass");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_bass")) {
                                icoImage.setImageResource(R.drawable.ico_mid_tone);
                                icoImage.setTag(R.string.tag0, "ico_mid_tone");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_tone")) {
                                icoImage.setImageResource(R.drawable.ico_mid_tone_white);
                                icoImage.setTag(R.string.tag0, "ico_mid_tone_white");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_mid);
                                icoImage.setTag(R.string.tag0, "ico_mid");
                            }
                        updateEnsembleFromGui((LinearLayout) instrumentLayout.getParent(), ensemble);
                    }
                }
            });
            icoMidImage1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (AfroStudioVersion.equals("free") && (ensemble.getBars() > 1)) {
                        popGetFullAfroStudioMessage(viewAnimator, activityContext);
                    } else {
                        ImageView icoImage = (ImageView) v;
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).contains("ico_djembe")  || String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_balet")) {
                            DialogFragmentSpecialStroke dialogFragmentSpecialStroke = new DialogFragmentSpecialStroke();
                            dialogFragmentSpecialStroke.icoImage = icoImage;
                            dialogFragmentSpecialStroke.ensemble = ensemble;
                            dialogFragmentSpecialStroke.show(((FragmentActivity) activityContext).getSupportFragmentManager(), "Select Instrument");
                        }
                    }
                    return false;
                }
            });
            //Log.v(TAG, "AddInstrument: instrumentImageCompass - id:" + currentID);
            //icoMidImage1.setId(currentID++);
            instrumentLayout.addView(icoMidImage1);

            if (ensemble.getBeatsPerBar() == 16) { // If 12 or 9 keep ternary
                ImageView icoMidImage2 = new ImageView(activityContext);
                icoMidImage2.setPadding(0, 0, 0, 0);
                icoMidImage2.setImageResource(R.drawable.ico_mid);
                icoMidImage2.setLayoutParams(new ViewGroup.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f)));
                icoMidImage2.setTag(R.string.tag0, "ico_mid");
                icoMidImage2.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (AfroStudioVersion.equals("free") && (ensemble.getBars() > 1)) {
                            popGetFullAfroStudioMessage(viewAnimator, activityContext);
                        } else {
                            ImageView icoImage = (ImageView) v;
                            if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).contains("ico_djembe"))
                                if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_slap);
                                    icoImage.setTag(R.string.tag0, "ico_mid_slap");
                                } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_slap")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_tone);
                                    icoImage.setTag(R.string.tag0, "ico_mid_tone");
                                } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_tone")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_bass);
                                    icoImage.setTag(R.string.tag0, "ico_mid_bass");
                                } else {
                                    icoImage.setImageResource(R.drawable.ico_mid);
                                    icoImage.setTag(R.string.tag0, "ico_mid");
                                }
                            if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_dun"))
                                if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_bass_bell);
                                    icoImage.setTag(R.string.tag0, "ico_mid_bass_bell");
                                } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_bass_bell")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_bell);
                                    icoImage.setTag(R.string.tag0, "ico_mid_bell");
                                } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_bell")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_bass_bell_mute);
                                    icoImage.setTag(R.string.tag0, "ico_mid_bass_bell_mute");
                                } else {
                                    icoImage.setImageResource(R.drawable.ico_mid);
                                    icoImage.setTag(R.string.tag0, "ico_mid");
                                }
                            if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_sag"))
                                if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_tone_bell);
                                    icoImage.setTag(R.string.tag0, "ico_mid_tone_bell");
                                } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_tone_bell")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_bell);
                                    icoImage.setTag(R.string.tag0, "ico_mid_bell");
                                } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_bell")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_tone_bell_mute);
                                    icoImage.setTag(R.string.tag0, "ico_mid_tone_bell_mute");
                                } else {
                                    icoImage.setImageResource(R.drawable.ico_mid);
                                    icoImage.setTag(R.string.tag0, "ico_mid");
                                }
                            if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_ken"))
                                if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_tone_bell_white);
                                    icoImage.setTag(R.string.tag0, "ico_mid_tone_bell_white");
                                } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_tone_bell_white")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_bell);
                                    icoImage.setTag(R.string.tag0, "ico_mid_bell");
                                } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_bell")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_tone_bell_mute_white);
                                    icoImage.setTag(R.string.tag0, "ico_mid_tone_bell_mute_white");
                                } else {
                                    icoImage.setImageResource(R.drawable.ico_mid);
                                    icoImage.setTag(R.string.tag0, "ico_mid");
                                }
                            if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_shek"))
                                if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_slap);
                                    icoImage.setTag(R.string.tag0, "ico_mid_slap");
                                } else {
                                    icoImage.setImageResource(R.drawable.ico_mid);
                                    icoImage.setTag(R.string.tag0, "ico_mid");
                                }
                            if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_balet"))
                                if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_bass);
                                    icoImage.setTag(R.string.tag0, "ico_mid_bass");
                                } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_bass")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_tone);
                                    icoImage.setTag(R.string.tag0, "ico_mid_tone");
                                } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_mid_tone")) {
                                    icoImage.setImageResource(R.drawable.ico_mid_tone_white);
                                    icoImage.setTag(R.string.tag0, "ico_mid_tone_white");
                                } else {
                                    icoImage.setImageResource(R.drawable.ico_mid);
                                    icoImage.setTag(R.string.tag0, "ico_mid");
                                }
                            updateEnsembleFromGui((LinearLayout) instrumentLayout.getParent(), ensemble);
                        }
                    }
                });
                icoMidImage2.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (AfroStudioVersion.equals("free") && (ensemble.getBars() > 1)) {
                            popGetFullAfroStudioMessage(viewAnimator, activityContext);
                        } else {
                            ImageView icoImage = (ImageView) v;
                            if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).contains("ico_djembe")  || String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_balet")) {
                                DialogFragmentSpecialStroke dialogFragmentSpecialStroke = new DialogFragmentSpecialStroke();
                                dialogFragmentSpecialStroke.icoImage = icoImage;
                                dialogFragmentSpecialStroke.ensemble = ensemble;
                                dialogFragmentSpecialStroke.show(((FragmentActivity) activityContext).getSupportFragmentManager(), "Select Instrument");
                            }
                        }
                        return false;
                    }
                });
                //Log.v(TAG, "AddInstrument: instrumentImageCompass - id:" + currentID);
                //icoMidImage2.setId(currentID++);
                instrumentLayout.addView(icoMidImage2);
            }

            ImageView icoEndImage = new ImageView(activityContext);
            icoEndImage.setPadding(0, 0, 0, 0);
            icoEndImage.setImageResource(R.drawable.ico_end);
            icoEndImage.setLayoutParams(new ViewGroup.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f)));
            icoEndImage.setTag(R.string.tag0, "ico_end");
            icoEndImage.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (AfroStudioVersion.equals("free") && (ensemble.getBars() > 1)) {
                        popGetFullAfroStudioMessage(viewAnimator, activityContext);
                    } else {
                        ImageView icoImage = (ImageView) v;
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).contains("ico_djembe"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end")) {
                                icoImage.setImageResource(R.drawable.ico_end_slap);
                                icoImage.setTag(R.string.tag0, "ico_end_slap");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end_slap")) {
                                icoImage.setImageResource(R.drawable.ico_end_tone);
                                icoImage.setTag(R.string.tag0, "ico_end_tone");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end_tone")) {
                                icoImage.setImageResource(R.drawable.ico_end_bass);
                                icoImage.setTag(R.string.tag0, "ico_end_bass");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_end);
                                icoImage.setTag(R.string.tag0, "ico_end");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_dun"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end")) {
                                icoImage.setImageResource(R.drawable.ico_end_bass_bell);
                                icoImage.setTag(R.string.tag0, "ico_end_bass_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end_bass_bell")) {
                                icoImage.setImageResource(R.drawable.ico_end_bell);
                                icoImage.setTag(R.string.tag0, "ico_end_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end_bell")) {
                                icoImage.setImageResource(R.drawable.ico_end_bass_bell_mute);
                                icoImage.setTag(R.string.tag0, "ico_end_bass_bell_mute");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_end);
                                icoImage.setTag(R.string.tag0, "ico_end");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_sag"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end")) {
                                icoImage.setImageResource(R.drawable.ico_end_tone_bell);
                                icoImage.setTag(R.string.tag0, "ico_end_tone_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end_tone_bell")) {
                                icoImage.setImageResource(R.drawable.ico_end_bell);
                                icoImage.setTag(R.string.tag0, "ico_end_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end_bell")) {
                                icoImage.setImageResource(R.drawable.ico_end_tone_bell_mute);
                                icoImage.setTag(R.string.tag0, "ico_end_tone_bell_mute");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_end);
                                icoImage.setTag(R.string.tag0, "ico_end");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_ken"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end")) {
                                icoImage.setImageResource(R.drawable.ico_end_tone_bell_white);
                                icoImage.setTag(R.string.tag0, "ico_end_tone_bell_white");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end_tone_bell_white")) {
                                icoImage.setImageResource(R.drawable.ico_end_bell);
                                icoImage.setTag(R.string.tag0, "ico_end_bell");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end_bell")) {
                                icoImage.setImageResource(R.drawable.ico_end_tone_bell_mute_white);
                                icoImage.setTag(R.string.tag0, "ico_end_tone_bell_mute_white");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_end);
                                icoImage.setTag(R.string.tag0, "ico_end");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_shek"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end")) {
                                icoImage.setImageResource(R.drawable.ico_end_slap);
                                icoImage.setTag(R.string.tag0, "ico_end_slap");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_end);
                                icoImage.setTag(R.string.tag0, "ico_end");
                            }
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_balet"))
                            if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end")) {
                                icoImage.setImageResource(R.drawable.ico_end_bass);
                                icoImage.setTag(R.string.tag0, "ico_end_bass");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end_bass")) {
                                icoImage.setImageResource(R.drawable.ico_end_tone);
                                icoImage.setTag(R.string.tag0, "ico_end_tone");
                            } else if (String.valueOf(icoImage.getTag(R.string.tag0)).equals("ico_end_tone")) {
                                icoImage.setImageResource(R.drawable.ico_end_tone_white);
                                icoImage.setTag(R.string.tag0, "ico_end_tone_white");
                            } else {
                                icoImage.setImageResource(R.drawable.ico_end);
                                icoImage.setTag(R.string.tag0, "ico_end");
                            }
                        updateEnsembleFromGui((LinearLayout) instrumentLayout.getParent(), ensemble);
                    }
                }
            });
            icoEndImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (AfroStudioVersion.equals("free") && (ensemble.getBars() > 1)) {
                        popGetFullAfroStudioMessage(viewAnimator, activityContext);
                    } else {
                        ImageView icoImage = (ImageView) v;
                        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).contains("ico_djembe")  || String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_balet")) {
                            DialogFragmentSpecialStroke dialogFragmentSpecialStroke = new DialogFragmentSpecialStroke();
                            dialogFragmentSpecialStroke.icoImage = icoImage;
                            dialogFragmentSpecialStroke.ensemble = ensemble;
                            dialogFragmentSpecialStroke.show(((FragmentActivity) activityContext).getSupportFragmentManager(), "Select Instrument");
                        }
                    }
                    return false;
                }
            });
            //icoEndImage.setId(currentID++);
            instrumentLayout.addView(icoEndImage);
        }

        // Division
        ImageView icoDivImage = new ImageView(activityContext);
        icoDivImage.setPadding(0, 0, 0, 0);
        icoDivImage.setImageResource(R.drawable.ico_div);
        icoDivImage.setLayoutParams(new ViewGroup.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f)));
        icoDivImage.setTag(R.string.tag0, "ico_div");
        icoDivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AfroStudioVersion.equals("free") && (ensemble.getBars() > 1)) {
                    popGetFullAfroStudioMessage(viewAnimator, activityContext);
                } else {
                    DialogFragmentSetRepetition dialogFragmentSetRepetition = new DialogFragmentSetRepetition();
                    dialogFragmentSetRepetition.imageDiv = (ImageView) v;
                    dialogFragmentSetRepetition.ensemble = ensemble;
                    dialogFragmentSetRepetition.activityContext = activityContext;
                    dialogFragmentSetRepetition.show(((FragmentActivity) activityContext).getSupportFragmentManager(), "Select Instrument");
                }
            }
        });
        //icoDivImage.setId(currentID++);
        instrumentLayout.addView(icoDivImage);
    }

    // CHANGE IMAGES FUNCTION
    public static void setImageScale(LinearLayout ensembleLayout, HorizontalScrollView horizontalScrollView, final android.content.Context activityContext, float iconScale, Ensemble ensemble, final ViewAnimator viewAnimator) {

        // just redraw the scene :)
        setEnsembleFromGui(ensembleLayout, ensemble);
        setGuiFromEnsemble(ensembleLayout, ensemble, activityContext, iconScale, viewAnimator);

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
    }

    public static void saveEnsemble(final Ensemble ensemble, final android.content.Context activityContext, boolean notify, boolean fromMenu, final ViewAnimator viewAnimator, String user) {

        if (AfroStudioVersion.equals("free") && fromMenu) { //when notify
            popGetFullAfroStudioMessage(viewAnimator, activityContext);
        } else {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) { //Media Ready to read and write
                user = user.substring(0, user.indexOf("@"));
                String fileName = ensemble.ensembleName + "_by_" + ensemble.ensembleAuthor + "_u_" + user + ".afr";
                File file = new File(activityContext.getExternalFilesDir(null), fileName);

                try {
                    OutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(ensemble.saveVectorsInJSON().getBytes());
                    outputStream.close();
                    if (notify)
                        Toast.makeText(activityContext, ensemble.ensembleName + " " + activityContext.getResources().getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
                } catch (IOException e) { // Unable to create file, likely because external storage is not currently mounted.
                    //Log.w("ExternalStorage", "Error writing " + file, e);
                }
            }
        }
    }

    public static void popGetFullAfroStudioMessage(final ViewAnimator viewAnimator, final android.content.Context activityContext) {

        Animation slide_in_left = AnimationUtils.loadAnimation(activityContext, android.R.anim.slide_in_left);
        Animation slide_out_right = AnimationUtils.loadAnimation(activityContext, android.R.anim.slide_out_right);
        viewAnimator.setOutAnimation(null);
        viewAnimator.setInAnimation(null);
        viewAnimator.setDisplayedChild(7);
        viewAnimator.setOutAnimation(slide_out_right);
        viewAnimator.setInAnimation(slide_in_left);
        viewAnimator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ViewAnimator) v).getCurrentView().getId() == R.id.tut_getfull) {
                    v.setOnClickListener(null); // avoid bouncing if several clicks
                    Animation slide_out_right = AnimationUtils.loadAnimation(activityContext, android.R.anim.slide_out_right);
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
    }

    public static void popSupportAfroStudioMessage(final ViewAnimator viewAnimator, final android.content.Context activityContext) {


        Animation slide_in_left = AnimationUtils.loadAnimation(activityContext, android.R.anim.slide_in_left);
        Animation slide_out_right = AnimationUtils.loadAnimation(activityContext, android.R.anim.slide_out_right);
        viewAnimator.setOutAnimation(null);
        viewAnimator.setInAnimation(null);
        viewAnimator.setDisplayedChild(6);
        viewAnimator.setOutAnimation(slide_out_right);
        viewAnimator.setInAnimation(slide_in_left);
        viewAnimator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ViewAnimator) v).getCurrentView().getId() == R.id.tut_rate) {
                    v.setOnClickListener(null); // avoid bouncing if several clicks
                    Animation slide_out_right = AnimationUtils.loadAnimation(activityContext, android.R.anim.slide_out_right);
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
    }

    // To keep compatibility with filenames that do not have _by_ or _u_ in their names
    // To be removed one all users are on version 20 or higher
    public static void correctFileNames(final android.content.Context activityContext, String user) {

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) { //Media Ready to read and write
            String newUser = user.substring(0, user.indexOf("@"));
            File localFileList[] = activityContext.getExternalFilesDir(null).listFiles();
            if (localFileList != null)
                for (int i = 0; i < localFileList.length; i++) {
                    String fileName = localFileList[i].getName();
                    if (fileName.contains(".afr") && (!fileName.contains("_by_") || !fileName.contains("_u_"))) { //rename!
                        fileName = fileName.substring(0, fileName.indexOf(".afr"));
                        File newFile = new File(activityContext.getExternalFilesDir(null), fileName + "_by_" + newUser + "_u_" + newUser + ".afr");
                        localFileList[i].renameTo(newFile);
                    }
                }
        }
    }

}
