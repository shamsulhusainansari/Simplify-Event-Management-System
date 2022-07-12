package com.knoxtech.simplify.nav_fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.knoxtech.simplify.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class    SettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextInputEditText fullName,phone,branch,clgName,organizer;
    private Button updateDetails;
    private TextInputLayout txtOrgField;
    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        assert user != null;
        DocumentReference updateData = firebaseFirestore.collection("Users").document(user.getUid());
        Source source = Source.CACHE;
        CircleImageView profileImage = view.findViewById(R.id.profileImage);
        TextView name = view.findViewById(R.id.profileName);
        TextView email = view.findViewById(R.id.profileEmail);

        fullName = view.findViewById(R.id.editName);
        phone = view.findViewById(R.id.editPhone);
        branch = view.findViewById(R.id.editBranch);
        clgName = view.findViewById(R.id.editClg);
        organizer = view.findViewById(R.id.editOrg);
        txtOrgField = view.findViewById(R.id.txtOrgField);
        updateDetails = view.findViewById(R.id.btnUpdate);
        fullName.addTextChangedListener(updateTextWatcher);
        phone.addTextChangedListener(updateTextWatcher);
        branch.addTextChangedListener(updateTextWatcher);
        clgName.addTextChangedListener(updateTextWatcher);
        Picasso.get().load(user.getPhotoUrl()).into(profileImage);
        name.setText(user.getDisplayName());
        email.setText(user.getEmail());
        updateData.get(source).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                fullName.setText(document.getString("name"));
                branch.setText(document.getString("branch"));
                phone.setText(document.getString("phone"));
                clgName.setText(document.getString("clgName"));
                organizer.setText(document.getString("organizer"));
                String role = document.getString("role");
                if (role!=null) {
                    txtOrgField.setVisibility(View.VISIBLE);
                    if (document.getString("organizer")!=null){
                        txtOrgField.setEnabled(false);
                    }else {
                        txtOrgField.setEnabled(true);
                    }
                }else {
                    txtOrgField.setVisibility(View.GONE);
                }
            } else {
                Log.d(TAG, "Cached get failed: ", task.getException());
            }
        });
        updateDetails.setOnClickListener(v -> {
            String org = Objects.requireNonNull(organizer.getText()).toString();
            Map<String,Object> details = new HashMap<>();
            details.put("name", Objects.requireNonNull(fullName.getText()).toString());
            details.put("phone", Objects.requireNonNull(phone.getText()).toString());
            details.put("clgName", Objects.requireNonNull(clgName.getText()).toString());
            details.put("branch", Objects.requireNonNull(branch.getText()).toString());
            if (org.equals("")){
                Log.i("Settings", "null");
            }else {
                details.put("organizer",Objects.requireNonNull(organizer.getText()).toString());
            }


            updateData.update(details).addOnSuccessListener(aVoid -> {
                Toast.makeText(requireContext(), "successfully updated!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "DocumentSnapshot successfully updated!");
            }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Error"+e, Toast.LENGTH_SHORT).show());

        });
        return view;
    }

    private final TextWatcher updateTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String name = Objects.requireNonNull(fullName.getText()).toString().trim();
            String phoneString = Objects.requireNonNull(phone.getText()).toString().trim();
            String branchString = Objects.requireNonNull(branch.getText()).toString().trim();

            String clgName = Objects.requireNonNull(branch.getText()).toString().trim();
            updateDetails.setEnabled(!name.isEmpty() && !phoneString.isEmpty() && !branchString.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}