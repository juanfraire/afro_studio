package com.yaray.afrostudio;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.yaray.afrostudio.databinding.ActivityMainBinding;
import com.yaray.afrostudio.databinding.FragmentMainBinding;

import kotlin.Unit;

public class MainActivity extends AppCompatActivity
        implements
        DialogFragmentAddInstrument.AddInstrumentListener,
        DialogFragmentTempoSettings.TempoSettingsListener,
        DialogFragmentNewEnsemble.NewEnsembleListener,
        DialogFragmentInstrumentOptions.InstrumentOptionsListener,
        DialogFragmentSetRepetition.SetRepetitionListener,
        DialogFragmentSetName.SetNameListener,
        DialogFragmentLoad.LoadListener,
        DialogFragmentEqualizer.EqualizerListener,
        DialogFragmentBarOptions.BarOptionsListener,
        DialogFragmentSpecialStroke.SpecialStrokeListener,
        DialogFragmentEmail.EmailListener,
        DialogFragmentShare.ShareListener {

    private ActivityMainBinding binding;
    private PlaybackViewModel playbackViewModel;
    private ServerViewModel serverViewModel;

    @SuppressWarnings("unused")
    private static final String TAG = "AfroStudio.MainActivity";
    final String PREFS_NAME = "MyPrefsFile";
    Random rnd = new Random(); //random source for ads

    SharedPreferences settings;
    LinearLayout ensembleLayout; // MasterLayout
    HorizontalScrollView horizontalScrollView; // Horizontal Scroll
    ViewAnimator viewAnimator;
    float iconScale = (float) 0.75; // 1 = 37.5 x 52.5 dp
    Ensemble ensemble;

    DialogFragmentLoad dialogFragmentLoad; // to access dialog from connectServerAsynctask
    Encoder encoder;

    private FragmentMainBinding getFragmentBinding() {
        MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        return fragment != null ? fragment.getBinding() : null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        playbackViewModel = new ViewModelProvider(this).get(PlaybackViewModel.class);
        serverViewModel = new ViewModelProvider(this).get(ServerViewModel.class);

        // Set up observers
        serverViewModel.getServerResponse().observe(this, response -> {
            handleServerResponse(response);
        });
        playbackViewModel.isPlaying().observe(this, isPlaying -> {
            FragmentMainBinding fragmentBinding = getFragmentBinding();
            if (fragmentBinding != null) {
                if (isPlaying) {
                    fragmentBinding.butPlay.setImageResource(R.drawable.but_stop);
                } else {
                    fragmentBinding.butPlay.setImageResource(R.drawable.but_play);
                }
            }
        });

        playbackViewModel.getCurrentBeat().observe(this, currentBeat -> {
            // Update UI for the current beat (handle the onProgressUpdate from AsyncTask)
            updateBeatProgress(currentBeat);
        });

        // Init Stuff
        settings = getSharedPreferences(PREFS_NAME, 0);

        FragmentMainBinding fragmentBinding = getFragmentBinding();

        ensembleLayout = fragmentBinding.ensembleLayout;
        horizontalScrollView = (HorizontalScrollView) ensembleLayout.getParent();
        viewAnimator = fragmentBinding.animatorIntro;
        ensemble = new Ensemble(MainActivity.this);

        // Init Rate Image (shown in help and randomly after several start ups
        ImageView rateImage = fragmentBinding.tutRateImage;
        rateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                settings.edit().putBoolean("show_rate", false).apply();
            }
        });

        // First Time Intro
        if (settings.getBoolean("first_time_2", true)) {

            Animation slide_in_left = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            Animation slide_out_right = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
            viewAnimator.setOutAnimation(slide_out_right);
            viewAnimator.setInAnimation(slide_in_left);
            viewAnimator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((ViewAnimator) v).getCurrentView().getId() == R.id.tut_welcome) {
                        Animation slide_out_right = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_out_right);
                        slide_out_right.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                viewAnimator.setVisibility(View.INVISIBLE);
                                if (settings.getString("user", "undefined@undefined").equals("undefined@undefined")) {
                                    DialogFragmentEmail dialogFragmentEmail = new DialogFragmentEmail();
                                    dialogFragmentEmail.show(getSupportFragmentManager(), "SetEmail");
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        v.startAnimation(slide_out_right);
                    } else
                        ((ViewAnimator) v).showNext();
                }
            });

            // Get current gmail address for username
            AccountManager am = AccountManager.get(MainActivity.this);
            Account[] accounts = am.getAccounts();
            String googleAccount = "undefined@undefined"; // if not account present use this one
            for (Account ac : accounts)
                if (ac.name.contains("@")) // use any email present
                    googleAccount = ac.name;
            for (Account ac : accounts)
                if (ac.type.equals("com.google"))  // use google account
                    googleAccount = ac.name;
            settings.edit().putString("user", googleAccount).apply();

            settings.edit().putBoolean("first_time_2", false).apply();

        } else { // Remove Animator from Master Layout
            viewAnimator.setVisibility(View.INVISIBLE);
            // Check account exists (?)
            if ((rnd.nextInt(3) < 1) && (settings.getBoolean("show_rate", true))) // 1 de cada 5 pop up support Afro...
                EnsembleUtils.popSupportAfroStudioMessage(viewAnimator, MainActivity.this);
        }

        // Verify filenames compatibility
        EnsembleUtils.correctFileNames(MainActivity.this, settings.getString("user", "undefined@undefined"));

        // Register User
        serverViewModel.connectToServer(
                settings.getString("user", "undefined@undefined"),
                "register",
                null,
                null,
                null,
                null
        );

        // Low Buttons init
        ImageView but_zoomIn = fragmentBinding.butZoomIn; // Init Buttons
        but_zoomIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                iconScale += 0.05;
                EnsembleUtils.setImageScale(ensembleLayout, horizontalScrollView, MainActivity.this, iconScale, ensemble, viewAnimator);
            }
        });
        ImageView but_zoomOut = fragmentBinding.butZoomOut;
        but_zoomOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                iconScale -= 0.05;
                EnsembleUtils.setImageScale(ensembleLayout, horizontalScrollView, MainActivity.this, iconScale, ensemble, viewAnimator);
            }
        });
        /*
           // this is the old way, before refactoring to ViewModel
        ImageView but_play = fragmentBinding.butPlay;
        but_play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!ensemble.onPlay) { // Play
                    ImageView but_play = fragmentBinding.butPlay;
                    but_play.setImageResource(R.drawable.but_stop);
                    PlayRhythmTask playRhythmTask = new PlayRhythmTask(ensemble, encoder, MainActivity.this, getFragmentBinding());
                    playRhythmTask.params = "";
                    playRhythmTask.execute("");
                } else {  // Stop
                    ensemble.onPlay = false;
                    ImageView but_play = fragmentBinding.butPlay;
                    but_play.setImageResource(R.drawable.but_play);
                }
            }
        });
        but_play.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!ensemble.onPlay) { // Play
                    ImageView but_play = fragmentBinding.butPlay;
                    but_play.setImageResource(R.drawable.but_rec);
                    PlayRhythmTask playRhythmTask = new PlayRhythmTask(ensemble, encoder, MainActivity.this, getFragmentBinding());
                    playRhythmTask.params = "record reproduce";
                    playRhythmTask.execute("");
                }
                return true;
            }
        });
*/
        ImageView but_equ = fragmentBinding.butEqu;
        but_equ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragmentEqualizer dialogFragmentEqualizer = new DialogFragmentEqualizer();
                dialogFragmentEqualizer.activityContext = MainActivity.this;
                dialogFragmentEqualizer.ensemble = ensemble;
                dialogFragmentEqualizer.ensembleLayout = ensembleLayout;
                dialogFragmentEqualizer.show(getSupportFragmentManager(), "Equalizer");
            }
        });

