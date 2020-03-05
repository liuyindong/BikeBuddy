package com.example.bikebuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.bikebuddy.Permissions.Permissions;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    boolean permissionsGranted = false; //boolean that checks if permissions are granted or not (Location...)
    final int permissionsRequestCode = 1;

    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupUI();
    }

    /*
    This method connects the UI elements and onClickListeners
     */
    private void setupUI() {
        loginButton = findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                To DO:
                Check if input email and password is correct (PRIOR to permissions). If correct, proceed to verify permissions.
                Otherwise, inform user they have entered incorrect information
                 */

                //Check permissions (Location,etc.)
                checkPermissions();
                if (permissionsGranted == true) {
                    Log.d(TAG,"Moving to MainActivity");
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    /*
    This method verifies if ALL permissions are granted.
    If permissions have not yet been granted, then it invokes requestPermissions()
     */
    private void checkPermissions() {
        Log.d(TAG, "checkPermissions() method");
        for (int i = 0; i < Permissions.permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, Permissions.permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                permissionsGranted = false;
                Log.d(TAG, "Missing Permission: " + Permissions.permissions[i]);
                requestPermissions();
                return;
            }
        }
        permissionsGranted = true; //If all permissions are granted, we set this boolean to true
    }

    /*
    Requests permissions from the user
     */
    private void requestPermissions() {
        Log.d(TAG, "Requesting Permissions");
        ActivityCompat.requestPermissions(this, Permissions.permissions, permissionsRequestCode);
    }

    /*
    Determines if the user accepted or denied the permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case permissionsRequestCode: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(getApplicationContext(),"Please enable permissions to proceed",Toast.LENGTH_SHORT).show();
                            permissionsGranted = false;
                            return;
                        }
                    }
                    Log.d(TAG,"Moving to MainActivity"); //If all permissions have been granted, we can move to main activity
                    permissionsGranted = true;
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        }
    }
}

