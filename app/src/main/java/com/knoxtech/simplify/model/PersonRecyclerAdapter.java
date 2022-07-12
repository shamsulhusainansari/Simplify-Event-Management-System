package com.knoxtech.simplify.model;

import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.knoxtech.simplify.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonRecyclerAdapter extends FirestoreRecyclerAdapter<Data, PersonRecyclerAdapter.PersonViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    private final OnItemClickListener listener;
    private final FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseUser user = firebaseAuth.getCurrentUser();
    String selectedRole;
    public PersonRecyclerAdapter(FirestoreRecyclerOptions<Data> options) {
        super(options);
        this.listener = null;
    }
    private static final String[] ROLES = new String[]{
            "President", "Vice President","Event Coordinator","Coordinator","Club In-Charge"
    };
    static class PersonViewHolder extends RecyclerView.ViewHolder {

        private final ImageView searchProfile;
        private final TextView name;
        private final TextView email;
        private final TextView branch;
        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            searchProfile = itemView.findViewById(R.id.searchProfile);
            name=itemView.findViewById(R.id.searchName);
            email=itemView.findViewById(R.id.searchEmail);
            branch = itemView.findViewById(R.id.searchBranch);
        }
    }
    @Override
    public void onBindViewHolder(final PersonViewHolder holder, @NonNull int position, @NonNull final Data person) {
        holder.name.setText(person.getName());
        holder.email.setText(person.getEmail());
        holder.branch.setText(person.getBranch());
        Picasso.get().load(person.getProfilePicture()).into(holder.searchProfile);
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onItemClick(holder.getAdapterPosition()));
        }

        holder.itemView.setOnClickListener(v -> {

            final Dialog dialog = new Dialog(v.getContext(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.popup_profile);
            DocumentReference delUser = firebaseFirestore.collection("Users").document(person.getUserId());
            final CircleImageView[] profile = {dialog.findViewById(R.id.popup_profile_image)};
            Picasso.get().load(person.getProfilePicture()).into(profile[0]);

            RelativeLayout re = dialog.findViewById(R.id.popup_profile);
            re.setOnClickListener(view -> dialog.dismiss());
            TextView name = dialog.findViewById(R.id.popup_person_name);
            name.setText(person.getName());
            TextView email = dialog.findViewById(R.id.popup_person_email);
            email.setText(person.getEmail());
            TextView clg = dialog.findViewById(R.id.popup_person_clg_name);
            clg.setText(person.getClgName());
            TextView branch = dialog.findViewById(R.id.popup_person_clg_branch);
            branch.setText(person.getBranch());

            final String[] sham = new String[1];


            Button change_role = dialog.findViewById(R.id.assRole);
            TextInputLayout selectRole = dialog.findViewById(R.id.txtRole);
            AutoCompleteTextView assRole = dialog.findViewById(R.id.autoBranch);
            ImageView delete_role = dialog.findViewById(R.id.more_cancel_btn);
            DocumentReference docRef = firebaseFirestore.collection("Users").document(user.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                Data d = documentSnapshot.toObject(Data.class);
                assert d != null;
                String role = d.getRole();
                sham[0] = d.getOrganizer();
                try {
                    if (role.equals("HOD")) {
                        change_role.setVisibility(View.VISIBLE);
                        selectRole.setVisibility(View.VISIBLE);
                        delete_role.setVisibility(View.VISIBLE);
                        change_role.setEnabled(true);
                        selectRole.setEnabled(true);
                    }
                }catch (Exception e){
                    Log.i("Role :",e.getMessage());
                }
                if (user.getUid().equals(person.getUserId())){
                    change_role.setVisibility(View.GONE);
                    selectRole.setVisibility(View.GONE);
                    change_role.setEnabled(false);
                    selectRole.setEnabled(false);
                    delete_role.setVisibility(View.GONE);
                }

            });

            delete_role.setOnClickListener(view -> {
                Map<String,Object> updates = new HashMap<>();
                updates.put("role", FieldValue.delete());

                delUser.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(view.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            });

            String[] rrRoles = v.getResources().getStringArray(R.array.roles);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(v.getContext(),
                    R.layout.custom_list_item, R.id.text_view_list_item, rrRoles);
            assRole.setAdapter(adapter);


            assRole.setOnItemClickListener((parent, view, position1, id) -> selectedRole = parent.getItemAtPosition(position1).toString());

            dialog.findViewById(R.id.assRole).setOnClickListener(v1 -> {

                Map<String,Object> roles = new HashMap<>();
                roles.put("role", selectedRole);
                roles.put("organizer", sham[0]);
                    DocumentReference assignedRole = firebaseFirestore.collection("Users").document(person.getUserId());
                    assignedRole
                            .update(roles)
                            .addOnSuccessListener(aVoid -> {
                                dialog.dismiss();
                                Toast.makeText(v1.getContext(), ""+selectedRole, Toast.LENGTH_SHORT).show();

                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(v1.getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();

                            });

            });
            dialog.show();

        });
    }
    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.search_profile_layout,parent,false);
        return new PersonViewHolder(view);
    }
}