// Update the play button click listener
        ImageView but_play = fragmentBinding.butPlay;
        but_play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!ensemble.onPlay) { // Play
                    playbackViewModel.startPlayback(
                            ensemble,
                            encoder,
                            "",
                            currentBeat -> {
                                updateBeatProgress(currentBeat);
                                return Unit.INSTANCE;
                            },
                            () -> {
                                // Handle playback completion if needed
                                return Unit.INSTANCE;
                            }
                    );
                } else { // Stop
                    playbackViewModel.stopPlayback(ensemble);
                }
            }
        });

// Similar updates for long click listener
        but_play.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!ensemble.onPlay) {
                    playbackViewModel.startPlayback(
                            ensemble,
                            encoder,
                            "record reproduce",
                            currentBeat -> {
                                updateBeatProgress(currentBeat);
                                return Unit.INSTANCE;
                            },
                            () -> {
                                // Handle recording completion if needed
                                return Unit.INSTANCE;
                            }
                    );
                    return true;
                }
                return false;
            }
        });

        ImageView but_tempo = fragmentBinding.butTempo;
        but_tempo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragmentTempoSettings dialogFragmentTempoSettings = new DialogFragmentTempoSettings();
                dialogFragmentTempoSettings.ensemble = ensemble;
                dialogFragmentTempoSettings.show(getSupportFragmentManager(), "TempoSettings");
            }
        });
        TextView tempoText = fragmentBinding.tempoText;
        tempoText.setText(ensemble.bpm + " bpm");

        horizontalScrollView.setTag(R.string.tag0, "smallIconsUnSet");
        horizontalScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((v.getScrollX() > 120) && String.valueOf((v.getTag(R.string.tag0))).equals("smallIconsUnSet")) { // set small icons
                    v.setTag(R.string.tag0, "smallIconsSet");
                    final float displayDensity = getResources().getDisplayMetrics().density;
                    for (int i = 1; i < ensembleLayout.getChildCount() - 1; i++) { // only instruments icons
                        LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
                        ImageView smallIconImageView = new ImageView(MainActivity.this);
                        switch (String.valueOf(instrumentLayout.getTag(R.string.tag0))) {
                            case "ico_djembe":
                            case "ico_djembe_1":
                            case "ico_djembe_2":
                            case "ico_djembe_3":
                                smallIconImageView.setImageResource(R.drawable.ico_djembe);
                                break;
                            case "ico_dun":
                                smallIconImageView.setImageResource(R.drawable.ico_dun);
                                break;
                            case "ico_sag":
                                smallIconImageView.setImageResource(R.drawable.ico_sag);
                                break;
                            case "ico_ken":
                                smallIconImageView.setImageResource(R.drawable.ico_ken);
                                break;
                            case "ico_shek":
                                smallIconImageView.setImageResource(R.drawable.ico_shek);
                                break;
                            case "ico_balet":
                                smallIconImageView.setImageResource(R.drawable.ico_balet);
                                break;
                        }

                        smallIconImageView.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.green_afrostudio));
                        smallIconImageView.setAlpha((float) 0.3);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (iconScale * 37.5 * displayDensity + 0.5f), (int) (iconScale * 52.5 * displayDensity + 0.5f));
                        params.topMargin = instrumentLayout.getTop();

                        RelativeLayout relativeLayout = new RelativeLayout(MainActivity.this);
                        relativeLayout.setTag(R.string.tag0, "ico_small");
                        relativeLayout.addView(smallIconImageView, params);
                        relativeLayout.setPadding(0, (int) (iconScale * 10 * displayDensity + 0.5f), 0, (int) (iconScale * 10 * displayDensity + 0.5f)); // (int left, int top, int right, int bottom) in pixels
                        ((FrameLayout) v.getParent()).addView(relativeLayout);

                        Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setInterpolator(new AccelerateInterpolator());
                        fadeIn.setDuration(1000);
                        smallIconImageView.startAnimation(fadeIn);
                    }

                } else if ((v.getScrollX() <= 120) && String.valueOf((v.getTag(R.string.tag0))).equals("smallIconsSet")) { // remove small icons
                    v.setTag(R.string.tag0, "smallIconsUnSet");
                    final FrameLayout frameLayout = (FrameLayout) v.getParent();
                    Vector<View> allView = new Vector<View>();
                    for (int i = 0; i < frameLayout.getChildCount(); i++)
                        if (String.valueOf(frameLayout.getChildAt(i).getTag(R.string.tag0)).equals("ico_small"))
                            allView.add(frameLayout.getChildAt(i));
                    for (int i = 0; i < allView.size(); i++) {
                        final View smallIconView = allView.get(i);
                        Animation fadeOut = new AlphaAnimation(1, 0);
                        fadeOut.setInterpolator(new AccelerateInterpolator());
                        fadeOut.setDuration(10);
                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                            public void onAnimationEnd(Animation animation) {
                                frameLayout.removeView(smallIconView);
                            }

                            public void onAnimationRepeat(Animation animation) {
                            }

                            public void onAnimationStart(Animation animation) {
                            }
                        });
                        smallIconView.startAnimation(fadeOut);
                    }

                }
                return false;
            }
        });

        // Always generate this Demo Rithms and save them so they can be loaded
        ensemble.setVectorsFromRaw(R.raw.afr_sinte_demo, MainActivity.this);
        EnsembleUtils.saveEnsemble(ensemble, MainActivity.this, false, false, viewAnimator, settings.getString("user", "undefined@undefined"));
        ensemble.setVectorsFromRaw(R.raw.afr_dundunbe_demo, MainActivity.this);
        EnsembleUtils.saveEnsemble(ensemble, MainActivity.this, false, false, viewAnimator, settings.getString("user", "undefined@undefined"));
        ensemble.setVectorsFromRaw(R.raw.afr_kuku_demo, MainActivity.this);
        EnsembleUtils.saveEnsemble(ensemble, MainActivity.this, false, false, viewAnimator, settings.getString("user", "undefined@undefined"));
        ensemble.setVectorsFromRaw(R.raw.afr_soko_demo, MainActivity.this);
        EnsembleUtils.saveEnsemble(ensemble, MainActivity.this, false, false, viewAnimator, settings.getString("user", "undefined@undefined"));
    }

    private void handleServerResponse(ServerViewModel.ServerResponse response) {
        String action = response.getAction();
        String responseData = response.getResponseData();

        if (responseData != null && responseData.contains("PostTooLong")) {
            Toast.makeText(MainActivity.this, "Too long to share!", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (action) {
            case "register":
                // Nothing to do for registration
                break;
            case "setEnsemble":
                if (response.isSuccess()) {
                    Toast.makeText(MainActivity.this, R.string.toast_share_ok, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.toast_share_error, Toast.LENGTH_SHORT).show();
                }
                break;
            case "getEnsemble":
                if (response.isSuccess()) {
                    ensemble.setVectorsFromJSON(responseData);
                    EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                } else {
                    Toast.makeText(MainActivity.this, R.string.toast_getensemble_error, Toast.LENGTH_SHORT).show();
                }
                break;
            case "getEnsembleList":
            case "deleteEnsemble":
                if (response.isSuccess()) {
                    dialogFragmentLoad.updateRemoteFileListString(response.getEnsembleList());
                } else {
                    Toast.makeText(MainActivity.this, R.string.toast_getensemble_error, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    // https://www.mobomo.com/2011/06/android-understanding-activity-launchmode/

    @Override
    protected void onResume() { // Called when the activity will start interacting with the user.
        serverViewModel.connectToServer(
                settings.getString("user", "undefined@undefined"),
                "register",
                null,
                null,
                null,
                null
        );

        File file = new File(getCacheDir(), this.hashCode() + ".tmp");
        if (file.exists()) { // there is a tem file for this hash id?, load it!
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) { // Media Ready to at least read
                try {
                    InputStream inputStream = new FileInputStream(file);
                    java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                    String jsonString = s.hasNext() ? s.next() : "";
                    ensemble.setVectorsFromJSON(jsonString);
                    EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                } catch (IOException e) { // Unable to create file, likely because external storage is not currently mounted.
                    Log.e(TAG, "Error loading from external mem: " + file, e);
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.toast_storage_not_ready, Toast.LENGTH_SHORT).show();
            }
        } else { // there is not a temp file, check if view intent or just put a new rythm on screen.
            Intent intent = getIntent();
            String action = intent.getAction();
            if (action.compareTo(Intent.ACTION_VIEW) == 0) {
                String scheme = intent.getScheme();
                Uri uri = intent.getData();
                ContentResolver resolver = getContentResolver();
                if ((scheme.compareTo(ContentResolver.SCHEME_FILE) == 0) || (scheme.compareTo(ContentResolver.SCHEME_CONTENT) == 0)) {

                    String state = Environment.getExternalStorageState();
                    if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) { // Media Ready to at least read
                        try {
                            InputStream inputStream = resolver.openInputStream(uri);
                            //InputStream inputStream = new FileInputStream(file);
                            java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                            String jsonString = s.hasNext() ? s.next() : "";
                            ensemble.setVectorsFromJSON(jsonString);
                            EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                        } catch (IOException e) {
                            Log.e(TAG, "Error generating inputStream for loading " + uri, e);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, R.string.toast_storage_not_ready, Toast.LENGTH_SHORT).show();
                    }
                }
            } else { // Set empty ensemble to begin
                ensemble.setVectorsFromEmpty(16, 1, true, MainActivity.this); //Empty Ensemble
                EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                // Set initial GUI
                ((TextView) ((LinearLayout) ensembleLayout.getChildAt(0)).getChildAt(0)).setText(R.string.init_string); // set custom ensebleName
            }
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        // commit unsaved changes to persistent data, stop animations and other things that may be consuming CPU
        // onResume takes the control once back


        // UPD: No need to manually handle AsyncTask cancellation anymore
        // The ViewModel will handle coroutine cancellation appropriately

        if (ensemble.onPlay) {
            playbackViewModel.stopPlayback(ensemble);
        }

        // Store work in temp file with this app hash Id.
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) { //Media Ready to read and write
            String fileName = this.hashCode() + ".tmp";
            File file = new File(getCacheDir(), fileName);
            try {
                OutputStream outputStream = new FileOutputStream(file);
                outputStream.write(ensemble.saveVectorsInJSON().getBytes());
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error writing on cache: " + file, e);
            }
        }
        super.onPause();
    }

    // MENU FUNCTIONS
    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentMainBinding fragmentBinding = getFragmentBinding();

        int id = item.getItemId();

        if (id == R.id.action_new) {
            DialogFragmentNewEnsemble dialogFragmentNewEnsemble = new DialogFragmentNewEnsemble();
            dialogFragmentNewEnsemble.show(getSupportFragmentManager(), "NewEnsemble");
            return true;
        }

        if (id == R.id.action_load) {
            // Stop Playback if any
            if (ensemble.onPlay) {
                ImageView but_play = fragmentBinding.butPlay;
                but_play.callOnClick();
            }

            // Get Server File list
            serverViewModel.connectToServer(
                    settings.getString("user", "undefined@undefined"),
                    "getEnsembleList",
                    ensemble.ensembleName,
                    ensemble.ensembleAuthor,
                    ensemble.saveVectorsInJSON(),
                    null
            );

            dialogFragmentLoad = new DialogFragmentLoad();
            dialogFragmentLoad.localUser = settings.getString("user", "undefined@undefined");
            dialogFragmentLoad.dir = this.getExternalFilesDir(null);
            dialogFragmentLoad.activityContext = MainActivity.this;
            dialogFragmentLoad.show(((FragmentActivity) MainActivity.this).getSupportFragmentManager(), "Load");
            return true;
        }

        if (id == R.id.action_save) {
            EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble); //creo que puedo hacer onPlay
            EnsembleUtils.saveEnsemble(ensemble, MainActivity.this, true, true, viewAnimator, settings.getString("user", "undefined@undefined"));
            return true;
        }

        if (id == R.id.action_upload) {
            if (EnsembleUtils.AfroStudioVersion.equals("free")) { // no sharing with free app
                EnsembleUtils.popGetFullAfroStudioMessage(viewAnimator, MainActivity.this);
                return true;
            }

            if (ensemble.ensembleName.equals("Unnamed") || ensemble.ensembleName.equals("Sin Nombre") || ensemble.ensembleAuthor.equals("Autor") || ensemble.ensembleAuthor.equals("Author")) {
                Toast.makeText(MainActivity.this, "Set name and author before sharing", Toast.LENGTH_SHORT).show();
                return true;
            }

            // Stop Playback if any
            if (ensemble.onPlay) {
                ImageView but_play = fragmentBinding.butPlay;
                but_play.callOnClick();
            }

            serverViewModel.connectToServer(
                    settings.getString("user", "undefined@undefined"),
                    "setEnsemble",
                    ensemble.ensembleName,
                    ensemble.ensembleAuthor,
                    ensemble.saveVectorsInJSON(),
                    null
            );
            return true;
        }

        if (id == R.id.action_share) {
            DialogFragmentShare dialogFragmentShare = new DialogFragmentShare();
            dialogFragmentShare.show(getSupportFragmentManager(), "ShareDialog");
        }

        if (id == R.id.action_about) {
            DialogFragmentAbout dialogFragmentAbout = new DialogFragmentAbout();
            dialogFragmentAbout.activityContext = MainActivity.this;
            dialogFragmentAbout.AfroStudioVersion = EnsembleUtils.AfroStudioVersion;
            dialogFragmentAbout.show(getSupportFragmentManager(), "AboutDialog");
            return true;
        }

        if (id == R.id.action_help) {
            Animation slide_in_left = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            Animation slide_out_right = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
            viewAnimator.setOutAnimation(null);
            viewAnimator.setInAnimation(null);
            viewAnimator.setDisplayedChild(1);
            viewAnimator.setOutAnimation(slide_out_right);
            viewAnimator.setInAnimation(slide_in_left);
            viewAnimator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((ViewAnimator) v).getCurrentView().getId() == R.id.tut_rate) {
                        v.setOnClickListener(null); // avoid bouncing if several clicks
                        Animation slide_out_right = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_out_right);
                        slide_out_right.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                viewAnimator.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        v.startAnimation(slide_out_right);
                    } else
                        ((ViewAnimator) v).showNext();
                }
            });
            viewAnimator.startAnimation(slide_in_left);
            viewAnimator.setVisibility(View.VISIBLE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // INTERFACES
    public void addInstrumentPositiveClick(Vector<String> instrumentChoice) {

        //horizontalScrollView just remove the smallIcons, they are to be added as soon as new horizontal scrollbar is pushed
        horizontalScrollView.setTag(R.string.tag0, "smallIconsUnSet");
        final FrameLayout frameLayout = (FrameLayout) horizontalScrollView.getParent();
        Vector<View> allView = new Vector<View>();
        for (int i = 0; i < frameLayout.getChildCount(); i++)
            if (String.valueOf(frameLayout.getChildAt(i).getTag(R.string.tag0)).equals("ico_small"))
                allView.add(frameLayout.getChildAt(i));
        for (int i = 0; i < allView.size(); i++)
            frameLayout.removeView(allView.get(i));
        horizontalScrollView.callOnClick();

        for (int i = 0; i < instrumentChoice.size(); i++)
            EnsembleUtils.addInstrumentInGui(instrumentChoice.get(i), MainActivity.this, ensembleLayout, iconScale, ensemble, 100, 1, viewAnimator);
        if (ensemble.onPlay)
            ensemble.flagEnsembleUpdate = true;
        else {
            EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble); // To fix repetition ico_div issue
            EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
        }
    }

    public void tempoSettingsPositiveClick() {
        FragmentMainBinding fragmentBinding = getFragmentBinding();

        TextView tempoText = fragmentBinding.tempoText;
        tempoText.setText(ensemble.bpm + " bpm");
    }

    public void newEnsemblePositiveClick(int beatsPerBar, int numBar, boolean emptyInstruments) {
        FragmentMainBinding fragmentBinding = getFragmentBinding();

        if ((EnsembleUtils.AfroStudioVersion.equals("free")) && (numBar > 1)) {
            numBar = 1;
        }

        //horizontalScrollView just remove the smallIcons, they are to be added as soon as new horizontal scrollbar is pushed
        horizontalScrollView.setTag(R.string.tag0, "smallIconsUnSet");
        final FrameLayout frameLayout = (FrameLayout) horizontalScrollView.getParent();
        Vector<View> allView = new Vector<View>();
        for (int i = 0; i < frameLayout.getChildCount(); i++)
            if (String.valueOf(frameLayout.getChildAt(i).getTag(R.string.tag0)).equals("ico_small"))
                allView.add(frameLayout.getChildAt(i));
        for (int i = 0; i < allView.size(); i++)
            frameLayout.removeView(allView.get(i));
        horizontalScrollView.callOnClick();

        if (ensemble.onPlay) {
            ensemble.onPlay = false;
            try {
                Thread.sleep(200); //wait async task to find a pause value
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ImageView but_play = fragmentBinding.butPlay;
            but_play.setImageResource(R.drawable.but_play);
        }

        ensemble.setVectorsFromEmpty(beatsPerBar, numBar, emptyInstruments, MainActivity.this);
        EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
    }

    public void instrumentOptionsPositiveClick(String option, int volume, ImageView instImage) {

        switch (option) { // duplicate, clear, remove when different than "" (onPlay)
            case "clear":
                LinearLayout instrumentLayout = (LinearLayout) instImage.getParent();
                for (int i = 0; i < instrumentLayout.getChildCount(); i++) {
                    if (String.valueOf(instrumentLayout.getChildAt(i).getTag(R.string.tag0)).contains("ico_ini")) {
                        ((ImageView) instrumentLayout.getChildAt(i)).setImageResource(R.drawable.ico_ini);
                        instrumentLayout.getChildAt(i).setTag(R.string.tag0, "ico_ini");
                    }
                    if (String.valueOf(instrumentLayout.getChildAt(i).getTag(R.string.tag0)).contains("ico_mid")) {
                        ((ImageView) instrumentLayout.getChildAt(i)).setImageResource(R.drawable.ico_mid);
                        instrumentLayout.getChildAt(i).setTag(R.string.tag0, "ico_mid");
                    }
                    if (String.valueOf(instrumentLayout.getChildAt(i).getTag(R.string.tag0)).contains("ico_end")) {
                        ((ImageView) instrumentLayout.getChildAt(i)).setImageResource(R.drawable.ico_end);
                        instrumentLayout.getChildAt(i).setTag(R.string.tag0, "ico_end");
                    }
                }
                if (ensemble.onPlay)
                    ensemble.flagEnsembleUpdate = true;
                else {
                    EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble);
                    EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                }
                break;
            case "remove":
                ((LinearLayout) instImage.getParent().getParent()).removeView((View) instImage.getParent());

                //horizontalScrollView just remove the smallIcons, they are to be added as soon as new horizontal scrollbar is pushed
                horizontalScrollView.setTag(R.string.tag0, "smallIconsUnSet");
                final FrameLayout frameLayout = (FrameLayout) horizontalScrollView.getParent();
                Vector<View> allView = new Vector<View>();
                for (int i = 0; i < frameLayout.getChildCount(); i++)
                    if (String.valueOf(frameLayout.getChildAt(i).getTag(R.string.tag0)).equals("ico_small"))
                        allView.add(frameLayout.getChildAt(i));
                for (int i = 0; i < allView.size(); i++)
                    frameLayout.removeView(allView.get(i));
                horizontalScrollView.callOnClick();

                if (ensemble.onPlay)
                    ensemble.flagEnsembleUpdate = true;
                else {
                    EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble);
                    EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                }

                break;
        }
    }

    public void setRepetitionPositiveClick(int thisBar, int barCount, int countTotal) {

        ensemble.repetitions.get(thisBar)[0] = barCount; //barCount
        ensemble.repetitions.get(thisBar)[1] = countTotal; //total
        ensemble.repetitions.get(thisBar)[2] = countTotal; //current

        if (ensemble.onPlay)
            ensemble.flagEnsembleUpdate = true;
        else {
            EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble);
            EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
        }
    }

    public void setNamePositiveClick(String name, String author) {
        ensemble.ensembleName = name;
        ensemble.ensembleAuthor = author;
        ((TextView) ((LinearLayout) ensembleLayout.getChildAt(0)).getChildAt(0)).setText(name + " - by " + author);
    }

    public void loadPositiveClick(String fileName) {
        FragmentMainBinding fragmentBinding = getFragmentBinding();

        //horizontalScrollView just remove the smallIcons, they are to be added as soon as new horizontal scrollbar is pushed
        horizontalScrollView.setTag(R.string.tag0, "smallIconsUnSet");
        final FrameLayout frameLayout = (FrameLayout) horizontalScrollView.getParent();
        Vector<View> allView = new Vector<View>();
        for (int i = 0; i < frameLayout.getChildCount(); i++)
            if (String.valueOf(frameLayout.getChildAt(i).getTag(R.string.tag0)).equals("ico_small"))
                allView.add(frameLayout.getChildAt(i));
        for (int i = 0; i < allView.size(); i++)
            frameLayout.removeView(allView.get(i));
        horizontalScrollView.callOnClick();

        if (fileName != null) {
            if (ensemble.onPlay) {
                ensemble.onPlay = false;
                try {
                    Thread.sleep(200); //wait async task to find a pause value
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ImageView but_play = fragmentBinding.butPlay;
                but_play.setImageResource(R.drawable.but_play);
            }

            if (fileName.contains(".afr")) { // File is local
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) { // Media Ready to at least read
                    File file = new File(getExternalFilesDir(null), fileName);
                    try {
                        InputStream inputStream = new FileInputStream(file);
                        java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                        String jsonString = s.hasNext() ? s.next() : "";
                        ensemble.setVectorsFromJSON(jsonString);
                        EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                    } catch (IOException e) { // Unable to create file, likely because external storage is not currently mounted.
                        Log.e("ExternalStorage", "Error reading " + file, e);
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.toast_storage_not_ready, Toast.LENGTH_SHORT).show();
                }
            } else { // File in server
                String ensembleName = fileName.substring(0, fileName.indexOf("_by_"));
                String ensembleAuthor = fileName.substring(fileName.indexOf("_by_") + 4, fileName.indexOf("_u_"));
                String ensembleUser = fileName.substring(fileName.indexOf("_u_") + 3, fileName.length());

                serverViewModel.connectToServer(
                        settings.getString("user", "undefined@undefined"),
                        "getEnsemble",
                        ensembleName,
                        ensembleAuthor,
                        null,
                        ensembleUser

                );
                // wait for ensemble to load?
            }
        } else {
            Toast.makeText(MainActivity.this, R.string.toast_no_file_chosen, Toast.LENGTH_SHORT).show();
        }
    }

    public void loadDeleteEnsembleInServer(String fileName) {
        String name = fileName.substring(0, fileName.indexOf("_by_"));
        String author = fileName.substring(fileName.indexOf("_by_") + 4, fileName.indexOf("_u_"));

        // Delete (returns and updates the list in dialog once completed
        serverViewModel.connectToServer(
                settings.getString("user", "undefined@undefined"),
                "deleteEnsemble",
                ensemble.ensembleName,
                ensemble.ensembleAuthor,
                null,
                null
        );
    }

    public void equalizerPositiveClick() {
        EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
    }

    public void barOptionsPositiveClick(int barPos, String option, String name) {

        ensemble.barName.setElementAt(name, barPos);// set Name
        EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);

        switch (option) { //browse option (options != ""
            case "clear":
                for (int i = 0; i < ensemble.djembeVector.size(); i++)
                    for (int j = ensemble.getBeatsPerBar() * barPos; j < ensemble.getBeatsPerBar() * (barPos + 1); j++)
                        ensemble.djembeVector.get(i).setElementAt(0, j);
                for (int i = 0; i < ensemble.shekVector.size(); i++)
                    for (int j = ensemble.getBeatsPerBar() * barPos; j < ensemble.getBeatsPerBar() * (barPos + 1); j++)
                        ensemble.shekVector.get(i).setElementAt(0, j);
                for (int i = 0; i < ensemble.dunVector.size(); i++)
                    for (int j = ensemble.getBeatsPerBar() * barPos; j < ensemble.getBeatsPerBar() * (barPos + 1); j++)
                        ensemble.dunVector.get(i).setElementAt(0, j);
                for (int i = 0; i < ensemble.sagVector.size(); i++)
                    for (int j = ensemble.getBeatsPerBar() * barPos; j < ensemble.getBeatsPerBar() * (barPos + 1); j++)
                        ensemble.sagVector.get(i).setElementAt(0, j);
                for (int i = 0; i < ensemble.kenVector.size(); i++)
                    for (int j = ensemble.getBeatsPerBar() * barPos; j < ensemble.getBeatsPerBar() * (barPos + 1); j++)
                        ensemble.kenVector.get(i).setElementAt(0, j);
                for (int i = 0; i < ensemble.baletVector.size(); i++)
                    for (int j = ensemble.getBeatsPerBar() * barPos; j < ensemble.getBeatsPerBar() * (barPos + 1); j++)
                        ensemble.baletVector.get(i).setElementAt(0, j);
                EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                break;
            case "remove": // do in gui so it can be done in real time
                if (EnsembleUtils.AfroStudioVersion.equals("free")) {
                    EnsembleUtils.popGetFullAfroStudioMessage(viewAnimator, MainActivity.this);
                    return;
                }
                for (int i = 0; i < ensembleLayout.getChildCount(); i++) {
                    LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
                    Vector<View> viewsToDelete = new Vector<>();
                    if ((String.valueOf(instrumentLayout.getTag(R.string.tag0)).contains("ico_djembe")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_shek")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_dun")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_sag")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_ken")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_balet")))
                        for (int j = (ensemble.getBeatsPerBar() + 1) * barPos + 1; j < (ensemble.getBeatsPerBar() + 1) * (barPos + 1) + 1; j++)
                            viewsToDelete.add(instrumentLayout.getChildAt(j));
                    if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("last_inst")) {
                        viewsToDelete.add(instrumentLayout.getChildAt(barPos + 1)); //first element is + icon
                    }
                    for (int j = 0; j < viewsToDelete.size(); j++)
                        instrumentLayout.removeView(viewsToDelete.get(j));
                }
                //Also remove the repetitions in ensemble which is not updated from GUI
                ensemble.repetitions.remove(barPos);
                if (ensemble.onPlay)
                    ensemble.flagEnsembleUpdate = true;
                else {
                    EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble);
                    EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                }
                break;
            case "clone_bar": // do in gui so it can be done in real time
                if (EnsembleUtils.AfroStudioVersion.equals("free")) {
                    EnsembleUtils.popGetFullAfroStudioMessage(viewAnimator, MainActivity.this);
                    return;
                }
                for (int i = 0; i < ensembleLayout.getChildCount(); i++) {
                    LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
                    if ((String.valueOf(instrumentLayout.getTag(R.string.tag0)).contains("ico_djembe")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_shek")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_dun")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_sag")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_ken")) || (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("ico_balet")))
                        for (int j = (ensemble.getBeatsPerBar() + 1) * barPos + 2; j < (ensemble.getBeatsPerBar() + 1) * (barPos + 1) + 1; j++) {
                            //Log.e(TAG, "barPos=" + barPos + ", j = " + ((ensemble.getBeatsPerBar() + 1) * barPos + 2) + " - " + ((ensemble.getBeatsPerBar() + 1) * (barPos + 1) + 1));
                            ImageView clonedImage = new ImageView(this);
                            clonedImage.setImageDrawable(((ImageView) instrumentLayout.getChildAt(j)).getDrawable());
                            clonedImage.setScaleType(((ImageView) instrumentLayout.getChildAt(j)).getScaleType());
                            clonedImage.setTag(R.string.tag0, (instrumentLayout.getChildAt(j)).getTag(R.string.tag0)); //they wont have a second tag
                            try {
                                clonedImage.setLayoutParams((instrumentLayout.getChildAt(j)).getLayoutParams());
                            } catch (Exception e) {
                            }
                            instrumentLayout.addView(clonedImage, j + (ensemble.getBeatsPerBar() + 1));
                        }
                    // Bar Names
                    if (String.valueOf(instrumentLayout.getTag(R.string.tag0)).equals("last_inst")) {
                        TextView clonedText = new TextView(this);
                        clonedText.setTag(R.string.tag0, (instrumentLayout.getChildAt(barPos + 1)).getTag(R.string.tag0)); //they wont have a second tag
                        try {
                            clonedText.setLayoutParams((instrumentLayout.getChildAt(barPos + 1)).getLayoutParams());
                        } catch (Exception e) {
                        }
                        instrumentLayout.addView(clonedText, barPos + 1);
                    }
                }

                if (ensemble.onPlay)
                    ensemble.flagEnsembleUpdate = true;
                else {
                    EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble);
                    EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, MainActivity.this, iconScale, viewAnimator);
                }
                break;
        }
    }

    public void specialStrokePositiveClick() {
        EnsembleUtils.updateEnsembleFromGui(ensembleLayout, ensemble);
    }

    public void emailPositiveClick(String email) {
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            settings.edit().putString("user", email).apply();
        else
            Toast.makeText(MainActivity.this, "Email not valid, using 'undefined'.", Toast.LENGTH_SHORT).show();
    }

    // Update sharePositiveClick method to use ViewModel
    public void sharePositiveClick(String option) {
        FragmentMainBinding fragmentBinding = getFragmentBinding();

        if (option.equals("audio")) {
            if (ensemble.onPlay) {
                playbackViewModel.stopPlayback(ensemble);
            }

            fragmentBinding.butPlay.setImageResource(R.drawable.but_rec);
            playbackViewModel.startPlayback(
                    ensemble,
                    encoder,
                    "record share",
                    currentBeat -> {
                        updateBeatProgress(currentBeat);
                        return Unit.INSTANCE;
                    },
                    () -> {
                        // Handle share audio completion
                        String state = Environment.getExternalStorageState();
                        if (Environment.MEDIA_MOUNTED.equals(state)) {
                            shareRecordedFile();
                            return Unit.INSTANCE;
                        } else {
                            Toast.makeText(MainActivity.this, "Cannot access storage", Toast.LENGTH_SHORT).show();
                            return Unit.INSTANCE;
                        }
                    }
            );
        } else if (option.equals("ensemble")) {
            if (EnsembleUtils.AfroStudioVersion.equals("free")) { // Do not save in free version
                EnsembleUtils.popGetFullAfroStudioMessage(viewAnimator, this);
            } else {
                // Save ensemble
                EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble); //creo que puedo hacer onPlay
                EnsembleUtils.saveEnsemble(ensemble, MainActivity.this, true, true, viewAnimator, settings.getString("user", "undefined@undefined"));

                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    //sharingIntent.setType("application/octet-stream");
                    sharingIntent.setType("application/json");
                    try {
                        String path = MainActivity.this.getExternalFilesDir(null).getPath();
                        String user = settings.getString("user", "undefined@undefined").substring(0, settings.getString("user", "undefined@undefined").indexOf("@"));
                        String fileName = ensemble.ensembleName + "_by_" + ensemble.ensembleAuthor + "_u_" + user + ".afr";
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path + "/" + fileName));
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    } catch (NullPointerException e) {
                        Toast.makeText(MainActivity.this, "Cannot access storage", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Cannot access storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    // Method to handle beat progress updates (replaces AsyncTask.onProgressUpdate)
    private void updateBeatProgress(int currentBeat) {
        for (int i = 1; i < ensembleLayout.getChildCount() - 1; i++) {
            LinearLayout instrumentLayout = (LinearLayout) ensembleLayout.getChildAt(i);
            int currentBar = 0;
            for (int j = 0; j < instrumentLayout.getChildCount(); j++) {
                if (String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).contains("ico_ini") ||
                        String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).contains("ico_mid") ||
                        String.valueOf(instrumentLayout.getChildAt(j).getTag(R.string.tag0)).contains("ico_end")) {

                    if (((currentBar + 1) % ensemble.getBeatsPerBar()) == 0) { //Just clear this bar for the repetition issue
                        ((ImageView) (instrumentLayout.getChildAt(j))).clearColorFilter();
                    }
                    if ((currentBar == currentBeat - 1) || (currentBar == ensemble.getBeats() - 1)) {//Remove Color!
                        ((ImageView) (instrumentLayout.getChildAt(j))).clearColorFilter();
                    }
                    if (currentBar == currentBeat) { //Color this Bar!
                        ((ImageView) (instrumentLayout.getChildAt(j))).setColorFilter(ContextCompat.getColor(this, R.color.playback_afrostudio));
                    }
                    currentBar++;
                }
            }
        }

        if (ensemble.flagEnsembleUpdate) { //update Ensemble
            EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble);
            EnsembleUtils.setGuiFromEnsemble(ensembleLayout, ensemble, this, iconScale, getFragmentBinding().animatorIntro);
            ensemble.flagEnsembleUpdate = false;
        }
    }

    private void shareRecordedFile() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("audio/*");
            try {
                String path = getExternalFilesDir(null).getPath();
                String user = settings.getString("user", "undefined@undefined").substring(0,
                        settings.getString("user", "undefined@undefined").indexOf("@"));
                String fileName = ensemble.ensembleName + "_by_" + ensemble.ensembleAuthor + "_u_" + user + ".aac";
                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path + "/" + fileName));
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            } catch (NullPointerException e) {
                Toast.makeText(MainActivity.this, "Cannot access storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
// TODO: Ensure clean coroutine cancellation
//  The viewModelScope takes care of cancellation when the ViewModel is cleared
//Check isActive flag regularly in long-running coroutines
//Make sure to stop any ongoing operations in onPause/onDestroy