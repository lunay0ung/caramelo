package com.example.luna.caramelo.Settings;

import android.app.Activity;
import android.app.Application;

import com.example.luna.caramelo.Settings.Account.KakaoSDKAdapter;
import com.kakao.auth.KakaoSDK;

/**
 * Created by LUNA on 2017-12-24.
 */

public class GlobalApplication extends Application{


    private static volatile GlobalApplication obj = null;
    private static volatile Activity currentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();

        obj = this;
        KakaoSDK.init(new KakaoSDKAdapter());
    }//onCreate

    public static GlobalApplication getGlobalApplicationContext() {
        return obj;
    }//GlobalApplication

    public static Activity getCurrentActivity() {
        return currentActivity;
    }//Activity

    //Activity가 올라올 때마다 Activity의 onCreate에서 호출해줘야 한다
    public static void setCurrentActivity(Activity currentActivity) {
        GlobalApplication.currentActivity = currentActivity;
    }//setCurrentActivity


}//GlobalApplication