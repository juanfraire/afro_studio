package com.yaray.afrostudio;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentLoad extends DialogFragment {

    private static final String TAG = "AfroStudio.FileLoader";

    public interface LoadListener {
        public void loadPositiveClick(String fileName);

        public void loadDeleteEnsembleInServer(String fileName);
    }

    LoadListener loadListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            loadListener = (LoadListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    String localUser = "";

    LayoutInflater inflater;
    View view;
    android.content.Context activityContext;

    View previousView = null; // for selection coloring in file list

    int fileSelection = -1;
    String remoteFileSelection = null;
    File dir;
    final List<String> localFileListString = new ArrayList<String>();
    final List<String> remoteFileListString = new ArrayList<String>();
    LoadFileArrayAdapter fileListAdapterCopy;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        localUser = localUser.substring(0, localUser.indexOf("@"));

        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_load, null);
        final File localFileList[] = dir.listFiles();

        localFileListString.add(new String("Local"));
        if (localFileList != null)
            for (int i = 0; i < localFileList.length; i++)
                if (localFileList[i].getName().contains(".afr"))
                    localFileListString.add(localFileList[i].getName());
        localFileListString.add(new String("Server"));

        final LoadFileArrayAdapter fileListAdapter = new LoadFileArrayAdapter(view.getContext(), R.layout.dialog_load_file_name, R.id.file_textview, localFileListString);
        fileListAdapterCopy = fileListAdapter;
        ListView fileListView = (ListView) view.findViewById(R.id.file_listView);
        fileListView.setAdapter(fileListAdapter);
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int fileListPosition = -1; //Get position in original list
                String name = ((TextView) ((LinearLayout) view).getChildAt(0)).getText().toString();
                String line2 = ((TextView) ((LinearLayout) view).getChildAt(1)).getText().toString();
                if (!(name.equals("Local") || name.equals("Server"))) {
                    String author = line2.substring(0, line2.indexOf(" ("));
                    String user = line2.substring(line2.indexOf(" (") + 2, line2.indexOf(")"));
                    for (int i = 0; i < localFileList.length; i++)
                        if ((localFileList[i].getName().equals(name + "_by_" + author + "_u_" + user + ".afr"))) //check if a remote has same name as local!
                            fileListPosition = i;

                    if (position > fileListPosition + 1) // Im selecting a rithm in the server that matches a locar name/author/user pair.
                        fileListPosition = -1;

                    remoteFileSelection = name + "_by_" + author + "_u_" + user;
                    fileSelection = fileListPosition; // file to load in localFileList (can be -1 if server file chosen)

                    if (previousView != null) {
                        ((TextView) ((LinearLayout) previousView).getChildAt(0)).setTextColor(ContextCompat.getColor(view.getContext(), R.color.gray));
                        ((TextView) ((LinearLayout) previousView).getChildAt(1)).setTextColor(ContextCompat.getColor(view.getContext(), R.color.gray));
                    }
                    ((TextView) ((LinearLayout) view).getChildAt(0)).setTextColor(ContextCompat.getColor(view.getContext(), R.color.green_afrostudio));
                    ((TextView) ((LinearLayout) view).getChildAt(1)).setTextColor(ContextCompat.getColor(view.getContext(), R.color.green_afrostudio));
                    previousView = view;
                }
            }
        });
        fileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                int fileListPosition = -1; //Get position in original list
                String name = ((TextView) ((LinearLayout) view).getChildAt(0)).getText().toString();
                String line2 = ((TextView) ((LinearLayout) view).getChildAt(1)).getText().toString();
                if (!(name.equals("Local") || name.equals("Server"))) {
                    String author = line2.substring(0, line2.indexOf(" ("));
                    String user = line2.substring(line2.indexOf(" (") + 2, line2.indexOf(")"));
                    for (int i = 0; i < localFileList.length; i++)
                        if (localFileList[i].getName().equals(name + "_by_" + author + "_u_" + user + ".afr"))
                            fileListPosition = i;

                    if (position > fileListPosition + 1) // Im selecting a rithm in the server that matches a locar name/author/user pair.
                        fileListPosition = -1;

                    if (fileListPosition != -1) {
                        if (localFileList[fileListPosition].delete()) { // update strings in adapter
                            Toast.makeText(view.getContext(), "File deleted", Toast.LENGTH_SHORT).show();
                            File newlocalFileList[] = dir.listFiles();
                            localFileListString.clear();
                            localFileListString.add(new String("Local"));
                            if (newlocalFileList != null)
                                for (int i = 0; i < newlocalFileList.length; i++)  //Log.e(TAG, "File: " + newlocalFileList[i].getAbsolutePath());
                                    if (newlocalFileList[i].getName().contains(".afr"))
                                        localFileListString.add(newlocalFileList[i].getName());
                            localFileListString.add(new String("Server"));
                            localFileListString.addAll(remoteFileListString); // add remote files if any
                            fileListAdapter.notifyDataSetChanged();
                        } else {
                            Log.e(TAG, "Error deleting " + localFileListString.get(position));
                            Toast.makeText(view.getContext(), "Error deleting " + localFileListString.get(position), Toast.LENGTH_SHORT).show();
                        }
                        return true; // Not let single click attend an erased position!
                    } else {
                        if (localUser.equals(user)) {
                            String remoteFileDelete = name + "_by_" + author + "_u_" + user;
                            loadListener.loadDeleteEnsembleInServer(remoteFileDelete);
                            return true;
                        } else {
                            return false; // choose a sever file - take it as a single click
                        }
                    }
                }
                return false;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (fileSelection != -1) // local file
                    loadListener.loadPositiveClick(localFileList[fileSelection].getName());
                else if (remoteFileSelection != null) { // remote file
                    loadListener.loadPositiveClick(remoteFileSelection);
                } else {
                    loadListener.loadPositiveClick(null); // nothing chosen
                }
            }
        }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    public void updateRemoteFileListString(List<String> newRemoteFileListString) {

        // Update localFileList
        File newlocalFileList[] = dir.listFiles();
        localFileListString.clear();
        localFileListString.add(new String("Local"));
        if (newlocalFileList != null)
            for (int i = 0; i < newlocalFileList.length; i++)  //Log.e(TAG, "File: " + newlocalFileList[i].getAbsolutePath());
                if (newlocalFileList[i].getName().contains(".afr"))
                    localFileListString.add(newlocalFileList[i].getName());
        localFileListString.add(new String("Server"));

        remoteFileListString.clear();
        remoteFileListString.addAll(newRemoteFileListString);
        localFileListString.addAll(remoteFileListString);
        fileListAdapterCopy.notifyDataSetChanged();
        //Log.e(TAG, "Updated!");
    }

}
