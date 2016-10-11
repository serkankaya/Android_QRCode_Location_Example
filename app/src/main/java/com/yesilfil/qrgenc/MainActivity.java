package com.yesilfil.qrgenc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

public class MainActivity extends AppCompatActivity {
    ImageButton tara;
    TextView lokasyonn;
    String urunID = "0";
    double Longitude = 0, Latitude = 0;
    private LocationManager locationManager;
    private LocationListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tara = (ImageButton) findViewById(R.id.tarabutton);
        lokasyonn = (TextView) findViewById(R.id.lokasyon);


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Longitude = location.getLongitude();
                Latitude = location.getLatitude();

                /*Toast.makeText(MainActivity.this, "Long :"+location.getLongitude()+" "+"Loc :" + location.getLatitude() , Toast.LENGTH_SHORT).show();*/
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        configure_button();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            TextView tvlink = (TextView) findViewById(R.id.linktext);
            if (resultCode == RESULT_OK) {
                tvlink.setText(intent.getStringExtra("SCAN_RESULT"));
                urunID = intent.getStringExtra("SCAN_RESULT");
                String tokenid = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);

                String url = "http://jsonbulut.com/json/likeManagement.php?ref=7b7392076900968d8e4ad78351ad55d3&productId=" + urunID + "&vote=3&customerId=5&long=" + Longitude + "&lat=" + Latitude + "&tokenID=" + tokenid;
                Log.d("url :", url);
                Toast.makeText(this, "URL : "+ url, Toast.LENGTH_SHORT).show();
                lokasyonn.setText("Long :" + Longitude + " " + "Loc :" + Latitude);
                new urunKontrol(url, this).execute();


            } else if (resultCode == RESULT_CANCELED) {
                tvlink.setText("Taramadan Vazgeçildi");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    void configure_button() {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        tara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);    //Barcode Scanner to scan for us

                locationManager.requestLocationUpdates("gps", 900000000, 0, listener);
                Thread th = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                               return;
                            }

                        } catch (Exception ex) {

                        } finally {

                        }
                    }
                };
                th.start();

            }


        });

    }


    class urunKontrol extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pro;
        String data = "";
        String url = "";

        public urunKontrol(String url, Activity ac) {
            this.url = url;
            pro = new ProgressDialog(ac);
            pro.setMessage("Lütfen Bekleyiniz ...");
            pro.show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... param) {
            try {
                data = Jsoup.connect(url).ignoreContentType(true).execute().body();
            } catch (Exception ex) {
                Log.d("Json hatası : ", ex.toString());
            } finally {
                pro.dismiss();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void o) {
            super.onPostExecute(o);
            // data çözümleme
            try {
                JSONObject obj = new JSONObject(data);
                JSONArray arr = obj.getJSONArray("votes");
                JSONObject oj = arr.getJSONObject(0);
                //denetim yapılıyor
                if (oj.getBoolean("durum")) {
                    Toast.makeText(getApplication(), oj.getString("mesaj"), Toast.LENGTH_SHORT).show();
                    //giriş başarılı aşağıdaki işlemleri yap
                } else {
                    Toast.makeText(getApplication(), oj.getString("mesaj"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.d("Giriş hatası : ", e.toString());
            }


            Log.d("Gelen Data : ", data);
        }
    }


}
