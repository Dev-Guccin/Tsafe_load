package com.example.tsafe_load;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.BoringLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;


public class MainActivity extends Activity {
    ArrayList<TMapPOIItem> tmppoi = new ArrayList<>();
    boolean flag = false;

    public TMapView tMapView;
    LocationManager lm;

    Button srchButton;
    View Street_lamp;
    View Street_police;
    View Street_setting;
    LinearLayout linearLayoutTmap;
    FloatingActionButton floatingActionButton;

    TMapMarkerItem markerStart = new TMapMarkerItem();
    TMapMarkerItem markerEnd = new TMapMarkerItem();
    TMapMarkerItem markerCur = new TMapMarkerItem();

    TMapMarkerItem[] streetLight = new TMapMarkerItem[100];


    public double[] startGPS = new double[2];
    public String startName;
    public double[] endGPS = new double[2];
    public String endName;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            if (resultCode == 200) {

                Toast.makeText(MainActivity.this, "Result: " + data.getStringExtra("start_address")+"\n"+
                        "lat: " + data.getDoubleExtra("start_lat",1.0)+"\n"+
                        "lon: "+data.getDoubleExtra("start_lon", 1.0), Toast.LENGTH_SHORT).show();
                //출발지와 도착지의 GPS좌표값 저장
                startName = data.getStringExtra("start_address");
                startGPS[0]=data.getDoubleExtra("start_lat",1.0);
                startGPS[1]=data.getDoubleExtra("start_lon",1.0);
                endName = data.getStringExtra("end_address");
                endGPS[0]=data.getDoubleExtra("end_lat",1.0);
                endGPS[1]=data.getDoubleExtra("end_lon",1.0);
                setmark(startGPS,startName, markerStart);
                setmark(endGPS, endName, markerEnd);
                Down down = new Down();
                down.execute(startGPS[0], startGPS[1], endGPS[0], endGPS[1]);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        srchButton = (Button) findViewById(R.id.search_load);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        linearLayoutTmap = (LinearLayout) findViewById(R.id.linearLayoutTmap);
        Street_lamp = (View) findViewById(R.id.Street_lamp);
        Street_police = (View) findViewById(R.id.Street_police);
        Street_setting = (View) findViewById(R.id.Street_setting);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        tMapView = new TMapView(this);

        tMapView.setSKTMapApiKey("l7xx62fb5e4a60904039a3d5ff7e62318cd2");
        linearLayoutTmap.addView(tMapView);

        //가로등 객체배열 초기화 선언
        for(int i=0; i<100; i++){
            streetLight[i] = new TMapMarkerItem();
        }
        srchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FindLoad.class);
                startActivityForResult(intent,200);
            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions( MainActivity.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                            0 );
                }else{
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    String provider = location.getProvider();
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    double altitude = location.getAltitude();

                    Log.d("위치","위치정보 : " + provider + "\n" +
                            "위도 : " + longitude + "\n" +
                            "경도 : " + latitude + "\n" +
                            "고도  : " + altitude);
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            1000,
                            1,
                            gpsLocationListener);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            1000,
                            1,
                            gpsLocationListener);
                }
            }
        });

        // 클릭 이벤트 설정
        tMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint, PointF pointF) {
                //Toast.makeText(MapEvent.this, "onPress~!", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint, PointF pointF) {
                //Toast.makeText(MainActivity.this, "onPressUp~!", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // 롱 클릭 이벤트 설정
        tMapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {
            @Override
            public void onLongPressEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint) {
                //Toast.makeText(MapEvent.this, "onLongPress~!", Toast.LENGTH_SHORT).show();
            }
        });

        // 지도 스크롤 종료
        tMapView.setOnDisableScrollWithZoomLevelListener(new TMapView.OnDisableScrollWithZoomLevelCallback() {
            @Override
            public void onDisableScrollWithZoomLevelEvent(float zoom, TMapPoint centerPoint) {
                //Toast.makeText(MainActivity.this, "zoomLevel=" + zoom + "\nlon=" + centerPoint.getLongitude() + "\nlat=" + centerPoint.getLatitude(), Toast.LENGTH_SHORT).show();
            }
        });
        Street_lamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputStream is = getResources().openRawResource(R.raw.streetlight);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                try {
                    String line;
                    String[] info;
                    String addressname;
                    double[] GPS = new double[2];
                    int index = 0;
                    while ((line = reader.readLine()) != null) {//라인별로 마킹표시
                        if(line.contains("표찰"))
                            continue;
                        if(index == 100){
                            return;
                        }
                        info = line.split(",");
                        addressname = info[1];
                        GPS[1] = Double.parseDouble(info[3]);//lon
                        GPS[0] = Double.parseDouble(info[4]);//lat
                        //검증 시작 경로에서 가까운 값들 뽑아내기
                        if(startGPS[0] < endGPS[0]){//위도 검증
                            if(startGPS[0] < GPS[0] && endGPS[0] > GPS[0]){
                                if(startGPS[1] < endGPS[1]){//경도 검증
                                    if(startGPS[1] < GPS[1] && endGPS[1] > GPS[1]){
                                        //검증 성공 출발지와 도착지 중간의 값임
                                        setmark(GPS,"light"+addressname,streetLight[index]);
                                        index++;
                                    }
                                }else{
                                    if(startGPS[1] > GPS[1] && endGPS[1] < GPS[1]){
                                        setmark(GPS,"light"+addressname,streetLight[index]);
                                        index++;
                                    }
                                }
                            }
                        }else{
                            if(startGPS[0] > GPS[0] && endGPS[0] < GPS[0]){
                                if(startGPS[1] < endGPS[1]) {//경도 검증
                                    if (startGPS[1] < GPS[1] && endGPS[1] > GPS[1]) {
                                        //위도 경도 검증 종료
                                        setmark(GPS,"light"+addressname,streetLight[index]);
                                        index++;
                                    }
                                }else{
                                    if(startGPS[1] > GPS[1] && endGPS[1] < GPS[1]){
                                        setmark(GPS,"light"+addressname,streetLight[index]);
                                        index++;
                                    }
                                }
                            }
                        }

                        Log.d("라인", info[3] + info[4]);
                    }
                }
                catch (IOException ex) {
                    // handle exception
                }
                finally {
                    try {
                        is.close();
                    }
                    catch (IOException e) {
                        // handle exception
                    }
                }
            }
        });
        Street_police.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        Street_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ReportActivity.class);
                startActivity(intent);
            }
        });

    }
    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            String provider = location.getProvider();
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double altitude = location.getAltitude();

            Log.d("위치2","위치정보 : " + provider + "\n" +
                    "위도 : " + longitude + "\n" +
                    "경도 : " + latitude + "\n" +
                    "고도  : " + altitude);
            tMapView.setCenterPoint(longitude, latitude, true);
            double[] Cur = new double[]{latitude,longitude};
            setmark(Cur,"current", markerCur);
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onProviderDisabled(String provider) {
        }
    };


    public void setmark(double[] GPS, String name, TMapMarkerItem marker){
        TMapMarkerItem markerItem1 = marker;
        TMapPoint tMapPoint1 = new TMapPoint(GPS[0], GPS[1]);
// 마커 아이콘
        Bitmap bitmap;
        if(name.equals("current"))
            bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.current);
        else if(name.contains("light")){
            bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.street_light_icon);
        }
        else
            bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.location);
        markerItem1.setIcon(bitmap); // 마커 아이콘 지정
        markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
        markerItem1.setTMapPoint( tMapPoint1 ); // 마커의 좌표 지정
        markerItem1.setName(name); // 마커의 타이틀 지정
        tMapView.addMarkerItem(name, markerItem1); // 지도에 마커 추가
        tMapView.setCenterPoint(GPS[1], GPS[0]);

    }
    private class Down extends AsyncTask<Double, String, Void>{
        @Override
        protected Void doInBackground(Double... GPS) {
            double[] start = new double[]{GPS[0],GPS[1]};
            double[] end = new double[]{GPS[2],GPS[3]};
            find_way(start, end);
            return null;
        }
        public void find_way(double[] startGPS, double[] endGPS){
            TMapPoint tMapPointStart = new TMapPoint(startGPS[0], startGPS[1]); // SKT타워(출발지)
            TMapPoint tMapPointEnd = new TMapPoint(endGPS[0], endGPS[1]); // N서울타워(목적지)
            //TMapPoint tMapPointStart = new TMapPoint(37.570841, 126.985302); // SKT타워(출발지)
            //TMapPoint tMapPointEnd = new TMapPoint(37.551135, 126.988205); // N서울타워(목적지)

            try {
                TMapPolyLine tMapPolyLine = new TMapData().findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPointStart, tMapPointEnd);
                tMapPolyLine.setLineColor(Color.RED);
                tMapPolyLine.setLineWidth(15);
                tMapView.addTMapPolyLine("Line1", tMapPolyLine);

            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
