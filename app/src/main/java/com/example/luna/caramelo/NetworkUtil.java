package com.example.luna.caramelo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by LUNA on 2017-12-22.
 */


//세팅액티비티(설정)에서 데이터 환경 변경 시 알림을 허용한 유저에게 쓰일 클래스
//브로드캐스트 리시버부터 공부하쟛..
//참고 블로그 http://koreaparks.tistory.com/128

//관련 클래스: NetworkChangeReceiver

public class NetworkUtil{


    private static final String TAG = "네트워크DataConnectionChange클래스";

    //네트워크 연결상태
    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;


    public static int getConnectiviyStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if(null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.d(TAG, ConnectivityManager.TYPE_WIFI + "와이파이");
                return TYPE_WIFI;

            } else if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                Log.d(TAG, ConnectivityManager.TYPE_MOBILE+"모바일넷웍");
                return  TYPE_MOBILE;
            }
        }

        Log.d(TAG, TYPE_NOT_CONNECTED+"노데이터넷월ㅋ");
        return TYPE_NOT_CONNECTED;
    }//getConnectiviyStatus


    public static String getConnectivityStatusString(Context context) {
        int conn = NetworkUtil.getConnectiviyStatus(context);
        String status = null;

        if(conn==NetworkUtil.TYPE_WIFI) {
            status ="무선 네트워크(WiFi)에 연결되었습니다.";
        } else if(conn == NetworkUtil.TYPE_MOBILE) {
            status = "모바일 네트워크에 연결되었습니다.";
        } else if (conn == NetworkUtil.TYPE_NOT_CONNECTED) {
            status = "연결된 네트워크가 없습니다.";
        }

        return status;
    }//getConnectivityStatusString


}//NetworkUtil
