package com.jay.ai;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Real-time sensor hub. It activates every requested sensor that the phone
 * actually exposes and keeps the latest readings in memory.
 * Hardware availability is device-dependent; Jay never invents a sensor value.
 */
public final class JaySensorManager implements SensorEventListener {
    private final SensorManager manager;
    private final Map<Integer, float[]> latest = new HashMap<>();
    private final Map<Integer, String> names = new HashMap<>();
    private final List<Integer> requestedTypes = new ArrayList<>();
    private boolean running;

    public JaySensorManager(Context context) {
        manager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        add(Sensor.TYPE_ACCELEROMETER, "Accelerometer");
        add(Sensor.TYPE_GYROSCOPE, "Gyroscope");
        add(Sensor.TYPE_MAGNETIC_FIELD, "Magnetic field");
        add(Sensor.TYPE_LIGHT, "Light");
        add(Sensor.TYPE_PRESSURE, "Barometer");
        add(Sensor.TYPE_PROXIMITY, "Proximity");
        add(Sensor.TYPE_GRAVITY, "Gravity");
        add(Sensor.TYPE_LINEAR_ACCELERATION, "Linear acceleration");
        add(Sensor.TYPE_ROTATION_VECTOR, "Rotation vector");
        add(Sensor.TYPE_GAME_ROTATION_VECTOR, "Game rotation vector");
        add(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, "Geomagnetic rotation vector");
        add(Sensor.TYPE_SIGNIFICANT_MOTION, "Significant motion");
        add(Sensor.TYPE_STEP_DETECTOR, "Step detector");
        add(Sensor.TYPE_STEP_COUNTER, "Step counter");
        add(Sensor.TYPE_RELATIVE_HUMIDITY, "Relative humidity");
        add(Sensor.TYPE_AMBIENT_TEMPERATURE, "Ambient temperature");
        add(Sensor.TYPE_GYROSCOPE_UNCALIBRATED, "Uncalibrated gyroscope");
        add(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, "Uncalibrated magnetic field");
        add(Sensor.TYPE_POSE_6DOF, "Pose 6DOF");
        add(Sensor.TYPE_HEART_RATE, "Heart rate");
        start();
    }

    private void add(int type, String name) {
        requestedTypes.add(type);
        names.put(type, name);
    }

    public synchronized void start() {
        if (manager == null || running) return;
        for (Integer type : requestedTypes) {
            Sensor sensor = manager.getDefaultSensor(type);
            if (sensor != null) {
                try {
                    int delay = (type == Sensor.TYPE_SIGNIFICANT_MOTION || type == Sensor.TYPE_STEP_COUNTER || type == Sensor.TYPE_STEP_DETECTOR)
                            ? SensorManager.SENSOR_DELAY_NORMAL : SensorManager.SENSOR_DELAY_GAME;
                    manager.registerListener(this, sensor, delay);
                } catch (SecurityException ignored) {
                    // Some sensors, such as heart rate/step sensors, can require permissions.
                }
            }
        }
        running = true;
    }

    public synchronized void stop() {
        if (manager != null && running) manager.unregisterListener(this);
        running = false;
    }

    @Override public synchronized void onSensorChanged(SensorEvent event) {
        latest.put(event.sensor.getType(), event.values.clone());
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    public synchronized String snapshot() {
        StringBuilder out = new StringBuilder("Live sensors:\n");
        int available = 0;
        for (Integer type : requestedTypes) {
            Sensor sensor = manager == null ? null : manager.getDefaultSensor(type);
            if (sensor == null) continue;
            available++;
            float[] values = latest.get(type);
            out.append("• ").append(names.get(type)).append(": ");
            if (values == null) out.append("waiting for reading");
            else out.append(format(values));
            out.append('\n');
        }
        out.append("Available: ").append(available).append('/').append(requestedTypes.size());
        return out.toString().trim();
    }

    public synchronized String sensorStatus() {
        int available = 0;
        int live = 0;
        for (Integer type : requestedTypes) {
            Sensor sensor = manager == null ? null : manager.getDefaultSensor(type);
            if (sensor != null) {
                available++;
                if (latest.containsKey(type)) live++;
            }
        }
        return String.format(Locale.US, "Sir, Jay has %d of %d requested sensor types available and %d are currently reporting live readings.", available, requestedTypes.size(), live);
    }

    private String format(float[] values) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) b.append(", ");
            b.append(String.format(Locale.US, "%.3f", values[i]));
        }
        return b.toString();
    }
}
