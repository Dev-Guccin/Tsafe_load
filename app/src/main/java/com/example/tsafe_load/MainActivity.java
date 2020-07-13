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

import java.io.IOException;
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

        srchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FindLoad.class);
                startActivityForResult(intent,200);
                /*LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final LinearLayout ii = (LinearLayout) inflater.inflate(R.layout.searching_road, null);
                ii.setBackgroundColor(Color.parseColor("#99000000"));
                LinearLayout.LayoutParams paramll = new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
                addContentView(ii, paramll);
                LinearLayout iioutline = ii.findViewById(R.id.외부);
                final EditText iistart = ii.findViewById(R.id.출발지);
                final ImageButton iistartsearch = ii.findViewById(R.id.출발지검색);
                final EditText iiterminate = ii.findViewById(R.id.도착지);
                Button iibutton = ii.findViewById(R.id.동선조회);

                iistart.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        // input창에 문자를 입력할때마다 호출된다.
                        // search 메소드를 호출한다.
                    }
                });

                iibutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("똥선", iistart.getText().toString());
                        Log.d("똥선", iiterminate.getText().toString());
                        FindWay findway = new FindWay();
                        findway.startGPS = findway.getGPS(iistart.getText().toString());
                        findway.endGPS = findway.getGPS(iiterminate.getText().toString());


                        Log.d("출발지", findway.startGPS.toString());
                        Log.d("도착지", findway.endGPS.toString());
                        //findway.find_way();
                        setmark(findway.startGPS, iistart.getText().toString(), markerStart);
                        setmark(findway.endGPS, iiterminate.getText().toString(), markerEnd);
                        ((ViewManager) ii.getParent()).removeView(ii);

                        Down down = new Down();
                        down.execute(findway.startGPS[0], findway.startGPS[1], findway.endGPS[0], findway.endGPS[1]);
                    }
                });
                iioutline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("똥선", "아웃");
                        ((ViewManager) ii.getParent()).removeView(ii);
                    }
                });
                iistartsearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            new Search().execute(iistart.getText().toString()).get();
                            Log.d("플래그", String.valueOf(flag));
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ArrayList<String> addresslist = new ArrayList<>();

                        Log.d("플래그", String.valueOf(flag));
                        //poi에 있는 주소를 긁어옴
                        flag = false;
                        Log.d("사이즈", Integer.toString(tmppoi.size()));
                        for(int i =0; i< tmppoi.size(); i++){
                            addresslist.add(tmppoi.get(i).name);//주소를 하나씩 입력한다.
                            Log.d("POI", addresslist.get(i));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                MainActivity.this, //context(액티비티 인스턴스)
                                android.R.layout.simple_list_item_1, // 한 줄에 하나의 텍스트 아이템만 보여주는 레이아웃 파일
                                // 한 줄에 보여지는 아이템 갯수나 구성을 변경하려면 여기에 새로만든 레이아웃을 지정하면 됩니다.
                                addresslist  // 데이터가 저장되어 있는 ArrayList 객체
                        );
                        ListView listview = findViewById(R.id.address_list);
                        listview.setAdapter(adapter);
                    }
                });*/

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
