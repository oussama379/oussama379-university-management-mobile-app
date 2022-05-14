package com.example.miolaapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.miolaapp.entities.Etudiant;
import com.example.miolaapp.entities.Professeur;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

/**
 * This class for manage 'Etudiant' creation, it use the same XML file (dialog_add_prof.xml) with 'AddProfDialog'
 * */
public class AddEtudiantDialog extends DialogFragment {
    private static final String TAG = "AddEtudiantDialog";
    private static final String DIR = "etu-pictures/";

    private String ID; // for edit
    private Etudiant instanceToEdit;

    private EditText nom, prenom, email, tele, filiere;
    private SwitchMaterial cord; // element to hide
    private Button btnSelect;
    private Toolbar toolbar;

    private Uri filePath; // Uri indicates, where the image will be picked from
    private String uuid;
    private final int PICK_IMAGE_REQUEST = 22; // request code

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private EtudiantsListActivity activity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        activity = ((EtudiantsListActivity)AddEtudiantDialog.this.getActivity());

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
        filiere = view.findViewById(R.id.depart); filiere.setHint("Filière"); // change placeholder
        cord = view.findViewById(R.id.cord); cord.setVisibility(View.GONE); // remove element from the ui
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
                        saveIstance();
                    }
                })
                .setNegativeButton("ANNULER", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddEtudiantDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    private void saveIstance(){
        // generate image filename
        uuid = UUID.randomUUID().toString();

        // Data
        String nom = this.nom.getText().toString();
        String prenom = this.prenom.getText().toString();
        String email = this.email.getText().toString();
        String tele = this.tele.getText().toString();
        String filiere = this.filiere.getText().toString();

        Etudiant instance = new Etudiant(nom, prenom, email, tele, filiere, DIR+uuid);
        if (isEdit()){
            if (activity.filePath == null)
                instance.setImage(instanceToEdit.getImage());
        }

        DocumentReference docRef = db.collection("etudiants").document(email);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Toast.makeText(activity, "Etudiant existe déjà", Toast.LENGTH_SHORT).show();
                } else {
                    // Showing progressDialog while saving
                    ProgressDialog progressDialog = new ProgressDialog(activity);
                    progressDialog.setMessage("Enregistrement ...");
                    progressDialog.show();
                    docRef.set(instance)
                            .addOnCompleteListener(savingTask -> {
                                if (savingTask.isSuccessful()) {
                                    mAuth.createUserWithEmailAndPassword(email, tele);
                                    uploadImage(progressDialog);
                                } else {
                                    Toast.makeText(activity, "Echec de l'enregistrement", Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, "SAVING ERROR", savingTask.getException());
                                    progressDialog.dismiss();
                                }
                            });
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });


//        db.collection("etudiants").document(email).set(instance)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        mAuth.createUserWithEmailAndPassword(email, tele);
//                        Toast.makeText(activity, "Enregistré avec succès", Toast.LENGTH_SHORT).show();
//                        uploadImage(progressDialog);
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

    public void edit(Etudiant obj){
        instanceToEdit = obj;
    }

    private boolean isEdit(){
        return instanceToEdit != null;
    }

    private void fillData(){
        toolbar.setTitle("Modifier");
        email.setEnabled(false);
        nom.setText(instanceToEdit.getNom());
        prenom.setText(instanceToEdit.getPrenom());
        email.setText(instanceToEdit.getEmail());
        tele.setText(instanceToEdit.getTele());
        filiere.setText(instanceToEdit.getFiliere());
    }

}
