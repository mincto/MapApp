/*
* 초시계를 제작하되 Handler가 아닌 AsyncTask 로 구현해 본다!!
* */
package com.study.mapapp;

import android.os.AsyncTask;

public class Counter extends AsyncTask<Void , Integer, Void>{
    int count=0;
    /*쓰레드로 작업할 코드는 이 메서드에 기재한다!!!
    * 이 영역은 쓰레드에 의해 실행됨!!
    * */
    protected Void doInBackground(Void... params) {
        while(true){
            count++;
        }
    }

}
