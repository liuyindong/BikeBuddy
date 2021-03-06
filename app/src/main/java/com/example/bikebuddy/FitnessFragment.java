package com.example.bikebuddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bikebuddy.Data.DbHelper;
import com.example.bikebuddy.Services.LocationService;
import com.example.bikebuddy.Services.RecordingService;
import com.example.bikebuddy.Utils.Workout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class FitnessFragment extends Fragment {

    private static final long MILLSECS_PER_DAY = (long) 8.64e+7;
    //Public static variables to be accessed in this fragment AND recording service
    //***************************************************************************************
    public static Chronometer chronometer;
    public static boolean running;
    public static long WorkoutDuration;
    //***************************************************************************************

    private Button RecordWorkout;
    private static Boolean mockDatagenerated = false;  //used to remove mock data button after use

    private TextView speedTextView;
    private TextView distanceTextView;
    private TextView distanceTitleTextView;

    FrameLayout distanceFrameLayout;

    public static final String TAG = "FitnessFragment";

    private ImageView imageViewBluetoothStatus; //This is the ImageView that displays whether a device is connected or not
    private TextView textBluetoothStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fitness,container,false);
        RecordWorkout=view.findViewById(R.id.button_record_workout);
        chronometer= view.findViewById(R.id._chronometer);
        speedTextView = view.findViewById(R.id.text_speed_rt);
        distanceTextView = view.findViewById(R.id.text_distance_rt);
        distanceTitleTextView = view.findViewById(R.id.text_distance);
        distanceFrameLayout = view.findViewById(R.id.frame_distance);


        imageViewBluetoothStatus = view.findViewById(R.id.image_bluetooth_status);
        textBluetoothStatus = view.findViewById(R.id.text_bluetooth_status);

        if (MainActivity.isDeviceConnected == true)
        {
            imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_on);
            textBluetoothStatus.setText("Connected");
        }
        else
        {
            imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_off);
            textBluetoothStatus.setText("Disconnected");
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
                    startRecording();




                }
                else{ // when running
                    Log.d(TAG,"workout stopped");
                    chronometer.stop();
                    RecordWorkout.setText("record workout");
                    running=false;
                    chronometer.setVisibility(View.INVISIBLE);
                    distanceTextView.setVisibility(View.INVISIBLE);
                    distanceTitleTextView.setVisibility(View.INVISIBLE);
                    distanceFrameLayout.setVisibility(View.INVISIBLE);
                    WorkoutDuration = chronometer.getBase();
                    resetWorkoutDistance(); //Reset the workout distance
                    Toast.makeText(getActivity(),"Workout Recorded",Toast.LENGTH_SHORT).show();
                    stopRecordingService();
                    if( !mockDatagenerated ){
                        //generateMockData();
                        //mockDatagenerated = true;
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        // Saving the information of the timer when leaving the fragment
        Log.d(TAG,"onStop()");
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
            distanceTitleTextView.setVisibility(View.VISIBLE);
            distanceTextView.setVisibility(View.VISIBLE);
            distanceFrameLayout.setVisibility(View.VISIBLE);
        chronometer.start();
        }
    }

    private void resetWorkoutDistance()
    {
        LocationService.WORKOUT_DISTANCE = 0;
        TextView distanceTextView = getActivity().findViewById(R.id.text_distance_rt);
        DecimalFormat dec_0 = new DecimalFormat("#0"); //0 decimal places https://stackoverflow.com/questions/14845937/java-how-to-set-precision-for-double-value
        if (distanceTextView != null)
        {
            distanceTextView.setText(dec_0.format(LocationService.WORKOUT_DISTANCE)); //Update the Heart Rate TextView (Real Time)
        }
    }

    /*
    This methods displays an alert dialog prior to beginning a workout.
    It informs the user to secure their phone and make sure the sensor connection is stable.
    The user has the ability to proceed or cancel.
     */
    private void startRecording()
    {
        //First we create a dialog to be displayed to the user
        getWorkoutDialog();
    }

    /*
    This method creates a recording service.
     */
    public void createRecordingService()
    {
        Log.d(TAG,"createRecordingService()");
        getActivity().startService(new Intent(getActivity(), RecordingService.class));
    }

    /*
    This method STOPS the recording service.
     */
    public void stopRecordingService()
    {
        Log.d(TAG,"createRecordingService()");
        getActivity().stopService(new Intent(getActivity(), RecordingService.class));
    }

    //**************************************************************************************
    //THIS IS JUST FOR POPULATING MOCK DATA
    public String loadJSONfromAsset(){
        String json = null;
        try{
            InputStream inputStream = getActivity().getAssets().open("MockData");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer,"UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
    private double[] calculateCalorieRate(double heartRate, double speed, double estimate, double sigma, double Q, double C, int age, int weight){
        /*
        Returns value of best guess for power output. Uses velocity data to approximate power
        based on MET standards and then HR to approximate output based on Keytel approximation.
        The MET standards typically provide approximations that are conservatively large. Keytel
        approximation will be used to bring the output to a more appropriate level (matching
        clinical trials).
         */

        //Internal Parameters
        double K;
        double MET;
        double newVal;
        double[] kalReturn;

        //Calculating MET level
        if(speed < 11) MET = 4.8;
        else if(speed <16) MET = 5.9;
        else if(speed <21) MET = 7.1;
        else if(speed <26) MET = 8.4;
        else MET = 9.8;

        //*****************************************************************************************************************************
        //Performing the filter approximation.
        //  assume keytelPower only
        if(heartRate > 30) {
            //Male
            newVal = ((-55.0969 + (0.6309 * heartRate) + (0.1988 * weight) + (0.2017 * age)) / 4.184);  //kcal/min

            if (newVal < 0) newVal = 0;

            K = (sigma * sigma * C) / (sigma * sigma * C * C + Q * Q);
            estimate = estimate + K * (newVal - C * estimate);
            sigma = (1 - K * C) * sigma;
        } else newVal = estimate;


        //  update with metPower
        if(speed > 3) {
            newVal = MET * weight / 60;     //kcal/min
            K = (sigma * sigma * C) / (sigma * sigma * C * C + Q * Q);
            estimate = estimate + K * (newVal - C * estimate);
            sigma = (1 - K * C) * sigma;
            if(newVal < 0) newVal = 0;
        } else newVal = estimate;

        //*****************************************************************************************************************************

        //Returning estimate and deviation, both used at next method call
        kalReturn = new double[]{estimate, sigma};
        //Log.d(TAG,"Estimated Calorie Rate: " + kalReturn[0] + " cal/min");
        return kalReturn;
    }


    //returns random date in year 2020 and between mar 1 and apr 10
    //  time is random between 6am and 9pm
    public Date generateRandomDate(){
        //New Way
        Date end = new Date(120,3,10);
        Date start = new Date(120,2,1);

        long deltaTime = ( end.getTime() - start.getTime() );
        int deltaDays = (int) TimeUnit.DAYS.convert(deltaTime, TimeUnit.MILLISECONDS);
        Log.d(TAG,"Start Date = " + start);
        Log.d(TAG,"End Date = " + end);
        Log.d(TAG,"deltaDays = " + deltaDays);

        //set time
        int hour = randBetween(6,21);
        int minute = randBetween(0,59);
        int second = randBetween(0,59);

        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        cal.add(Calendar.DATE, new Random().nextInt(deltaDays));  // diff calculated in 1)
        cal.set(Calendar.HOUR,hour);
        cal.set(Calendar.MINUTE,minute);
        cal.set(Calendar.SECOND,second);
        Date randomDate = cal.getTime();

        return randomDate;
    }
    public static int randBetween(int start, int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }

    // Call to generate random function
    public void generateMockData(){
            DbHelper dbHelper = new DbHelper(getContext());
            for(int j = 0; j < 14; j++) {       //this 14 is hardcoded  to make 14 workouts
                //data initialization for workout
                List<Long> listTime = new ArrayList<>();
                List<Double> listHR = new ArrayList<>();
                List<Double> listSpeed = new ArrayList<>();
                List<Double> listLongitude = new ArrayList<>();
                List<Double> listLattitude = new ArrayList<>();
                Date date;

                //Data for kalman filter
                double C = 1.0;
                double Q = 0.05;
                double[] kalReturn;
                double sigma = Q;
                double calRateEstimate = 0;
                int userWeight = 0;
                int userAge = 0;
                SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(getActivity());
                if(sharedPreferenceHelper.getProfile()){
                    userAge = sharedPreferenceHelper.getProfileAge();
                    userWeight = sharedPreferenceHelper.getProfileWeight();
                } else{
                    Toast.makeText(getContext(),"No Profile Found, Please create a profile " ,Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                }

                //Parsing the JSON
                try {
                    JSONObject obj = new JSONObject(loadJSONfromAsset());
                    String bikeData = "bikeData" + (j+1);
                    JSONObject data = obj.getJSONObject(bikeData);
                    int id = data.getInt("id");     //used to verify the correct object was retrieved
                    JSONArray heartRate = data.getJSONArray("heart_rate");
                    JSONArray speed = data.getJSONArray("speed");
                    JSONArray time = data.getJSONArray("timestamp");
                    JSONArray longitude = data.getJSONArray("longitude");
                    JSONArray lattitude = data.getJSONArray("latitude");
                    Long initialTime = time.getLong(0);
                    for (int i = 0; i < time.length(); i++) {
                        listTime.add(time.getLong(i)-initialTime);
                        listHR.add(heartRate.getDouble(i));
                        listSpeed.add(speed.getDouble(i));
                        listLongitude.add(longitude.getDouble(i));
                        listLattitude.add(lattitude.getDouble(i));
                    }
                    for(int i = 0; i<listTime.size(); i++){
                        if(i>0) {
                            //Run Kalman filter on data
                            kalReturn = calculateCalorieRate(listHR.get(i),listSpeed.get(i),calRateEstimate,sigma,Q,C,userAge,userWeight);
                            sigma = kalReturn[1];
                            calRateEstimate = kalReturn[0];
                        }
                    }

                    //generating random date and time
                    date = generateRandomDate();

                    //Creating workout
                    Workout workout = new Workout();
                    workout.setTime(listTime);
                    workout.setListSpeed(listSpeed);
                    workout.setListHR(listHR);
                    workout.setAverageSpeed(workout.calculateAverageSpeed());
                    workout.setAverageHR(workout.calculateAverageHR());
                    workout.setMaxHR(workout.calculateMaxHR());
                    workout.setCaloriesRate(calRateEstimate);
                    workout.setCaloriesBurned(workout.calculateCaloriesBurned(calRateEstimate));
                    workout.setTotalDuration(listTime.get(listTime.size()-1)-listTime.get(0));
                    workout.setTotalDistance(workout.calculateAverageSpeed()*workout.getTotalDuration()/3.6);
                    workout.setDate(date);
                    workout.setListLatCoords(listLattitude);
                    workout.setListLngCoords(listLongitude);
                    workout.print(TAG);

                    //add workout to database
                    dbHelper.insertWorkout(workout);
                    dbHelper.UpdateBike( sharedPreferenceHelper.getSelectedBike() ,workout.getTotalDistance(),workout.getTotalDuration());


                        /*

                        Log.d(TAG,"Workout created with random date: " + date);
                        Log.d(TAG,"Workout calRate = " + calRateEstimate);
                        Log.d(TAG,"Workout calBurned = " + workout.getCaloriesBurned());
                         */

                        /* THIS SECTION IS USED TO VERIFY PARSING OF JSON DATA IN LOG
                        Log.d(TAG, "JSON RETRIEVED data: " + bikeData);
                        Log.d(TAG, "JSON CONVERTED LISTS:" + "time = " + listTime);
                        Log.d(TAG, "JSON CONVERTED LISTS:" + "heart rate = " + listHR);
                        Log.d(TAG, "JSON CONVERTED LISTS:" + "speed = " + listSpeed);
                         */
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mockDatagenerated = true;
    }
    //**************************************************************************************


    //Opens a dialog fragment
    private void getWorkoutDialog()
    {
        RecordWorkoutDialog recordWorkoutDialog = new RecordWorkoutDialog();
        recordWorkoutDialog.show(getChildFragmentManager(),"Record Workout Dialog");
        StringBuilder message = new StringBuilder();
    }

    //To be called when we select "Continue" from the dialog
    public void continueWorkoutAction()
    {
        resetWorkoutDistance(); //Reset the workout distance before we display it
        chronometer.setVisibility(View.VISIBLE);
        distanceTextView.setVisibility(View.VISIBLE);
        distanceTitleTextView.setVisibility(View.VISIBLE);
        distanceFrameLayout.setVisibility(View.VISIBLE);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        RecordWorkout.setText("Stop Recording");
        running=true;
        createRecordingService();
    }

}
