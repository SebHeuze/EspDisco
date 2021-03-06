package espdisco.hodor.ninja.espdisco.fragment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import espdisco.hodor.ninja.espdisco.EspApplication;
import espdisco.hodor.ninja.espdisco.LedView;
import espdisco.hodor.ninja.espdisco.MainActivity;
import espdisco.hodor.ninja.espdisco.R;
import espdisco.hodor.ninja.espdisco.enums.LEDMode;
import espdisco.hodor.ninja.espdisco.model.Esp8266;
import espdisco.hodor.ninja.espdisco.utils.EspManager;

public class ESPDiscoFragment extends Fragment{

    private static final String LOG_TAG = "ESPDiscoFragment";

    private static String mFileName = null;

    private int monoColor = -16776961; //Bleu par défaut

    private boolean recording;
    private Handler handler;

    private LEDMode currentMode;
    private MediaRecorder mRecorder = null;

    /**
     * GUI
     */
    private SeekBar seekBar;
    private TextView seekBarValue;
    private ProgressBar progressBar;
    private LedView ledView;
    private Button discoButton;
    private Switch modeButton;

    public ESPDiscoFragment() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_esp_disco, container, false);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState){
        seekBar = (SeekBar) getView().findViewById(R.id.seekBar);
        seekBarValue = (TextView) getView().findViewById(R.id.seekbarValue);
        progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
        ledView = (LedView) getView().findViewById(R.id.ledView);
        discoButton = (Button) getView().findViewById(R.id.discoButton);
        modeButton = (Switch) getView().findViewById(R.id.modeButton);

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

        modeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    currentMode = LEDMode.MULTI_COLOR;
                } else {
                    currentMode = LEDMode.SINGLE_COLOR;
                }
            }
        });

        ledView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ColorPickerDialogBuilder
                        .with(getActivity())
                        .setTitle("Choose color")
                        .initialColor(Color.RED)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                monoColor = selectedColor;
                                ledView.changeColor(monoColor);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
            }
        });

        discoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (recording) {
                    stopRecording();
                    discoButton.setText("Start Disco Mode");
                } else {
                    startRecording();
                    discoButton.setText("Stop Disco Mode");
                }
            }
        });


        currentMode = LEDMode.SINGLE_COLOR;
    }


    /**
     * On Record
     * @param start
     */
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


        handler = new Handler();
        List<Esp8266> espList = ((MainActivity)getActivity()).getBdd().getCheckedEsp();
        EspManager.getInstance().init(espList);

        final Runnable r = new Runnable() {
            public void run() {
                if(recording) {
                    handler.postDelayed(this, 100);
                    recordingListener();
                }
            }
        };

        handler.postDelayed(r, 50);

    }

    private void recordingListener(){
        int amplitude = mRecorder.getMaxAmplitude();
        progressBar.setProgress(amplitude);
        switch (currentMode) {
            case MULTI_COLOR:
                if (amplitude > seekBar.getProgress()) {
                    Random rnd = new Random();
                    int red = rnd.nextInt(256);
                    int green = rnd.nextInt(256);
                    int blue = rnd.nextInt(256);

                    Log.d(LOG_TAG, "Amplitude " + amplitude);
                    Log.d(LOG_TAG, "seekBar.getProgress" + seekBar.getProgress());
                    int color = Color.argb(255, (int) red, (int) green, (int) blue);
                    EspManager.getInstance().changeColor((int) red, (int) green, (int) blue);
                    ledView.changeColor(color);
                }
                break;
            case SINGLE_COLOR:
                //Log.d(LOG_TAG, "Amplitude " + amplitude);
                //Log.d(LOG_TAG, "seekBar.getProgress" + seekBar.getProgress());
                double blue = 0;
                double red = 0;
                double green = 0;
                if (amplitude > seekBar.getProgress()) {
                    blue = (monoColor & 255) * (((double) amplitude - (double) seekBar.getProgress()) / (double) seekBar.getProgress());
                    green = ((monoColor >> 8) & 255) * (((double) amplitude - (double) seekBar.getProgress()) / (double) seekBar.getProgress());
                    red = ((monoColor >> 16) & 255) * (((double) amplitude - (double) seekBar.getProgress()) / (double) seekBar.getProgress());
                }
                red = (red > 255)?255:red;
                green = (green > 255)?255:green;
                blue = (blue > 255)?255:blue;

                int color = Color.argb(255, (int) red, (int) green, (int) blue);
                EspManager.getInstance().changeAllColor((int) red, (int) green, (int) blue);
                ledView.changeColor(color);
                break;
        }
    }


    private void stopRecording() {
        EspManager.getInstance().close();
        recording = false;
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

}