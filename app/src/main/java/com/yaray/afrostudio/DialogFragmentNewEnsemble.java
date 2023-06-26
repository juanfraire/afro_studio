package com.yaray.afrostudio;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentNewEnsemble extends DialogFragment {

    public interface NewEnsembleListener {
        public void newEnsemblePositiveClick(int beatsPerBar, int numBar, boolean emptyInstruments);
    }

    NewEnsembleListener newEnsembleListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            newEnsembleListener = (NewEnsembleListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    // 16/8 -> **** **** **** **** (16 beats -eights- per bar) (beatsPerBar=16)
    // 12/8 -> ***  ***  ***  ***  (12 beats -eights- per bar) (beatsPerBar=12)
    //  9/8 -> ***  ***  ***       ( 9 beats -eights- per bar) (beatsPerBar=9)
    int beatsPerBar=16;
    int numBar=1;
    boolean emptyInstruments=false;
    LayoutInflater inflater;
    View view;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_new_ensemble, null);

        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.time_signature_rgroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.beatsPerBar16)
                    beatsPerBar = 16;
                else if (checkedId == R.id.beatsPerBar12)
                    beatsPerBar = 12;
                else if (checkedId == R.id.beatsPerBar9)
                    beatsPerBar = 9;
            }
        });

        final TextView tempoTextView = (TextView) view.findViewById(R.id.bar_number_text);
        tempoTextView.setText(String.valueOf(numBar) + " Bars");

        final SeekBar tempoSeekBar = (SeekBar) view.findViewById(R.id.bar_number_seekbar);
        tempoSeekBar.setProgress((numBar - 1) * 10); //bars = progress/10 + 1 - progress = (bars-1)*10
        tempoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                numBar = progress/10 + 1;
                tempoTextView.setText(String.valueOf(numBar) + " Bars");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                newEnsembleListener.newEnsemblePositiveClick(beatsPerBar, numBar, emptyInstruments);
            }
        }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }
    //int beatsPerBar; //16, 12, 9

}
