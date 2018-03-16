package com.example.luna.caramelo.Main;




import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.luna.caramelo.Favorites.FavoriteSite.Site;
import com.example.luna.caramelo.Favorites.FavoriteSite.SiteListAdapter;
import com.example.luna.caramelo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LUNA on 2017-12-12.
 */

    //메인액티비티에 음악/뉴스/기타 세 개의 탭이 있음
    //news fragment는 뉴스 탭을 구성
    //국내에서 스페인어 뉴스를 제공하는 사이트, 영미권에서 스페인어 뉴스를 제공하는 사이트,
    //스페인이나 중남미 현지의 뉴스 사이트들을 등록
    //20171219 현재 연합뉴스, BBC, CNN, los40 네 개가 등록됨
    //TODO 추후 국가별, 카테고리별(음악, 스포츠 등?) more 사이트 등록하는 게 좋을 듯
    //이 중 즐겨찾기에 등록된 사이트들은 Fav_NewsFragment에서 띄워짐
public class NewsFragment extends Fragment {

    //리스트뷰 위해
    List<Site> siteList;
    ListView listView;

    String type ="news";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab, container, false);

        //객체 initialize
        siteList = new ArrayList<>();
        listView = (ListView) view.findViewById(R.id.listView);


        //값 넣기
        siteList.add(new Site(R.drawable.yonhap, "AGECIA DE NOTICIAS YONHAP", "연합뉴스 스페인어판", "http://spanish.yonhapnews.co.kr",type));
        siteList.add(new Site(R.drawable.bbcmundo, "BBC Mundo", "BBC에서 제공하는 스페인어 뉴스", "http://www.bbc.com/mundo",type));
        siteList.add(new Site(R.drawable.cnn, "CNN en Español", "CNN에서 제공하는 스페인어 뉴스", "http://cnnespanol.cnn.com",type));
        siteList.add(new Site(R.drawable.los40, "los 40", "라틴음악 최신 소식", "http://los40.com",type));


        //어댑터 생성
        SiteListAdapter siteListAdapter = new SiteListAdapter(getContext(), R.layout.site_custom_list, siteList);
        listView.setAdapter(siteListAdapter);

        //리스트뷰 클릭
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MainActivity.this, position+"번 클릭", Toast.LENGTH_SHORT).show();


                final Site site = siteList.get(position);

                Intent intent = new Intent(getActivity(), WebviewActivity.class);
                intent.putExtra("url", site.getUrl());
                startActivity(intent);

            }
        });

        return view;
    }//onCreate
}//class