package com.example.luna.caramelo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by LUNA on 2017-12-11.
 */

public class SharedPrefManager {

    //수집하는 유저정보는 이메일과 유저네임
    //서버에서 자동 생성되는 것은 유저넘버(인덱스)와 유저타입(general/google), 그리고 가입일시
    //유저타입 general, google은 가입할 때 일반 이메일로 했는지 구글 계정으로 했느냐에 따라 달라진다
    public static final String SHARED_PREF_NAME = "carameloSharedPref";
    public static final String KEY_EMAIL = "keyemail";
    public static final String KEY_USERNAME = "keyusername";
    public static final String KEY_USER_NO = "keyuserno";
    public static final String KEY_USER_TYPE = "keyusertype";
    public static final String KEY_USER_CREATED_AT = "keyusercreatedat";

    //2017-12-22 데이터 환경변경 설정에 대한 정보 추가
    public static final String KEY_DATA_ALARM = "keydata";

    private static SharedPrefManager mInstance;
    private static Context mCtx;

    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized  SharedPrefManager getmInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }

        return mInstance;
    }

    //유저 로그인을 위한 함수
    //여기에 쉐어드를 통해 유저 데이터를 저장함
    public void userLogin(User user) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NO, user.getUser_no()); //유저 넘버, 자동 생성되는 primary key임
        editor.putString(KEY_EMAIL, user.getEmail());   //유저 이메일, 수정 불가능.
        editor.putString(KEY_USERNAME, user.getUsername()); //유저 네임, 수정 가능
        editor.putString(KEY_USER_TYPE, user.getType()); //이메일로 가입했는지, 구글 계정을 통해 가입했는지 구별
        editor.putString(KEY_USER_CREATED_AT, user.getCreated_at()); //가입 날짜와 시간 기록
        editor.putBoolean(KEY_DATA_ALARM, user.isGetDataNoti()); //알림 기본값은 false
        editor.apply();
    }

    //유저가 로그인 했는지 안 했는지 체크한당
    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_EMAIL, null) != null;
    }

    //로그인된 유저 반환
    public User getUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return new User(
                sharedPreferences.getString(KEY_USER_NO, null),
                //ㄴ아니 왜 여기에 유저 타입이 저장되는것임,.?
                //  하고 위로 올라가 보니 저번엔 보이지 않던.....셰어드의 키값이 유저넘버, 유저 타입 둘다 keyuserno으로 되어있엇따 ㅗ
                sharedPreferences.getString(KEY_EMAIL, null),
                sharedPreferences.getString(KEY_USERNAME, null),
                sharedPreferences.getString(KEY_USER_CREATED_AT, null),
                sharedPreferences.getString(KEY_USER_TYPE, null),
                sharedPreferences.getBoolean(KEY_DATA_ALARM, false)
        );
    }


    //유저 로그아웃 시키기
    public void logout() {

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        mCtx.startActivity(new Intent(mCtx, SettingActivity.class));

    }


}//SharedPrefManager
