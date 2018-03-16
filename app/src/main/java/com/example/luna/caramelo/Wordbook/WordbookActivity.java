package com.example.luna.caramelo.Wordbook;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import com.example.luna.caramelo.Tools.ApiClient;
import com.example.luna.caramelo.Tools.ApiService;
import com.example.luna.caramelo.Tools.BackPressCloseHandler;
import com.example.luna.caramelo.Main.MainActivity;
import com.example.luna.caramelo.R;
import com.example.luna.caramelo.Settings.SettingActivity;
import com.example.luna.caramelo.Settings.Account.SharedPrefManager;
import com.example.luna.caramelo.Settings.Account.User;
import com.example.luna.caramelo.Tools.ResponseModel;
import com.trafi.anchorbottomsheetbehavior.AnchorBottomSheetBehavior;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
* 유저가 저장한 단어를 보여주는 단어장 액티비티
* 단어장에 메모를 추가/수정할 수도 있고
* 하단에 고정된 바텀시트에서도 모르는 단어를 찾아서 그 결과를 단어장에 저장할 수 있다
* */

public class WordbookActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, WordAdpater.WordAdapterListener {


    private final String TAG = "단어장액티비티";

    //뒤로 가기 두번-> 종료
    private BackPressCloseHandler backPressCloseHandler;

    private AnchorBottomSheetBehavior<View> anchorBottomSheetBehavior; //액티비티 하단에 고정시킬 바텀시트
    private WebView webView_dic; //바텀시트에 보일 웹뷰
    private String DICTIONARY = "http://spdic.naver.com/#/search?query="; //웹뷰로 보여줄 사이트 주소
    TextView tv_sentenceMode, tv_msg; //단어장에서 보여줄 바텀시트에서는 필요없는 기능
    Button btn_save;// 단어장에서도 단어를 저장할 수 있다
    Button btn_simpleMode;// 사전을 심플하게 볼 것인가, 아니면 다양한 사전의 결과물을 볼 것인가 선택
    Button btn_cancelSearch;// 검색어 입력 칸 초기화하는 버튼
    EditText et_search;// 검색어 입력
    EditText et_meaning, et_meaningEn;// 네이버 이외의 사이트에서 단어 뜻을 크롤링해온다
    InputMethodManager inputMethodManager; //사전에서 직접 검색할 경우 키보드 제어위한 변수
    private boolean originalMode, koreanMode, enlishMode, simpleMode;
    // simpleMode: 단어뜻을 보여주는 editText를 지운다
    //originalMode : 바텀시트에서 단어뜻을 한국어와 영어로 모두 볼 수 있다
    //KoreanMode : 단어뜻을 한국어로 본다
    //EnlishMode: 단어뜻을 영어로 본다

    //단어장에 저장할 단어와 관련된 변수들
    String wordToSaveKor, wordToSaveEn; //검색어를 각각 traduction.sensagent.com(스페인어-한국어)/ spanishdict.com(스페인어-영어)에 검색했을 때 식별되는 단어의 원형
    String meaningToSaveKor, meaningToSaveEn; //검색어를 각각 traduction.sensagent.com(스페인어-한국어)/ spanishdict.com(스페인어-영어)에 검색했을 때 검색결과(단어 뜻)
    String searchWord; //검색어

    String msgFromServer; //단어 저장 요청 시 서버에서 보낸 응답

    private RecyclerView recyclerViewWord;
    private WordAdpater wordAdpater;
    private CoordinatorLayout coordinatorLayout; //WordbookActivity의 메인 레이아웃, 스낵바를 달아주기 위함
    List<Word> wordList;
    private SearchView searchView; //For 단어장 검색기능

    private static String username; //현재 로그인 중인 유저네임

