package com.example.luna.caramelo;




import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LUNA on 2017-12-12.
 */
    //메인액티비티에 음악/뉴스/기타 세 개의 탭이 있음
    //others fragment는 뉴스 탭을 구성
    //20171219 현재 팟캐스트 사이트, 동화 오디오북, 문법 강의 사이트 등 등록
    //TODO 그러나 팟캐스트가 재생되지 않는 경우도 있었으므로 이 부분 한번 더 체크 후 보완
    //그리고 테드 사이트 등 콘텐츠 보강!
    //이 중 즐겨찾기에 등록된 사이트들은 Fav_OthersFragment에서 띄워짐
public class OthersFragment extends Fragment {

    //리스트뷰 위해
    List<Site> siteList;
    ListView listView;

    String type = "others";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab, container, false);

        //객체 initialize
        siteList = new ArrayList<>();
        listView = (ListView) view.findViewById(R.id.listView);



        //값 넣기
        siteList.add(new Site(R.drawable.practicaespanol, "Practica Español", "기사와 오디오, 관련 연습문제를 제공", "http://www.practicaespanol.com",type));
        siteList.add(new Site(R.drawable.hoyhablamos, "Hoy Hablamos", "팟캐스트/ 스크립트 제공", " https://hoyhablamos.com/category/podcast",type));
        siteList.add(new Site(R.drawable.spanishpodcast, "Spanish Podcast", "팟캐스트/ 스크립트 제공", "https://www.spanishpodcast.net/blog",type));
        siteList.add(new Site(R.drawable.cuentosparadormir, "Cuentos para dormir", "동화 오디오북/ 스크립트 제공", "https://cuentosparadormir.com/audiocuentos-originales",type));
        siteList.add(new Site(R.drawable.mundoprimaria, "Mundo Primaria", "동화 오디오북", "https://www.mundoprimaria.com/audiocuentos-infantiles",type));
        siteList.add(new Site(R.drawable.youtube, "Tu escuela de español", "10분 문법수업 시리즈/ 자막 제공", "https://www.youtube.com/watch?v=Nn9Jiaz83dg&list=PL_JdwZWnDhAVEttBLQcJ5IrSucqhLOTuF",type));

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
}//clasee