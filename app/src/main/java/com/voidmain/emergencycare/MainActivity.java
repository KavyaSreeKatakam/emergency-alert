package com.voidmain.emergencycare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.voidmain.emergencycare.dao.DAO;
import com.voidmain.emergencycare.form.Family;
import com.voidmain.emergencycare.form.User;
import com.voidmain.emergencycare.util.Constants;
import com.voidmain.emergencycare.util.Session;
import com.voidmain.emergencycare.view.ListUsers;
import com.voidmain.emergencycare.view.LoginActivity;
import com.voidmain.emergencycare.view.RegisterActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;

import com.google.android.gms.location.LocationListener;

//2020emergencycare@gmail.com
public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Button b1, b2, b3, b5;
    SQLiteDatabase sqLiteDatabase;
    String userName;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;

    private static final String TAG = "MainActivity";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;

    private String txtLocation;

    private boolean isGPS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(getApplicationContext(), "dont have accelerometer sensor", Toast.LENGTH_LONG).show();
        }

        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });

        createLocationRequest();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        b1 = (Button) findViewById(R.id.loginButton1);
        b2 = (Button) findViewById(R.id.registerButton);
        b3 = (Button) findViewById(R.id.emergencyalert3);
        b5 = (Button) findViewById(R.id.userviewusers2);

        final Session session = new Session(getApplicationContext());

        sqLiteDatabase = openOrCreateDatabase(Constants.sqLiteDatabase, MODE_PRIVATE, null);
        sqLiteDatabase.execSQL("create table if not exists login(username varchar)");
        Cursor cursor = sqLiteDatabase.rawQuery("select * from login", null);

        if (cursor != null && cursor.moveToFirst()) {
            userName = cursor.getString(cursor.getColumnIndex("username"));
            cursor.close();
        }
        if (userName != null && userName != "") {
            session.setusename(userName);
            session.setRole("user");

            b1.setEnabled(false);
            b2.setEnabled(false);
        } else {

            b3.setEnabled(false);
        }

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (null != mCurrentLocation) {

                    String lat = String.valueOf(mCurrentLocation.getLatitude());
                    String lng = String.valueOf(mCurrentLocation.getLongitude());

                    txtLocation = lat + "," + lng;

                    Toast.makeText(getApplicationContext(),"Location:"+txtLocation,Toast.LENGTH_LONG).show();
                }

                if (txtLocation != null) {

                    final String[] userLatLongs = txtLocation.split(",");

                    DAO d = new DAO();
                    d.getDBReference(Constants.FAMILY_DB).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshotNode : dataSnapshot.getChildren()) {

                                final Family family = snapshotNode.getValue(Family.class);

                                if (family != null && family.getUserName().equals(userName)) {

                                    final Set<String> senders = new HashSet<>();

                                    senders.add(family.getMobile1());
                                    senders.add(family.getMobile2());
                                    senders.add(family.getMobile3());

                                    DAO d = new DAO();
                                    d.getDBReference(Constants.USER_DB).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            for (DataSnapshot snapshotNode : dataSnapshot.getChildren()) {

                                                User user = (User) snapshotNode.getValue(User.class);

                                                if (user != null) {
                                                    Log.v("user info :", user.toString());

                                                    if (!user.getType().equals("user")) {

                                                        String[] latLongs = user.getAddress().split(",");

                                                        float distance = getDistanceFromCurrentPosition(new Double(userLatLongs[0]), new Double(userLatLongs[1]), new Double(latLongs[0]), new Double(latLongs[1]));

                                                        if (distance < 10000) {
                                                            senders.add(user.getMobile());
                                                        }
                                                    }
                                                }
                                            }

                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                                            ArrayList<PendingIntent> pendingIntents = new ArrayList<PendingIntent>();
                                            pendingIntents.add(pi);

                                            //Get the SmsManager instance and call the sendTextMessage method to send message
                                            SmsManager sms = SmsManager.getDefault();

                                            for (String mobile : senders) {
                                                ArrayList<String> parts = sms.divideMessage(userName + " is in Emergency at https://maps.google.com/?q=" + userLatLongs[0] + "," + userLatLongs[1]);
                                                //smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                                                sms.sendMultipartTextMessage(mobile, null, parts,
                                                        pendingIntents, null);

                                                Toast.makeText(getApplicationContext(), "Message Sent successfully!",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Location Not Found",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("going to list users :", "");
                Intent i = new Intent(getApplicationContext(), ListUsers.class);
                startActivity(i);
            }
        });
    }

    public static float getDistanceFromCurrentPosition(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.75;

        double dLat = Math.toRadians(lat2 - lat1);

        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double dist = earthRadius * c;

        int meterConversion = 1609;

        return new Float(dist * meterConversion).floatValue();

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {

        if (!isGPS) {
            Toast.makeText(getApplicationContext(), "Please turn on GPS", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, AppConstants.LOCATION_REQUEST);
        }
        else {

            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.d(TAG, "Location update started ..............: ");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        if (null != mCurrentLocation) {

            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());

            txtLocation = lat + "," + lng;

            Toast.makeText(getApplicationContext(),"Location:"+txtLocation,Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopLocationUpdates();
        sensorManager.unregisterListener(this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                            mGoogleApiClient, mLocationRequest, this);
                    Log.d(TAG, "Location update started ..............: ");

                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > 100) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

            if (speed > SHAKE_THRESHOLD) {

                if (null != mCurrentLocation) {

                    String lat = String.valueOf(mCurrentLocation.getLatitude());
                    String lng = String.valueOf(mCurrentLocation.getLongitude());

                    txtLocation = lat + "," + lng;

                    Toast.makeText(getApplicationContext(),"Location:"+txtLocation,Toast.LENGTH_LONG).show();
                }

                if(txtLocation!=null)
                {
                    final String[] userLatLongs=txtLocation.split(",");

                    DAO d=new DAO();
                    d.getDBReference(Constants.FAMILY_DB).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshotNode: dataSnapshot.getChildren()) {

                                final Family family=snapshotNode.getValue(Family.class);

                                if(family!=null && family.getUserName().equals(userName)) {

                                    final Set<String> senders = new HashSet<>();

                                    senders.add(family.getMobile1());
                                    senders.add(family.getMobile2());
                                    senders.add(family.getMobile3());

                                    DAO d=new DAO();
                                    d.getDBReference(Constants.USER_DB).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            for (DataSnapshot snapshotNode: dataSnapshot.getChildren()) {

                                                User user=(User)snapshotNode.getValue(User.class);

                                                if(user!=null)
                                                {
                                                    Log.v("user info :",user.toString());

                                                    if(!user.getType().equals("user")) {

                                                        String[] latLongs=user.getAddress().split(",");

                                                        float distance = getDistanceFromCurrentPosition(new Double(userLatLongs[0]), new Double(userLatLongs[1]), new Double(latLongs[0]), new Double(latLongs[1]));

                                                        if(distance<10000)
                                                        {
                                                            senders.add(user.getMobile());
                                                        }
                                                    }
                                                }
                                            }

                                            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                                            PendingIntent pi=PendingIntent.getActivity(getApplicationContext(), 0, intent,0);

                                            ArrayList<PendingIntent> pendingIntents=new ArrayList<PendingIntent>();
                                            pendingIntents.add(pi);

                                            //Get the SmsManager instance and call the sendTextMessage method to send message
                                            SmsManager sms=SmsManager.getDefault();

                                            for(String mobile : senders)
                                            {
                                                ArrayList<String> parts = sms.divideMessage(userName+" is in Emergency at https://maps.google.com/?q="+userLatLongs[0]+","+userLatLongs[1]);
                                                //smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                                                sms.sendMultipartTextMessage(mobile, null, parts,
                                                        pendingIntents, null);

                                                Toast.makeText(getApplicationContext(), "Message Sent successfully!",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                Toast.makeText(getApplicationContext(),"accident occured x: "+x+" \n y:"+y+" \n z:"+z+" \n speed:"+speed,Toast.LENGTH_LONG).show();
            }

            last_x = x;
            last_y = y;
            last_z = z;
        }
    }
}