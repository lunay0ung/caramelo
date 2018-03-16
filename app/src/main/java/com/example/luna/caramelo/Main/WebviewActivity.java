package com.example.luna.caramelo.Main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bossturban.webviewmarker.TextSelectionSupport;
import com.example.luna.caramelo.R;
import com.example.luna.caramelo.Settings.Account.SharedPrefManager;
import com.example.luna.caramelo.Settings.Account.User;
import com.example.luna.caramelo.Tools.ApiService;
import com.example.luna.caramelo.Tools.BackPressCloseHandler;
import com.example.luna.caramelo.Tools.ApiClient;
import com.example.luna.caramelo.Tools.ResponseModel;
import com.trafi.anchorbottomsheetbehavior.AnchorBottomSheetBehavior;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
* 웹뷰 액티비티는 말그대로 웹뷰를 통해 유저에게 웹사이트를 보여주기 위해 만든 액티비티이다.
* 웹뷰 액티비티 내에는 두 개의 웹뷰(mWebView, webView_dic)가 있는데 각각 메인웹뷰, 사전웹뷰라고 부른다.
* 메인웹뷰는, 음악/뉴스/기타의 카테고리에서 유저가 선택한 컨텐츠를 보여주는 웹뷰이다.
* 유저는 메인화면이나 즐겨찾기 화면에서 컨텐츠 목록을 확인할 수 있다. 유저가 컨텐츠 목록에서 컨텐츠를 선택하면,
* 메인웹뷰를 통해 스페인어 컨텐츠를 볼 수 있으며, 모르는 단어나 문장을 클릭하여 검색결과 혹은 번역결과를 확인할 수 있다.
* 검색결과와 번역결과는 화면 하단에 고정된 anchorbottomsheet(이하 바텀시트)를 통해 볼 수 있다.
* 유저가 단어나 문장을 클릭하면, 바텀시트가 위로 확장되면서 검색결과 혹은 번역결과를 불러온다. 이때 검색결과와 번역결과를 보여주는 웹뷰가 사전웹뷰이다.
* 유저는 화면 하단의 바텀시트에서 단어/문장 버튼을 클릭하여 단어검색모드와 문장번역모드를 선택할 수 있다.
* 2018-02-05 현재 단어검색모드와 문장번역모드 간 변경이 되지 않아 해결 중이다.
*
* 2018-02-06 단어검색모드일 때, 검색한 단어의 뜻을 바로 알 수 있도록 하는 메소드 getMeaning()을 추가했다
* 단어의 뜻은 spanishdict.com에서 가져오며 영어로 되어 있다.
* 스페인어의 단어는 원형을 유지 하지 않는 경우가 많은데(eg. echarme, bailando, salga) 네이버에서 예시의 단어들을 검색할 경우
* 한글 뜻이 나오지 않거나 나오더라도 부정확한 경우가 다반사인 데 반해, spanishdict에서 해당 단어들을 검색하면
* 단어의 원형을 정확히 골라내어 정확한 뜻을 보여주기 때문이다
* 스페인어를 공부하는 사람들 중에는 영어도 어느 정도 할 줄 아는 사람도 많고,
* 영어와 스페인어 단어들이 비슷한 형태를 띄는 경우가 많아서
* 영어 뜻을 보여주어도 좋을 것이라고 생각했다.
* */


public class WebviewActivity extends AppCompatActivity{

    //뒤로 가기 두번-> 종료
    private BackPressCloseHandler backPressCloseHandler;

    //로그 용
    private final String TAG = "웹뷰액티비티WebviewActivity";


    //웹뷰
    private String SITE_URL;
    private WebView mWebView;
    private ProgressBar mProgressBar; //웹뷰에서 웹페이지 로딩 시 로딩상황 보여주는 horizontal 프로그레스 바

    //웹뷰로 불러오는 웹페이지의 텍스트를 클릭하기 위해 import한 라이브러리
    private TextSelectionSupport mTextSelectionSupport;

    //유저가 웹페이지에서 클릭한 텍스트
    public String selectedWord;

    private WebView webView_dic; //사전페이지를 보여주는 또 다른 웹뷰

    //anchor bottom sheet
    private AnchorBottomSheetBehavior<View> anchorBottomSheetBehavior;

    //anchorbottomsheet_dictionary.xml 사전 바텀시트 xml 파일의 뷰
    private TextView tv_wordMode, tv_sentenceMode; //단어검색모드(디폴트), 문장번역모드
    //어떤 모드가 선택되었는가 구별하기 위해서
    private boolean wordMode;  //단어검색모드가 디폴트이므로 true로 해놨었으나 뭔가..문제가 생겨서 false로 변경
    private boolean sentenceMode;
    private boolean originalMode, koreanMode, enlishMode, simpleMode;
    // simpleMode: 단어뜻을 보여주는 editText를 지운다
    //originalMode : 바텀시트에서 단어뜻을 한국어와 영어로 모두 볼 수 있다
    //KoreanMode : 단어뜻을 한국어로 본다
    //EnlishMode: 단어뜻을 영어로 본다
    private int clicked = 1; //btn_simpleMode가 클릭되는 횟수를 카운트한다 //처음 클릭하자마자 모드 변경하고 싶어서 clicked의 초기값을 0->1로 바꿨다

    private LinearLayout layout_meaning; //et_meaning, btn_cancelMeaning를 포함하는 레이아웃

    private Button btn_save, btn_cancelSearch, btn_cancelMeaning; //단어장에 저장버튼과 단어 검색/단어 뜻 editText뷰에서 입력된 글자 삭제하는 버튼
    //private String naverDicQuery = "http://spdic.naver.com/#/search?query=";
    private Button btn_simpleMode; //사전바텀시트를 심플하게 바꾼다(이하 심플모드), 영어뜻을 보여주는 et_meaningEn을 gone시킴

    private EditText et_search, et_meaning; //단어 검색하는 창과 단어 뜻이 보이는 창
    private EditText et_meaningEn; //단어 뜻을 영어로 보여주기 위해

    //바텀시트의 에딧텍스트에서(et_search) 직접 단어를 검색할 때 필요


    //단어검색or문장번역 두 가지 모드에 따라서 해당 변수에 할당될 주소가 바뀜
    private String DICTIONARY; //"http://spdic.naver.com/#/search?query="; or "https://translate.google.co.kr/?hl=ko#es/ko/";
    private String SCRIPT_FILE; //android.selection.word or sentence.js


    //소프트 키보드를 제어하기 위한 변수
    private InputMethodManager inputMethodManager;


    //유저가 선택한 텍스트의 사전 검색 결과를 웹뷰에 보여주기 위한 쓰레드
    //메인웹뷰로 스페인어 사이트를 보여주고 있고, 바텀시트에서는 웹뷰를 통해 사전 검색결과 등을 보여주는데
    //이때 쓰레드가 필요
    ShowResultThread showResultThread; //유저가 클릭한 텍스트의 검색/번역결과를 불러옴
    SearchResultThread searchResultThread; //유저가 et_search 에딧텍스트에 직접 검색한 것의 결과를 불러옴

    //검색어 및 단어장에 저장할 단어 원형과 단어의 뜻
    String searchWord; //검색어
    String wordToSaveKor, wordToSaveEn; //검색어를 각각 traduction.sensagent.com(스페인어-한국어)/ spanishdict.com(스페인어-영어)에 검색했을 때 식별되는 단어의 원형
    String meaningToSaveKor, meaningToSaveEn; //검색어를 각각 traduction.sensagent.com(스페인어-한국어)/ spanishdict.com(스페인어-영어)에 검색했을 때 검색결과(단어 뜻)

