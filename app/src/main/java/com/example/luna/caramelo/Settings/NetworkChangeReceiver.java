package com.example.luna.caramelo.Settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.luna.caramelo.Settings.Account.SharedPrefManager;
import com.example.luna.caramelo.Settings.Account.User;

/**
 * Created by LUNA on 2017-12-22.
 */

//관련 클래스 NetworkUtil

public class NetworkChangeReceiver extends BroadcastReceiver {

    Context context;
    //현재 유저는 뉴규?
    final User user = SharedPrefManager.getmInstance(context).getUser();

    @Override
    public void onReceive(final Context context, final Intent intent) {

        String status = NetworkUtil.getConnectivityStatusString(context);
        if(user.isGetDataNoti()==true) {
            //유저가 설정메뉴(setting activity)에서 데이터 사용환경 변경 시 알림 스위치를 켜두었다면
            //데이터 이용환경 변경 시 토스트를 날린다
            //근데 왜 와이파이 잡으면 나와야하는 메시지는 안 나왕?
            //.....NetworkUtil클래스의 getConnectiviyStatus 메소드에서 if문에 괄호를 쓰지 않았다 ㅋ
            //리턴값이 걍 한 줄이라서 문제 없을 줄 알았는데 문제가 있었음. 앞으로는 무조건 괄호를 써주자
            Toast.makeText(context, status, Toast.LENGTH_LONG).show();
        }
    }//onReceive
}//NetworkChangeReceiver
