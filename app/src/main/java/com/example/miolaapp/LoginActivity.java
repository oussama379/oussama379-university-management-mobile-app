package com.example.miolaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.miolaapp.entities.Professeur;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.os.Bundle;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    public static FirebaseUser USER;
    public static boolean IS_NOT_CORD = true;

    private FirebaseAuth mAuth;
    private Button bSignin,bSignup;
    private EditText tfEmail,tfPassword;
//    private TextView message;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        USER = mAuth.getCurrentUser();
        if (USER != null) {
            nextActivity();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bSignin = (Button)findViewById(R.id.signin);
        tfEmail = (EditText)findViewById(R.id.tfEmail);
        tfPassword = (EditText)findViewById(R.id.tfPassword);

        bSignup = (Button)findViewById(R.id.signup);

        bSignin.setOnClickListener(v -> {
            mAuth.signInWithEmailAndPassword(tfEmail.getText().toString(), tfPassword.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            USER = mAuth.getCurrentUser();
                            updateUI();
//                            message.setText("USER IN");
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }

            });
        });

        bSignup.setOnClickListener(v -> {
            mAuth.createUserWithEmailAndPassword(tfEmail.getText().toString(), tfPassword.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            USER = mAuth.getCurrentUser();
//                            updateUI(user);
//                            message.setText("USER CREATED");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
//                            updateUI(null);
//                            message.setText("USER NOT NOT CREATED");
                        }});
        });
    }

    /** Called when the user authenticate */
    public void nextActivity() {
        Intent intent = new Intent(this, ProfsListActivity.class);
        startActivity(intent);
    }

    private void updateUI(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("professeurs").document(USER.getEmail());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Professeur prof = document.toObject(Professeur.class);
                    IS_NOT_CORD = !prof.isCord();
                }
            }
            nextActivity();
        });
    }
}