    //서버에서 날아오는 메시지값
    String msgFromServer;

    Button btn_goHome; //웹뷰에서 바로 메인화면으로 갈 수 있는 버튼
    Button btn_bookmark; //현재 페이지를 저장할 수 있는 버튼
    View view_bookmark; //북마크 다이얼로그
    EditText et_Url, et_titleUrl; //다이얼로그 내 위젯
    String titleUrl, currentUrl; //북마크 정보 담을 변수
    String username, email; //유저정보 담을 변수



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        //인텐트에서 사이트 주소 정보 가져오기
        //music, news, others fragment에서 가져온다
        Intent intent = getIntent();
        Log.d("url", intent.getExtras().getString("url"));

        //인텐트에서 받아온 사이트 주소
        SITE_URL = intent.getExtras().getString("url");


        final User user = SharedPrefManager.getmInstance(this).getUser(); // 현재 로그인 된 유저정보
        username = user.getUsername();
        email = user.getEmail();

        //객체 initialize
        initViews();


////////////////////////////////////////////////////////////////////////////////

        //바텀시트 내 단어/문장 검색창에 쓰인 문자 지우기
        btn_cancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_search.getText().clear();
            }
        });


        originalMode = true; //디폴트모드, 단어 뜻을 영어와 한국어로 모두 본다
        koreanMode = false;  //한글모드, 단어 뜻을 한국어로만 본다
        enlishMode = false;  //영어모드, 단어 뜻을 영어로만 본다
        simpleMode = false;  //심플모드, 단어 뜻을 네이버 스페인어 사전으로만 확인한다

        //사전을 심플하게 바꾸기
        //버튼을 한번도 안 누른 상태에선 오리지널모드
        //버튼을 한번 누르면 한국어 뜻만 보인다(영어뜻 gone)
        //버튼을 두번 누르면 영어뜻만 보인다(한국어뜻 gone)
        //버튼을 세번 누르면 한국어뜻/영어뜻 모두 보이지 않는다
        //버튼을 네번 누르면 오리지널 모드로 돌아간다
        btn_simpleMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(clicked%4 == 0) {

                    Toast.makeText(WebviewActivity.this, "스페인어-한국어, 영어 뜻을 모두 확인합니다.", Toast.LENGTH_SHORT).show();
                    originalMode = true;
                    koreanMode = false;
                    enlishMode = false;
                    simpleMode = false;
                    et_meaning.setVisibility(View.VISIBLE);
                    et_meaningEn.setVisibility(View.VISIBLE);
                    clicked++;

                } else if(clicked%4==1) {

                    Toast.makeText(WebviewActivity.this, "스페인어-한국어 뜻만 확인합니다.", Toast.LENGTH_SHORT).show();
                    originalMode = false;
                    koreanMode = true;
                    enlishMode = false;
                    simpleMode = false;

                    et_meaning.setVisibility(View.VISIBLE);
                    et_meaningEn.setVisibility(View.GONE);
                    clicked++;

                } else if(clicked%4==2) {

                    Toast.makeText(WebviewActivity.this, "스페인어-영어 뜻만 확인합니다.", Toast.LENGTH_SHORT).show();
                    originalMode = false;
                    enlishMode = true;
                    koreanMode = false;
                    simpleMode = false;

                    et_meaning.setVisibility(View.GONE);
                    et_meaningEn.setVisibility(View.VISIBLE);
                    clicked++;

                } else if(clicked%4==3) {

                    Toast.makeText(WebviewActivity.this, "심플모드입니다.", Toast.LENGTH_SHORT).show();
                    originalMode = false;
                    koreanMode = false;
                    enlishMode = false;
                    simpleMode = true;


                    //단어 뜻에 적혀있던 글씨를 지워준다. 이렇게 안 하면 클릭 5번 했을 때 첫번째 검색했던 단어의 뜻이 그대로 남아있다.
                    //음. 그런데 굳이 원래 있던 뜻을 지워줄 필요가 없을 것 같단 생각도 들어서, 2018-02-09 주석처리
//                    et_meaning.setText("");
//                    et_meaningEn.setText("");

                    et_meaning.setVisibility(View.GONE);
                    et_meaningEn.setVisibility(View.GONE);
                    clicked++;
                }

            }//onClick
        });//리스너

        if(originalMode) {

            originalMode = true;
            koreanMode = false;
            enlishMode = false;
            simpleMode = false;

            et_meaning.setVisibility(View.VISIBLE);
            et_meaningEn.setVisibility(View.VISIBLE);

        } else if(koreanMode) {

            originalMode = false;
            koreanMode = true;
            enlishMode = false;
            simpleMode = false;

            et_meaning.setVisibility(View.VISIBLE);
            et_meaningEn.setVisibility(View.GONE);

        } else if(enlishMode) {

            originalMode = false;
            koreanMode = false;
            enlishMode = true;
            simpleMode = false;

            et_meaning.setVisibility(View.GONE);
            et_meaningEn.setVisibility(View.VISIBLE);

        } else if(simpleMode) {

            originalMode = false;
            koreanMode = false;
            enlishMode = false;
            simpleMode = true;


            et_meaning.setVisibility(View.GONE);
            et_meaningEn.setVisibility(View.GONE);
        }


        //직접 검색하기
        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                switch (actionId) {

                    case EditorInfo.IME_ACTION_SEARCH :
                        //여기서 유저가 입력한 단어의 검색결과를 가져오는 또 다른 핸들러 필요
                        String text = et_search.getText().toString();
                        Toast.makeText(WebviewActivity.this, "Search "+text, Toast.LENGTH_SHORT).show();
                        Message msgTosend = Message.obtain();
                        msgTosend.obj = text;
                        searchResultThread.searchHandler.sendMessage(msgTosend);
                        //소프트 키보드를 내려야 웹뷰가 보임
                        //키보드를 숨긴다!
                        HideKeyboard();
                        break;

                }//switch
                return true;
            }//onEditorAction
        });//OnEditorActionListener
