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

public class DialogFragmentSetName extends DialogFragment {

    public interface SetNameListener {
        public void setNamePositiveClick(String name, String author);
    }

    SetNameListener setNameListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            setNameListener = (SetNameListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    LayoutInflater inflater;
    View view;

    String name;
    String author;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_set_name, null);

        final EditText editTextName = (EditText) view.findViewById(R.id.set_name_name);
        if (name.equals("Unnamed") || name.equals("Sin Nombre"))
            editTextName.setHint(name);
        else
            editTextName.setText(name);
        final EditText editTextAuthor = (EditText) view.findViewById(R.id.set_name_author);
        if (author.equals("Author") || author.equals("Autor"))
            editTextAuthor.setHint(author);
        else
            editTextAuthor.setText(author);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                name = editTextName.getText().toString();
                name = name.replaceAll("[^\\p{Alpha}\\p{Digit}]+"," ");
                author = editTextAuthor.getText().toString();
                author = author.replaceAll("[^\\p{Alpha}\\p{Digit}]+"," ");

                // Return
                setNameListener.setNamePositiveClick(name, author);
            }
        }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

}
