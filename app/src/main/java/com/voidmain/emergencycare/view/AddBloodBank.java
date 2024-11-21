package com.voidmain.emergencycare.view;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.voidmain.emergencycare.R;
import com.voidmain.emergencycare.dao.DAO;
import com.voidmain.emergencycare.form.User;
import com.voidmain.emergencycare.util.Constants;

public class AddBloodBank extends AppCompatActivity {

    EditText e1,e2,e3,e4,e5;
    Button b1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_blood_bank);

        e1=(EditText)findViewById(R.id.addbloodbankname);
        e2=(EditText)findViewById(R.id.addbloodbankEmail);
        e3=(EditText)findViewById(R.id.addbloodbankMobile);
        e4=(EditText)findViewById(R.id.addbloodbankDescription);
        e5=(EditText)findViewById(R.id.addbloodbankAddress);

        b1=(Button)findViewById(R.id.registerBloodBank);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name=e1.getText().toString();
                String email=e2.getText().toString();
                String mobile=e3.getText().toString();
                String description=e4.getText().toString();
                String address=e5.getText().toString();

                if(name==null|| email==null|| mobile==null|| description==null|| address==null)
                {
                    Toast.makeText(getApplicationContext(),"Please Enter Valid Data",Toast.LENGTH_SHORT).show();
                }
                else if(mobile.length()<10|| mobile.length()>12) {
                    Toast.makeText(getApplicationContext(), "Invalid Mobile", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    User user=new User();

                    user.setName(name);
                    user.setEmail(email);
                    user.setMobile(mobile);
                    user.setDescription(description);
                    user.setAddress(address);
                    user.setType("bloodbank");

                    DAO dao=new DAO();

                    try
                    {
                        dao.addObject(Constants.USER_DB,user,user.getMobile());

                        Toast.makeText(getApplicationContext(),"BloodBank Added Successfully",Toast.LENGTH_SHORT).show();

                        Intent i=new Intent(getApplicationContext(),AdminHome.class);
                        startActivity(i);
                    }
                    catch (Exception ex)
                    {
                        Toast.makeText(getApplicationContext(),"Register Error",Toast.LENGTH_SHORT).show();
                        Log.v("BloodBank Registration", ex.toString());
                        ex.printStackTrace();
                    }

                }
            }
        });
    }
}
