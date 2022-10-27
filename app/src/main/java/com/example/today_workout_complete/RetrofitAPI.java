package com.example.today_workout_complete;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitAPI {
    public static final String API_URL = "http://118.67.132.81/:3000";
    @GET("/api/sendEmgData")
    Call<List<EmgData>> getEmgData(@Query("Year") int Year,@Query("nickname") String nickname,
                                   @Query("Month") int Month, @Query("Day") int Day);

    @GET("/api/calendarEmgDate")
    Call<List<UserInfo>> getData(@Query("nickname") String nickname);

    @GET("/api/myEmgDataList")
    Call<List<MyEmgData>> getMyEmgDataList(@Query("nickname") String nickname);

    @GET("/api/likedPost")
    Call<List<LikedEmgData>> getLikedEmgData(@Query("nickname") String nickname, @Query("board_id") Integer boardId);

    @GET("/api/likedEmgData")
    Call<EmgData> getEmgData(@Query("emgDataFileName") String emgDataFileName);


    @POST("/api/myPage/emgData")
    Call<List<EmgData>> postEmgData(@Body EmgData emgData);
}
