package com.example.luna.caramelo;

/**
 * Created by LUNA on 2017-12-11.
 */

//클래스 만드는 데 참고한 예제 사이트:
//https://www.simplifiedcoding.net/android-login-and-registration-tutorial/
public class URLs {


    private static final String ROOT_URL ="http://13.124.67.214/caramelo/"; //기본 url
    public static final String URL_LOGIN = ROOT_URL + "/account_management/login_getUserInfo.php"; //로그인할 때 유저 정보를 가져오는 url

    //카카오 로그인 시도 시
    public static final String URL_KAKAO = ROOT_URL + "/account_management/login_kakao.php";

    //유저가 즐겨찾기 목록에 등록한 웹사이트 목록을 불러오기 위한 것
    //메뉴로는 스크랩 메뉴, 액티비티로는 favorite activity에서 사용되는 것
    //favorite activity에서도 main activity처럼 탭 페이저를 사용했고 각 탭들은 fragment로 연결됨
    //즐겨찾기 목록을 구성하는 fragment들은 Fav_~Fragment의 이름을 가짐
    public static final String GET_FAVORITE = "http://13.124.67.214/caramelo/favorite/get_favorite.php";

    //즐겨찾기 목록에서 삭제하는 url
    public static final String DELETE_FAVORITE = "http://13.124.67.214/caramelo/favorite/delete_favorite.php";


    public static final String TEST = "http://13.124.67.214/caramelo/favorite/test.php";//테스트용



}
