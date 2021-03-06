package com.example.responsibilityhome.Network;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.responsibilityhome.ImageConverter;
import com.example.responsibilityhome.R;
import com.example.responsibilityhome.RealEstateRecyclerAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class NetworkTask extends AsyncTask<Void, Void, String> {

    private Context context;
    private String url;
    private String data;
    private int selection;
    ProgressDialog asyncDialog;
    List<RealEstateItem> items = new ArrayList<>();

    public NetworkTask(Context _context, String url, String data, int action){
        this.context = _context;
        this.url = url;
        this.data = data;
        this.selection = action;
        if(_context != null)
            this.asyncDialog = new ProgressDialog(_context);
    }

    @Override
    protected String doInBackground(Void... voids) {
        String result = null;

        try {
            items = GetXmlData();
            /*RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.Request(url, data);*/

        }catch (Exception e){
            result = "Error";
        }
        return result;
    }

    @Override
    protected void onPreExecute(){
        //로딩
        if(asyncDialog != null) {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("Loading");
            asyncDialog.show();
            asyncDialog.setCanceledOnTouchOutside(false);
        }
        super.onPreExecute();
    }


    @Override
    protected void onPostExecute(String result) {

        try {
            //경우에 따라 결과 값을 받아 일어났으면 하는 작업
            switch (this.selection) {

                //부동산 매매 가져오기
                case 1:
                    try {
                        RecyclerView recyclerView = (RecyclerView) ((Activity) context).findViewById(R.id.real_estate_list);
                        recyclerView.setAdapter(new RealEstateRecyclerAdapter(context, items, R.layout.content_main));
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(layoutManager);
                        /*JSONObject jsonObject = new JSONObject(result);
                        String resultMsg = jsonObject.getString("resultCode");
                        if (resultMsg.equals("00")) {
                        } else {
                        }*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

            }
            //로딩 종료
            if(asyncDialog!=null) {
                asyncDialog.setCanceledOnTouchOutside(true);
                asyncDialog.dismiss();
            }

        } catch (Exception e) {
            e.printStackTrace();
            //로딩 종료
            if(asyncDialog!=null) {
                asyncDialog.setCanceledOnTouchOutside(true);
                asyncDialog.dismiss();
            }
        }
    }



    //공공데이터 가져오기
    public List<RealEstateItem> GetXmlData(){

        List<String> apartment = new ArrayList<>(); // 아파트
        List<String> deposit = new ArrayList<>(); // 보증금
        List<String> monthlyList = new ArrayList<>(); // 월세
        List<String> dong = new ArrayList<>(); //법정동
        List<String> m2 = new ArrayList<>(); //전용면적
        List<String> buildYear = new ArrayList<>(); //건축년도


        List<RealEstateItem> items = new ArrayList<>();
        StringBuffer buffer = new StringBuffer();
        String title=null;
        String desc=null;
        String monthly = null;

        //String str = edit.getText().toString();//EditText에 작성된 Text얻어오기
        //String location = URLEncoder.encode(str);//한글의 경우 인식이 안되기에 utf-8 방식으로 encoding     //지역 검색 위한 변수

        try {
            URL url3= new URL(url);//문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is= url3.openStream(); //url위치로 입력스트림 연결

            XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
            XmlPullParser xpp= factory.newPullParser();
            xpp.setInput( new InputStreamReader(is, "UTF-8") ); //inputstream 으로부터 xml 입력받기

            String tag;

            xpp.next();
            int eventType= xpp.getEventType();

            while( eventType != XmlPullParser.END_DOCUMENT ){
                switch( eventType ){
                    case XmlPullParser.START_DOCUMENT:
                        //buffer.append("파싱 시작...\n\n");
                        break;
                    case XmlPullParser.START_TAG:
                        tag= xpp.getName();//태그 이름 얻어오기

                        if(tag.equals("item")) ;// 첫번째 검색결과
                        else if(tag.equals("아파트")){
                            xpp.next();
                            /*buffer.append("아파트 : ");
                            buffer.append(xpp.getText());//addr 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n"); //줄바꿈 문자 추가*/
                            title = xpp.getText().trim();
                            apartment.add(title);
                            /*RealEstateItem item = new RealEstateItem(bytes,title,desc);
                            items.add(item);*/
                        }
                        else if(tag.equals("보증금액")){
                            xpp.next();
                            /*buffer.append("보증금액 :");
                            buffer.append(xpp.getText());
                            buffer.append("\n");*/
                            desc = xpp.getText().trim();
                            deposit.add(desc);
                        }
                        else if(tag.equals("월세금액")){
                            xpp.next();
                            monthly = xpp.getText().trim();
                            monthlyList.add(monthly);
                        }
                        else if(tag.equals("법정동")){
                            xpp.next();
                            dong.add(xpp.getText().trim());
                        }
                        else if(tag.equals("전용면적")){
                            xpp.next();
                            m2.add(xpp.getText());
                        }
                        else if(tag.equals("건축년도")){
                            xpp.next();
                            buildYear.add(xpp.getText());
                        }

                        break;

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        tag= xpp.getName(); //태그 이름 얻어오기
                        if(tag.equals("item")) buffer.append("\n");// 첫번째 검색결과종료..줄바꿈
                        break;
                }

                eventType= xpp.next();
            }

            byte[] bytes = ImageGo(R.drawable.room1);
            byte[] bytes2 = ImageGo(R.drawable.room2);
            byte[] bytes3 = ImageGo(R.drawable.room3);

            // 다 넣어줘어어어어ㅓ
            for(int i = 0 ; i < apartment.size(); i++)
            {
                RealEstateItem item = new RealEstateItem(bytes,apartment.get(i),deposit.get(i),monthlyList.get(i),dong.get(i),m2.get(i),buildYear.get(i));
                items.add(item);
                if(i%2==1)
                    bytes = bytes2;
                else
                    bytes = bytes3;
            }


        }

        catch (Exception e) {

        }

        /*buffer.append("파싱 끝\n");*/

        //return buffer.toString();//StringBuffer 문자열 객체 반환
        return items;

    }

    public byte[] ImageGo(int image){
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(),image);
        bm = ImageConverter.resize(bm);
        ByteArrayOutputStream  byteArray = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArray);
        byte[] bytes = byteArray.toByteArray();

        return bytes;
    }

}
