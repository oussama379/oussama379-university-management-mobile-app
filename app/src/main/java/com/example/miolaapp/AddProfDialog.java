package com.example.miolaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.miolaapp.entities.Professeur;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class AddProfDialog extends DialogFragment {
    private static final String TAG = "AddProfDialog";
    private static final String DIR = "prof-pictures/";

    private String ID; // for edit
    private Professeur profToEdit;

    private EditText nom, prenom, email, tele, depart;
    private SwitchMaterial cord;
    private Button btnSelect;
    private Toolbar toolbar;

    private Uri filePath; // Uri indicates, where the image will be picked from
    private String uuid;
    private final int PICK_IMAGE_REQUEST = 22; // request code

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private ProfsListActivity activity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        activity = ((ProfsListActivity)AddProfDialog.this.getActivity());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_add_prof, null);

        // bind elements
        nom = view.findViewById(R.id.nom);
        prenom = view.findViewById(R.id.prenom);
        email = view.findViewById(R.id.email);
        tele = view.findViewById(R.id.tele);
        depart = view.findViewById(R.id.depart);
        cord = view.findViewById(R.id.cord);
        btnSelect = view.findViewById(R.id.btnSelect);
        toolbar = view.findViewById(R.id.toolbar);

        if (isEdit()) fillData();

        // on pressing btnSelect SelectImage() is called
        btnSelect.setOnClickListener(v -> activity.selectImage());

        builder.setView(view)
                // Add action buttons
                .setPositiveButton("SAUVEGARDER", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        saveProf();
                    }
                })
                .setNegativeButton("ANNULER", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddProfDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    private void saveProf(){
        // generate image filename
        uuid = UUID.randomUUID().toString();


        // Data
        String nom = this.nom.getText().toString();
        String prenom = this.prenom.getText().toString();
        String email = this.email.getText().toString();
        String tele = this.tele.getText().toString();
        String depart = this.depart.getText().toString();
        boolean cord = this.cord.isChecked();

        Professeur prof = new Professeur(nom, prenom, email, tele, depart, cord, DIR+uuid);
        if (isEdit()){
            if (activity.filePath == null)
                prof.setImage(profToEdit.getImage());
        }

        DocumentReference docRef = db.collection("professeurs").document(email);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists() && !isEdit()) {
                    Toast.makeText(activity, "Professeur existe déjà", Toast.LENGTH_SHORT).show();
                } else {
                    // Showing progressDialog while saving
                    ProgressDialog progressDialog = new ProgressDialog(activity);
                    progressDialog.setMessage("Enregistrement ...");
                    progressDialog.show();
                    docRef.set(prof)
                            .addOnCompleteListener(savingTask -> {
                                if (savingTask.isSuccessful()) {
                                    mAuth.createUserWithEmailAndPassword(email, tele);
                                    uploadImage(progressDialog);
                                    Log.i(TAG, "SAVING GOOD");
                                } else {
                                    Toast.makeText(activity, "Echec de l'enregistrement", Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, "SAVING ERROR", savingTask.getException());
                                    progressDialog.dismiss();
                                }
                            });
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });

//        db.collection("professeurs").document(email).set(prof)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        mAuth.createUserWithEmailAndPassword(email, tele);
//                        Toast.makeText(activity, "Enregistré avec succès", Toast.LENGTH_SHORT).show();
//                        uploadImage(progressDialog);
//                        Log.i(TAG, "SAVING GOOD");
//                    } else {
//                        Toast.makeText(activity, "Echec de l'enregistrement", Toast.LENGTH_SHORT).show();
//                        Log.w(TAG, "SAVING ERROR", task.getException());
//                        progressDialog.dismiss();
//                    }
//                });
    }

    // UploadImage method
    private void uploadImage(ProgressDialog progressDialog){
        filePath = activity.filePath;
        if (filePath != null) {
            // Defining the child of storageReference
            StorageReference ref = storageReference.child(DIR+uuid);

            ref.putFile(filePath)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully
                    // Dismiss dialog
                    Toast.makeText(activity, "Enregistré avec succès", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    activity.refresh();
                })
                .addOnFailureListener(e -> {
                    // Error, Image not uploaded
                    progressDialog.dismiss();
                    activity.refresh();
                });
        }else{
            progressDialog.dismiss();
            activity.refresh();
        }
        activity.filePath = null;
    }

    public void edit(String id){
        ID = id;
    }

    private boolean isEdit(){
        return ID != null;
    }

    private void fillData(){
        toolbar.setTitle("Modifier");
        email.setEnabled(false);
        db.collection("professeurs").document(ID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    profToEdit = documentSnapshot.toObject(Professeur.class);
                    nom.setText(profToEdit.getNom());
                    prenom.setText(profToEdit.getPrenom());
                    email.setText(profToEdit.getEmail());
                    tele.setText(profToEdit.getTele());
                    depart.setText(profToEdit.getDepart());
                    cord.setChecked(profToEdit.isCord());
                });
    }

}