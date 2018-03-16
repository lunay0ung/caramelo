package com.example.luna.caramelo.Settings.Account;

/**
 * Created by LUNA on 2017-12-11.
 */


    //유저 생성
    //유저로부터 직접 수집하는 정보는 가입 시 요구되는 이메일과 유저네임
    //인덱스인 유저넘버(user_no)와 일반/유저 가입을 구별하는 type(general/google), 그리고 유저 가입일시를 저장하는 created_at은 자동 생성됨
    //어떻게 쓰일지 몰라서 일단 모두 shared preferences에 저장함
    //-> 관련 클래스: SharedPrefManager -여기서 로그인여부 구별, 로그인한 유저 정보 가져오기, 로그아웃 모두 관리
    //원래 유저 넘버를 integer로 저장하였으나 그렇게 하면 오류가 나서 string으로 바꾸었음
    //이유는 20171219 아직 규명하지 못한 상태
public class User {


    private String email, username, type, created_at, user_no;
    private boolean getDataNoti; //2017-12-22 추가, 데이터 환경변경 알림 관련


    public User(String user_no, String email, String username, String created_at, String type, boolean getDataNoti) {
        this.user_no = user_no;
        this.email = email;
        this.username = username;
        this.created_at = created_at;
        this.type = type;
        this.getDataNoti = getDataNoti;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUser_no() {
        return user_no;
    }

    public void setUser_no(String user_no) {
        this.user_no = user_no;
    }

    public boolean isGetDataNoti() {
        return getDataNoti;
    }

    public void setGetDataNoti(boolean getDataNoti) {
        this.getDataNoti = getDataNoti;
    }
}
