package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.StudentTeacher.StudentTeacherHomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {


    //variables for the UI
    private EditText email_editText;
    private EditText password_editText;
    private Button login_button;
    private EditText name;
    //Firebase variables.
    private FirebaseAuth mAuth;

    // A common TAG variable for easy debugging
    private static final String TAG = "Mobile Application Dev";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //initialize the firebase instance.
        mAuth = FirebaseAuth.getInstance();

        //initialize the UI variables.
        email_editText = (EditText) findViewById(R.id.emailSignupField);
        password_editText = (EditText) findViewById(R.id.passwordSignupField);
        login_button = (Button) findViewById(R.id.signUpButton);
        name = (EditText)findViewById(R.id.name);
        //set a button callback method for the login button
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = email_editText.getText().toString().trim();
                String password = password_editText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill out both fields", Toast.LENGTH_LONG).show();
                    return;
                }

                //Use firebase auth to create a user with the email, and password.
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    //notify the user that the sign up was succesful.
                                    Toast.makeText(SignUpActivity.this, "Authentication successful.",
                                            Toast.LENGTH_SHORT).show();

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    user.sendEmailVerification();

                                    /*Find the name of the user, as well as the "status", and save them to variables
                                      Then, find the UID of the user who just created an account, and set these values
                                      to that UID in the "Users" directory of the database
                                     */
                                    String name1 = name.getText().toString().trim();
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
                                    String uid = mAuth.getCurrentUser().getUid();
                                    ref.child(uid).child("Name").setValue(name1);
                                    ref.child(uid).child("Status").setValue("Student");
                                    //create a new intent to move to the main page, since the user has been verified.
                                    Intent intent = new Intent(getApplicationContext(), StudentTeacherHomeActivity.class);
                                    //start the new intent to go to the new screen.
                                    startActivity(intent);

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }





}
