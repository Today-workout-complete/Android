package com.example.today_workout_complete;

import com.google.gson.annotations.SerializedName;

public class MyEmgData {
    @SerializedName("emg_data_path")
    private String emg_data_path;

    public String getEmg_data_path() {
        return emg_data_path;
    }
}
