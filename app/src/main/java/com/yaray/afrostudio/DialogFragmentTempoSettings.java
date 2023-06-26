package com.yaray.afrostudio;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentTempoSettings extends DialogFragment {

    public interface TempoSettingsListener {
        public void tempoSettingsPositiveClick();
    }

    TempoSettingsListener tempoSettingsListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            tempoSettingsListener = (TempoSettingsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    LayoutInflater inflater;
    View view;
    Ensemble ensemble; // Initialized from main Activity

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_tempo_settings, null);

        final TextView tempoText = (TextView) view.findViewById(R.id.tempoTextDialog);
        final SeekBar tempoSeekBar = (SeekBar) view.findViewById(R.id.tempoSeekBar);
        final CheckBox loopCheckBox = (CheckBox) view.findViewById(R.id.loopCheckBox);

        loopCheckBox.setChecked(ensemble.onLoop);
        // BPM to Buffer: byteBufferSizeInBytes=(60*176400)/(bpm);
        // Buffer to BPM: bpm = (60*176400) / byteBufferSizeInBytes
        // Since bpm = 60 + progress * 7
        // progress = (bpm-60)/7
        tempoSeekBar.setProgress(((ensemble.bpm - 60) / 7));
        tempoText.setText("Tempo " + String.valueOf(ensemble.bpm) + " bpm");
        tempoSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // BPM to Buffer: byteBufferSizeInBytes=(60*176400)/(bpm);
                        // SeekBar = 0 to 100 -> 60 bpm to 760 bpm -> bpm = 60 + progress * 7
                            ensemble.bpm = 60 + progress * 7;
                            ensemble.byteBufferSizeInBytes = (60 * 176400) / ensemble.bpm;
                            if ((ensemble.byteBufferSizeInBytes % 4) != 0)
                                ensemble.byteBufferSizeInBytes = ensemble.byteBufferSizeInBytes + 4 - ensemble.byteBufferSizeInBytes % 4;
                            tempoText.setText("Tempo " + String.valueOf(ensemble.bpm) + " bpm");
                    }
                }
        );

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ensemble.onLoop = loopCheckBox.isChecked();
                tempoSettingsListener.tempoSettingsPositiveClick(); //update Tempo text
            }
        });

        return builder.create();

    }

}
