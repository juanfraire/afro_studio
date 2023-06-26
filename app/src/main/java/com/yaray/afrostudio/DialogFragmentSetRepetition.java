package com.yaray.afrostudio;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentSetRepetition extends DialogFragment {

    public interface SetRepetitionListener {
        public void setRepetitionPositiveClick(int thisBar, int barCount, int countTotal);
    }

    SetRepetitionListener setRepetitionListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            setRepetitionListener = (SetRepetitionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    LayoutInflater inflater;
    View view;

    int thisBar;
    int barCount = 1;
    int countTotal = 4;

    android.content.Context activityContext;
    ImageView imageDiv; //Set from listener
    Ensemble ensemble;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int totalBars = 0; //Find thisBar position!
        LinearLayout instrumentLayout = (LinearLayout) imageDiv.getParent();
        LinearLayout ensembleLayout = (LinearLayout) instrumentLayout.getParent();
        for (int i = 0; i < instrumentLayout.getChildCount(); i++) {
            if (String.valueOf(instrumentLayout.getChildAt(i).getTag(R.string.tag0)).contains("ico_div")) {
                totalBars++;
                if (instrumentLayout.getChildAt(i) == imageDiv) {
                    thisBar = totalBars - 1; // bars array starts at 0!
                }
            }
        }

        // Se previous values
        barCount = ensemble.repetitions.elementAt(thisBar)[0]; //barCount
        countTotal = ensemble.repetitions.elementAt(thisBar)[1]; //total

        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_set_repetition, null);

        // Bar Count
        final TextView barCountSeekBarText = (TextView) view.findViewById(R.id.barCountSeekBarText);
        barCountSeekBarText.setText(String.valueOf(barCount) + " " + activityContext.getResources().getString(R.string.dialog_set_repetition_barcount));
        final SeekBar barCountSeekBar = (SeekBar) view.findViewById(R.id.barCountSeekBar);
        // set progress to countTotal-1 -> countTotal = + progress / 5 -> progress = (countTotal-1)*5
        barCountSeekBar.setProgress((barCount - 1) * 10);
        barCountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // SeekBar = 0 to 100 -> 1 barCount to 16 barCount -> barCount = 1 + progress / 6
                barCount = 1 + progress / 10;
                barCountSeekBarText.setText(String.valueOf(barCount) + " " + activityContext.getResources().getString(R.string.dialog_set_repetition_barcount));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Repetitions

        final TextView repetitionsSeekBarText = (TextView) view.findViewById(R.id.repetitionsSeekBarText);
        repetitionsSeekBarText.setText(String.valueOf(countTotal - 1) + " " + activityContext.getResources().getString(R.string.dialog_set_repetition_totalcount));
        final SeekBar repetitionsSeekBar = (SeekBar) view.findViewById(R.id.repetitionsSeekBar);
        // set progress to countTotal-1 -> countTotal = + progress / 5 -> progress = (countTotal-1)*5
        repetitionsSeekBar.setProgress((countTotal - 1) * 5);
        repetitionsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // SeekBar = 0 to 100 -> 1 countTotal to 20 countTotal -> countTotal = 1 + progress / 5
                countTotal = 1 + progress / 5;
                repetitionsSeekBarText.setText(String.valueOf(countTotal - 1) + " " + activityContext.getResources().getString(R.string.dialog_set_repetition_totalcount));
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
                setRepetitionListener.setRepetitionPositiveClick(thisBar, barCount, countTotal);
            }
        }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

}
