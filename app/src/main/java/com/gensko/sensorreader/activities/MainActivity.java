package com.gensko.sensorreader.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.gensko.sensorreader.R;
import com.gensko.sensorreader.utils.Graph;
import com.gensko.sensorreader.views.Oscilloscope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private static final int ACCELEROMETER_X = 0;
    private static final int ACCELEROMETER_Y = 1;
    private static final int ACCELEROMETER_Z = 2;

    private SensorManager manager;
    private Sensor accelerometer;

    @InjectView(R.id.x)
    TextView xAccelerationView;

    @InjectView(R.id.y)
    TextView yAccelerationView;

    @InjectView(R.id.z)
    TextView zAccelerationView;

    @InjectView(R.id.write)
    Switch writeSwitch;

    @InjectView(R.id.x_progress)
    ProgressBar xProgressView;

    @InjectView(R.id.y_progress)
    ProgressBar yProgressView;

    @InjectView(R.id.z_progress)
    FrameLayout zProgressView;

    @InjectView(R.id.oscilloscope)
    Oscilloscope oscilloscopeView;

    private File log;
    private FileOutputStream logStream;
    private boolean allowWrite;
    private boolean calibrate;

    private float xNormal;
    private float yNormal;
    private float zNormal;

    private Graph xGraph;
    private Graph yGraph;
    private Graph zGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        xGraph = new Graph(Color.RED, 5);
        yGraph = new Graph(Color.GREEN, 5);
        zGraph = new Graph(Color.BLUE, 5);

        oscilloscopeView.addGraph(xGraph);
        oscilloscopeView.addGraph(yGraph);
        oscilloscopeView.addGraph(zGraph);
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(this, accelerometer, 100 * 1000 * 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long timestamp = sensorEvent.timestamp;
        float x = sensorEvent.values[ACCELEROMETER_X];
        float y = sensorEvent.values[ACCELEROMETER_Y];
        float z = sensorEvent.values[ACCELEROMETER_Z];

        if (calibrate) {
            xNormal = x;
            yNormal = y;
            zNormal = z;

            xGraph.setZero(xNormal);
            yGraph.setZero(yNormal);
            zGraph.setZero(zNormal);

            calibrate = false;
        }

        if (allowWrite) {
            byte[] buff = ByteBuffer
                    .allocate(Long.SIZE / 8 + Float.SIZE / 8 * 3)
                    .putLong(timestamp)
                    .putFloat(x)
                    .putFloat(y)
                    .putFloat(z)
                    .array();
            try {
                logStream.write(buff, 0, buff.length);
            } catch (IOException ignored) {}
        }

        xAccelerationView.setText(String.valueOf(x));
        yAccelerationView.setText(String.valueOf(y));
        zAccelerationView.setText(String.valueOf(z));

        showXProgress(x);
        showYProgress(y);
        showZProgress(z);

        xGraph.add(x);
        yGraph.add(y);
        zGraph.add(z);

        oscilloscopeView.invalidate();
    }

    private void showZProgress(float z) {
        ViewGroup.LayoutParams params = zProgressView.getLayoutParams();
        int value = (int) (200 + (zNormal - z) * 30);
        params.height = value;
        params.width = value;
        zProgressView.setLayoutParams(params);
    }

    private void showYProgress(float y) {
        yProgressView.setProgress((int) (5000 - (yNormal - y) * 1000));
    }

    private void showXProgress(float x) {
        xProgressView.setProgress((int) (5000 + (xNormal - x) * 1000));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @OnClick(R.id.write)
    public void onWriteClick(View view) {
        if (writeSwitch.isChecked()) {

            if (log == null) {
                try {
                    log = createTempFile();
                } catch (Exception e) {
                    Toast.makeText(this, "can not create file " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            try {
                logStream = new FileOutputStream(log);
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "can not open file " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            allowWrite = true;
        } else {
            allowWrite = false;

            try {
                logStream.close();
            } catch (IOException ignored) {}

            log = null;
        }
    }

    @OnClick(R.id.calibrate)
    public void onCalibrateClick(View view) {
        calibrate = true;
    }

    private static File createTempFile() throws IOException {
        // Create a tmp file name
        String timeStamp =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
        String imageFileName =
                "Report_" + timeStamp;
        File storageDir =
                new File (
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "SensorReader");

        if (!storageDir.exists() && !storageDir.mkdirs())
            throw new IOException("can't create dir " + storageDir.getPath());

        return File.createTempFile(
                imageFileName,  /* prefix */
                ".srlogv0",       /* suffix */
                storageDir      /* directory */
        );
    }
}
