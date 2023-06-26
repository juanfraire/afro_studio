package com.yaray.afrostudio;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentEqualizer extends DialogFragment {

    public interface EqualizerListener {
        public void equalizerPositiveClick();
    }

    EqualizerListener equalizerListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            equalizerListener = (EqualizerListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    LayoutInflater inflater;
    View view;

    android.content.Context activityContext;
    LinearLayout ensembleLayout;
    Ensemble ensemble;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_equalizer, null);

        LinearLayout equalizerLayout = (LinearLayout) view.findViewById(R.id.equalizerLayout);;

        float iconScale = (float) 0.75;
        final float displayDensity = getResources().getDisplayMetrics().density;

        for (int i = 0; i < ensemble.shekVector.size(); i++) {
            LinearLayout.LayoutParams paramsSeekBar = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) (3 * iconScale * 52.5 * displayDensity + 0.5f));
            VerticalSeekBar instrumentSeekBar = new VerticalSeekBar(activityContext);
            instrumentSeekBar.setProgress(ensemble.shekVolume.elementAt(i));
            final int instPosInVector = i;
            instrumentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ensemble.shekVolume.setElementAt(progress, instPosInVector);
                    //Update in Gui
                    int instPosInEnsemble=0;
                    for (int i = 0; i < ensembleLayout.getChildCount(); i++) {
                        LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
                        if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_shek")) {
                            if(instPosInEnsemble==instPosInVector){ //This instrument layout has the instrument I want
                                ((ImageView) (instrumentLayout.getChildAt(0))).setTag(R.string.tag1, progress);
                            }
                            instPosInEnsemble++;
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            LinearLayout.LayoutParams paramsIcon = new LinearLayout.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f));
            ImageView instrumentIcon = new ImageView(activityContext);
            instrumentIcon.setImageResource(R.drawable.ico_shek);
            if(ensemble.shekStatus.elementAt(instPosInVector)==1) //enabled
                instrumentIcon.setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
            else
                instrumentIcon.setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
            instrumentIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ensemble.shekStatus.elementAt(instPosInVector)==1){
                        ensemble.shekStatus.setElementAt(0,instPosInVector);
                        ((ImageView)v).setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
                    } else {
                        ensemble.shekStatus.setElementAt(1,instPosInVector);
                        ((ImageView)v).setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
                    }
                }
            });

            LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsLayout.setMargins(20, 0, 20, 10);
            LinearLayout instrumentEqualizerLayout = new LinearLayout(activityContext);
            instrumentEqualizerLayout.setOrientation(LinearLayout.VERTICAL);
            instrumentEqualizerLayout.addView(instrumentSeekBar, paramsSeekBar);
            instrumentEqualizerLayout.addView(instrumentIcon, paramsIcon);

            equalizerLayout.addView(instrumentEqualizerLayout, paramsLayout);
        }
        for (int i = 0; i < ensemble.baletVector.size(); i++) {
            LinearLayout.LayoutParams paramsSeekBar = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) (3 * iconScale * 52.5 * displayDensity + 0.5f));
            VerticalSeekBar instrumentSeekBar = new VerticalSeekBar(activityContext);
            instrumentSeekBar.setProgress(ensemble.baletVolume.elementAt(i));
            final int instPosInVector = i;
            instrumentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ensemble.baletVolume.setElementAt(progress, instPosInVector);
                    //Update in Gui
                    int instPosInEnsemble=0;
                    for (int i = 0; i < ensembleLayout.getChildCount(); i++) {
                        LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
                        if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_balet")) {
                            if(instPosInEnsemble==instPosInVector){ //This instrument layout has the instrument I want
                                ((ImageView) (instrumentLayout.getChildAt(0))).setTag(R.string.tag1, progress);
                            }
                            instPosInEnsemble++;
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            LinearLayout.LayoutParams paramsIcon = new LinearLayout.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f));
            ImageView instrumentIcon = new ImageView(activityContext);
            instrumentIcon.setImageResource(R.drawable.ico_balet);
            if(ensemble.baletStatus.elementAt(instPosInVector)==1) //enabled
                instrumentIcon.setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
            else
                instrumentIcon.setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
            instrumentIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ensemble.baletStatus.elementAt(instPosInVector)==1){
                        ensemble.baletStatus.setElementAt(0,instPosInVector);
                        ((ImageView)v).setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
                    } else {
                        ensemble.baletStatus.setElementAt(1,instPosInVector);
                        ((ImageView)v).setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
                    }
                }
            });

            LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsLayout.setMargins(20, 0, 20, 10);
            LinearLayout instrumentEqualizerLayout = new LinearLayout(activityContext);
            instrumentEqualizerLayout.setOrientation(LinearLayout.VERTICAL);
            instrumentEqualizerLayout.addView(instrumentSeekBar, paramsSeekBar);
            instrumentEqualizerLayout.addView(instrumentIcon, paramsIcon);

            equalizerLayout.addView(instrumentEqualizerLayout, paramsLayout);
        }
        for (int i = 0; i < ensemble.dunVector.size(); i++) {
            LinearLayout.LayoutParams paramsSeekBar = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) (3 * iconScale * 52.5 * displayDensity + 0.5f));
            VerticalSeekBar instrumentSeekBar = new VerticalSeekBar(activityContext);
            instrumentSeekBar.setProgress(ensemble.dunVolume.elementAt(i));
            final int instPosInVector = i;
            instrumentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ensemble.dunVolume.setElementAt(progress, instPosInVector);
                    //Update in Gui
                    int instPosInEnsemble=0;
                    for (int i = 0; i < ensembleLayout.getChildCount(); i++) {
                        LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
                        if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_dun")) {
                            if(instPosInEnsemble==instPosInVector){ //This instrument layout has the instrument I want
                                ((ImageView) (instrumentLayout.getChildAt(0))).setTag(R.string.tag1, progress);
                            }
                            instPosInEnsemble++;
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            LinearLayout.LayoutParams paramsIcon = new LinearLayout.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f));
            ImageView instrumentIcon = new ImageView(activityContext);
            instrumentIcon.setImageResource(R.drawable.ico_dun);
            if(ensemble.dunStatus.elementAt(instPosInVector)==1) //enabled
                instrumentIcon.setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
            else
                instrumentIcon.setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
            instrumentIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ensemble.dunStatus.elementAt(instPosInVector) == 1) {
                        ensemble.dunStatus.setElementAt(0, instPosInVector);
                        ((ImageView) v).setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
                    } else {
                        ensemble.dunStatus.setElementAt(1, instPosInVector);
                        ((ImageView) v).setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
                    }
                }
            });

            LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsLayout.setMargins(20, 0, 20, 10);
            LinearLayout instrumentEqualizerLayout = new LinearLayout(activityContext);
            instrumentEqualizerLayout.setOrientation(LinearLayout.VERTICAL);
            instrumentEqualizerLayout.addView(instrumentSeekBar, paramsSeekBar);
            instrumentEqualizerLayout.addView(instrumentIcon, paramsIcon);

            equalizerLayout.addView(instrumentEqualizerLayout, paramsLayout);
        }
        for (int i = 0; i < ensemble.sagVector.size(); i++) {
            LinearLayout.LayoutParams paramsSeekBar = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) (3 * iconScale * 52.5 * displayDensity + 0.5f));
            VerticalSeekBar instrumentSeekBar = new VerticalSeekBar(activityContext);
            instrumentSeekBar.setProgress(ensemble.sagVolume.elementAt(i));
            final int instPosInVector = i;
            instrumentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ensemble.sagVolume.setElementAt(progress, instPosInVector);
                    //Update in Gui
                    int instPosInEnsemble=0;
                    for (int i = 0; i < ensembleLayout.getChildCount(); i++) {
                        LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
                        if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_sag")) {
                            if(instPosInEnsemble==instPosInVector){ //This instrument layout has the instrument I want
                                ((ImageView) (instrumentLayout.getChildAt(0))).setTag(R.string.tag1, progress);
                            }
                            instPosInEnsemble++;
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            LinearLayout.LayoutParams paramsIcon = new LinearLayout.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f));
            ImageView instrumentIcon = new ImageView(activityContext);
            instrumentIcon.setImageResource(R.drawable.ico_sag);
            if(ensemble.sagStatus.elementAt(instPosInVector)==1) //enabled
                instrumentIcon.setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
            else
                instrumentIcon.setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
            instrumentIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ensemble.sagStatus.elementAt(instPosInVector) == 1) {
                        ensemble.sagStatus.setElementAt(0, instPosInVector);
                        ((ImageView) v).setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
                    } else {
                        ensemble.sagStatus.setElementAt(1, instPosInVector);
                        ((ImageView) v).setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
                    }
                }
            });

            LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsLayout.setMargins(20, 0, 20, 10);
            LinearLayout instrumentEqualizerLayout = new LinearLayout(activityContext);
            instrumentEqualizerLayout.setOrientation(LinearLayout.VERTICAL);
            instrumentEqualizerLayout.addView(instrumentSeekBar, paramsSeekBar);
            instrumentEqualizerLayout.addView(instrumentIcon, paramsIcon);

            equalizerLayout.addView(instrumentEqualizerLayout, paramsLayout);
        }
        for (int i = 0; i < ensemble.kenVector.size(); i++) {
            LinearLayout.LayoutParams paramsSeekBar = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) (3 * iconScale * 52.5 * displayDensity + 0.5f));
            VerticalSeekBar instrumentSeekBar = new VerticalSeekBar(activityContext);
            instrumentSeekBar.setProgress(ensemble.kenVolume.elementAt(i));
            final int instPosInVector = i;
            instrumentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ensemble.kenVolume.setElementAt(progress, instPosInVector);
                    //Update in Gui
                    int instPosInEnsemble=0;
                    for (int i = 0; i < ensembleLayout.getChildCount(); i++) {
                        LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
                        if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_ken")) {
                            if(instPosInEnsemble==instPosInVector){ //This instrument layout has the instrument I want
                                ((ImageView) (instrumentLayout.getChildAt(0))).setTag(R.string.tag1, progress);
                            }
                            instPosInEnsemble++;
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            LinearLayout.LayoutParams paramsIcon = new LinearLayout.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f));
            ImageView instrumentIcon = new ImageView(activityContext);
            instrumentIcon.setImageResource(R.drawable.ico_ken);
            if(ensemble.kenStatus.elementAt(instPosInVector)==1) //enabled
                instrumentIcon.setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
            else
                instrumentIcon.setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
            instrumentIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ensemble.kenStatus.elementAt(instPosInVector) == 1) {
                        ensemble.kenStatus.setElementAt(0, instPosInVector);
                        ((ImageView) v).setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
                    } else {
                        ensemble.kenStatus.setElementAt(1, instPosInVector);
                        ((ImageView) v).setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
                    }
                }
            });

            LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsLayout.setMargins(20, 0, 20, 10);
            LinearLayout instrumentEqualizerLayout = new LinearLayout(activityContext);
            instrumentEqualizerLayout.setOrientation(LinearLayout.VERTICAL);
            instrumentEqualizerLayout.addView(instrumentSeekBar, paramsSeekBar);
            instrumentEqualizerLayout.addView(instrumentIcon, paramsIcon);

            equalizerLayout.addView(instrumentEqualizerLayout, paramsLayout);
        }
        for (int i = 0; i < ensemble.djembeVector.size(); i++) {
            LinearLayout.LayoutParams paramsSeekBar = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) (3 * iconScale * 52.5 * displayDensity + 0.5f));
            VerticalSeekBar instrumentSeekBar = new VerticalSeekBar(activityContext);
            instrumentSeekBar.setProgress(ensemble.djembeVolume.elementAt(i));
            final int instPosInVector = i;
            instrumentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ensemble.djembeVolume.setElementAt(progress, instPosInVector);
                    //Update in Gui
                    int instPosInEnsemble=0;
                    for (int i = 0; i < ensembleLayout.getChildCount(); i++) {
                        LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
                        if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).contains("ico_djembe")) {
                            if(instPosInEnsemble==instPosInVector){ //This instrument layout has the instrument I want
                                ((ImageView) (instrumentLayout.getChildAt(0))).setTag(R.string.tag1, progress);
                            }
                            instPosInEnsemble++;
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            LinearLayout.LayoutParams paramsIcon = new LinearLayout.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f));
            ImageView instrumentIcon = new ImageView(activityContext);

            if(i%3==0)
                instrumentIcon.setImageResource(R.drawable.ico_djembe_1);
            if(i%3==1)
                instrumentIcon.setImageResource(R.drawable.ico_djembe_2);
            if(i%3==2)
                instrumentIcon.setImageResource(R.drawable.ico_djembe_3);

            if(ensemble.djembeStatus.elementAt(instPosInVector)==1) //enabled
                instrumentIcon.setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
            else
                instrumentIcon.setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
            instrumentIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ensemble.djembeStatus.elementAt(instPosInVector) == 1) {
                        ensemble.djembeStatus.setElementAt(0, instPosInVector);
                        ((ImageView) v).setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
                    } else {
                        ensemble.djembeStatus.setElementAt(1, instPosInVector);
                        ((ImageView) v).setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
                    }
                }
            });

            LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsLayout.setMargins(20, 0, 20, 10);
            LinearLayout instrumentEqualizerLayout = new LinearLayout(activityContext);
            instrumentEqualizerLayout.setOrientation(LinearLayout.VERTICAL);
            instrumentEqualizerLayout.addView(instrumentSeekBar, paramsSeekBar);
            instrumentEqualizerLayout.addView(instrumentIcon, paramsIcon);

            equalizerLayout.addView(instrumentEqualizerLayout, paramsLayout);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                equalizerListener.equalizerPositiveClick();
            }
        });

        return builder.create();
    }
}
