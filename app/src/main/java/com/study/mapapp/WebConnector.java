package com.study.mapapp;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/*
   웹서버와의 연동을 위한 클래스

   HttpURLConnection 객체를 이용하면 됨..

   네트워크 연동시 쓰레드를 이용해야 하고, 네트워크 연동이 끝난 시점에
   서버로 부터 가져온 데이터를 UI에 반영하려면 Handler로 제어해야 한다!!
   (이유? UI제어는 메인쓰레드만 가능하므로...)

   구글에서는 이런 쓰레드와 ui를 제어하는 코드를 반복함에 있어, 보다 개발자가
   편리한 객체를 제공해주는데,..AsyncTask 임...
   AsyncTask 를 사용하고 싶지 않은 경우는 쓰레드+Handler로 처리하면 되지만
   웹서버와의 연동에 있어서는, 네트워크 연결을 쓰레드로 하고, 가져온 데이터를
   UI에 반영하는 업무가 대부분이므로 AsyncTask 사용시 상당히 편리하다...

   AsyncTask<Param , Progress, Result>
   1.Param :  doInBackground() 메서드 호출시 사용할 인수
                   doInBackground는 쓰레드에 의해 호출되므로 주로 웹서버의 주소나
                   url의 파라미터를 넘길때 사용함 String 많이 사용...

   2.Progress : 쓰레드 실행 도중에 사용할 데이터
                    사용예) 웹서버와의 연동시 진행율, 파일의 다운로드 진행율...
                               Integer를 많이 씀..
   3.Result :   doInBackground 메서드가 완료된 이후에 반환된 값...
                  사용예)  웹서버로부터 가져온 json 데이터를 반환하는 용도로 씀..
                  자료형은 주로  String 많이 씀..

*/
public class WebConnector extends AsyncTask<Void, Void, String>{
    String TAG;
    URL url;
    HttpURLConnection httpURLConnection;
    BufferedReader buffr; /* 문자기반의 버퍼처리된 입력스트림*/
    MainActivity mainActivity;

    public WebConnector(MainActivity mainActivity) {
        this.mainActivity=mainActivity;
        TAG=this.getClass().getName();
    }


    /*서버로부터 데이터 가져오는 메서드!!*/
    public String loadFromServer(){
        String data=null;

        try {
            url = new URL("http://192.168.0.137:8888");
            httpURLConnection=(HttpURLConnection)url.openConnection(); /*웹서버에 요청이 일어남!!*/
            httpURLConnection.setRequestMethod("GET");

            /*서버와의 스트림 연결을 통해 데이터를 입력받으려면
            * setDoInput(true) 로 설정..
            * */
            httpURLConnection.setDoInput(true);
            //httpURLConnection.connect();

            int responseCode=httpURLConnection.getResponseCode();
            Log.d(TAG , "responseCode는 "+responseCode);
            buffr= new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(),"UTF-8"));


            StringBuilder sb = new StringBuilder();

            while(true){
                data = buffr.readLine();
                if(data == null){/*데이터가 없다면...*/
                    break;
                }
                sb.append(data);
            }

            data=sb.toString();
            Log.d(TAG, data);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(buffr !=null){
                try {buffr.close();} catch (IOException e) {}
            }
        }
        return data;
    }

    /*
    * 메인쓰레드에 의해 실행되며, doInBackground 메서드가 호출되면,
    * 먼저 작동하게 됨..따라서 개발자는 웹서버와의 연동 이전에해야할 작업이
    * 있다면 여기서 함!!
    * 주로 프로그래스 바의 시작시점을 여기서 잡는다. 왜?? doInBackground는
    * 쓰레드에 의해 실행되므로 쓰레드는 UI제어할 수 없다는 원칙이 있기 때문..
    * */
    protected void onPreExecute() {/*메인쓰레드에 의해 실행*/
        super.onPreExecute();
    }

    /*쓰레드에 의해 실행되므로, 네트워크 작업이나 지연,대기 상태에 빠지는
    * 작업은 여기서 진행...우리는 여기서 웹서버 연동을 시도하면 된다..
    * */
    protected String doInBackground(Void[] params) {
        return loadFromServer();
    }

    /*
    * doInBackground가 실행되고 있는중에 만약 UI를 제어할 일이 있다면
    * 이 메서드에서 수행하면 된다... 결국 이 메서드로 메인쓰레드에 의해 작동
    * 프로그래스바의 상태의 변경에 주로 사용됨...
    * */
    protected void onProgressUpdate(Void[] values) {
        super.onProgressUpdate(values);
    }

    /*
    * doInBackground 메서드 모든 작업을 완료하면, 호출되는 메서드이며
    * 메인쓰레드에 의해 실행되므로 주로 웹서버에서 가져온 데이터를 UI에
    * 반영하는데 많이 쓰임...프로그래스바의 종료시점이기도 하다..
    * */
    protected void onPostExecute(String json) {
        super.onPostExecute(json);

        /* 모든 데이터가 다운로드 완료된 시점이므로, 바로 이시점에
        * 구글 맵에 반영하면 됨!!
        * */
        Toast.makeText(mainActivity, "제이스 데이터 전송완료 맵연결시작", Toast.LENGTH_LONG).show();
        List list=parse(json);/*맵 호출보다 앞서서 파싱을 먼저 해야함!!*/
        mainActivity.list=list;
        mainActivity.connectMap();
    }

    /* json 데이터는 그냥 스트링일 뿐이므로, 파싱작업을 거쳐서 쓰기 좋은
    * 형태로 변환해보자!!*/
    public List parse(String json){
        List<MapDTO> list = new ArrayList<MapDTO>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray array=jsonObject.getJSONArray("list");

            /*배열의 크기만큼 반복하면서, json 객체 하나를, DTO로 담아서
            * List에 채우자!!
            * */


            for(int i=0;i<array.length();i++){
                MapDTO dto = new MapDTO();
                JSONObject obj=array.getJSONObject(i);

                dto.setLati(obj.getDouble("Lat"));
                dto.setLongi(obj.getDouble("Lng"));
                dto.setTitle(obj.getString("title"));

                list.add(dto);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}








