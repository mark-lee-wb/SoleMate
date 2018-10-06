package solemate.solemate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.macroyau.thingspeakandroid.ThingSpeakChannel;
import com.macroyau.thingspeakandroid.model.ChannelFeed;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


//Need to query every 1 sec for any new updates on the data?

    public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

        private GoogleMap mMap;
        private Button button;
        private TextView textView;
        private LocationManager locationManager;
        private LocationListener locationListener;
        private BroadcastReceiver broadcastReceiver;
        private IntentFilter intentFilter;
        private Marker marker = null;
        private Marker patient_marker = null;
        private ArrayList<Marker> arrayMarker = new ArrayList<>();
        private LatLngBounds.Builder builder = new LatLngBounds.Builder();
        private LatLngBounds bounds;
        private SupportMapFragment mapFragment;
        private databaseHelper myDb = new databaseHelper(this);
        private ThingSpeakChannel tsChannel;

//    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            //Display coordinates upon receiving sms
//            textView = (TextView) findViewById(R.id.textView);
//            String message = intent.getExtras().getString("message");
//
//            //Tokenize string to check for valid SMS
//            String [] parts = message.split("_");
//            String condition_check = parts[0];
//
//                if (condition_check.equals("SoleMate2018")){
//                    Double latitude = Double.parseDouble(parts[1]);
//                    Double longitude = Double.parseDouble(parts[2]);
//                    LatLng patientLocation = new LatLng(latitude, longitude);
//                    String address = getAddressFromCoordinates(latitude, longitude);
//                    patient_marker = mMap.addMarker(new MarkerOptions().position(patientLocation).title("Patient's location:").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//                    textView.append("\nPatient's Location: "+ address);
//                    patient_marker.setSnippet(address);
//                    builder.include(patient_marker.getPosition());
//                    //Modify this to include more markers in future
//                    arrayMarker.add(1, patient_marker);
//                    renderCamera();
//                    myDb.insertLocationData(1, latitude, longitude, System.currentTimeMillis());
//                    Cursor res = myDb.getLastEntry();
//                    if(res.getCount() == 0) {
//                        // show message
//                        return;
//                    }
//
//                    StringBuffer buffer = new StringBuffer();
//                    while (res.moveToNext()) {
//                        buffer.append("Id :"+ res.getString(0)+"\n");
//                        buffer.append("Patient_ID :"+ res.getString(1)+"\n");
//                        buffer.append("Latitude :"+ res.getString(2)+"\n");
//                        buffer.append("Longitude :"+ res.getString(3)+"\n");
//                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
//                        Date resultdate = new Date(res.getLong(4));
//                        buffer.append("Timestamp :"+ resultdate +"\n\n");
//                    }
//
//                    // Show all data
//                    textView.append(buffer.toString());
//                }
//
//
//
//        }
//    };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            //Request for permissions
            checkPermission();
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_maps);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


            //button

            button = (Button) findViewById(R.id.button);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    registerReceiver(broadcastReceiver, new IntentFilter("location_update"));

                    retrieveInformation();

                }
            });

            textView = (TextView) findViewById(R.id.textView);

            //scrollable - might not be necessary in final setup
            textView.setMovementMethod(new ScrollingMovementMethod());

            tsChannel = new ThingSpeakChannel(595680);
            tsChannel.setChannelFeedUpdateListener(new ThingSpeakChannel.ChannelFeedUpdateListener() {
                @Override
                public void onChannelFeedUpdated(long channelId, String channelName, ChannelFeed channelFeed) {
                    // Make use of your Channel feed here!
//                    getSupportActionBar().setTitle(channelName);
//                    getSupportActionBar().setSubtitle("Channel " + channelId);
                    // Notify last update time of the Channel feed through a Toast message
                    Date lastUpdate = channelFeed.getChannel().getUpdatedAt();
                    Toast.makeText(MapsActivity.this, lastUpdate.toString(), Toast.LENGTH_LONG).show();
                }
            });
            tsChannel.loadChannelFeed();

            retrieveInformation();

        }


        @Override
        protected void onResume() {
//        registerReceiver(intentReceiver, intentFilter);
            checkPermission();
            super.onResume();
        }

        @Override
        protected void onPause() {
//        unregisterReceiver(intentReceiver);
            super.onPause();
        }

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */
        @SuppressLint("MissingPermission")
        @Override
        public void onMapReady(GoogleMap googleMap) {
            retrieveInformation();
            mMap = googleMap;
            //User set location = true
            try {
                mMap.setMyLocationEnabled(true);

                //Test - Get Location Instantly once APP starts


                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
//                textView.append("\n Current Location: "+location.getLatitude() + " " + location.getLongitude()); //For Testing, display coordinates on textView every time it's triggered
                        LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if (marker == null) {
                            String address = getAddressFromCoordinates(location.getLatitude(), location.getLongitude());
                            textView.setText("\nCurrent Location: " + address); //For Testing, display coordinates on textView every time it's triggered
                            marker = mMap.addMarker(new MarkerOptions().position(newLocation).title("Your current location:"));
                            arrayMarker.add(0, marker);
                            builder.include(marker.getPosition());
                            marker.setSnippet(address);
                        } else {
                            marker.setPosition(newLocation);
                            String address = getAddressFromCoordinates(location.getLatitude(), location.getLongitude());
                            textView.setText("\nCurrent Location: " + address);
                            marker.setTitle("Your current location:");
                            marker.setSnippet(address);
                        }
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 14.0f));
                        renderCamera();
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {
                        //if GPS is down
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                };

                locationManager.requestLocationUpdates("gps", 8000, 0, locationListener);

                Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                startService(i);

            } catch (Exception e) {
                System.out.println(e);
            }
        }

        public void checkPermission() {

            //FINE_LOCATION
            if (ContextCompat.checkSelfPermission(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
                } else {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
                }

            } else {
                // do nothing
            }

            //COARSE_LOCATION
            if (ContextCompat.checkSelfPermission(MapsActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
                } else {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
                }

            } else {
                // do nothing
            }

            //Permission for Internet
            if (ContextCompat.checkSelfPermission(MapsActivity.this,
                    Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                        Manifest.permission.INTERNET)) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.INTERNET}, 10);
                } else {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.INTERNET}, 10);
                }

            } else {
                // do nothing
            }

        }

        public String getAddressFromCoordinates(double latitude, double longitude) {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());


            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//            String city = addresses.get(0).getLocality();