////////////////////////////////////////////////////////////////////////////////
        //단어장에 단어 저장하기
        //검색한 단어가 있을 경우에만 단어를 저장한다
        //즉, 단어검색창 혹은 단어뜻을 보여주는 에딧텍스트가 비어있다면 아무 것도 저장하지 않는다
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    if(originalMode) {

                        Log.e("오리지널모드에서", "한국어: "+wordToSaveKor+"/영어: "+wordToSaveEn);

                        //검색창이 비어있다면
                        if(et_search.getText().toString().equals("")) {
                            Snackbar.make(v, "저장할 단어가 없습니다.", Snackbar.LENGTH_SHORT).show();
                        //스페인어-영어뜻 보여주는 에딧텍스트만 비어있다면
                            //TODO 검색어, 단어원형, 스페인어-한국어 뜻만 저장한다
                        } else if (!et_search.getText().toString().equals("") && !et_meaning.getText().toString().equals("") && et_meaningEn.getText().toString().equals("")) {
                            Snackbar.make(v, "스페인어-한국어 사전의 검색결과만 저장합니다.",Snackbar.LENGTH_LONG).setAction("확인", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.e("검색한 단어 뜻 Kor", wordToSaveKor+":"+meaningToSaveKor);

                                    //스-영 뜻이 없으면 스-한 뜻 넣어줘야 함
                                    if(wordToSaveEn==null) {
                                        SaveWord(searchWord, wordToSaveKor, meaningToSaveKor, 1);
                                    } else {
                                        SaveWord(searchWord, wordToSaveEn, meaningToSaveKor, 1); //영어사전 결과가 더 정확해서, 단어 원형은 spanishdict.com에서 가져온 걸 넣어준다
                                    }
                                }//onClick
                            }).show();

                        //스페인어-한국어 뜻 보여주는 에딧텍스트만 비어있다면
                            //TODO 검색어, 단어원형, 스페인어-영어 뜻만 저장한다
                        }  else if (!et_search.getText().toString().equals("") && et_meaning.getText().toString().equals("") && !et_meaningEn.getText().toString().equals("")) {
                            Snackbar.make(v, "스페인어-영어 사전의 검색결과만 저장합니다.",Snackbar.LENGTH_LONG).setAction("확인", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //스-영 뜻이 없으면 스-한 뜻 넣어줘야 함
                                    if(wordToSaveEn==null) {
                                        SaveWord(searchWord, wordToSaveKor, meaningToSaveEn, 0);
                                    } else {
                                        SaveWord(searchWord, wordToSaveEn, meaningToSaveEn, 0); //영어사전 결과가 더 정확해서, 단어 원형은 spanishdict.com에서 가져온 걸 넣어준다
                                    }
                                   // SaveWord(searchWord, wordToSaveEn, meaningToSaveEn, 0);
                                    Log.e("검색한 단어와 뜻 En/eng mode", wordToSaveEn+":"+meaningToSaveEn);
                                }//onClick
                            }).show();

                        } else { //검색어, 스페인어-한국어 뜻, 스페인어-영어 뜻이 모두 다 editText에 입력되어 있다면
                            //TODO 검색어, 단어원형, 스페인어-한국어/영어 뜻 모두 저장한다
                            //스-영 뜻이 없으면 스-한 뜻 넣어줘야 함
                            if(wordToSaveEn==null) {
                                SaveWord(searchWord, wordToSaveKor, meaningToSaveKor, meaningToSaveEn);
                            } else {
                                SaveWord(searchWord, wordToSaveEn, meaningToSaveKor, meaningToSaveEn); //영어사전 결과가 더 정확해서, 단어 원형은 spanishdict.com에서 가져온 걸 넣어준다
                            }
                           // SaveWord(searchWord, wordToSaveEn, meaningToSaveKor, meaningToSaveEn);\
                            Log.e("검색한 단어 뜻 En", wordToSaveEn+":"+meaningToSaveEn);

                        }

                    } else if(koreanMode) {

                        //검색창과 스페인어-한국어뜻 보여주는 에딧텍스트가 비어있다면
                        if(et_search.getText().toString().equals("") || et_meaning.getText().toString().equals("")) {
                            Snackbar.make(v, "저장할 단어/뜻 (이)가 없습니다.", Snackbar.LENGTH_SHORT).show();
                        } else{
                            //TODO 검색어, 단어원형, 스페인어-한국어 뜻만 저장한다
                            SaveWord(searchWord, wordToSaveKor, meaningToSaveKor, 1);
                            Log.e("검색한 단어 뜻 Kor", wordToSaveKor+":"+meaningToSaveKor);
                        }

                    } else if(enlishMode) {
                        //TODO 검색어, 단어원형, 스페인어-영어 뜻만 저장한다
                        //검색창과 스페인어-영어뜻 보여주는 에딧텍스트가 비어있다면
                        if(et_search.getText().toString().equals("") || et_meaningEn.getText().toString().equals("")) {
                            Snackbar.make(v, "저장할 단어/뜻 (이)가 없습니다.", Snackbar.LENGTH_SHORT).show();
                        } else {
                            SaveWord(searchWord, wordToSaveEn, meaningToSaveEn, 0);
                            Log.e("검색한 단어 뜻 En", wordToSaveEn+":"+meaningToSaveEn);
                        }

                    } else if(simpleMode) {
                        //TODO 검색어만 저장한다
                        SaveWord(searchWord, wordToSaveKor, meaningToSaveKor, 1);
                        Snackbar.make(v, msgFromServer, Snackbar.LENGTH_SHORT).show();
                    }
                //단어 저장 후에는 변수 초기화 필요
//                wordToSaveEn="";
//                meaningToSaveKor="";
//                meaningToSaveEn="";
            }//onClick
        });//Listener



////////////////////////////////////////////////////////////////////////////////
        //기본값, 즉 단어검색/문장번역모드 중 디폴트모드는 단어검색 모드이다.
        wordMode = true;

        //단어 검색/ 문장 선택 모드 선택을 위해서 텍스트 객체에 클릭리스너를 달아준다
        tv_wordMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //일단 모드가 선택되면 기존에 입력되어있던(있었을지도 모를) 검색어를 지워준다(검색창 초기화).
                et_search.setText("");

                //웹뷰 리로드
                //단어검색용 자바스크립트 파일을 삽입하기 위해
                mWebView.reload();

                //단어 뜻을 보여주는 에딧텍스트를 담은 레이아웃을 make visible
                layout_meaning.setVisibility(View.VISIBLE);


                //단어 뜻을 어떤 언어로 볼 것인지 선택하는 버튼을 보이게 한다
                btn_simpleMode.setVisibility(View .VISIBLE);

                //단어 저장 버튼을 보이게 한다
                btn_save.setVisibility(View.VISIBLE);

                wordMode = true;
                Log.e("모드", "sentenceMode: "+sentenceMode+"// wordMode:"+wordMode);
                Toast.makeText(WebviewActivity.this, "단어검색 모드 ", Toast.LENGTH_SHORT).show();
                tv_wordMode.setPaintFlags(tv_wordMode.getPaintFlags()| Paint.FAKE_BOLD_TEXT_FLAG);
                //ㄴ어떤 모드가 선택되어 있는지 시각적으로 구별할 수 있도록 글씨를 굵게 만들어준다
                tv_sentenceMode.setPaintFlags(0);
                //ㄴ단어검색모드가 선택되었으므로 볼드체로 되어있던 '문장'버튼을 초기화한다
                DICTIONARY = "http://spdic.naver.com/#/search?query=";
                //단어검색모드에서는 네이버사전의 단어검색결과를 불러온다
                //replaceScriptFile(mWebView, "android.selection.word.js", "android.selection.sentence.js");
                SCRIPT_FILE = ""; //초기화
                SCRIPT_FILE = "android.selection.word.js";
                //단어선택 메소드가 포함된 자바스크립트 파일을 remote webpage에 삽입한다
                injectScriptFile(mWebView,"android.selection.sentence.js");
            }//onClick
        });//setOnClickListener