    int clicked = 1; //클릭수에 따라 view의 visibility를 제어하기 위해


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordbook);


        //로그인 여부를 판별, 로그인 안 되어있으면 단어장을 이용할 수 없다
        if(!SharedPrefManager.getmInstance(this).isLoggedIn()) {
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(this, SettingActivity.class));
        }

        final User user = SharedPrefManager.getmInstance(this).getUser(); // 현재 로그인 된 유저정보
        username = user.getUsername(); //유저네임

        getAllDataWord(); //DB에서 단어장 데이터를 가져온다
        initViews(); // initiate views

        Toolbar toolbar = findViewById(R.id.toolbar); //툴바제어
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("단어장");
        toolbar.setNavigationIcon(R.drawable.home_24); //툴바에 홈 버튼 만들기
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //애니메이션 없앰
                startActivity(new Intent(WordbookActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
            }//onClick
        });

        wordList = new ArrayList<>(); //단어장에 올라갈 재료
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        recyclerViewWord = (RecyclerView) findViewById(R.id.recycler_view);

        wordAdpater = new WordAdpater(this, wordList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewWord.setLayoutManager(mLayoutManager);
        recyclerViewWord.setItemAnimator(new DefaultItemAnimator());
        recyclerViewWord.setAdapter(wordAdpater);



        //단어장에 붙여줄 바텀시트에는 필요없는 기능이므로 안 보여도 된다
        tv_msg.setVisibility(View.INVISIBLE);
        tv_sentenceMode.setVisibility(View.INVISIBLE);

        //직접 사전 검색할 때
        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {

                    case EditorInfo.IME_ACTION_SEARCH :
                        btn_save.setVisibility(View.VISIBLE); //단어장에 있는 단어를 검색할 때와는 달리 저장버튼을 살려준다
                        searchWord = et_search.getText().toString();
                        Toast.makeText(WordbookActivity.this, "Search "+searchWord, Toast.LENGTH_SHORT).show();
                        DICTIONARY = DICTIONARY + searchWord;
                        webView_dic.loadUrl(DICTIONARY);
                        getMeaningEn(searchWord);
                        getOriginalWordandMeaning(searchWord);
                        DICTIONARY = "http://spdic.naver.com/#/search?query="; //주소 초기화
                        HideKeyboard(); //소프트 키보드를 내려야 웹뷰가 보이므로 키보드를 감춘다
                        break;

                }//switch
                return true;
            }//onEditorAction
        });//OnEditorActionListener

        btn_cancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_search.getText().clear(); //검색창 초기화
            }//onClick
        });//OnClickListener


        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(originalMode) {
                    Log.e("오리지널모드에서", "한국어: "+wordToSaveKor+"/영어: "+wordToSaveEn);

                    //검색창이 비어있다면
                    if(et_search.getText().toString().equals("")) {
                        Snackbar.make(v, "저장할 단어가 없습니다.", Snackbar.LENGTH_SHORT).show();
                        //스페인어-영어뜻 보여주는 에딧텍스트만 비어있다면
                        //TODO 검색어, 단어원형, 스페인어-영어 뜻만 저장한다
                    } else if (!et_search.getText().toString().equals("") && !et_meaning.getText().toString().equals("") && et_meaningEn.getText().toString().equals("")) {
                        Snackbar.make(v, "스페인어-한국어 사전의 검색결과만 저장합니다.",Snackbar.LENGTH_LONG).setAction("확인", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.e("검색한 단어 뜻 Kor", wordToSaveKor+":"+meaningToSaveKor);
                                SaveWord(searchWord, wordToSaveEn, meaningToSaveKor, 1); //영어사전 결과가 더 정확해서, 단어 원형은 spanishdict.com에서 가져온 걸 넣어준다
                            }//onClick
                        }).show();
                        wordAdpater.notifyDataSetChanged();
                        //스페인어-한국어 뜻 보여주는 에딧텍스트만 비어있다면
                        //TODO 검색어, 단어원형, 스페인어-영어 뜻만 저장한다
                    }  else if (!et_search.getText().toString().equals("") && et_meaning.getText().toString().equals("") && !et_meaningEn.getText().toString().equals("")) {
                        Snackbar.make(v, "스페인어-영어 사전의 검색결과만 저장합니다.",Snackbar.LENGTH_LONG).setAction("확인", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SaveWord(searchWord, wordToSaveEn, meaningToSaveEn, 0);
                                Log.e("검색한 단어와 뜻 En/eng mode", wordToSaveEn+":"+meaningToSaveEn);
                            }//onClick
                        }).show();
                        wordAdpater.notifyDataSetChanged();
                    } else { //검색어, 스페인어-한국어 뜻, 스페인어-영어 뜻이 모두 다 editText에 입력되어 있다면
                        //TODO 검색어, 단어원형, 스페인어-한국어/영어 뜻 모두 저장한다
                        SaveWord(searchWord, wordToSaveEn, meaningToSaveKor, meaningToSaveEn);
                        //Log.e("검색한 단어 뜻 Kor", wordToSaveKor+":"+meaningToSaveKor);
                        Log.e("검색한 단어 뜻 En", wordToSaveEn+":"+meaningToSaveEn);
                        wordAdpater.notifyDataSetChanged();
                    }

                } else if(koreanMode) {

                    //검색창과 스페인어-한국어뜻 보여주는 에딧텍스트가 비어있다면
                    if(et_search.getText().toString().equals("") || et_meaning.getText().toString().equals("")) {
                        Snackbar.make(v, "저장할 단어/뜻 (이)가 없습니다.", Snackbar.LENGTH_SHORT).show();
                    } else{
                        //TODO 검색어, 단어원형, 스페인어-한국어 뜻만 저장한다
                        SaveWord(searchWord, wordToSaveKor, meaningToSaveKor, 1);
                        Log.e("검색한 단어 뜻 Kor", wordToSaveKor+":"+meaningToSaveKor);
                        wordAdpater.notifyDataSetChanged();
                    }

                } else if(enlishMode) {
                    //TODO 검색어, 단어원형, 스페인어-영어 뜻만 저장한다
                    //검색창과 스페인어-영어뜻 보여주는 에딧텍스트가 비어있다면
                    if(et_search.getText().toString().equals("") || et_meaningEn.getText().toString().equals("")) {
                        Snackbar.make(v, "저장할 단어/뜻 (이)가 없습니다.", Snackbar.LENGTH_SHORT).show();
                    } else {
                        SaveWord(searchWord, wordToSaveEn, meaningToSaveEn, 0);
                        Log.e("검색한 단어 뜻 En", wordToSaveEn+":"+meaningToSaveEn);
                        wordAdpater.notifyDataSetChanged();
                    }

                } else if(simpleMode) {
                    //TODO 검색어만 저장한다
                    SaveWord(searchWord, wordToSaveKor, meaningToSaveKor, 1);
                    Snackbar.make(v, msgFromServer, Snackbar.LENGTH_SHORT).show();
                    wordAdpater.notifyDataSetChanged();
                }
            }//onClick
        });//Listener

        // 리사이클러뷰에 아이템 터치 헬퍼를 달아준다
        // 좌->우로 슬라이드할 때 목록을 삭제하도록 한다
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerViewWord);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback1 = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Row is swiped from recycler view
                // remove it from adapter

            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        // attaching the touch helper to recycler view
        new ItemTouchHelper(itemTouchHelperCallback1).attachToRecyclerView(recyclerViewWord);


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

