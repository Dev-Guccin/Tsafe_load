package com.example.tsafe_load;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;


public class MainActivity extends AppCompatActivity {
    public TMapView tMapView;

    Button srchButton;
    View Street_lamp;
    View Street_police;
    View Street_setting;
    LinearLayout linearLayoutTmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        srchButton = (Button)findViewById(R.id.search_load);
        linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        Street_lamp  = (View)findViewById(R.id.Street_lamp);
        Street_police  = (View)findViewById(R.id.Street_police);
        Street_setting  = (View)findViewById(R.id.Street_setting);


        tMapView = new TMapView(this);

        tMapView.setSKTMapApiKey("l7xx62fb5e4a60904039a3d5ff7e62318cd2");
        linearLayoutTmap.addView(tMapView);

        srchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final LinearLayout ii = (LinearLayout)inflater.inflate(R.layout.searching_road, null);
                ii.setBackgroundColor(Color.parseColor("#99000000"));
                LinearLayout.LayoutParams paramll = new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);
                addContentView(ii, paramll);
                LinearLayout iioutline = ii.findViewById(R.id.외부);
                final EditText iistart = ii.findViewById(R.id.출발지);
                final EditText iiterminate = ii.findViewById(R.id.도착지);
                final TextView iiresult = ii.findViewById(R.id.결과GPS);
                Button iibutton = ii.findViewById(R.id.동선조회);

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
                        setmark(findway.startGPS,iistart.getText().toString());
                        setmark(findway.endGPS,iiterminate.getText().toString());
                        ((ViewManager)ii.getParent()).removeView(ii);

                        Down down = new Down();
                        down.execute(findway.startGPS[0],findway.startGPS[1], findway.endGPS[0],findway.endGPS[1]);
                    }
                });
                iioutline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("똥선", "아웃");
                        ((ViewManager)ii.getParent()).removeView(ii);
                    }
                });

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

    public void setmark(double[] GPS, String name){
        TMapMarkerItem markerItem1 = new TMapMarkerItem();
        TMapPoint tMapPoint1 = new TMapPoint(GPS[0], GPS[1]); // SKT타워
// 마커 아이콘
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.map_pin_red);
        markerItem1.setIcon(bitmap); // 마커 아이콘 지정
        markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
        markerItem1.setTMapPoint( tMapPoint1 ); // 마커의 좌표 지정
        markerItem1.setName(name); // 마커의 타이틀 지정
        tMapView.addMarkerItem("markerItem1", markerItem1); // 지도에 마커 추가
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
                tMapPolyLine.setLineColor(Color.BLUE);
                tMapPolyLine.setLineWidth(2);
                tMapView.addTMapPolyLine("Line1", tMapPolyLine);

            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class FindWay extends MainActivity{
    public double[] startGPS = new double[2];
    public double[] endGPS = new double[2];

    public double[] getGPS(String addressName) {
        double[] GPS = new double[2];
        Geocoder geocoder = new Geocoder(this);
        List<Address> list = null;
        String str = addressName;
        try {
            list = geocoder.getFromLocationName(str, 10); // 지역이름, 읽을갯수
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생");
        }

        if (list != null) {
            if (list.size() == 0) {
                Log.d("똥선", "해당되는 주소 없음");
            } else {
                // 해당되는 주소로 인텐트 날리기
                Address addr = list.get(0);
                double lat = addr.getLatitude();
                double lon = addr.getLongitude();

                String sss = String.format("geo:%f,%f", lat, lon);
                Log.d("똥선", Double.toString(lat) + "/" + Double.toString(lon));
                GPS[0] = lat;
                GPS[1] = lon;
                return GPS;
            }
        }
        //적당한 위치를 발견하지 못하는 경우
        GPS[0]=0;
        GPS[1]=0;
        return GPS;
    }

}