package com.yaray.afrostudio;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentShare extends DialogFragment {

    public interface ShareListener {
        public void sharePositiveClick(String option);
    }

    ShareListener shareListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            shareListener = (ShareListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Listener");
        }
    }

    LayoutInflater inflater;
    View view;

    String option="audio";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_share, null);

        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.share_options_rgroup);
        radioGroup.check(R.id.share_audio);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.share_audio)
                    option = "audio";
                else if (checkedId == R.id.share_ensemble)
                    option = "ensemble";
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                shareListener.sharePositiveClick(option);
            }
        }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        return builder.create();

    }

}
