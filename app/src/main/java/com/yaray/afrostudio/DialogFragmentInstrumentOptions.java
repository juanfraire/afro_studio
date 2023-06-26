package com.yaray.afrostudio;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentInstrumentOptions extends DialogFragment {

    public interface InstrumentOptionsListener {
        public void instrumentOptionsPositiveClick(String option, int volume, ImageView instImage);
    }

    InstrumentOptionsListener instrumentOptionsListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            instrumentOptionsListener = (InstrumentOptionsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Listener");
        }
    }

    LayoutInflater inflater;
    View view;
    int volume;
    boolean onPlay=false;

    Ensemble ensemble;
    LinearLayout ensembleLayout;
    android.content.Context activityContext;

    String option=""; // duplicate, clear, remove
    ImageView instImage;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_instrument_options, null);

        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.instrument_options_rgroup);
        radioGroup.clearCheck();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.clear_instrument)
                    option = "clear";
                else if (checkedId == R.id.remove_instrument)
                    option = "remove";
            }
        });

        //get current volume from instImage
        volume = (Integer)instImage.getTag(R.string.tag1);

        final SeekBar volumeSeekBar = (SeekBar) view.findViewById(R.id.volume_seekbar);
        volumeSeekBar.setProgress(volume); //bars = progress/10 + 1 - progress = (bars-1)*10
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volume = progress;
                instImage.setTag(R.string.tag1, volume);

                EnsembleUtils.updateEnsembleFromGui(ensembleLayout, ensemble);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final CheckBox enabledCheckBox = (CheckBox) view.findViewById(R.id.dialog_enabled);
        if (String.valueOf(instImage.getTag(R.string.tag0)).equals("enabled"))
            enabledCheckBox.setChecked(true);
        else
            enabledCheckBox.setChecked(false);
        enabledCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (String.valueOf(instImage.getTag(R.string.tag0)).equals("enabled")) { //Disable
                    instImage.setTag(R.string.tag0, "disabled");
                    ((ImageView) instImage).setColorFilter(ContextCompat.getColor(activityContext, R.color.lightGray_afrostudio));
                } else {
                    instImage.setTag(R.string.tag0, "enabled");
                    ((ImageView) instImage).setColorFilter(ContextCompat.getColor(activityContext, R.color.green_afrostudio));
                }
                EnsembleUtils.updateEnsembleFromGui(ensembleLayout, ensemble);
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                instrumentOptionsListener.instrumentOptionsPositiveClick(option, volume, instImage);
            }
        }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        return builder.create();

    }


}
