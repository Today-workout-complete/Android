package com.example.today_workout_complete;

import com.google.gson.annotations.SerializedName;

public class LikedEmgData {
    @SerializedName("title")
    private String title;

    @SerializedName("emg_data_file")
    private String emgDataFile;

    public String getTitle() {
        return title;
    }

    public String getEmgDataFile() {
        return emgDataFile;
    }
}
