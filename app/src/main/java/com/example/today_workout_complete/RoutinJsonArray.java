package com.example.today_workout_complete;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RoutinJsonArray {
    private static RoutinJsonArray instance;
    private JSONArray routinArray;
    private String TAG = RoutinJsonArray.class.getSimpleName();

    public static RoutinJsonArray getInstance() {
        if(instance == null) instance = new RoutinJsonArray();
        return instance;
    }

    public static void setInstance(JSONArray routinArray) {
        instance = new RoutinJsonArray(routinArray);
    }

    public RoutinJsonArray(){
        routinArray = new JSONArray();
    }

    public RoutinJsonArray(JSONArray routinArray){
        this.routinArray = routinArray;
    }

    public String stringfyRoutinArray(){
        return routinArray.toString();
    }

    public JSONArray getRoutinArray(){
        return routinArray;
    }

    public JSONObject getRoutin(int routinIndex){
        try {
            return routinArray.getJSONObject(routinIndex);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getBreakTime(int routinIndex, int exerciseIndex){
        try {
            return routinArray.getJSONObject(routinIndex).getJSONArray("exercises").getJSONObject(exerciseIndex).getInt("breakTime");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 60;
    }

    public JSONObject getExercise(int routinIndex, int exerciseIndex){
        try {
            return routinArray.getJSONObject(routinIndex).getJSONArray("exercises").getJSONObject(exerciseIndex);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addRotin(Routin routin){
        JSONObject routinJsonObject = new JSONObject();                                        // 루틴
        JSONArray exercisesJsonArray = new JSONArray();

        for(int i = 0; i < routin.getExercises().size(); i++){                  // Exercises JSON Array에 반복 추가
            Exercise exercise = routin.getExercises().get(i);
            JSONObject exerciseJsonObject = new JSONObject();                   // Exercise JSON Object 생성
            JSONArray regsJsonArray = new JSONArray();                          // Regs JSON Array 생성
            try {
                exerciseJsonObject.put("exerciseName", exercise.getExerciseName());
                exerciseJsonObject.put("setCount", exercise.getSetCount());
                exerciseJsonObject.put("breakTime", exercise.getBreak_time());
                exerciseJsonObject.put("measuredMuscle", exercise.getMeasuredMuscle());
                for (int j = 0; j < exercise.getReps().size(); j++) regsJsonArray.put(exercise.getReps().get(j));
                exerciseJsonObject.put("reps", regsJsonArray);
                exercisesJsonArray.put(exerciseJsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            routinJsonObject.put("routinName", routin.getRoutinName());
            routinJsonObject.put("lastExerciseTimeTook", routin.getLastExerciseTimeTook());
            routinJsonObject.put("lastExerciseDate", routin.getLastExerciseDate());
            routinJsonObject.put("exercises", exercisesJsonArray);
            routinArray.put(routinJsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addExercise(int routinPosition, Exercise exercise){

        JSONObject exerciseJsonObject = new JSONObject();                   // Exercise JSON Object 생성
        JSONArray regsJsonArray = new JSONArray();                          // Regs JSON Array 생성
        try {
            exerciseJsonObject.put("exerciseName", exercise.getExerciseName());
            exerciseJsonObject.put("setCount", exercise.getSetCount());
            exerciseJsonObject.put("breakTime", exercise.getBreak_time());
            exerciseJsonObject.put("measuredMuscle", exercise.getMeasuredMuscle());
            for (int j = 0; j < exercise.getReps().size(); j++) regsJsonArray.put(exercise.getReps().get(j));
            exerciseJsonObject.put("reps", regsJsonArray);
            routinArray.getJSONObject(routinPosition).getJSONArray("exercises").put(exerciseJsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void modifyRoutinName(int idx, String preRoutinName, String newRoutinName) {
        try {
            routinArray.getJSONObject(idx).put("routinName", newRoutinName);
        } catch (JSONException e){
            Log.d(TAG, e.toString());
        }
    }

    public void updateLastExerciseTimeTook(int idx, int lastExerciseTimeTook) throws JSONException {
        routinArray.getJSONObject(idx).put("lastExerciseTimeTook", lastExerciseTimeTook);
    }

    public void updateLastExerciseDate(int idx, int lastExerciseDate) throws JSONException {
        routinArray.getJSONObject(idx).put("lastExerciseDate", lastExerciseDate);
    }

    public void updateSetCount(int routinIndex, int exerciseIndex, int setCount) throws JSONException {
        routinArray.getJSONObject(routinIndex).getJSONArray("exercises").getJSONObject(exerciseIndex).put("setCount", setCount);
    }

    public void updateBrakeTime(int routinIndex, int exerciseIndex, int breakTime) throws JSONException {
        routinArray.getJSONObject(routinIndex).getJSONArray("exercises").getJSONObject(exerciseIndex).put("breakTime", breakTime);
    }

    public void updateAllReps(int routinIndex, int exerciseIndex, int reps) throws JSONException {
        JSONArray repsArray = new JSONArray();
        for(int i = 0; i < routinArray.getJSONObject(routinIndex).getJSONArray("exercises").getJSONObject(exerciseIndex).getJSONArray("reps").length(); i++){
            repsArray.put(reps);
        }
        routinArray.getJSONObject(routinIndex).getJSONArray("exercises").getJSONObject(exerciseIndex).put("reps", repsArray);
    }

    public void updateReps(int routinIndex, int exerciseIndex, int repsIndex, int reps) throws JSONException {
        routinArray.getJSONObject(routinIndex).getJSONArray("exercises").getJSONObject(exerciseIndex).getJSONArray("reps").put(repsIndex, reps);
    }

    public void deleteRoutin(int routinIndex){
        routinArray.remove(routinIndex);
    }

    public void deleteExercise(int routinIndex, int exerciseIndex) {
        try {
            routinArray.getJSONObject(routinIndex).getJSONArray("exercises").remove(exerciseIndex);
        } catch (JSONException e){
            Log.d(TAG, e.toString());
        }
    }
}
