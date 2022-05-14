package com.example.miolaapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miolaapp.adapters.EtudiantAdapter;
import com.example.miolaapp.adapters.ProfAdapter;
import com.example.miolaapp.entities.Etudiant;
import com.example.miolaapp.entities.Professeur;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class EtudiantsListActivity extends AppCompatActivity {
    private static final String TAG = "EtudiantsListActivity";

    private FirebaseFirestore db;
    private static ArrayList<Etudiant> list;

    private EtudiantAdapter adapter;
    private RecyclerView recyclerView;
    private ExtendedFloatingActionButton fabAdd;
    private SearchView searchView;

    public Uri filePath; // Uri indicates, where the image will be picked from
    private final int PICK_IMAGE_REQUEST = 22; // request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etudiants_list);

        // set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // toolbar fancy stuff
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Liste des étudiants");

        // Initialize and assign variable
        BottomNavigationView bottomNavigationView=findViewById(R.id.navigation);
        // Set ProfList selected
        bottomNavigationView.setSelectedItemId(R.id.navigation_etud);
        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // set up FireStore
        db = FirebaseFirestore.getInstance();
        list = new ArrayList<>();

        // set up the RecyclerView
        recyclerView = findViewById(R.id.rvEtudiants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new EtudiantAdapter(this, list);
        recyclerView.setAdapter(adapter);

        // set up the FloatingActionButton
        fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(view -> {
            AddEtudiantDialog dialog = new AddEtudiantDialog();
            dialog.show(getSupportFragmentManager(), "ADD-DIALOG");
        });
        if (LoginActivity.IS_NOT_CORD) fabAdd.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchData();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchData(){
        // Showing progressDialog while fetching
        ProgressDialog progressDialog  = new ProgressDialog(this);
        progressDialog.setMessage("Chargement ...");
        progressDialog.show();

        list.clear();
        db.collection("etudiants")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            list.add(document.toObject(Etudiant.class));
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                    // Notifier l'adapter
                    adapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                });
    }

    public void refresh(){
        fetchData();
    }

    // Select Image method
    public void selectImage() {
        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
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
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                adapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
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
