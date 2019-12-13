package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private State state;
    private Button btnSignUpLogin,btnOneTimeLogin;
    private RadioButton driverRadioButton, passengerRadioButton;
    private EditText edtUserName, edtPassword, edtDriverOrPassenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseInstallation.getCurrentInstallation().saveInBackground();
        if(ParseUser.getCurrentUser() != null){
//            ParseUser.logOut();
            transitionToPassengerActivity();
            transitionToDriverRequestListActivity();
        }
        btnSignUpLogin=findViewById(R.id.btnSignUp);
        btnOneTimeLogin=findViewById(R.id.btnOneTime);
        driverRadioButton=findViewById(R.id.rdbDriver);
        passengerRadioButton=findViewById(R.id.rdbPassenger);
        edtUserName=findViewById(R.id.edtUsername);
        edtPassword=findViewById(R.id.edtPassword);
        edtDriverOrPassenger=findViewById(R.id.edtDriverOrPassenger);
        btnOneTimeLogin.setOnClickListener(MainActivity.this);
        state=State.SIGNUP;

        btnSignUpLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state ==State.SIGNUP){
                  if(driverRadioButton.isChecked() == false && passengerRadioButton.isChecked()==false){
                      FancyToast.makeText(MainActivity.this,"Are you a driver or passenger ?", FancyToast.LENGTH_SHORT,FancyToast.INFO,true).show();
                      return;
                  }
                  ParseUser appUser= new ParseUser();
                  appUser.setUsername(edtUserName.getText().toString());
                  appUser.setPassword(edtPassword.getText().toString());
                  if(driverRadioButton.isChecked()){
                      appUser.put("as", "Driver");
                  } else if(passengerRadioButton.isChecked()){
                      appUser.put("as", "Passenger");
                  }
                  appUser.signUpInBackground(new SignUpCallback() {
                      @Override
                      public void done(ParseException e) {
                          if(e == null){
                              FancyToast.makeText(MainActivity.this,"Signed up!", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();
                              transitionToPassengerActivity();
                              transitionToDriverRequestListActivity();
                          }
                      }
                  });
                } else if (state ==State.LOGIN) {
                    ParseUser.logInInBackground(edtUserName.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if(user != null && e==null){
                                FancyToast.makeText(MainActivity.this, "User Logged ",FancyToast.LENGTH_SHORT,FancyToast.INFO, true).show();
                                transitionToPassengerActivity();
                                transitionToDriverRequestListActivity();
                            }
                        }
                    });

                }

            }

        });
    }

    @Override
    public void onClick(View view) {
        if (edtDriverOrPassenger.getText().toString().equals("Driver") || edtDriverOrPassenger.getText().toString().equals("Passenger")) {
            if (ParseUser.getCurrentUser() == null) {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Loging in anonymoslly ");
                progressDialog.show();
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user != null && e == null) {
                            FancyToast.makeText(MainActivity.this, " We anonymoslly use app as " + edtDriverOrPassenger.getText().toString(), FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();
                            user.put("as", edtDriverOrPassenger.getText().toString());
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    transitionToPassengerActivity();
                                    transitionToDriverRequestListActivity();
                                }
                            });
                        }
                    }
                });
                progressDialog.dismiss();
            }

        } else {


            FancyToast.makeText(MainActivity.this, " Are you a Driver or a Passenger " + edtDriverOrPassenger.getText().toString(), FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signup_activity, menu);
        return super.onCreateOptionsMenu(menu);

//        if(edtDriverOrPassenger.getText().toString().equals("Driver")){
//            if(ParseUser.getCurrentUser()== null){
//                ParseAnonymousUtils.logIn();
//            }
//        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.loginItem:
                if(state == State.SIGNUP) {
                    state = State.LOGIN;
                    item.setTitle("Sign Up");
                    btnSignUpLogin.setText("Log In");
                } else if(state==State.LOGIN)
                    {
                        state =State.SIGNUP;
                        item.setTitle("Log in");
                        btnSignUpLogin.setText("Sign up");
                    }
                    break;
        }
        return super.onOptionsItemSelected(item);
    }
    enum State{
        SIGNUP, LOGIN
    }
    private void transitionToPassengerActivity(){
        if(ParseUser.getCurrentUser() != null){
            if(ParseUser.getCurrentUser().get("as").equals("Passenger")){
                Intent intent= new Intent(MainActivity.this, PassengerActivity.class);
                startActivity(intent);
            }
        }
    }
    private void transitionToDriverRequestListActivity(){
        if(ParseUser.getCurrentUser() != null){
            if (ParseUser.getCurrentUser().get("as").equals("Driver")) {
                Intent intent= new Intent(MainActivity.this,DriverRequestListActivity.class );
                startActivity(intent);

            }
        }
    }
}