////////////////////////////////////////////////////////////////////////////////////

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

                    Toast.makeText(WordbookActivity.this, "스페인어-한국어, 영어 뜻을 모두 확인합니다.", Toast.LENGTH_SHORT).show();
                    originalMode = true;
                    koreanMode = false;
                    enlishMode = false;
                    simpleMode = false;
                    et_meaning.setVisibility(View.VISIBLE);
                    et_meaningEn.setVisibility(View.VISIBLE);
                    clicked++;

                } else if(clicked%4==1) {

                    Toast.makeText(WordbookActivity.this, "스페인어-한국어 뜻만 확인합니다.", Toast.LENGTH_SHORT).show();
                    originalMode = false;
                    koreanMode = true;
                    enlishMode = false;
                    simpleMode = false;

                    et_meaning.setVisibility(View.VISIBLE);
                    et_meaningEn.setVisibility(View.GONE);
                    clicked++;

                } else if(clicked%4==2) {

                    Toast.makeText(WordbookActivity.this, "스페인어-영어 뜻만 확인합니다.", Toast.LENGTH_SHORT).show();
                    originalMode = false;
                    enlishMode = true;
                    koreanMode = false;
                    simpleMode = false;

                    et_meaning.setVisibility(View.GONE);
                    et_meaningEn.setVisibility(View.VISIBLE);
                    clicked++;

                } else if(clicked%4==3) {

                    Toast.makeText(WordbookActivity.this, "심플모드입니다.", Toast.LENGTH_SHORT).show();
                    originalMode = false;
                    koreanMode = false;
                    enlishMode = false;
                    simpleMode = true;

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
//////////////////////////////////////////////////////

        //뒤로 가기 두번 -> 종료
        backPressCloseHandler = new BackPressCloseHandler(this);



    }//onCreate



    //DB에서 단어장 재료들을 가져오는 메소드
    private void getAllDataWord() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ListWordModel> call = apiService.getAllDataWord(username);// ./displayWord.php로 유저네임을 보낸다
        call.enqueue(new Callback<ListWordModel>() {
            @Override
            public void onResponse(Call<ListWordModel> call, Response<ListWordModel> response) {
                ListWordModel listWordModel = response.body();

                if(listWordModel.getStatus() == 1) {//정보 불러오는 데 성공하면
                    wordList = listWordModel.getWordList(); //단어 리스트에 단어장 정보를 담는다
                    wordAdpater.setWordList(getApplicationContext(),wordList);

                    //인덱스 기준으로 내림차순 정렬
                    Comparator<Word> newest = new Comparator<Word>() {
                        @Override
                        public int compare(Word o1, Word o2) {
                            return (o2.getIndex()-o1.getIndex());
                        }
                    };
                    Collections.sort(wordList, newest);
                    wordAdpater.notifyDataSetChanged();

                } else {
                    Toast.makeText(WordbookActivity.this, listWordModel.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }//onResponse

            @Override
            public void onFailure(Call<ListWordModel> call, Throwable t) {
                Toast.makeText(WordbookActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }//onFailure
        });//Callback

    }//getAllDataWord


    //단어를 삭제하는 메소드
    private void deleteWord(int index, String username, String originalWord) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Call<ResponseModel> call = apiService.deleteWord(index, username, originalWord);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                ResponseModel responseModel = response.body();

                if(responseModel.getStatus() == 1 ){
                   //삭제 성공
                    //Toast.makeText(WordbookActivity.this, "삭제쿼리성공", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WordbookActivity.this, responseModel.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(WordbookActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }//onFailure
        });//Callback
    }//deleteWord


    //단어목록을 왼쪽으로 슬라이드하면 해당 단어가 삭제되도록 한다
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof WordAdpater.MyViewHolder) {
            final int index = wordList.get(viewHolder.getAdapterPosition()).getIndex();
            final String originalWord = wordList.get(viewHolder.getAdapterPosition()).getOriginalWord();
            deleteWord(index, username, originalWord);

            //리사이클러뷰에서 아이템 제거
            wordAdpater.removeItem(viewHolder.getAdapterPosition());

            Snackbar snackbar = Snackbar.make(coordinatorLayout, "단어장에서 삭제합니다.", Snackbar.LENGTH_LONG);
            snackbar.show();

        }//if

    }//onSwiped


    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }

        //바텀시트가 활성화되어있을 때 뒤로가기 버튼을 누르면 바텀시트를 아래로 내려준다
        if(anchorBottomSheetBehavior.getState()==AnchorBottomSheetBehavior.STATE_ANCHORED
                || anchorBottomSheetBehavior.getState() == AnchorBottomSheetBehavior.STATE_EXPANDED) {
            anchorBottomSheetBehavior.setState(AnchorBottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
            backPressCloseHandler.onBackPressed(); //뒤로 가기 두번 누르면 앱 종료
        }
    }//onBackPressed

    @Override //툴바에 menu_wordbook.xml을 inflate시킨다
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wordbook, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                wordAdpater.getFilter().filter(query);
                //ㄴRecyclerView is refreshed with filtered data
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                wordAdpater.getFilter().filter(query);
                //ㄴRecyclerView is refreshed with filtered data
                return false;
            }
        });
        return true;
    }

    @Override //툴바에 있는 메뉴 제어
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {
            case R.id.action_search :
                    //nothing happens
                return true;

            case R.id.btn_hideAll:
                //Boolean isVisible = false;
                if(clicked%2 == 1){
                    wordAdpater.updateVisibility(false);
                } else {
                    wordAdpater.updateVisibility(true);
                }
                clicked++;
                wordAdpater.notifyDataSetChanged();

                Log.e("클릭수", ""+clicked);
                return true;

            case R.id.btn_refresh:
                getAllDataWord(); //데이터 다시 뿌리깅
                return true;
            case R.id.oldest:
                //인덱스 기준으로 오름차순 정렬
                Comparator<Word> oldest = new Comparator<Word>() {
                    @Override
                    public int compare(Word o1, Word o2) {
                        return (o1.getIndex()-o2.getIndex());
                    }
                };
                Collections.sort(wordList, oldest);
                wordAdpater.notifyDataSetChanged();
                return true;
            case R.id.newest:
                //인덱스 기준으로 내림차순 정렬
                Comparator<Word> newest = new Comparator<Word>() {
                    @Override
                    public int compare(Word o1, Word o2) {
                        return (o2.getIndex()-o1.getIndex());
                    }
                };
                Collections.sort(wordList, newest);
                wordAdpater.notifyDataSetChanged();
                return true;
            case R.id.alphabetically:
                //알파벳 내림차순
                Comparator<Word> alphabetically = new Comparator<Word>(){
                    @Override
                    public int compare(Word word1, Word word2) {

                        return word1.getOriginalWord().toLowerCase().compareTo(word2.getOriginalWord().toLowerCase());
                    }//compare
                }; //comparator

                Collections.sort(wordList, alphabetically);
                wordAdpater.notifyDataSetChanged();
                return true;

            case R.id.removeAll:
                deleteAll(username);
                return true;
        }//switch

        return super.onOptionsItemSelected(item);
    }

    private void deleteAll(String username) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseModel> call = apiService.deleteAll(username);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                ResponseModel responseModel = response.body();
                if(responseModel.getStatus() == 1){
                    //리스트에서도 모든 단어를 삭제한다
                    wordList.clear();
                    wordAdpater.notifyDataSetChanged();
                    Toast.makeText(WordbookActivity.this, responseModel.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WordbookActivity.this, responseModel.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(WordbookActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }//deleteAll


    @Override //리스트를 클릭하면 단어장의 표제어를 사전에 검색한 결과를 가져온다
    public void onWordSelected(Word word) {
        anchorBottomSheetBehavior.setState(AnchorBottomSheetBehavior.STATE_ANCHORED);
        btn_save.setVisibility(View.INVISIBLE); //이미 저장된 단어를 검색하는 거니까 저장버튼이 필요없다
        String headWord = word.getOriginalWord(); //사전의 표제어
        DICTIONARY = DICTIONARY+headWord;
        et_search.setText(word.getOriginalWord());
        getMeaningEn(headWord);
        getOriginalWordandMeaning(headWord);
        Log.e("사전주소 변경?", DICTIONARY);
        webView_dic.loadUrl(DICTIONARY);

        DICTIONARY ="http://spdic.naver.com/#/search?query="; //사전주소 초기화
        //Toast.makeText(this, ""+word.getOriginalWord(), Toast.LENGTH_SHORT).show();
    }


    //소프트 키보드를 감추는 메소드
    private void HideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }//HideKeyboard


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


    //단어장에 단어를 저장하기 위한 메소드, 검색어, 단어원형, 단어 뜻 하나만 저장할 때
    //int lang은 단어뜻이 한국어(1)인지 영어(0)인지 구별하기 위한 변수
    private void SaveWord(String searchWord, final String originalWord, final String meaning, final int lang) {
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
                msgFromServer = responseModel.getMessage();
                Log.e("단어장으로 서버가 보낸 값", msgFromServer);
                if(responseModel.getStatus() == 1 ) {
                    //성공
                    msgFromServer = responseModel.getMessage(); //서버가 전달한 메시지를 스낵바로 띄워줄 메시지변수에 넣는다.
                    //Toast.makeText(WordbookActivity.this, "단어장에 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    wordList.get(0).setOriginalWord(originalWord);
                    if(lang == 0) { //스페인어-영어 뜻
                        wordList.get(0).setMeaningEn(meaning);
                        wordList.get(0).setMemo("");
                    } else if(lang==1){ //스페인어-한국어뜻
                        wordList.get(0).setMeaningKor(meaning);
                        wordList.get(0).setMemo("");
                    }
                    wordAdpater.notifyItemInserted(0);
                } else if(responseModel.getStatus() == 0) {
                    msgFromServer = responseModel.getMessage(); //서버가 전달한 메시지를 스낵바로 띄워줄 메시지변수에 넣는다.
                                                                //if문 쓸데없이 구별되어있지만..일단 내버려둔다. 2018-03-09
                    //Toast.makeText(WordbookActivity.this, "이미 등록된 단어입니다.", Toast.LENGTH_SHORT).show();

                }  else {
                    msgFromServer = responseModel.getMessage(); //서버가 전달한 메시지를 스낵바로 띄워줄 메시지변수에 넣는다.
                    //Toast.makeText(WordbookActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();

                }

            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(WordbookActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }//onFailure
        });

    }//SaveWord

    //단어장에 단어를 저장하기 위한 메소드, 검색어, 단어원형, 단어 2개 모두 저장할 때
    private void SaveWord(String searchWord, final String originalWord, final String meaningKor, final String meaningEn) {
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
                msgFromServer = responseModel.getMessage();
                Log.e("단어장으로 서버가 보낸 값", msgFromServer);
                //check the status code
                if(responseModel.getStatus() == 1 ) {
                    //성공
                    msgFromServer = responseModel.getMessage(); //서버가 전달한 메시지를 스낵바로 띄워줄 메시지변수에 넣는다.
                    //Toast.makeText(WordbookActivity.this, "단어장에 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    //단어장 제일 첫번째 목록에 방금 저장한 단어 추가해줌
                    wordList.get(0).setOriginalWord(originalWord);
                    wordList.get(0).setMeaningKor(meaningKor);
                    wordList.get(0).setMeaningEn(meaningEn);
                    wordList.get(0).setMemo("");
                    wordAdpater.notifyItemInserted(0);
                } else if(responseModel.getStatus() == 0) {
                    msgFromServer = responseModel.getMessage(); //서버가 전달한 메시지를 스낵바로 띄워줄 메시지변수에 넣는다.
                    //Toast.makeText(WordbookActivity.this, "이미 등록된 단어입니다.", Toast.LENGTH_SHORT).show();
                }  else {
                    msgFromServer = responseModel.getMessage(); //서버가 전달한 메시지를 스낵바로 띄워줄 메시지변수에 넣는다.
                    //Toast.makeText(WordbookActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                }
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(WordbookActivity.this, "에러"+t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("에러메시지", t.getMessage());
            }//onFailure
        });

    }//SaveWord

    private void initViews() { //뷰 initiate
        anchorBottomSheetBehavior = AnchorBottomSheetBehavior.from(findViewById(R.id.bottom_sheet)); //화면 하단에 고정된 바텀시트
        webView_dic = (WebView) findViewById(R.id.webview_dic); //바텀 시트에 들어가는 웹뷰
        tv_msg = (TextView) findViewById(R.id.tv_msg);
        tv_sentenceMode = (TextView) findViewById(R.id.tv_sentenceMode);
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_simpleMode = (Button) findViewById(R.id.btn_simpleMode);
        btn_cancelSearch = (Button) findViewById(R.id.btn_cancelSearch);
        et_search = (EditText) findViewById(R.id.et_search);
        et_meaning = (EditText) findViewById(R.id.et_meaning);
        et_meaningEn = (EditText) findViewById(R.id.et_meaningEn);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //소프트 키보드를 제어하는 매니저
    }//initViews

    //액티비티 종료 시
    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }

}//activity

