package com.yaray.afrostudio;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import java.util.Vector;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentAddInstrument extends DialogFragment {

    public interface AddInstrumentListener {
        public void addInstrumentPositiveClick(Vector<String> instrumentChoice);
    }

    AddInstrumentListener addInstrumentListener;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            addInstrumentListener = (AddInstrumentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    LayoutInflater inflater;
    View view;
    Vector<String> instrumentChoice;//"djembe", "dun", "sag", "ken", "shek"

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_add_instrument, null);

        instrumentChoice = new Vector<String>();

        final CheckBox djembeCheckBox = (CheckBox) view.findViewById(R.id.dialog_add_instrument_Djembe);
        final CheckBox shekCheckBox = (CheckBox) view.findViewById(R.id.dialog_add_instrument_Shekere);
        final CheckBox dunCheckBox = (CheckBox) view.findViewById(R.id.dialog_add_instrument_Dundun);
        final CheckBox sagCheckBox = (CheckBox) view.findViewById(R.id.dialog_add_instrument_Sagbang);
        final CheckBox kenCheckBox = (CheckBox) view.findViewById(R.id.dialog_add_instrument_Kenkeny);
        final CheckBox baletCheckBox = (CheckBox) view.findViewById(R.id.dialog_add_instrument_Balet);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(view).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if(djembeCheckBox.isChecked())
                    instrumentChoice.add("djembe");
                if(shekCheckBox.isChecked())
                    instrumentChoice.add("shek");
                if(dunCheckBox.isChecked())
                    instrumentChoice.add("dun");
                if(sagCheckBox.isChecked())
                    instrumentChoice.add("sag");
                if(kenCheckBox.isChecked())
                    instrumentChoice.add("ken");
                if(baletCheckBox.isChecked())
                    instrumentChoice.add("balet");

                addInstrumentListener.addInstrumentPositiveClick(instrumentChoice);
            }
        }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

//        builder.setSingleChoiceItems(instrumentList, instrumentChoice, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                instrumentChoice = which;
//            }
//        }).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                addInstrumentListener.addInstrumentPositiveClick(DialogFragmentAddInstrument.this, instrumentListSmall[instrumentChoice].toString());
//            }
//        }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                dialog.dismiss();
//            }
//        });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