////////////////////////////////////////////////////////////////////////////////
        tv_sentenceMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //문장번역모드가 선택되었다
                et_search.setText("");
                //기존에 입력되어있던(있었을지도 모를) 검색어를 지워준다(검색창 초기화).

                et_meaning.setText("");
                et_meaningEn.setText("");
                //기존에 입력되어있던(있었을지도 모를) 단어검색결과를 지워준다(검색창 초기화).

                //웹뷰 리로드
                //문장번역용 자바스크립트 파일을 삽입하기 위해
                mWebView.reload();

                //단어 뜻을 보여주는 에딧텍스트를 담은 레이아웃을 없앤다
                layout_meaning.setVisibility(View.GONE);

                //단어 뜻을 어떤 언어로 볼 것인지 선택하는 버튼을 없앤다
                btn_simpleMode.setVisibility(View.GONE);

                //단어 저장 버튼을 없앤다
                btn_save.setVisibility(View.GONE);


                Toast.makeText(WebviewActivity.this, "문장번역 모드", Toast.LENGTH_SHORT).show();

                wordMode = false; //단어검색모드가 아니라고 선언한다
                Log.e("모드", "sentenceMode: "+sentenceMode+"// wordMode:"+wordMode);

                tv_sentenceMode.setPaintFlags(tv_sentenceMode.getPaintFlags()| Paint.FAKE_BOLD_TEXT_FLAG);
                //ㄴ어떤 모드가 선택되어 있는지 시각적으로 구별할 수 있도록 글씨를 굵게 만들어준다
                tv_wordMode.setPaintFlags(0);
                //ㄴ문장번역모드가 선택되었으므로 볼드체로 되어있던 '단어'버튼을 초기화한다
                DICTIONARY = "https://translate.google.co.kr/?hl=ko#es/ko/";
                //문장번역모드에서는 구글번역페이지의 번역결과를 불러온다
                SCRIPT_FILE  = ""; //초기화
                SCRIPT_FILE = "android.selection.sentence.js";
                //문장선택 메소드가 포함된 자바스크립트 파일을 remote webpage에 삽입한다
                injectScriptFile(mWebView,"android.selection.sentence.js");
                //replaceScriptFile(mWebView, "android.selection.sentence.js", "android.selection.word.js");
            }//onClick
        });//setOnClickListener


        if(wordMode) { //단어검색 모드면
            //아 왜 불린이 안 먹지..
            tv_wordMode.setPaintFlags(tv_wordMode.getPaintFlags()| Paint.FAKE_BOLD_TEXT_FLAG); //볼드체
            tv_sentenceMode.setPaintFlags(0);
            DICTIONARY = "http://spdic.naver.com/#/search?query=";
            SCRIPT_FILE = ""; //초기화
            SCRIPT_FILE = "android.selection.word.js";
            injectScriptFileBelow(mWebView,"android.selection.word.js");

        } else { //단어검색 모드가 아니면
            tv_sentenceMode.setPaintFlags(tv_sentenceMode.getPaintFlags()| Paint.FAKE_BOLD_TEXT_FLAG);
            tv_wordMode.setPaintFlags(0);
            DICTIONARY = "https://translate.google.co.kr/?hl=ko#es/ko/";
            injectScriptFileBelow(mWebView,"android.selection.sentence.js");
            SCRIPT_FILE  = ""; //초기화
            SCRIPT_FILE = "android.selection.sentence.js";
            //replaceScriptFile(mWebView, "android.selection.sentence.js", "android.selection.word.js");
        }

////////////////////////////////////////////////////////////////////////////////
        //웹뷰
        WebView.setWebContentsDebuggingEnabled(true); //웹컨텐츠 디버깅이 가능하도록 만든다
        WebSettings webSettings = mWebView.getSettings();
        //웹뷰 속성 설정
        //출처: http://it77.tistory.com/117 [시원한물냉의 사람사는 이야기]
        mWebView.requestFocusFromTouch(); // Allow for touching selecting/deselecting data series

        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowContentAccess(true);
        //mWebView.clearCache(true); //정확히 어떤 기능을 하는지 몰라서..일단 주석처리
        //mWebView.clearHistory();   //상동
        webSettings.setAllowContentAccess(true);
        webSettings.setJavaScriptEnabled(true); // javascript를 실행할 수 있도록 설정
        webSettings.setJavaScriptCanOpenWindowsAutomatically (true);   // javascript가 window.open()을 사용할 수 있도록 설정
        //webSettings.setSupportZoom(true); // 확대,축소 기능을 사용할 수 있도록 설정--이건 써도 웹뷰 내에서 롱클릭이 되지만, 아래에 있는 setBuiltInZoomControls가 없으면 소용이 없는 것 같아 해제
        //webSettings.setBuiltInZoomControls(true); // 안드로이드에서 제공하는 줌 아이콘을 사용할 수 있도록 설정, 그러나 이걸 쓰면...롱클릭이 먹히지 않음
        webSettings.setSupportMultipleWindows(false); // 여러개의 윈도우를 사용할 수 있도록 설정
        webSettings.setBlockNetworkImage(false); // 네트워크의 이미지의 리소스를 로드하지않음
        webSettings.setLoadsImagesAutomatically(true); // 웹뷰가 앱에 등록되어 있는 이미지 리소스를 자동으로 로드하도록 설정
        webSettings.setUseWideViewPort(true); // wide viewport를 사용하도록 설정
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 웹뷰가 캐시를 사용하지 않도록 설정


        //http://ywook.tistory.com/17
        mWebView.setWebViewClient(new WebViewClient(){
            //로드가 시작되면 진행 다이얼로그 진행
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }//onPageStarted

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //shouldOverrideUrlLoading() 메소드를 구현하지 않으면 웹뷰 내 링크 터치할 때 연결 프로그램(기본 브라우저, 크롬 등) 설정창이 뜸.
                return super.shouldOverrideUrlLoading(view, request);

            }//shouldOverrideUrlLoading

            @Override
            public void onScaleChanged(WebView view, float oldScale, float newScale) {
                super.onScaleChanged(view, oldScale, newScale);
                mTextSelectionSupport.onScaleChanged(oldScale, newScale); //텍스트 클릭 인식하기 위해
            }//onScaleChanged

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(mWebView, url);
                //Toast.makeText(WebviewActivity.this, "페이지 로딩 끝", Toast.LENGTH_SHORT).show();

                injectScriptFile(view, SCRIPT_FILE);
                //injectScriptFile(view, SCRIPT_FILE); //단어모드인지 문장모드인지에 따라 달라짐
                //injectScriptFile(view, "android.selection.js"); //웹뷰마커 라이브러리에 포함된 자바스크립트 파일
                injectScriptFile(view, "jquery-1.8.3.js"); //웹뷰마커 라이브러리에 포함된 자바스크립트 파일
                injectScriptFile(view, "rangy-core-new.js"); //웹뷰마커 라이브러리에 포함된 자바스크립트 파일
                injectScriptFile(view, "rangy-serializer-new.js"); //웹뷰마커 라이브러리에 포함된 자바스크립트 파일

                injectScriptFileBelow(view, "android.selection.word.js");
                //처음에 단어검색 스크립트를 심을 땐 html의 헤드 첫번째 부분이 아닌 세번째 부분에 넣어준다(세번째로 한 건 그냥 임의로 한 것임 의미없음. 첫번째만 아니면 됨)
                //이렇게 하면 모드를 변경할 때마다 페이지를 reload해서 단어검색 혹은 문장번역 스크립트를 헤드의 제일 첫번째 부분에 넣어주기 때문에 스크립트 간 충돌이 없다 
            }//onPageFinished
        });//setWebViewClient

        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                // Update the progress bar with page loading progress
                mProgressBar.setProgress(newProgress);


            }//onProgressChanged

            //****웹에서 띄우는 팝업창 같은 것을 보여주기 위해****
            //신규 생성한 WebChromeClient에서 Alert이나 Confirm이 발생한 경우 해당 내용을 AlertDialog로 처리하도록 구현하는 것이 요지로
            // 이렇게 처리하지 않는 경우 Popup창이 발생해도 보이지 않아 화면이 동작하지 않는 것으로 착각할 수 있음.
            //출처: http://zeany.net/5?category=666373 [소소한 IT 이야기]
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(WebviewActivity.this);
                alertDialog
                        .setTitle("Alert")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }//onJsAlert

            @Override
            public boolean onJsConfirm(WebView view, String url, String message,
                                       final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirm")
                        .setMessage(message)
                        .setPositiveButton("Yes",
                                new AlertDialog.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("No",
                                new AlertDialog.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.cancel();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }//onJsConfirm

        });//setWebChromeClient


        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //ㄴ웹뷰 로딩 속도 높이기 위해
        //ㄴ참고: http://blog.naver.com/PostView.nhn?blogId=spiderwort&logNo=220841057193&categoryNo=28&parentCategoryNo=0&viewDate=&currentPage=1&postListTopCurrentPage=1&from=postView

        //마찬가지로 웹뷰의 로딩 속도 높이기 위해
        //뷰 가속: 참고) http://www.masterqna.com/android/64059/webview로는-브라우저-성능을-낼수-없을까요
        if (Build.VERSION.SDK_INT >= 19) {

            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null); //이것도 속도/성능 개선의 일환으로 넣었는데 정확한 로직은 모르겟..

        } else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        //hoyhablamos.com에서 팟캐스트 재생이 안 됨.
        //오류 메시지:
        // (index):1 Mixed Content: The page at 'https://hoyhablamos.com/261-noticias-espanol/' was loaded over HTTPS,
        // but requested an insecure video 'http://files.ivoox.com/download/23502210'.
        // This request has been blocked; the content must be served over HTTPS.
        //이를 해결하기 위해
        //참고: https://stackoverflow.com/questions/28626433/android-webview-blocks-redirect-from-https-to-http
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode( WebSettings.MIXED_CONTENT_ALWAYS_ALLOW );
        }



        //웹뷰 띄우기
        mWebView.loadUrl(SITE_URL);
        ///흠..딱히 속도가 빨라진 건 모르겠기도 하고 기분 탓인지 좀더 빨라진 것 같기도 하고 ㅇㅅㅇ


