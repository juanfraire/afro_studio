package com.yaray.afrostudio;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentBarOptions extends DialogFragment {

    public interface BarOptionsListener {
        public void barOptionsPositiveClick(int barPos, String option, String name);
    }

    BarOptionsListener barOptionsListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            barOptionsListener = (BarOptionsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Listener");
        }
    }

    LayoutInflater inflater;
    View view;

    String option="";
    String name;

    Ensemble ensemble;
    int barPos;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_bar_options, null);

        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.instrument_options_rgroup);
        radioGroup.clearCheck();

        // disable remove if last bar on ensemble
        if(ensemble.getBars()<=1)
            radioGroup.getChildAt(1).setEnabled(false);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.clear_bar)
                    option = "clear";
                else if (checkedId == R.id.remove_bar)
                    option = "remove";
                else if (checkedId == R.id.clone_bar)
                    option = "clone_bar";
            }
        });

        //set_bar_name
        final EditText editTextName = (EditText) view.findViewById(R.id.set_bar_name);
        editTextName.setText(ensemble.barName.elementAt(barPos));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                name = editTextName.getText().toString();
                barOptionsListener.barOptionsPositiveClick(barPos, option, name);
            }
        }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }



}
