package solemate.solemate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


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
        private int currentId = 0;
        private String [] locationArray = {"Pa has just left the house", "Pa has just reached IMM Shopping Centre", "Pa has just reached Jurong East BLK 71 Coffee Shop"};
        private NotificationManager notifManager;
        private int counterLocation = 0;

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

//            tsChannel = new ThingSpeakChannel(595680);
//            tsChannel.setChannelFeedUpdateListener(new ThingSpeakChannel.ChannelFeedUpdateListener() {
//                @Override
//                public void onChannelFeedUpdated(long channelId, String channelName, ChannelFeed channelFeed) {
//                    // Make use of your Channel feed here!
////                    getSupportActionBar().setTitle(channelName);
////                    getSupportActionBar().setSubtitle("Channel " + channelId);
//                    // Notify last update time of the Channel feed through a Toast message
//                    Date lastUpdate = channelFeed.getChannel().getUpdatedAt();
//                    Toast.makeText(MapsActivity.this, lastUpdate.toString(), Toast.LENGTH_LONG).show();
//                    textView.setText(lastUpdate.toString());
//                }
//            });
//            tsChannel.loadChannelFeed();

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
                            textView.setText("Your Current Location: " + address); //For Testing, display coordinates on textView every time it's triggered
                            marker = mMap.addMarker(new MarkerOptions().position(newLocation).title("Your current location:"));
                            arrayMarker.add(0, marker);
                            builder.include(marker.getPosition());
                            marker.setSnippet(address);
                        } else {
                            marker.setPosition(newLocation);
                            String address = getAddressFromCoordinates(location.getLatitude(), location.getLongitude());
//                            textView.setText("\nCurrent Location: " + address);
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

//            Intent jsonIntent = new Intent(MapsActivity.this, GetYourJsonTask.class);
//            jsonIntent.putExtra("key", currentId); //Optional parameters
//            MapsActivity.this.startActivity(jsonIntent);

            new GetYourJsonTask2().execute(new ApiConnector());

//        Retrieve Info from ThingSpeak
//        String lightApi = "https://api.thingspeak.com/channels/595680/fields/1.json?results=2";
//        JsonObjectRequest objectRequest =new JsonObjectRequest(Request.Method.GET, lightApi, null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        textView.append("lala");
//                        try {
//                            JSONArray feeds = response.getJSONArray("feeds");
//                            for(int i=0; i<feeds.length();i++){
//                                JSONObject jo = feeds.getJSONObject(i);
//                                String l=jo.getString("field1");
//                                Toast.makeText(getApplicationContext(),l,Toast.LENGTH_SHORT).show();
//                                textView.append(l);
//
//                            }
//                        } catch (JSONException e) {
//                            textView.append("error");
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//            }
//        });



    }

        private class GetYourJsonTask2 extends AsyncTask<ApiConnector,Long,JSONObject>
        {
            int id = 0;
            private Context context;
            private AsyncListener asyncInterface;

            @Override
            protected JSONObject doInBackground(ApiConnector... params) {

                // it is executed on Background thread
                System.out.println("test" + params[0].GetYourJson());
                try {
                    id = Integer.parseInt(params[0].GetYourJson().getString("entry_id"));
                    if (currentId == 0 && currentId != id){
                        currentId = id;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return params[0].GetYourJson();
            }
            @Override
            protected void onPostExecute(JSONObject jsonArray) {
                //TODO: Do what you want with your json here
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        new GetYourJsonTask2().execute(new ApiConnector());
                    }
                }, 300);



                if (currentId != id){
                    textView = (TextView) findViewById(R.id.textView);
                    currentId = id;
                    createNotification(locationArray[counterLocation % 3]);
                    updateLocationMarker();
                    ++counterLocation;
                }

                System.out.println("afterback" + currentId);

//                Intent intent = new Intent(context, MapsActivity.class);
//                context.startActivity(intent);
//                ((Activity)context).finish();
            }
        }

        public void createNotification(String aMessage) {
            final int NOTIFY_ID = 1002;

            // There are hardcoding only for show it's just strings
            String name = "my_package_channel";
            String id = "my_package_channel_1"; // The user-visible name of the channel.
            String description = "my_package_first_channel"; // The user-visible description of the channel.

            Intent intent;
            PendingIntent pendingIntent;
            NotificationCompat.Builder builder;

            if (notifManager == null) {
                notifManager =
                        (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = notifManager.getNotificationChannel(id);
                if (mChannel == null) {
                    mChannel = new NotificationChannel(id, name, importance);
                    mChannel.setDescription(description);
                    mChannel.enableVibration(true);
                    mChannel.setLightColor(Color.GREEN);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notifManager.createNotificationChannel(mChannel);
                }
                builder = new NotificationCompat.Builder(this, id);

                intent = new Intent(this, MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                builder.setContentTitle(aMessage)  // required
                        .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                        .setContentText(this.getString(R.string.app_name))  // required
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setTicker(aMessage)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            } else {

                builder = new NotificationCompat.Builder(this);

                intent = new Intent(this, MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                builder.setContentTitle(aMessage)                           // required
                        .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                        .setContentText(this.getString(R.string.app_name))  // required
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setTicker(aMessage)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setPriority(Notification.PRIORITY_HIGH);
            } // else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification notification = builder.build();
            notifManager.notify(NOTIFY_ID, notification);
        }

        public void updateLocationMarker () {
            DateFormat df = DateFormat.getTimeInstance();
            df.setTimeZone(TimeZone.getDefault());
            String gmtTime = df.format(new Date());

            if (counterLocation == 0) {
                Double latitude = 1.336085;
                Double longitude = 103.741876;
                LatLng patientLocation = new LatLng(latitude, longitude);
                String address = getAddressFromCoordinates(latitude, longitude);
                patient_marker = mMap.addMarker(new MarkerOptions().position(patientLocation).title("Patient's location:").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//                textView.append("\nPatient's Location: " + address);
                patient_marker.setSnippet(address);
                builder.include(patient_marker.getPosition());
                //Modify this to include more markers in future
                arrayMarker.add(1, patient_marker);
                textView.append("\n"+locationArray[counterLocation] + " at " + gmtTime);
            }
            else if (counterLocation == 1) {
                Double latitude = 1.334937;
                Double longitude = 103.746945;
                LatLng patientLocation = new LatLng(latitude, longitude);
                String address = getAddressFromCoordinates(latitude, longitude);
                patient_marker = mMap.addMarker(new MarkerOptions().position(patientLocation).title("Patient's location 2:").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                patient_marker.setSnippet(address);
                builder.include(patient_marker.getPosition());
                //Modify this to include more markers in future
                arrayMarker.add(2, patient_marker);
                textView.append("\n"+locationArray[counterLocation] + " at " + gmtTime);
            }

        }




}