////////////////////////////////////////////////////////////////////////////////

        //뒤로 가기 두번 -> 종료
        backPressCloseHandler = new BackPressCloseHandler(this);

        //웹페이지에서 클릭한 텍스트를 처리하기 위해 리스너 달아주기
        mTextSelectionSupport = TextSelectionSupport.support(this, mWebView);
        mTextSelectionSupport.setSelectionListener(new TextSelectionSupport.SelectionListener() {
            @Override
            public void startSelection() {
                //롱클릭으로 텍스틑 클릭한 첫번째에 뜨고, 두번째부턴 안 뜸
            }//startSelection

            @Override
            public void selectionChanged(String selectedText) {



                if(wordMode) { //단어뜻을 보여주는 메소드는 단어검색모드일 때만 사용한다

                    //검색어 변수에 검색어를 담는다
                    searchWord = selectedText;
                    Log.e("롱클릭한 검색어/searchWord", searchWord);

                    if(originalMode) { //오리지널모드일 땐 스페인어-한국어/영어 뜻 다 보여준다

                        getOriginalWordandMeaning(selectedText);
                        getMeaningEn(selectedText);

                    } else if(koreanMode || simpleMode) { //한글모드일 땐 스페인어-한국어 뜻만 보여준다

                        getOriginalWordandMeaning(selectedText);

                    } else if (enlishMode) { //영어모드일 땐 스페인어-영어 뜻만 보여준다

                        getMeaningEn(selectedText);
                    }

                }


                //메인웹뷰-웹페이지에 있는 텍스트를 롱클릭 시
                //바텀시트에 있는 사전/번역사이트로 텍스트를 보낸다
                Message msgTosend = Message.obtain();
                msgTosend.obj = selectedText;
                showResultThread.handler.sendMessage(msgTosend);

                //텍스트를 클릭하면 바텀시트가 위로 올라온다
                anchorBottomSheetBehavior.setState(AnchorBottomSheetBehavior.STATE_ANCHORED);

            }//selectionChanged

            @Override
            public void endSelection() {
                //Toast.makeText(MainActivity.this, "endSelection", Toast.LENGTH_SHORT).show();
                //텍스틑 이외의 곳을 터치할 때
                //텍스트라고 해도 롱클릭 아니라 숏클릭할 때
                anchorBottomSheetBehavior.setState(AnchorBottomSheetBehavior.STATE_COLLAPSED);
            }//endSelection
        });//setSelectionListener

        ////////////////////////////////////////////////////////////////////////////////

        //사전웹뷰-바텀시트에서 사전/번역페이지를 보여주는 웹뷰(webView_dic)
        WebSettings webSettings_dic = webView_dic.getSettings();
        webSettings_dic.setJavaScriptEnabled(true);

        webSettings_dic.setAllowFileAccessFromFileURLs(true);
        webSettings_dic.setAllowUniversalAccessFromFileURLs(true);
        webView_dic.clearCache(true);
        webView_dic.clearHistory();
        webView_dic.getSettings().setAllowContentAccess(true);


        webView_dic.setWebContentsDebuggingEnabled(true);

        WebChromeClient client2 = new WebChromeClient();
        webView_dic.setWebChromeClient(client2);
        WebView.setWebContentsDebuggingEnabled(true);

        // Allow for touching selecting/deselecting data series
        webView_dic.requestFocusFromTouch();
        webView_dic.setWebViewClient(new WebViewClient()); //이거 안 넣어주면 외부 브라우저로 링크가 열림
        webView_dic.loadUrl(DICTIONARY); //사전웹뷰에는 모드에 따라 네이버사전/구글번역 페이지를 로드한다

        //웹뷰에서 바로 메인페이지로 이동한다
        btn_goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }//onClick
        });

        //웹뷰 우측 하단에 북마크 버튼을 누르면 현재 페이지의 주소를 받아와 저장할 수 있는 액티비티로 이동한다
        btn_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!SharedPrefManager.getmInstance(getApplicationContext()).isLoggedIn()) {
                    Toast.makeText(WebviewActivity.this, "북마크 기능은 로그인 후 이용하실 수 있습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    //바텀시트가 내려져 있을 때만 북마크 저장 다이얼로그를 띄운다
                    if(anchorBottomSheetBehavior.getState() == AnchorBottomSheetBehavior.STATE_COLLAPSED) {
                        BookmarkPopup();
                    }
                }
            }//onClick
        });

    }//onCreate
