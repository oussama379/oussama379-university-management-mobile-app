package com.example.miolaapp.adapters;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.miolaapp.AddProfDialog;
import com.example.miolaapp.LoginActivity;
import com.example.miolaapp.ProfsListActivity;
import com.example.miolaapp.R;
import com.example.miolaapp.ShowProfDialog;
import com.example.miolaapp.entities.Professeur;
import com.example.miolaapp.utils.GlideApp;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ProfAdapter extends RecyclerView.Adapter<ProfAdapter.ViewHolder> implements Filterable {

    private static final String DIRECTORY = "prof-pictures/";

//    private Context context;
    private FragmentActivity fragmentActivity;
    private ArrayList<Professeur> localDataSet;
    private ArrayList<Professeur> localDataSetFiltered;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
//        final ImageView picture;
        final ShapeableImageView picture;
        final TextView name;
        final TextView email;
        final TextView buttonViewOption;
        final View root;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            picture = view.findViewById(R.id.imageView_picture);
            name = view.findViewById(R.id.textView_name);
            email = view.findViewById(R.id.textView_email);
            buttonViewOption = view.findViewById(R.id.textViewOptions);
            buttonViewOption.setVisibility(View.GONE);
            root = view.getRootView();
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     */
//    public ProfAdapter(Context context, ArrayList<Professeur> dataSet) {
//        localDataSet = dataSet;
//        this.context = context;
//    }
    public ProfAdapter(FragmentActivity fragmentActivity, ArrayList<Professeur> dataSet) {
        localDataSet = dataSet;
        localDataSetFiltered = dataSet;
        this.fragmentActivity = fragmentActivity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.prof_item_layout, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.name.setText(localDataSetFiltered.get(position).getNom().toUpperCase(Locale.ROOT)+" "+localDataSetFiltered.get(position).getPrenom());
        viewHolder.email.setText("Département "+localDataSetFiltered.get(position).getDepart());

        // Reference to an image file in Cloud Storage
        StorageReference storageReference =
                FirebaseStorage.getInstance().getReference(localDataSetFiltered.get(position).getImage());

        // Download directly from StorageReference using Glide
        Glide.with(fragmentActivity)
                .load(storageReference)
                .into(viewHolder.picture);

        // Set Popup Menu
        viewHolder.buttonViewOption.setOnClickListener(view -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(fragmentActivity, viewHolder.buttonViewOption);
            //inflating menu from xml resource
            popup.inflate(R.menu.popup_menu_items);
            //adding click listener
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.edit:
                        editProf(localDataSetFiltered.get(position).getEmail());
                        break;
                    case R.id.delete:
                        confirmDeleteProf(localDataSetFiltered.get(position).getEmail());
                        break;
                }
                return false;
            });
            //displaying the popup
            popup.show();
        });

        // Set Show Dialog
        viewHolder.root.setOnClickListener(view -> {
            ShowProfDialog showProfDialog = new ShowProfDialog(localDataSetFiltered.get(position), viewHolder.picture.getDrawable());
//            showProfDialog.fillData(localDataSetFiltered.get(position), viewHolder.picture.getDrawable());
            showProfDialog.show(fragmentActivity.getSupportFragmentManager(), "SHOW-DIALOG");
        });
        if (!LoginActivity.IS_NOT_CORD){
            // Set Edit Dialog
            viewHolder.root.setOnLongClickListener(view -> {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(fragmentActivity, viewHolder.root);
                //inflating menu from xml resource
                popup.inflate(R.menu.popup_menu_items);
                //adding click listener
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.edit:
                            editProf(localDataSetFiltered.get(position).getEmail());
                            break;
                        case R.id.delete:
                            confirmDeleteProf(localDataSetFiltered.get(position).getEmail());
                            break;
                    }
                    return false;
                });
                //displaying the popup
                popup.show();
                return true;
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSetFiltered.size();
    }

    private void editProf(String id){
        AddProfDialog addProfDialog = new AddProfDialog();
        addProfDialog.edit(id);
        addProfDialog.show(fragmentActivity.getSupportFragmentManager(), "DIALOG");
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    localDataSetFiltered = localDataSet;
                } else {
                    ArrayList<Professeur> filteredList = new ArrayList<>();
                    for (Professeur row : localDataSet) {
                        if (row.getFullName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    localDataSetFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = localDataSetFiltered;
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                localDataSetFiltered = (ArrayList<Professeur>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    private void confirmDeleteProf(String id){
        new AlertDialog.Builder(fragmentActivity)
                .setTitle("Suppression")
                .setMessage("Voulez-vous vraiment supprimer ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> deleteProf(id))
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void deleteProf(String id){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("professeurs").document(id).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    Toast.makeText(fragmentActivity, "Supprimé avec succès", Toast.LENGTH_SHORT).show();
                    ((ProfsListActivity)fragmentActivity).refresh();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error deleting document", e);
                    Toast.makeText(fragmentActivity, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                });
    }

}
