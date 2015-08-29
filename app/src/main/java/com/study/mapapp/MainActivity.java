package com.study.mapapp;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    WebConnector webConnector;
    List<MapDTO> list;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*맵을 보여주기전에 웹서버로 부터 데이터 가져오자!!*/
        webConnector=new WebConnector(this);
        webConnector.execute(); /* doInBackground 메서드 간접호출!!*/

    }

    public void connectMap(){
        SupportMapFragment supportMapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this); /*연결*/
    }

    /*구글맵이 준비되면, 호출되는 메서드!!*/
    public void onMapReady(GoogleMap map) {

        for(int i=0; i< list.size() ;i++) {
            MapDTO dto=list.get(i);

            LatLng sbs = new LatLng(dto.getLati(), dto.getLongi());
            map.addMarker(new MarkerOptions().position(sbs).title(dto.getTitle()));
            /*
            map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                    .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                    .position(sbs).title(dto.getTitle()));
            */
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(sbs, 17));
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }
}












