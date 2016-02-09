package espdisco.hodor.ninja.espdisco;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;


import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.IOException;
import java.util.Random;

import espdisco.hodor.ninja.espdisco.utils.EspManager;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";
    private static String mFileName = null;

    private boolean recording;

    private MediaRecorder mRecorder = null;

    private EspManager espManager;

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        recording = true;
        mRecorder.start();
        ColorPickerDialogBuilder
                .with(MainActivity.this)
                .setTitle("Choose color")
                .initialColor(Color.RED)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        //toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        //changeBackgroundColor(selectedColor);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();

        new Thread(new Runnable() {
            public void run() {
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                LedView ledView = (LedView) findViewById(R.id.ledView);
                final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
                int amplitude = 0;
                espManager = new EspManager();
                while (recording) {
                    try {

                        /*amplitude = mRecorder.getMaxAmplitude();
                        progressBar.setProgress(amplitude);*/
                        //if (amplitude>seekBar.getProgress()) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int amplitude = mRecorder.getMaxAmplitude();
                                    /*Random rnd = new Random();
                                    int red = rnd.nextInt(256);
                                    int green = rnd.nextInt(256);
                                    int blue = rnd.nextInt(256);*/
                                    Log.d(LOG_TAG,"Amplitude " + amplitude);
                                    Log.d(LOG_TAG,"seekBar.getProgress" + seekBar.getProgress());
                                    double blue = 0;
                                    if (amplitude>seekBar.getProgress()) {
                                        blue = 255.0 * (((double) amplitude - (double)seekBar.getProgress()) / (double) seekBar.getProgress());
                                    }
                                    Log.d(LOG_TAG,"blue" + blue);
                                    int red = 0;
                                    int green = 0;

                                    if(blue>255){
                                        blue = 255;
                                    }
                                    int color = Color.argb(255, (int) red, (int) green, (int) blue);
                                    espManager.changeColor((int) red,(int) green, (int)blue);
                                    LedView ledView = (LedView) findViewById(R.id.ledView);
                                    ledView.changeColor(color);
                                }
                            });
                        //}

                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, "Erreur InterruptedException");
                    }
                }
                espManager.close();
            }
        }).start();


    }

    private void stopRecording() {
        recording = false;
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }


    public MainActivity() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        final TextView seekBarValue = (TextView) findViewById(R.id.seekbarValue);
        seekBarValue.setText(String.valueOf(seekBar.getProgress()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                seekBarValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final Button discoButton = (Button) findViewById(R.id.discoButton);

        discoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(recording) {
                    stopRecording();
                    discoButton.setText("Start Disco Mode");
                } else {
                    startRecording();
                    discoButton.setText("Stop Disco Mode");
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

    }
}
