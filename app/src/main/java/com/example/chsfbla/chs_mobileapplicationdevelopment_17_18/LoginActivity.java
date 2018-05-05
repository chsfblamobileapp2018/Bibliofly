package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.Librarian.LibrarianHomeActivity;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.StudentTeacher.StudentTeacherHomeActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    // Declare firebase authentication instance variable
    private FirebaseAuth mAuth;


    // Declare UI variables that we will later use to access the views/widgets based on their ids
    Button login;
    EditText emailField;
    EditText passwordField;
    TextView signup;
    SignInButton gLogin;
    GoogleApiClient googleApiClient;
    Button reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()).child("Status");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue().toString().equals("Librarian")) {
                        startActivity(new Intent(getApplicationContext(), LibrarianHomeActivity.class));
                    } else
                        startActivity(new Intent(getApplicationContext(), StudentTeacherHomeActivity.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        // Intialize the widgets/views by using their ids
        gLogin = (SignInButton) findViewById(R.id.google_sign_in);
        signup = (TextView) findViewById(R.id.signUpLinkButton);
        login = (Button) findViewById(R.id.loginButton);
        emailField = (EditText) findViewById(R.id.usernameLoginField);
        passwordField = (EditText) findViewById(R.id.passwordLoginField);
        reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailField.getText().toString().trim();
                if (email.isEmpty() || !email.contains("@")) {
                    Toast.makeText(getApplicationContext(), "Please fill out the email field, and then retry!", Toast.LENGTH_LONG).show();
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(1000);
                } else {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Your password-reset link has been sent!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });

        // Create an onClickListener for the text that leads to the signup page
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the new SignUpActivity
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
            }
        });

        // Create an onClickListener for login button. It will use Firebase Auth to sign in.
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(login.getText().equals("Logout")){
//                    FirebaseAuth.getInstance().signOut();
//                    login.setText("Login");
//                }
//                else {
                // Create two Strings, email and password
                String email = emailField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill out both fields", Toast.LENGTH_LONG).show();
                    return;
                }

                //Call the Firebase signInWithEmailAndPassword method.
                mAuth.signInWithEmailAndPassword(email, password)
                        // Add a listener that will run a method when the user has signed in.
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // If sign in succeeds, display this message to the user.
                                    Toast.makeText(LoginActivity.this, "Authentication successful.",
                                            Toast.LENGTH_SHORT).show();
                                    // Open the StudentTeacherHomeActivity page
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()).child("Status");
                                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue().toString().equals("Librarian")) {
                                                startActivity(new Intent(getApplicationContext(), LibrarianHomeActivity.class));
                                            } else
                                                startActivity(new Intent(getApplicationContext(), StudentTeacherHomeActivity.class));


                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                //  }
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("543620889214-j2hu93b2ij23dk2uq3troq90j41bgldn.apps.googleusercontent.com").requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).

                enableAutoManage(this, this).

                addApi(Auth.GOOGLE_SIGN_IN_API, gso).

                build();
        gLogin.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

    }

    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, 9001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 9001) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                final GoogleSignInAccount account = result.getSignInAccount();
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    //CODE TO CHECK IF STUDENT/TEACHER OR LIBRARIAN
                                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            googleApiClient.clearDefaultAccountAndReconnect();
                                            if (dataSnapshot.hasChild("Status")) {
                                                //they already have an account
                                                String status = dataSnapshot.child("Status").getValue().toString();
                                                if (status.equals("Librarian")) {
                                                    startActivity(new Intent(getApplicationContext(), LibrarianHomeActivity.class));
                                                } else {
                                                    startActivity(new Intent(getApplicationContext(), StudentTeacherHomeActivity.class));
                                                }
                                            } else {
                                                String name = account.getDisplayName();
                                                ref.child("Name").setValue(name);
                                                ref.child("Status").setValue("Student");
                                                startActivity(new Intent(getApplicationContext(), StudentTeacherHomeActivity.class));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                                }


                            }
                        });
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
