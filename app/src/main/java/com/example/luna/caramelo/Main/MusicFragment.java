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
 *  Created by LUNA on 2017-12-12.
 */

    //메인액티비티에 음악/뉴스/기타 세 개의 탭이 있음
    //music fragment는 음악 탭을 구성
    //뮤직비디오와 가사를 함께 감상할 수 있는 사이트들을 보여준다
    //이 중 즐겨찾기에 등록된 사이트들은 Fav_MusicFragment에서 띄워짐

public class MusicFragment extends Fragment {

    //리스트뷰 위해
    List<Site> siteList;
    ListView listView;
    String type ="music";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.tab, container, false);

        View view = inflater.inflate(R.layout.tab, container, false);

        //객체 initialize
        siteList = new ArrayList<>();
        listView = (ListView) view.findViewById(R.id.listView);


        //값 넣기
        siteList.add(new Site(R.drawable.musicacom, "인기 뮤직비디오", "현재 가장 인기있는 뮤직비디오", "https://www.musica.com/letras.asp?videos=musica",type));
        //siteList.add(new Site(R.drawable.letras, "주간 인기 음악(1)", "", "https://www.letras.com/mais-acessadas",type));
        siteList.add(new Site(R.drawable.letras, "주간 인기 음악(1)", "", "https://m.letras.mus.br/mais-acessadas/musicas/",type)); //모바일용
        siteList.add(new Site(R.drawable.musicacom, "주간 인기 음악(2)", "", "https://www.musica.com/letras.asp?topmusica=musica&pais=todos",type));
        siteList.add(new Site(R.drawable.letras, "주간 인기 앨범", "Top Álbumes", "https://m.letras.mus.br/top-albuns/",type));
        siteList.add(new Site(R.drawable.musicacom, "주간 인기 아티스트", "", "https://www.musica.com/letras.asp?topmusica=musica",type));
        siteList.add(new Site(R.drawable.letras, "장르별 인기 음악(1)", "REGGAETON", "https://m.letras.mus.br/top-albuns/reggaeton/",type));
        siteList.add(new Site(R.drawable.letras, "장르별 인기음악(2)", "MÚSICA ROMÁNTICA", "https://m.letras.mus.br/top-albuns/romantico/",type));
        siteList.add(new Site(R.drawable.letras, "장르별 인기음악(3)", "DANCE", "https://m.letras.mus.br/top-albuns/dance/",type));
        siteList.add(new Site(R.drawable.letras, "장르별 인기음악(4)", "MÚSICA RELIGIOSA", "https://m.letras.mus.br/top-albuns/gospelreligioso/",type));
        siteList.add(new Site(R.drawable.letras, "장르별 인기음악(5)", "MÚSICA INFANTIL", "https://m.letras.mus.br/top-albuns/infantil/",type));
        siteList.add(new Site(R.drawable.letras, "장르별 인기음악(6)", "AXÉ", "https://m.letras.mus.br/top-albuns/axe/",type));
        siteList.add(new Site(R.drawable.letras, "장르별 인기음악(7)", "BREGA", "https://m.letras.mus.br/top-albuns/brega/",type));
        siteList.add(new Site(R.drawable.letras, "장르별 인기음악(8)", " BOSSA NOVA", "https://m.letras.mus.br/top-albuns/bossa-nova/",type));


        //이하 테스트용
    //        siteList.add(new Site(R.drawable.musicacom, "주간 인기 아티스트12", "", "https://www.musica.com/letras.asp?topmusica=musica",type));
    //        siteList.add(new Site(R.drawable.musicacom, "주간 인기 아3티스트1", "", "https://www.musica.com/letras.asp?topmusica=musica",type));
    //        siteList.add(new Site(R.drawable.musicacom, "주간 인기2 아티스3트13", "", "https://www.musica.com/letras.asp?topmusica=musica",type));
    //        siteList.add(new Site(R.drawable.musicacom, "주간 인기 아티스트1", "", "https://www.musica.com/letras.asp?topmusica=musica",type));
    //        siteList.add(new Site(R.drawable.musicacom, "주간 인기 아티스트14", "", "https://www.musica.com/letras.asp?topmusica=musica",type));
    //        siteList.add(new Site(R.drawable.musicacom, "주간 인기 아티스2트1", "", "https://www.musica.com/letras.asp?topmusica=musica",type));
    //        siteList.add(new Site(R.drawable.musicacom, "주간 인기 아티7스트1", "", "https://www.musica.com/letras.asp?topmusica=musica",type));
    //        siteList.add(new Site(R.drawable.musicacom, "주간 인기0 아티스트1", "", "https://www.musica.com/letras.asp?topmusica=musica",type));




        //어댑터 생성
        SiteListAdapter siteListAdapter = new SiteListAdapter(getContext(), R.layout.site_custom_list, siteList);
        listView.setAdapter(siteListAdapter);

        //리스트뷰 클릭
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getContext(), position+"번 클릭", Toast.LENGTH_SHORT).show();


                final Site site = siteList.get(position);

                Intent intent = new Intent(getActivity(), WebviewActivity.class);
                intent.putExtra("url", site.getUrl());
                startActivity(intent);

            }
        });

        return view;
    }//onCreate




}//fragment