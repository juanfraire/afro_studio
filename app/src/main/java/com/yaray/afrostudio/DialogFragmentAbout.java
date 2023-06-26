package com.yaray.afrostudio;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentAbout extends DialogFragment {

    android.content.Context activityContext;
    String AfroStudioVersion;
    LayoutInflater inflater;
    View view;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_about, null);

        int verCode=0;

        try {
            PackageInfo pInfo = activityContext.getPackageManager().getPackageInfo(activityContext.getPackageName(), 0);
            verCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView versionText = (TextView) view.findViewById(R.id.version);
        versionText.append("-b" + verCode);
        if (AfroStudioVersion.equals("free"))
            versionText.append(" Lite");
        else
            versionText.append(" Full");

        final ScrollView scrollView = (ScrollView) view.findViewById(R.id.about_scroll);
        final Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(7000);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                scrollView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.startAnimation(fadeIn);
            }
        }, 2000/* sec delay */);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Nothing
            }
        });

        return builder.create();
    }
}
