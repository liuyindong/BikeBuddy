package com.example.bikebuddy.Utils;

import java.util.Date;
import java.util.Calendar;
import java.util.List;

public class Workout {

    private Date date;
    private List <Long> time;
    private List <Double> listHR;
    private List <Double> listSpeed;
    private double totalDistance;
    private long totalDuration;
    private double caloriesBurned;
    private double averageHR;
    private double averageSpeed;

    // Constructor With date set explicitly
    public Workout(Date date, List<Long> time, List<Double> listHR, List<Double> listSpeed, double totalDistance, long totalDuration, double caloriesBurned, double averageHR, double averageSpeed) {
        this.date = date;
        this.time = time;
        this.listHR = listHR;
        this.listSpeed = listSpeed;
        this.totalDistance = totalDistance;
        this.totalDuration = totalDuration;
        this.caloriesBurned = caloriesBurned;
        this.averageHR = averageHR;
        this.averageSpeed = averageSpeed;
    }

    // Constructor without date
    public Workout(List<Long> time, List<Double> listHR, List<Double> listSpeed, double totalDistance, long totalDuration, double caloriesBurned, double averageHR, double averageSpeed) {
        this.date=  Calendar.getInstance().getTime();
        this.time = time;
        this.listHR = listHR;
        this.listSpeed = listSpeed;
        this.totalDistance = totalDistance;
        this.totalDuration = totalDuration;
        this.caloriesBurned = caloriesBurned;
        this.averageHR = averageHR;
        this.averageSpeed = averageSpeed;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Long> getTime() {
        return time;
    }

    public void setTime(List<Long> time) {
        this.time = time;
    }

    public List<Double> getListHR() {
        return listHR;
    }

    public void setListHR(List<Double> listHR) {
        this.listHR = listHR;
    }

    public List<Double> getListSpeed() {
        return listSpeed;
    }

    public void setListSpeed(List<Double> listSpeed) {
        this.listSpeed = listSpeed;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public double getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public double getAverageHR() {
        return averageHR;
    }

    public void setAverageHR(double averageHR) {
        this.averageHR = averageHR;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public double calculateAverageHR(){
        if (this.listHR.size()!=0) {// to make sure that we don't divide by zero
            double sum = 0;
            for (int i = 0; i < this.listHR.size(); i++) {
                sum += this.listHR.get(i);
            }

            return (sum / this.listHR.size());
        }
        return -1; // when the method returns -1, that means the list is empty
    }

    public double calculateAverageSpeed(){
        if (this.listSpeed.size()!=0) { // to make sure that we don't divide by zero
            double sum = 0;
            for (int i = 0; i < this.listSpeed.size(); i++) {
                sum += this.listSpeed.get(i);
            }

            return (sum / this.listSpeed.size());
        }
        return -1; // when the method returns -1, that means the list is empty
    }
}