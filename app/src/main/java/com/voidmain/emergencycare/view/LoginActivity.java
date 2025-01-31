package com.voidmain.emergencycare.view;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.voidmain.emergencycare.MainActivity;
import com.voidmain.emergencycare.R;
import com.voidmain.emergencycare.dao.DAO;
import com.voidmain.emergencycare.dao.GetFireBaseConnection;
import com.voidmain.emergencycare.form.User;
import com.voidmain.emergencycare.util.Constants;
import com.voidmain.emergencycare.util.Session;

public class LoginActivity extends AppCompatActivity {
    SQLiteDatabase db;
    private Session session;
    EditText e1,e2;
    Button b1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new Session(getApplicationContext());

        setContentView(R.layout.activity_login);

        e1=(EditText)findViewById(R.id.loginPhone);
        e2=(EditText)findViewById(R.id.loginPass);
        b1=(Button)findViewById(R.id.loginConfirm);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String username=e1.getText().toString();
                final String password=e2.getText().toString();

                if(username==null|| password==null || username.length()<=0|| password.length()<=0)
                {
                    Toast.makeText(getApplicationContext(),"Please Enter UserName and Password",Toast.LENGTH_SHORT).show();
                }
                else {

                    if (username.equals("admin") && password.equals("admin")) {

                        session.setusename("admin");
                        session.setRole("admin");

                        Intent i = new Intent(getApplicationContext(), AdminHome.class);
                        startActivity(i);

                    } else {

                        DAO d = new DAO();
                        d.getDBReference(Constants.USER_DB).child(username).addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                User user = (User) dataSnapshot.getValue(User.class);

                                if (user == null) {
                                    Toast.makeText(getApplicationContext(), "Invalid UserName ", Toast.LENGTH_SHORT).show();
                                } else if (user != null && user.getPassword().equals(password)) {

                                    session.setusename(user.getUsername());

                                    if(user.getType().equals("user"))
                                    {
                                        session.setRole("user");

                                        db=openOrCreateDatabase(Constants.sqLiteDatabase, MODE_PRIVATE, null);
                                        db.execSQL("create table if not exists login(username varchar)");
                                        db.execSQL("insert into login values('"+username+"')");

                                        Intent i=new Intent(getApplicationContext(),MainActivity.class);
                                        startActivity(i);
                                    }


                                } else {
                                    Toast.makeText(getApplicationContext(), "In valid Password", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        });
    }
}
