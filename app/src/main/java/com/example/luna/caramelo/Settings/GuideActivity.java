package com.example.luna.caramelo.Settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.luna.caramelo.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

//2017-12-22
    //이용안내 화면
    //뒤로 가기 버튼 두 번 누르면 종료되는 메소드는 적용하지 않는다
    //이용안내 메시지만 담겨있는 액티비티
    //메시지는 장문이라 xml파일이 아닌 raw파일에 guide.txt.라는 이름으로 넣어두고
    //openRawResource()함수를 통해 꺼내어 쓴다

    //*text파일 저장 시 UFT-8로 저장하면 오히려 깨지므로 디폴트 값인 ANSI로 저장하도록 한다

    //참고 블로그: http://mainia.tistory.com/1484

public class GuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        //guide.txt를 띄워줄 텍스트뷰
        TextView tv_guide = (TextView) findViewById(R.id.tv_guide);
        tv_guide.setText(readTxt());
    }//onCreate

    //raw폴더에 담긴 guide.txt를 불러서 읽어낸다
    private String readTxt() {
        String data = null; //data에 guide.txt를 담은 후 inputstream 객체로 리턴한다
        InputStream inputStream = getResources().openRawResource(R.raw.guide);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {

            i = inputStream.read();
            while (i != -1 ) {
                byteArrayOutputStream.write(i); //guide.txt가 담긴 inputStream객체를 ByteArrayOutputStream에 저장한다
                i = inputStream.read();
            }//while

            //문자가 깨지는 것을 방지하기 위해 ByteArrayOutputStream에 저장된 데이터를 MS949를 통해 변환한다
            data = new String(byteArrayOutputStream.toByteArray(), "MS949");
            inputStream.close();

        }catch (IOException e) {
            e.printStackTrace();
        }//catch

        //변환한 값을 리턴하여 tv_guide 텍스트뷰에 뿌려준다
        return data;
    }//readTxt


    //액티비티 종료 시
    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }


}//activity


