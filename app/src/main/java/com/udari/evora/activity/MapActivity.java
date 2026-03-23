package com.udari.evora.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.animation.CameraAnimationsUtils;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.gestures.GesturesUtils;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;
import com.udari.evora.R;
import com.udari.evora.databinding.ActivityMapBinding;

import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity {

    private ActivityMapBinding binding;
    private String selectedAddress = "";
    private final Handler addressHandler = new Handler(Looper.getMainLooper());
    private Runnable addressRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.mapbox.common.MapboxOptions.setAccessToken(getString(R.string.mapbox_access_token));
        super.onCreate(savedInstanceState);

        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            loadMap();
        }

        setupButtons();
    }

    private void loadMap() {
        binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            binding.mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                    .center(Point.fromLngLat(79.8612, 6.9271))
                    .zoom(12.0)
                    .build());
            setupMapListeners();
        });
    }

    private void setupMapListeners() {
        GesturesUtils.getGestures(binding.mapView).addOnMoveListener(new OnMoveListener() {
            @Override
            public void onMoveBegin(@NonNull com.mapbox.android.gestures.MoveGestureDetector detector) {
                binding.imgCenterPin.animate().translationY(-20f).setDuration(200).start();
                if (addressRunnable != null) addressHandler.removeCallbacks(addressRunnable);
            }

            @Override
            public boolean onMove(@NonNull com.mapbox.android.gestures.MoveGestureDetector detector) { return false; }

            @Override
            public void onMoveEnd(@NonNull com.mapbox.android.gestures.MoveGestureDetector detector) {
                binding.imgCenterPin.animate().translationY(0f).setDuration(200).start();
                Point center = binding.mapView.getMapboxMap().getCameraState().getCenter();
                addressRunnable = () -> updateAddress(center);
                addressHandler.postDelayed(addressRunnable, 600);
            }
        });
    }

    private void updateAddress(Point point) {
        runOnUiThread(() -> binding.etSearchLocation.setText("Locating..."));
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(point.latitude(), point.longitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    selectedAddress = addresses.get(0).getAddressLine(0);
                    runOnUiThread(() -> binding.etSearchLocation.setText(selectedAddress));
                }
            } catch (Exception e) {
                runOnUiThread(() -> binding.etSearchLocation.setText("Location not found"));
            }
        }).start();
    }

    @SuppressLint("MissingPermission")
    private void setupButtons() {
        binding.currentLocation.setOnClickListener(v -> {
            try {
                LocationComponentPlugin locationPlugin = LocationComponentUtils.getLocationComponent(binding.mapView);
                locationPlugin.setEnabled(true);

                // 1. Settings update කරනවා (Pulse effect එක පෙන්වන්න)
                locationPlugin.updateSettings(settings -> {
                    settings.setEnabled(true);
                    settings.setPulsingEnabled(true);
                    return null;
                });

                // 2. Listener එක හරහා කැමරාව move කරනවා
                OnIndicatorPositionChangedListener listener = new OnIndicatorPositionChangedListener() {
                    @Override
                    public void onIndicatorPositionChanged(@NonNull Point point) {
                        CameraOptions cameraOptions = new CameraOptions.Builder()
                                .center(point)
                                .zoom(15.0)
                                .build();

                        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder()
                                .duration(1500L)
                                .build();

                        CameraAnimationsUtils.getCamera(binding.mapView).easeTo(
                                cameraOptions,
                                animationOptions,
                                null
                        );

                        // එක පාරක් Zoom වුණාම Listener එක අයින් කරනවා
                        locationPlugin.removeOnIndicatorPositionChangedListener(this);
                    }
                };

                locationPlugin.addOnIndicatorPositionChangedListener(listener);
                Toast.makeText(this, "Finding your location...", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error finding location", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnConfirmLocation.setOnClickListener(v -> {
            String finalAddress = binding.etSearchLocation.getText().toString();
            if (!finalAddress.isEmpty() && !finalAddress.equals("Locating...")) {
                Intent intent = new Intent();
                intent.putExtra("selected_address", finalAddress);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "Please select a valid location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() { super.onStart(); if (binding.mapView != null) binding.mapView.onStart(); }
    @Override
    protected void onStop() { super.onStop(); if (binding.mapView != null) binding.mapView.onStop(); }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (addressRunnable != null) addressHandler.removeCallbacks(addressRunnable);
        if (binding.mapView != null) binding.mapView.onDestroy();
        binding = null;
    }
}