//            String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                return address;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        public void renderCamera() {
            bounds = builder.build();
            int width = mapFragment.getView().getMeasuredWidth();
            int height = mapFragment.getView().getMeasuredHeight();
//        int width = getResources().getDisplayMetrics().widthPixels;
//        int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (height * 0.10); // offset from edges of the map 10% of screen
            CameraUpdate cu;
            if (arrayMarker.size() == 1) {
                cu = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 12F);
            } else {
                cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            }

            mMap.animateCamera(cu);
        }

        public void AddLocationData(Integer patient_id, Double latitude, Double longitude, Long timeStamp) {
            boolean isInserted = myDb.insertLocationData(patient_id,
                    latitude,
                    longitude,
                    timeStamp);
        }

        public void retrieveInformation() {
            textView.setText("retrieve info");
//        Retrieve Info from ThingSpeak
        String lightApi = "https://api.thingspeak.com/channels/595680/fields/1.json?results=2";
        JsonObjectRequest objectRequest =new JsonObjectRequest(Request.Method.GET, lightApi, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        textView.append("lala");
                        try {
                            JSONArray feeds = response.getJSONArray("feeds");
                            for(int i=0; i<feeds.length();i++){
                                JSONObject jo = feeds.getJSONObject(i);
                                String l=jo.getString("field1");
                                Toast.makeText(getApplicationContext(),l,Toast.LENGTH_SHORT).show();
                                textView.append(l);

                            }
                        } catch (JSONException e) {
                            textView.append("error");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });



    }


}