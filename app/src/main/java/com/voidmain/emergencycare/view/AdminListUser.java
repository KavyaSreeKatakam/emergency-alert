package com.voidmain.emergencycare.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.voidmain.emergencycare.R;
import com.voidmain.emergencycare.dao.DAO;
import com.voidmain.emergencycare.form.User;
import com.voidmain.emergencycare.util.Constants;
import com.voidmain.emergencycare.util.MapUtil;
import com.voidmain.emergencycare.util.Session;

public class AdminListUser extends AppCompatActivity {

    ListView listView;
    RadioGroup radioGroup;
    RadioButton radioButton;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list_user);

        listView=(ListView) findViewById(R.id.AdminUsersList);

        DAO dao=new DAO();
        dao.setDataToAdapterList(listView,User.class, Constants.USER_DB,"all");

        final Session s=new Session(getApplicationContext());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Log.v("in list action perform ","in list action perform");

                String item = listView.getItemAtPosition(i).toString();
                item= MapUtil.stringToMap(s.getViewMap()).get(item);

                Intent intent= new Intent(getApplicationContext(),ViewUser.class);;
                intent.putExtra("userid",item);
                startActivity(intent);
            }
        });
    }
}
