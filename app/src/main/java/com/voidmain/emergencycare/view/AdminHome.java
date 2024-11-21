package com.voidmain.emergencycare.view;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.voidmain.emergencycare.MainActivity;
import com.voidmain.emergencycare.R;
import com.voidmain.emergencycare.util.Session;

public class AdminHome extends AppCompatActivity {

    private Session session;

    Button addAmbulance;
    Button addHospital;
    Button adminLogout;
    Button viewUsers;
    Button addbloodbank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        addHospital=(Button) findViewById(R.id.addhospital);
        addAmbulance=(Button) findViewById(R.id.addambulance);
        addbloodbank=(Button) findViewById(R.id.addbloodbank);
        viewUsers=(Button) findViewById(R.id.adminviewusers);
        adminLogout=(Button) findViewById(R.id.adminlogout);


        final Session s = new Session(getApplicationContext());

        addHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), AddHospital.class);
                startActivity(i);
            }
        });

        addAmbulance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(), AddAmbulance.class);
                startActivity(i);
            }
        });

        addbloodbank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(), AddBloodBank.class);
                startActivity(i);
            }
        });

        adminLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                s.loggingOut();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });

        viewUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.v("in list view action ","");
                Intent i = new Intent(getApplicationContext(),AdminListUser.class);
                startActivity(i);
            }
        });
    }
}