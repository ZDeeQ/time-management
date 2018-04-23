package com.example.time;

import android.widget.ImageView;

/**
 * Created by liutang on 2018/3/26.
 */

public class Goal {

    private String name;
    private float totalTime;
    private float todayTime;
    private int goalImageID;
    public Goal(String name, float totalTime, float todayTime,int goalImageID) {
        this.name = name;
        this.totalTime = totalTime;
        this.todayTime = todayTime;
        this.goalImageID=goalImageID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(float totalTime) {
        this.totalTime = totalTime;
    }

    public float getTodayTime() {
        return todayTime;
    }

    public void setTodayTime(float todayTime) {
        this.todayTime = todayTime;
    }

    public int getGoalImageID() {
        return goalImageID;
    }

    public void setGoalImageID(int goalImageID) {
        this.goalImageID = goalImageID;
    }
}