//
//    //단어장에 단어를 저장하기 위한 메소드, 검색어만 저장할 때
//    //2018-02-27 폐기/ 데이터베이스 관리가 어려워서 검색어만 저장할 수 있는 기능은 지원하지 않기로 결정
//    private void SaveWord(String searchWord) {
//        //현재 유저는 뉴규?
//        final User user = SharedPrefManager.getmInstance(this).getUser();
//
//        //일단 유저네임과 검색어만 넣는다
//        String username = user.getUsername();
//
//        ApiService apiService = ApiClient.getClient().create(ApiService.class);
//        Call<ResponseModel> call = apiService.saveWord(username, searchWord);
//        call.enqueue(new Callback<ResponseModel>() {
//            @Override
//            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
//                ResponseModel saveWordResponseModel = response.body();
//
//                //check the status code
//                if(saveWordResponseModel.getStatus() == 1 ) {
//                    //성공
//                    Toast.makeText(WebviewActivity.this, "success", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(WebviewActivity.this, "failed", Toast.LENGTH_SHORT).show();
//                }
//            }//onResponse
//
//            @Override
//            public void onFailure(Call<ResponseModel> call, Throwable t) {
//                Toast.makeText(WebviewActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
//            }//onFailure
//        });
//    }//SaveWord


    //현재 웹 페이지 저장 전 띄우는 다이얼로그
    private void BookmarkPopup() {

        currentUrl = mWebView.getUrl(); //현재 유저가 보고 있는 웹페이지 주소
        et_Url.setText(currentUrl);
        et_Url.setSelection(currentUrl.length()); //커서를 문자열 끝에 위치시킴

        // 북마크 버튼을 누르면, 북마크 다이얼로그를 띄운다.
        // 현재 웹페이지 주소를 자동으로 받아와서 URL 칸에 뿌려준 후, 유저에게 제목을 지정하게끔 한다. 제목은 필수.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WebviewActivity.this);
        alertDialogBuilder.setTitle("북마크");
        alertDialogBuilder.setView(view_bookmark);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                            //https://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
                            //Do nothing here because we override this button later to change the close behaviour.
                            //However, we still need this because on older versions of Android unless we
                            //pass a handler the button doesn't get instantiated
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((ViewGroup) view_bookmark.getParent()).removeView(view_bookmark);
                        dialog.cancel();
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        //((ViewGroup) view_bookmark.getParent()).removeView(view_bookmark);
        //Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleUrl = et_titleUrl.getText().toString();
                Log.e("제목", titleUrl);
                if(titleUrl.isEmpty()){
                    titleUrl = "[제목없음]";
                    //Toast.makeText(WebviewActivity.this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    currentUrl = et_Url.getText().toString(); //유저가 새로 값을 입력했다면 그걸 저장해준다
                    SaveBookmark(username, email, currentUrl, titleUrl);
                    ((ViewGroup) view_bookmark.getParent()).removeView(view_bookmark);
                    alertDialog.cancel();
                }
            }//onClick
        });
    }//BookmarkPopup

    //저장할 웹페이지 정보를 서버로 보낸다
    private void SaveBookmark(String username, String email, String url, String title) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseModel> call = apiService.saveBookmark(username, email, url, title);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                ResponseModel responseModel = response.body();
                Toast.makeText(WebviewActivity.this, responseModel.getMessage(), Toast.LENGTH_SHORT).show();
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(WebviewActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }//onFailure
        });
    }//SaveBookmark



    //단어장에 단어를 저장하기 위한 메소드, 검색어, 단어원형, 단어 뜻 하나만 저장할 때
    //int lang은 단어뜻이 한국어(1)인지 영어(0)인지 구별하기 위한 변수
    private void SaveWord(String searchWord, String originalWord, String meaning, int lang) {
        //현재 유저는 뉴규?
        final User user = SharedPrefManager.getmInstance(this).getUser();

        //유저네임
        String username = user.getUsername();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseModel> call = apiService.saveWord(username, searchWord, originalWord, meaning, lang);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                ResponseModel responseModel = response.body();
                msgFromServer = responseModel.getMessage(); //서버가 전달한 메시지를 스낵바로 띄워줄 메시지변수에 넣는다.
                                                            //이하는 원래 있던 코드인데...일단 삭제보류 2018-03-09
                Log.e("서버메시지", msgFromServer);
//                if(responseModel.getStatus() == 1 ) {
//                    //성공
//                    Toast.makeText(WebviewActivity.this, "단어장에 등록되었습니다.", Toast.LENGTH_SHORT).show();
//
//                } else if(responseModel.getStatus() == 0) {
//
//                    Toast.makeText(WebviewActivity.this, "이미 등록된 단어입니다.", Toast.LENGTH_SHORT).show();
//                }  else {
//                    Toast.makeText(WebviewActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
//                }
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(WebviewActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }//onFailure
        });

    }//SaveWord

    //단어장에 단어를 저장하기 위한 메소드, 검색어, 단어원형, 단어 2개 모두 저장할 때
    private void SaveWord(String searchWord, String originalWord, String meaningKor, String meaningEn) {
        //현재 유저는 뉴규?
        final User user = SharedPrefManager.getmInstance(this).getUser();

        //유저네임
        String username = user.getUsername();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseModel> call = apiService.saveWord(username, searchWord, originalWord, meaningKor, meaningEn);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                ResponseModel responseModel = response.body();
                msgFromServer = responseModel.getMessage(); //서버가 전달한 메시지를 스낵바로 띄워줄 메시지변수에 넣는다.
                Log.e("서버메시지", msgFromServer);
                //이하는 원래 있던 코드인데...일단 삭제보류 2018-03-09
//                if(responseModel.getStatus() == 1 ) {
//                    //성공
//                    Toast.makeText(WebviewActivity.this, "단어장에 등록되었습니다.", Toast.LENGTH_SHORT).show();
//
//                } else if(responseModel.getStatus() == 0) {
//
//                    Toast.makeText(WebviewActivity.this, "이미 등록된 단어입니다.", Toast.LENGTH_SHORT).show();
//                }  else {
//                    Toast.makeText(WebviewActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
//                }
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(WebviewActivity.this, "에러"+t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("에러메시지", t.getMessage());
            }//onFailure
        });

    }//SaveWord



    //웹뷰에 javascript 파일을 inject하기 위한 메소드
    private void injectScriptFile(WebView view, String scriptFile) {

        InputStream input;

        try {
            input = getAssets().open(scriptFile);
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            //스크립트 파일명도 입력시켜야 나중에 구별해서 삭제하거나 다른 걸로 대체할 수 있다

            // String-ify the script byte-array using BASE64 encoding !!!
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var script = document.createElement('script');" +
                    "script.type = 'text/javascript';" +
                    "script.setAttribute('name', '"+scriptFile+"');" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "script.innerHTML = window.atob('" + encoded + "');" +
                    //The innerHTML property sets or returns the HTML content (inner HTML) of an element.
                    //The atob() method decodes a base-64 encoded string.
                    "parent.appendChild(script)" +  //메소드는 한 노드를 특정 부모 노드의 자식 노드 리스트 중 마지막 자식으로 붙임
                    "})()");

            //파일명을 같이 삽입하기 위해 내가 넣은 코드 (참고:http://www.javascriptkit.com/javatutors/loadjavascriptcss2.shtml)
            // "script.setAttribute('name', '"+scriptFile+"');" +
            //뜻: element = script의 name을 scriptFile로 정한다
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }//injectScriptFile



    //웹뷰에 javascript 파일을 inject하기 위한 메소드
    private void injectScriptFileBelow(WebView view, String scriptFile) {

        InputStream input;

        try {
            input = getAssets().open(scriptFile);
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            //스크립트 파일명도 입력시켜야 나중에 구별해서 삭제하거나 다른 걸로 대체할 수 있다

            // String-ify the script byte-array using BASE64 encoding !!!
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(3);" +
                    "var script = document.createElement('script');" +
                    "script.type = 'text/javascript';" +
                    "script.setAttribute('name', '"+scriptFile+"');" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "script.innerHTML = window.atob('" + encoded + "');" +
                    //The innerHTML property sets or returns the HTML content (inner HTML) of an element.
                    //The atob() method decodes a base-64 encoded string.
                    "parent.appendChild(script)" +  //메소드는 한 노드를 특정 부모 노드의 자식 노드 리스트 중 마지막 자식으로 붙임
                    "})()");

            //파일명을 같이 삽입하기 위해 내가 넣은 코드 (참고:http://www.javascriptkit.com/javatutors/loadjavascriptcss2.shtml)
            // "script.setAttribute('name', '"+scriptFile+"');" +
            //뜻: element = script의 name을 scriptFile로 정한다
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }//injectScriptFile


    //2018-02-08 단어검색모드/문장번역모드 변경하는 방법 찾음
    //그러나 혹시 팀장님한테 컨펌 못받을 경우를 대비하여 아래 아이디어 박제 (doesn't work tho)
