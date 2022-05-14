package com.example.miolaapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ViewEmploiActivity extends AppCompatActivity {
    private static final int PICK_PDF = 123;
    public Uri filePath;

    String urls;
    PDFView pdfView;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_emploi);

        pdfView = findViewById(R.id.abc);

        setTitle("Emploi du temps");

        // Initialize and assign variable
        BottomNavigationView bottomNavigationView=findViewById(R.id.navigation);
        // Set ProfList selected
        bottomNavigationView.setSelectedItemId(R.id.navigation_emploi);
        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Firstly we are showing the progress
        // dialog when we are loading the pdf
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading..");
        dialog.show();
        StorageReference storageReference =
                FirebaseStorage.getInstance().getReference("emploi.pdf");

        // getting url of pdf using getItentExtra
//        urls = getIntent().getStringExtra("url");
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            urls=uri.toString();
            System.out.println(urls);
            new RetrivePdfStream().execute(urls);
        });
    }

    // Retrieving the pdf file using url
    class RetrivePdfStream extends AsyncTask<String, Void, InputStream> {

        @Override
        protected InputStream doInBackground(String... strings) {
            InputStream inputStream = null;
            try {

                // adding url
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                // if url connection response code is 200 means ok the execute
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }
            }
            // if error return null
            catch (IOException e) {
                return null;
            }
            return inputStream;
        }

        @Override
        // Here load the pdf and dismiss the dialog box
        protected void onPostExecute(InputStream inputStream) {
            pdfView.fromStream(inputStream).load();
            dialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!LoginActivity.IS_NOT_CORD){
            getMenuInflater().inflate(R.menu.menu_update, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_update) {
            selectPDF();
            return true;
        }
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Select Image method
    public void selectPDF() {
        // Defining Implicit Intent to file gallery
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select File from here..."),
                PICK_PDF);
    }

    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data)
    {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_PDF
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            uploadPDF();
        }
    }

    // UploadImage method
    private void uploadPDF(){
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading ...");
            progressDialog.show();

            // Defining the child of storageReference
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference ref = storage.getReference().child("emploi.pdf");

            ref.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully
                        // Dismiss dialog
                        progressDialog.dismiss();
                        Toast.makeText(this, "Enregistré avec succès", Toast.LENGTH_SHORT).show();
                        // Reload Activity
                        finish();
                        startActivity(getIntent());
                    })
                    .addOnFailureListener(e -> {
                        // Error, Image not uploaded
                        progressDialog.dismiss();
                        Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
                    });
        }
        filePath = null;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_prof:
                    startActivity(new Intent(getApplicationContext(), ProfsListActivity.class));
                    overridePendingTransition(0,0);
                    finish();
                    return true;
                case R.id.navigation_etud:
                    startActivity(new Intent(getApplicationContext(), EtudiantsListActivity.class));
                    overridePendingTransition(0,0);
                    finish();
                    return true;
                case R.id.navigation_emploi:
                    startActivity(new Intent(getApplicationContext(), ViewEmploiActivity.class));
                    overridePendingTransition(0,0);
                    finish();
                    return true;
                case R.id.navigation_signout:
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                    return true;
            }
            return false;
        }
    };
}