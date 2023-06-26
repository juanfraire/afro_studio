package com.yaray.afrostudio;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

// http://stackoverflow.com/questions/12767726/constrain-drawableleft-drawablerights-height-to-textviews-height/12767817#12767817
// http://stackoverflow.com/questions/29874465/using-image-in-radiobutton-instead-of-text

public class DialogFragmentSpecialStroke extends DialogFragment {

    public interface SpecialStrokeListener {
        public void specialStrokePositiveClick();
    }

    SpecialStrokeListener specialStrokeListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            specialStrokeListener = (SpecialStrokeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    LayoutInflater inflater;
    View view;

    ImageView icoImage;
    Ensemble ensemble;

    int option = 0;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();

        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).contains("ico_djembe")) {
            view = inflater.inflate(R.layout.dialog_special_stroke_djembe, null);
            RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.special_stroke_djembe_radiogroup);
            radioGroup.clearCheck();
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.special_stroke_flam_bass)
                        option = 4;
                    else if (checkedId == R.id.special_stroke_flam_tone)
                        option = 5;
                    else if (checkedId == R.id.special_stroke_flam_slap)
                        option = 6;
                }
            });
        }

        if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_balet")) {
            view = inflater.inflate(R.layout.dialog_special_stroke_balet, null);
            RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.special_stroke_balet_radiogroup);
            radioGroup.clearCheck();
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.special_stroke_bass_mute)
                        option = 4;
                    else if (checkedId == R.id.special_stroke_tone_mute)
                        option = 5;
                    else if (checkedId == R.id.special_stroke_tone_mute_white)
                        option = 6;
                    else if (checkedId == R.id.special_stroke_ring)
                        option = 7;
                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // Djembe
                if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).contains("ico_djembe")) {
                    if (String.valueOf(icoImage.getTag(R.string.tag0)).contains("_ini")) {
                        if (option == 4) {
                            icoImage.setImageResource(R.drawable.ico_ini_bass_flam);
                            icoImage.setTag(R.string.tag0, "ico_ini_bass_flam");
                        }
                        if (option == 5) {
                            icoImage.setImageResource(R.drawable.ico_ini_tone_flam);
                            icoImage.setTag(R.string.tag0, "ico_ini_tone_flam");
                        }
                        if (option == 6) {
                            icoImage.setImageResource(R.drawable.ico_ini_slap_flam);
                            icoImage.setTag(R.string.tag0, "ico_ini_slap_flam");
                        }
                    }
                    if (String.valueOf(icoImage.getTag(R.string.tag0)).contains("_mid")) {
                        if (option == 4) {
                            icoImage.setImageResource(R.drawable.ico_mid_bass_flam);
                            icoImage.setTag(R.string.tag0, "ico_mid_bass_flam");
                        }
                        if (option == 5) {
                            icoImage.setImageResource(R.drawable.ico_mid_tone_flam);
                            icoImage.setTag(R.string.tag0, "ico_mid_tone_flam");
                        }
                        if (option == 6) {
                            icoImage.setImageResource(R.drawable.ico_mid_slap_flam);
                            icoImage.setTag(R.string.tag0, "ico_mid_slap_flam");
                        }
                    }
                    if (String.valueOf(icoImage.getTag(R.string.tag0)).contains("_end")) {
                        if (option == 4) {
                            icoImage.setImageResource(R.drawable.ico_end_bass_flam);
                            icoImage.setTag(R.string.tag0, "ico_end_bass_flam");
                        }
                        if (option == 5) {
                            icoImage.setImageResource(R.drawable.ico_end_tone_flam);
                            icoImage.setTag(R.string.tag0, "ico_end_tone_flam");
                        }
                        if (option == 6) {
                            icoImage.setImageResource(R.drawable.ico_end_slap_flam);
                            icoImage.setTag(R.string.tag0, "ico_end_slap_flam");
                        }
                    }
                }

                // Balet
                if (String.valueOf(((LinearLayout) icoImage.getParent()).getTag(R.string.tag0)).equals("ico_balet")) {
                    if (String.valueOf(icoImage.getTag(R.string.tag0)).contains("_ini")) {
                        if (option == 4) {
                            icoImage.setImageResource(R.drawable.ico_ini_bass_mute);
                            icoImage.setTag(R.string.tag0, "ico_ini_bass_mute");
                        }
                        if (option == 5) {
                            icoImage.setImageResource(R.drawable.ico_ini_tone_mute);
                            icoImage.setTag(R.string.tag0, "ico_ini_tone_mute");
                        }
                        if (option == 6) {
                            icoImage.setImageResource(R.drawable.ico_ini_tone_mute_white);
                            icoImage.setTag(R.string.tag0, "ico_ini_tone_mute_white");
                        }
                        if (option == 7) {
                            icoImage.setImageResource(R.drawable.ico_ini_ring);
                            icoImage.setTag(R.string.tag0, "ico_ini_ring");
                        }
                    }
                    if (String.valueOf(icoImage.getTag(R.string.tag0)).contains("_mid")) {
                        if (option == 4) {
                            icoImage.setImageResource(R.drawable.ico_mid_bass_mute);
                            icoImage.setTag(R.string.tag0, "ico_mid_bass_mute");
                        }
                        if (option == 5) {
                            icoImage.setImageResource(R.drawable.ico_mid_tone_mute);
                            icoImage.setTag(R.string.tag0, "ico_mid_tone_mute");
                        }
                        if (option == 6) {
                            icoImage.setImageResource(R.drawable.ico_mid_tone_mute_white);
                            icoImage.setTag(R.string.tag0, "ico_mid_tone_mute_white");
                        }
                        if (option == 7) {
                            icoImage.setImageResource(R.drawable.ico_mid_ring);
                            icoImage.setTag(R.string.tag0, "ico_mid_ring");
                        }
                    }
                    if (String.valueOf(icoImage.getTag(R.string.tag0)).contains("_end")) {
                        if (option == 4) {
                            icoImage.setImageResource(R.drawable.ico_end_bass_mute);
                            icoImage.setTag(R.string.tag0, "ico_end_bass_mute");
                        }
                        if (option == 5) {
                            icoImage.setImageResource(R.drawable.ico_end_tone_mute);
                            icoImage.setTag(R.string.tag0, "ico_end_tone_mute");
                        }
                        if (option == 6) {
                            icoImage.setImageResource(R.drawable.ico_end_tone_mute_white);
                            icoImage.setTag(R.string.tag0, "ico_end_tone_mute_white");
                        }
                        if (option == 7) {
                            icoImage.setImageResource(R.drawable.ico_end_ring);
                            icoImage.setTag(R.string.tag0, "ico_end_ring");
                        }
                    }
                }

                specialStrokeListener.specialStrokePositiveClick();
            }
        }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}
