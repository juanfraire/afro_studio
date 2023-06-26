package com.yaray.afrostudio;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentEmail extends DialogFragment {

    public interface EmailListener {
        public void emailPositiveClick(String email);
    }

    EmailListener emailListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            emailListener = (EmailListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    LayoutInflater inflater;
    View view;

    String email = "";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_email, null);

        final EditText editTextName = (EditText) view.findViewById(R.id.set_email_email);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                email = editTextName.getText().toString();
                emailListener.emailPositiveClick(email);
            }
        }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

}
