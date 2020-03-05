package com.example.bikebuddy;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import me.aflak.bluetooth.Bluetooth;



public class FitnessFragment extends Fragment {
    Chronometer chronometer;
    Button RecordWorkout;
    boolean running;
    long WorkoutDuration;

    public static final String TAG = "FitnessFragment";

    ImageView imageViewBluetoothStatus; //This is the ImageView that displays whether a device is connected or not
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fitness,container,false);
        RecordWorkout=view.findViewById(R.id.button_record_workout);
        chronometer= view.findViewById(R.id._chronometer);



        imageViewBluetoothStatus = view.findViewById(R.id.image_bluetooth_status);
        if (MainActivity.isDeviceConnected == true)
        {
            imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_on);
        }
        else
        {
            imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_off);
        }

        imageViewBluetoothStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.isDeviceConnected != true)
                {
                    Log.d(TAG,"imageViewBluetoothStatus onClickListener");
                    ((MainActivity)getActivity()).connectToSensor();
                    Toast.makeText(getActivity(),"Trying to connect to " + MainActivity.SENSOR_NAME,Toast.LENGTH_SHORT).show();
                }
            }
        });
        // recording workout
        RecordWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!running){ // when it is not running
                    chronometer.setVisibility(View.VISIBLE);
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    RecordWorkout.setText("Stop Recording");
                    running=true;
                }
                else{ // when running
                    chronometer.stop();
                    RecordWorkout.setText("record workout");
                    running=false;
                    chronometer.setVisibility(View.INVISIBLE);
                    WorkoutDuration=chronometer.getBase();
                }

            }
        });


        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        // Saving the information of the timer when leaving the fragment
        ((MainActivity)getActivity()).SaveTimerState(running);
        ((MainActivity)getActivity()).SaveTimerTime(chronometer.getBase());
    }

    @Override
    public void onStart() {
        super.onStart();

        /* Simply when opening the fitness fragment,
         the app checks if the timer has been activated before in order to continue counting.
        */

        running = ((MainActivity) getActivity()).StateOfTimer();
        chronometer.setBase(((MainActivity) getActivity()).BaseOfTimer());
        if (running) {
            chronometer.setVisibility(View.VISIBLE);
            RecordWorkout.setText("Stop Recording");
        chronometer.start();
        }
    }
}