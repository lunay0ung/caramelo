package com.example.luna.caramelo.Settings.Account;

import android.content.Context;

import com.example.luna.caramelo.Settings.GlobalApplication;
import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;

/**
 * Created by LUNA on 2017-12-24.
 */

public class KakaoSDKAdapter extends KakaoAdapter{

    /**
     * 로그인을 위해 Session을 생성하기 위해 필요한 옵션을 얻기위한 abstract class.
     * 기본 설정은 KakaoAdapter에 정의되어있으며, 설정 변경이 필요한 경우 상속해서 사용할 수 있다.
     */

    @Override
    public ISessionConfig getSessionConfig() {
        return new ISessionConfig() {
            @Override
            public AuthType[] getAuthTypes() {
                return new AuthType[] {
                        AuthType.KAKAO_LOGIN_ALL
                };
            }

            //로그인 시 인증받을 타입을 지정한다.
            //지정하지 않을 시 가능한 모든 옵션이 지정된다

            //1.KAKAO_TALK 카카오톡으로 로그인하고 싶을 때 지정
            //2.KAKAO_STORY 카카오 스토리로 로그인하고 싶을 때
            //3. KAKAO_ACCOUNT 웹뷰 다이얼로그를 통해 카카오 계정연결을 제공하고 싶을 때
            //4.KAKAO_TALK_EXCLUDE_NATIVE_LOGIN 카카오톡으로만 로그인을 유도하고 싶으면서 계정이 없을 떄
            //  계정 생성을 위한 버튼도 같이 제공하고 싶다면 지정, KAKAO_TALK과 중복 지정 불가
            //5.KAKAO_LOGIN_ALL 모든 로그인방식 지정하고 싶을 때

            @Override
            public boolean isUsingWebviewTimer() {
                return false;
            }
            /* // SDK 로그인시 사용되는 WebView에서 pause와 resume시에 Timer를 설정하여 CPU소모를 절약한다.
            // true 를 리턴할경우 webview로그인을 사용하는 화면서 모든 webview에 onPause와 onResume
            // 시에 Timer를 설정해 주어야 한다. 지정하지 않을 시 false로 설정된다.*/

            @Override
            public boolean isSecureMode() {
                return false;
            }

            @Override
            public ApprovalType getApprovalType() {
                return ApprovalType.INDIVIDUAL;
            }
            //일반 사용자가 아닌 KAKAO에서 제휴된 앱에서 사용되는 값
            //값을 채워주지 않을 경우
            // ApprovalType.INDIVIDUAL 값을 사용하게 된다.


            @Override
            public boolean isSaveFormData() {
                return true;
            } //kakao sdk에서 사용되는 webview에서 이메일 입력폼에서 data를 저장할지 여부 결정
            //디폴트값-트로
        };//ISessionConfig()
    }//ISessionConfig

    //애플리케이션이 가지고 있는 정보를 얻기 위한 인터페이스
    @Override
    public IApplicationConfig getApplicationConfig() {
        return new IApplicationConfig() {


            @Override
            public Context getApplicationContext() {
                return GlobalApplication.getGlobalApplicationContext();
            }//Context
        };//IApplicationConfig()
    }//IApplicationConfig




}//KakaoSDKAdapter
