package com.knoxtech.simplify.settings;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.knoxtech.simplify.R;
import com.knoxtech.simplify.model.Data;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class MyEventActivity extends AppCompatActivity {

public static class WrapContentLinearLayoutManager extends LinearLayoutManager {

    public WrapContentLinearLayoutManager(Context context) {
        super(context);
    }

    public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public WrapContentLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Log.e("TAG", "meet a IOOBE in RecyclerView");
        }
    }
}
    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore firebaseFirestore;
    private LinearProgressIndicator certificate_progress;
    private String p_id,org,selectedRole;
    private ProgressDialog mProgressDialog;
    private StorageReference storageRef;
    private String formattedDate;
    private static final int PICK_IMAGE_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_event);
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            org = extras.getString("organizer");
        }
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        formattedDate = df.format(c);
        RecyclerView mFirestoreList = findViewById(R.id.searchRecycler2);
        certificate_progress = findViewById(R.id.certificate_progress);
        Query query = firebaseFirestore.collection("Participants").whereEqualTo("organizer",org);
        FirestoreRecyclerOptions<Data> options=new FirestoreRecyclerOptions.Builder<Data>()
                .setQuery(query,Data.class)
                .build();
        adapter=new FirestoreRecyclerAdapter<Data, MyEventActivity.DataViewHolder>(options) {
            @NonNull
            @Override
            public MyEventActivity.DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.my_registrations,parent,false);
                return new DataViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MyEventActivity.DataViewHolder holder, int position, @NonNull Data model) {
                holder.name.setText(model.getName());
                holder.eventName.setText(model.getEventName());
                holder.org.setText(" | ".concat(model.getOrganizer()));
                holder.itemView.setTag(model.getP_id());
                if (model.getPaymentId()!=null){
                    holder.date.setText(getString(R.string.payment_id).concat(model.getPaymentId()));
                }else {
                    holder.date.setText(model.getEmail());
                }

                holder.date.setOnClickListener(view -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("", model.getEmail());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MyEventActivity.this, "copied to clipboard", Toast.LENGTH_SHORT).show();
                });


                holder.itemView.setOnClickListener(view -> {

                    final Dialog dialog = new Dialog(view.getContext(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(true);
                    dialog.setContentView(R.layout.popup_profile);
                    final CircleImageView[] profile = {dialog.findViewById(R.id.popup_profile_image)};
                    Picasso.get().load(model.getProfilePicture()).into(profile[0]);

                    Button assWinners = dialog.findViewById(R.id.assRole);
                    assWinners.setVisibility(View.VISIBLE);
                    assWinners.setEnabled(true);
                    assWinners.setText("Submit Rank");
                    TextInputLayout chooseWin = dialog.findViewById(R.id.txtRole);
                    chooseWin.setVisibility(View.VISIBLE);
                    chooseWin.setEnabled(true);
                    chooseWin.setHint("Select Rank");

                    AutoCompleteTextView assRole = dialog.findViewById(R.id.autoBranch);
                    String[] rrRoles = view.getResources().getStringArray(R.array.winners);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(),
                            R.layout.custom_list_item, R.id.text_view_list_item, rrRoles);
                    assRole.setAdapter(adapter);
                    assRole.setOnItemClickListener((parent, view1, position1, id) -> selectedRole = parent.getItemAtPosition(position1).toString());

                    assWinners.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (selectedRole==null){
                                Toast.makeText(MyEventActivity.this, "null", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }else
                            {
                                Map<String, Object> hos = new HashMap<>();
                                hos.put("eventName", model.getEventName());
                                hos.put("organizer", model.getOrganizer());
                                hos.put("name", model.getName());
                                hos.put("branch", model.getBranch());
                                hos.put("date", formattedDate);
                                hos.put("clgName",model.getClgName());
                                hos.put("email",model.getEmail());
                                hos.put("p_id", model.getP_id());
                                hos.put("profilePicture",model.getProfilePicture());
                                hos.put("rank",selectedRole);
                                hos.put("timestamp", FieldValue.serverTimestamp());
                                firebaseFirestore.collection("LeaderBoard").document(model.getP_id())
                                        .set(hos)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(MyEventActivity.this, "successful", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                            dialog.dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w(TAG, "Error writing document");
                                            Toast.makeText(MyEventActivity.this, "Error" + e, Toast.LENGTH_SHORT).show();
                                            //progressBar.setVisibility(View.GONE);
                                            dialog.dismiss();
                                        });
                            }
                        }
                    });
                    RelativeLayout re = dialog.findViewById(R.id.popup_profile);
                    re.setOnClickListener(view1 -> dialog.dismiss());
                    TextView name = dialog.findViewById(R.id.popup_person_name);
                    name.setText(model.getName());
                    TextView email = dialog.findViewById(R.id.popup_person_email);
                    email.setText(model.getEmail());
                    TextView clg = dialog.findViewById(R.id.popup_person_clg_name);
                    clg.setText(model.getClgName() );
                    TextView branch = dialog.findViewById(R.id.popup_person_clg_branch);
                    branch.setText(model.getBranch());

                    dialog.show();
                });
                holder.wGroup.setOnClickListener(view -> {
                    p_id = model.getP_id();
                    Intent intent = new Intent();
                    intent.setType("application/pdf");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Choose Certificate"), PICK_IMAGE_REQUEST);
                });

            }

        };

        mFirestoreList.setHasFixedSize(true);
        mFirestoreList.setLayoutManager(new MyEventActivity.WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mFirestoreList.setAdapter(adapter);

        TextInputEditText searchBox = findViewById(R.id.searchBox);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void afterTextChanged(Editable s) {
                Log.d("SearchFragment", "Searchbox has changed to: " + s.toString());
                Query query;
                if (s.toString().isEmpty()) {
                    query = firebaseFirestore.collection("Participants").whereEqualTo("organizer",org)
                            .orderBy("eventName", Query.Direction.ASCENDING);
                } else {
                    query = firebaseFirestore.collection("Participants").whereEqualTo("organizer",org)
                            .whereEqualTo("eventName", s.toString());
                }
                FirestoreRecyclerOptions<Data> options = new FirestoreRecyclerOptions.Builder<Data>()
                        .setQuery(query, Data.class)
                        .build();
                adapter.updateOptions(options);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private static class DataViewHolder extends RecyclerView.ViewHolder{

        TextView eventName, name,date,org;
        FloatingActionButton wGroup;

        //private final View mView;
        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.reg_name);
            eventName=itemView.findViewById(R.id.reg_event_name);
            org = itemView.findViewById(R.id.reg_organizer);
            date = itemView.findViewById(R.id.reg_date);

            wGroup = itemView.findViewById(R.id.w_group);
            wGroup.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_baseline_cloud_upload_24));


        }
    }
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri mImageUri = data.getData();

            certificate_progress.setVisibility(View.VISIBLE);
            uploadFIle(data.getData());
        }
    }

    private void uploadFIle(Uri data) {
        StorageReference ref = storageRef.child(System.currentTimeMillis() + "" + "." + getFileExtension(data) + ".jpg");
        UploadTask image_path = ref.putFile(data);
        Task<Uri> urlTask = image_path.continueWithTask(task -> {
            if (task.isSuccessful()) {

                UploadTask.TaskSnapshot downloadUri = task.getResult();
                assert downloadUri != null;
                Log.e("TASK:", "" + downloadUri);
            }


            return ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                assert downloadUri != null;
                Log.e("URL:", "" + downloadUri.toString());

                Map<String, Object> hos = new HashMap<>();
                hos.put("certificate", downloadUri.toString());
                firebaseFirestore.collection("Participants").document(p_id).update(hos).addOnSuccessListener(aVoid -> {
                    Toast.makeText(MyEventActivity.this, "Certificate Attached successfully", Toast.LENGTH_SHORT).show();
                    certificate_progress.setVisibility(View.GONE);
                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                }).addOnFailureListener(e -> {
                    certificate_progress.setVisibility(View.GONE);
                    Toast.makeText(MyEventActivity.this, "Failed to upload!!" + e, Toast.LENGTH_SHORT).show();
                });
            } else {
                certificate_progress.setVisibility(View.GONE);
                Toast.makeText(MyEventActivity.this, "Failed to upload!!!", Toast.LENGTH_SHORT).show();
            }
        });

    }
    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }
}