//    private void replaceScriptFile (WebView view, String newScriptFile, String oldScriptFile) {
//
//        InputStream input;
//
//
//        try {
//            input = getAssets().open(newScriptFile);
//            byte[] buffer = new byte[input.available()];
//            input.read(buffer);
//            input.close();
//
//            //스크립트 파일명도 입력시켜야 나중에 구별해서 삭제하거나 다른 걸로 대체할 수 있다
//
//            // String-ify the script byte-array using BASE64 encoding !!!
//            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
//            view.loadUrl("javascript:(function() {" +
//                    "var parent = document.getElementsByTagName('head').item(0);" +
//                    "var script = document.createElement('script');" +
//                    "script.type = 'text/javascript';" +
//                    "script.setAttribute('name', '"+newScriptFile+"');" +
//                    // Tell the browser to BASE64-decode the string into your script !!!
//                    "script.innerHTML = window.atob('" + encoded + "');" +
//                    //The innerHTML property sets or returns the HTML content (inner HTML) of an element.
//                    //The atob() method decodes a base-64 encoded string.
//                    "var oldScriptFile = document.getElementsByName('"+oldScriptFile+"')"+
//                    "parent.replaceChild('"+oldScriptFile+"','"+newScriptFile+"')" +
//                    "})()");
//
//            //파일명을 같이 삽입하기 위해 내가 넣은 코드 (참고:http://www.javascriptkit.com/javatutors/loadjavascriptcss2.shtml)
//            // "script.setAttribute('name', '"+scriptFile+"');" +
//            //뜻: element = script의 name을 scriptFile로 정한다
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//    }//



    //유저가 클릭한 단어/문장의 검색/번역결과를 보여주기 위해 필요한 핸들러
    public class ShowResultThread extends Thread {
        ShowResultHandler handler;


        private ShowResultThread() {
            handler = new ShowResultHandler();
        }//ShowResultThread

        @Override
        public void run() {
            Looper.prepare();
            Looper.loop();
        }//run
    }//ShowResultThread

    public class ShowResultHandler extends Handler {
        public void handleMessage(Message msg){

            String selectedText = (String) msg.obj;
            Log.e("핸들러에서 찍히는 텍스트", selectedText);
            webView_dic.loadUrl(DICTIONARY+selectedText);
            Log.e("웹뷰에 로드하는 url/ 텍스트없이", DICTIONARY);
            Log.e("웹뷰에 로드하는 url/DICTIONARY+핸들러에 찍히는 텍스트", DICTIONARY+selectedText);
            et_search.setText(selectedText);
        }//handleMessage

    }//ShowResultHandler


    //바텀시트에 있는 에딧텍스트를 통해 직접 단어를 검색할 때 필요한 핸들러
    private class SearchResultThread extends Thread {
        SearchResultHandler searchHandler;


        private SearchResultThread() {
            searchHandler = new SearchResultHandler();
        }//SearchResultThread

        @Override
        public void run() {Looper.prepare();
            Looper.loop();
        }//run
    }//SearchResultThread


    public class SearchResultHandler extends Handler {
        public void handleMessage(Message msg){

            String writtenText = (String) msg.obj;
            webView_dic.loadUrl(DICTIONARY+writtenText);


            if(wordMode) { //단어뜻을 보여주는 메소드는 단어검색모드일 때만 사용한다

                //검색어 변수에 검색어를 담는다
                searchWord = writtenText;
                Log.e("직접입력한 검색어/searchWord", searchWord);


                if(originalMode) { //오리지널모드일 땐 스페인어-한국어/영어 뜻 다 보여준다

                    getOriginalWordandMeaning(writtenText);
                    getMeaningEn(writtenText);

                } else if(koreanMode) { //한글모드일 땐 스페인어-한국어 뜻만 보여준다

                    getOriginalWordandMeaning(writtenText);

                } else if (enlishMode) { //영어모드일 땐 스페인어-영어 뜻만 보여준다

                    getMeaningEn(writtenText);
                }
            }

        }//handleMessage

    }//SearchResultHandler

    //바텀시트에서 et_meaning이라는 에딧텍스트에 검색한 단어의 뜻을 보여주는 메소드
    //단어 뜻은 traduction.sensagent.com에서 크롤링해온다
    public void getOriginalWordandMeaning(final String WORD) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {
                    //.connect():jsoup객체가 하여금 앱과 웹사이트를 연결시킨다
                    //.get()->웹사이트의 컨텐트를 다운받는다
                    // --> 다큐먼트 객체의 인스턴스가 생성된다
                    org.jsoup.nodes.Document doc = Jsoup.connect("http://traduction.sensagent.com/"+WORD+"/es-ko/")
                            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                            .get(); //traduction.sensagent.com에서 유저가 클릭한 단어의 원형(혹은 vamos같이 그 자체로 관용적 표현인 경우 그 자체)과 뜻을 가져오는 인스턴스


                    Element originalWord = doc.select("div.divTranslations:eq(0) > p.entry > span.wording").first(); //단어 원형이 담기는 element
                    Element meaning = doc.select("p.translations").first();    //단어 뜻을 담는 element
                    //Element originalWord, meaning은 모두 사전이 제시하는 여러가지 단어의 원형/뜻 중에서 최상단에 있는 것을 담고 있다


                    if(originalWord !=null && meaning !=null) {
                        //일부 단어 eg.hazte 는 검색결과가 없는데 이런 경우에는 nullpointerException이 뜬다
                        builder.append(originalWord.text()).append(":").append("\t").append(meaning.text());
                        //형태---단어 원형: 단어 뜻

                        //단어장에 저장하기 위해서 담아놓음
                        wordToSaveKor = originalWord.text();
                        meaningToSaveKor = meaning.text();
                        Log.e("검색한 단어1", WORD);
                        Log.e("단어원형 스-한1", wordToSaveKor);
                        Log.e("단어뜻 스-한1", meaningToSaveKor);
                    } else { //결과가 null이면
                        wordToSaveKor="";
                        meaningToSaveKor="";
                        Log.e("단어원형 스-한1 null", wordToSaveKor+"null");
                        Log.e("단어뜻 스-한1 null", meaningToSaveKor+"null");
                    }



                } catch (IOException e) {
                    e.printStackTrace();
                    //스페인어가 아닌 단어를 검색하는 등의 이유로 검색결과가 없을 때 editText에 아무 결과도 넣어주지 않는다
                    //builder.append("Error : ").append(e.getMessage()).append("\n"); //실제 에러메시지
                    builder.append("");
                }

                runOnUiThread(new Runnable() {
                    //웹사이트에서 가져온 링크를 보여주기 위해 UI를 새로고침한다
                    //참고: 별개의 쓰레드에서 UI요소에 영향을주지는 못하므로 runOnUiThread필요
                    @Override
                    public void run() {
                        if(koreanMode || enlishMode || originalMode) { //심플모드일 땐 et_meaning이라는 뷰가 없어진다, 뜻만 가져옴

                            et_meaning.setText(builder.toString());
                        }

                    }//run
                });//runOnUiThread

            }//run
        }).start();//Thread

    }//getOriginalWordandMeaning

    //바텀시트에서 et_meaningEn이라는 에딧텍스트에 검색한 단어의 뜻을 보여주는 메소드
    //단어 뜻은 spanishdict.com에서 영어로 된 것을 가져온다
    //2018-02-09 서한사전, 네이버를 대신할 수 있는 좋은 사이트를 찾았고 크롤링도 돼서 한국어 뜻을 보여줄 수 있게 됨 --> 메소드명 getOriginalWordandMeaning
    //그러나 서영사전도 앞으로 활용할 수도 있기 때문에 해당 메소드를 삭제하는 대신 박제
    //데모 프로젝트는 jsoup2 임
    //2018-02-09(같은 날) 스페인어-한국어 검색결과가 정확하지 않은 경우가 많아, 보완하기 위해 영어뜻 다시 추가 -spanishdict.com은 매우 정확하므로
    //대신 한글,영어,심플모드를 만들어서 유저가 어떤 언어로 뜻을 확인할지 선택할 수 있도록 했다
    public void getMeaningEn(final String WORD){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {
                    //.connect():jsoup객체가 하여금 앱과 웹사이트를 연결시킨다
                    //.get()->웹사이트의 컨텐트를 다운받는다
                    // --> 다큐먼트 객체의 인스턴스가 생성된다
                    //String WORD = et_search.getText().toString(); //검색할 단어
                    org.jsoup.nodes.Document doc = Jsoup.connect("http://www.spanishdict.com/translate/"+WORD)
                            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                            .get();

                    //단어 원형
                    Element originalWord = doc.select("div.source").first();
                    //단어의 뜻은 "http://www.spanishdict.com/translate/검색한 단어"에서 div class=el 에 나옴
                    Elements meanings = doc.select("div.el");

                    if(originalWord != null && meanings != null) {
                    //검색결과가 없을 때를 대비하여
                        for (Element meaning : meanings) {
                            builder.append(originalWord.text()).append(":").append("\t").append(meaning.text());
                            //builder.append(meaning.text()).append("\t");

                            //단어장에 저장하기 위해서 담아놓음
                            wordToSaveEn = originalWord.text();
                            //cf. toString()하니까 html태그가 잔뜩 담김
                            meaningToSaveEn = meaning.text();
                            Log.e("단어원형 스-영", wordToSaveEn);
                            Log.e("단어뜻 스-영", meaningToSaveEn);
                        }
                    } else { //검색결과가 없으면
                        wordToSaveEn = "";
                        meaningToSaveEn = "";
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    //스페인어가 아닌 단어를 검색하는 등의 이유로 검색결과가 없을 때 editText에 아무 결과도 넣어주지 않는다
                    //builder.append("Error : ").append(e.getMessage()).append("\n"); //실제 에러메시지
                    builder.append("");
                }

                runOnUiThread(new Runnable() {
                    //웹사이트에서 가져온 링크를 보여주기 위해 UI를 새로고침한다
                    //참고: 별개의 쓰레드에서 UI요소에 영향을주지는 못하므로 runOnUiThread필요
                    @Override
                    public void run() {
                        et_meaningEn.setText(builder.toString());
                    }//run
                });//runOnUiThread

            }//run
        }).start();//Thread

    }//getMeaningEn


    //method to initialize the views
    private void initViews() {
        //새로 정의한 스레드 새로운 객체 생성
        showResultThread = new ShowResultThread();
        searchResultThread = new SearchResultThread();

        mWebView = (WebView) findViewById(R.id.webView); //메인웹뷰, 스페인어 컨텐츠를 보여줌
        // bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheetLayout));
        anchorBottomSheetBehavior = AnchorBottomSheetBehavior.from(findViewById(R.id.bottom_sheet)); //화면 하단에 고정된 바텀시트
        TextView tv_msg = (TextView) findViewById(R.id.tv_msg); //"모르는 단어나 문장을 길게 클릭하세요"
        tv_wordMode = (TextView) findViewById(R.id.tv_wordMode); //단어검색모드
        tv_sentenceMode = (TextView) findViewById(R.id.tv_sentenceMode); //문장번역모드
        et_search = (EditText) findViewById(R.id.et_search); //바텀시트에서 유저가 직접 단어/문장을 검색할 수 있는 에딧텍스트
        btn_cancelSearch = (Button) findViewById(R.id.btn_cancelSearch); //et_search객체에 입력된 텍스트를 지운다
        btn_simpleMode = (Button) findViewById(R.id.btn_simpleMode); //et_meaningEn을 gone시켜서(스페인어-영어뜻) 사전을 좀더 심플하게 바꾼다
        layout_meaning = (LinearLayout) findViewById(R.id.layout_meaning); //문장번역 모드일 때에는 이 레이아웃 자체를 gone시킨다
            //상기 레이아웃에는 et_meaninig, btn_cancelMeaning이 포함되어 있음
        et_meaning = (EditText) findViewById(R.id.et_meaning); //단어검색 모드일 때에만 나타남. 단어의 뜻을 한국어로 보여줌-traduction.sensagent.com에서 가져옴.
        et_meaningEn= (EditText) findViewById(R.id.et_meaningEn); //단어검색 모드일 때에만 나타남. 단어의 뜻을 영문으로 보여줌-spanishdict.com에서 가져옴.
        //btn_cancelMeaning = (Button) findViewById(R.id.btn_cancelMeaning); //입력된 단어 뜻을 지우기 위한 버튼
        btn_save = (Button) findViewById(R.id.btn_save); //검색한 텍스트를 저장한다(et_search에 입력된 텍스트)
        webView_dic = (WebView) findViewById(R.id.webview_dic); //바텀시트에서 사전/번역페이지를 보여주는 웹뷰
        mProgressBar = (ProgressBar) findViewById(R.id.pb);//메인웹뷰에서 웹페이지 로딩 시 로딩상황을 보여준다
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //소프트 키보드를 제어하는 매니저

        btn_goHome = (Button) findViewById(R.id.btn_goHome);
        btn_bookmark = (Button) findViewById(R.id.btn_bookmark);
        //북마크 다이얼로그 관련
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        view_bookmark = inflater.inflate(R.layout.webview_bookmark, null);
        et_Url = (EditText) view_bookmark.findViewById(R.id.et_Url);
        et_titleUrl = (EditText) view_bookmark.findViewById(R.id.et_titleUrl);
    }//initViews


    //소프트 키보드를 감추는 메소드
    private void HideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }//HideKeyboard



    //웹뷰 내에서 뒤로 가기 설정
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == event.KEYCODE_BACK) {
            if(mWebView.canGoBack()) {
                mWebView.goBack();
                return false;
            }//main_webView
        }//keyCode
        return super.onKeyDown(keyCode, event);
    }//onKeyDown



    //뒤로 가기 두번 누르면 종료
    @Override public void onBackPressed() {
        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }


    //액티비티 종료 시
    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        //다른 액티비티로 이동하기 전에 다이얼로그 종료
        super.onDestroy();
    }
}//activity
