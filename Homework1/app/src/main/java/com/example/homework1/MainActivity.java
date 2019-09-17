/*
Names: Alexis Abrego (AMA5788), Vinisha Venugopal (vv6523
Homework 1 - Android App
9/17/19
Github link: https://github.com/vinishavenu53/Vinisha-Alexis-Homework1
 */

package com.example.homework1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import static java.lang.Double.parseDouble;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    EditText address;
    Button submit;
    TextView latLong;
    public volatile static String lat = "0.0";
    public volatile static String lng = "0.0";
    String temp, humidity, windSpeed, precipitationChance;
    MapView mapView;
    GoogleMap googleMap;
    String total = "";

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        address = (EditText) findViewById(R.id.editText_address);
        latLong = (TextView) findViewById(R.id.latLongTextView);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
            }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        String latString;
        String lngString;

        StringTokenizer st = new StringTokenizer(total);

        latString = st.nextToken();
        lngString = st.nextToken();

        LatLng coordinates = new LatLng(parseDouble(latString), parseDouble(lngString));

        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(coordinates).title("Marker"));
        float zoomLevel = 16.0f; //This goes up to 21
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoomLevel));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

        @Override
        protected void onPause() {
            mapView.onPause();
            super.onPause();
        }

        @Override
        protected void onDestroy() {
            mapView.onDestroy();
            super.onDestroy();
        }

        @Override
        public void onLowMemory() {
            super.onLowMemory();
            mapView.onLowMemory();
        }

    private class GetCoordThread extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            String urlString = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=AIzaSyDt6U6xgrGnoFbyeEWCYpKiRFBZck7DLVU", strings[0]);
            String response = "";
            //String total = "";
            try {
                URL url = new URL(urlString);
                response = getData(url);
                JSONObject jsonObject = new JSONObject(response);
                lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry").getJSONObject("location").get("lat").toString();
                lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry").getJSONObject("location").get("lng").toString();



                urlString = String.format("https://api.darksky.net/forecast/4450465d6b3a87adab67446d8ea47f48/%s,%s", lat, lng);
                url = new URL(urlString);
                response = getWeatherData(url);
                jsonObject = new JSONObject(response);
                temp = jsonObject.getJSONObject("currently").get("temperature").toString();
                humidity = jsonObject.getJSONObject("currently").get("humidity").toString();
                windSpeed = jsonObject.getJSONObject("currently").get("windSpeed").toString();
                precipitationChance = jsonObject.getJSONObject("currently").get("precipProbability").toString();
                System.out.println(temp + " " + humidity + " " + windSpeed + " " + " " + precipitationChance);
                total = lat + " " + lng + " " + temp + " " + humidity + " " + windSpeed + " " + precipitationChance;
            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return total;

        }
    }

    public void getWeather(View view){
        GetCoordThread gct = new GetCoordThread();
        String addressString = address.getText().toString().replace(" ", "+");
        try {
           total = gct.execute(addressString).get();        //to fix the multithreading-global variable issue
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        StringTokenizer st = new StringTokenizer(total);

        String latString = st.nextToken();
        String lngString = st.nextToken();
        String tempString = st.nextToken();
        String humidityString = st.nextToken();
        String windSpeedString = st.nextToken();
        String precipString = st.nextToken();

        latLong.setText("Latitude: " + latString + "°\t\t\t"
        + "Longitude: " + lngString + "°\n"
        + "Temperature: " + tempString + "°F\t\t\t"
        + "Humidity: " + humidityString + "%\n"
        + "Wind Speed: " + windSpeedString + " mph\t\t\t"
        + "Chances of Precipitation: " + precipString + "%\n");
        //weather.setText();
        mapView.getMapAsync(this);
        mapView.onStart();
    }

    //Vinisha is driving now
    String getData(URL url){
        String response = "";
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while((line = br.readLine()) != null){
                    response +=line;
                }
            }
            else{
                response = "";
            }
        }
        catch(Exception e){e.printStackTrace();}
        return response;
    }

    //end of Vinisha driving, Alexis is driving now
    String getWeatherData(URL url){
        String response = "";
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while((line = br.readLine()) != null){
                    response +=line;
                }
            }
            else{
                response = "";
            }
        }
        catch(Exception e){e.printStackTrace();}
        return response;
    }
    //end of Alexis driving, Vinisha and Alexis driving (debugging)

}

