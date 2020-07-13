package com.example.tsafe_load;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPOIItem;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

public class FindLoad extends Activity {
    EditText startAddress;
    EditText endAddress;
    ImageButton startSearch;
    ImageButton endSearch;
    Button SearchRoad;
    ListView startlist;
    ListView endlist;

    TMapData tmapdata = new TMapData();
    ArrayList<TMapPOIItem> poilist = new ArrayList<>();
    ArrayList<String> startnamelist = new ArrayList<>();
    ArrayList<String> endnamelist = new ArrayList<>();
    PoiList CustomPoiList_start = new PoiList();
    PoiList CustomPoiList_end = new PoiList();
    ArrayAdapter<String> adapter1;
    ArrayAdapter<String> adapter2;
    InputMethodManager imm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searching_road);

        startAddress = findViewById(R.id.출발지);
        endAddress = findViewById(R.id.도착지);
        startSearch = findViewById(R.id.출발지검색);
        endSearch = findViewById(R.id.도착지검색);
        SearchRoad= findViewById(R.id.동선조회);
        startlist = findViewById(R.id.start_list);
        endlist = findViewById(R.id.end_list);


        adapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, startnamelist);
        startlist.setAdapter(adapter1);
        adapter2 = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, endnamelist);
        endlist.setAdapter(adapter2);

        startSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = startAddress.getText().toString();
                Log.d("출발", address);
                getGPS(address,1);
                Toast.makeText(FindLoad.this, "뒤로가기 버튼을 눌러주세요", Toast.LENGTH_SHORT).show();
            }
        });
        endSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = endAddress.getText().toString();
                Log.d("도착", address);
                getGPS(address,2);
                Toast.makeText(FindLoad.this, "뒤로가기 버튼을 눌러주세요", Toast.LENGTH_SHORT).show();
            }
        });
        SearchRoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("start_address", CustomPoiList_start.address);
                intent.putExtra("start_lat", CustomPoiList_start.lat);
                intent.putExtra("start_lon", CustomPoiList_start.lon);
                intent.putExtra("end_address", CustomPoiList_end.address);
                intent.putExtra("end_lat", CustomPoiList_end.lat);
                intent.putExtra("end_lon", CustomPoiList_end.lon);
                setResult(200,intent);
                finish();
            }
        });

        startlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CustomPoiList_start.AllSet(poilist.get(position).name, Double.parseDouble(poilist.get(position).noorLat), Double.parseDouble(poilist.get(position).noorLon));
                    startAddress.setText(startnamelist.get(position));//입력창을 초기화
                    int size = startnamelist.size();//사이즈 확인해서 초기화할 넘버 계산
                    for(int i=0; i< size; i++){
                        startnamelist.remove(0);//리스트뷰 초기화
                        poilist.remove(0);
                    }
                    startnamelist.clear();
                    adapter1.notifyDataSetChanged();
                }
        });
        endlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CustomPoiList_end.AllSet(poilist.get(position).name,Double.parseDouble(poilist.get(position).noorLat),Double.parseDouble(poilist.get(position).noorLon));
                endAddress.setText(endnamelist.get(position));//입력창을 초기화
                int size = endnamelist.size();
                for(int i=0; i< size; i++){
                    endnamelist.remove(0);//리스트뷰 초기화
                    poilist.remove(0);
                }
                endnamelist.clear();
                adapter2.notifyDataSetChanged();
            }
        });

    }
    public ArrayList<TMapPOIItem> getGPS(String addressName, int num) {
        if(num == 1){
            tmapdata.findAllPOI(addressName,5, new TMapData.FindAllPOIListenerCallback(){
                @Override
                public void onFindAllPOI(ArrayList<TMapPOIItem> arrayList) {//리스너로 하려니 Async머시기 자꾸하라고함.
                    for(int i=0; i<arrayList.size();i++){
                        poilist.add(arrayList.get(i));
                        Log.d("리스너1", poilist.get(i).name);
                        startnamelist.add(poilist.get(i).name); // listview에 사용할 계획
                        Log.d("확인", startnamelist.get(i));
                    }

                }
            });
        }else{
            tmapdata.findAllPOI(addressName,5, new TMapData.FindAllPOIListenerCallback(){
                @Override
                public void onFindAllPOI(ArrayList<TMapPOIItem> arrayList) {//리스너로 하려니 Async머시기 자꾸하라고함.
                    for(int i=0; i<arrayList.size();i++){
                        poilist.add(arrayList.get(i));
                        Log.d("리스너1", poilist.get(i).name);
                        endnamelist.add(poilist.get(i).name); // listview에 사용할 계획
                        Log.d("확인", endnamelist.get(i));
                    }
                }
            });
        }

        return poilist;
    }
}
class PoiList {
    String address;
    Double lat;
    Double lon;

    public PoiList() {
        this.address = "";
        this.lat = 0.0;
        this.lon = 0.0;
    }
    public void AllSet(String address, Double lat, Double lon){
        this.address = address;
        this.lat = lat;
        this.lon = lon;
    }
}