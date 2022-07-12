package com.knoxtech.simplify;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.inappmessaging.FirebaseInAppMessaging;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.knoxtech.simplify.nav_fragments.HomeFragment;
import com.knoxtech.simplify.nav_fragments.RegistratonFragment;
import com.knoxtech.simplify.nav_fragments.SearchFragment;
import com.knoxtech.simplify.nav_fragments.SettingsFragment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class EventActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    HomeFragment homeFragment = new HomeFragment();
    SearchFragment searchFragment = new SearchFragment();
    RegistratonFragment registratonFragment = new RegistratonFragment();
    SettingsFragment settingsFragment = new SettingsFragment();
    FloatingActionButton fab;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private StorageReference storageRef;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    private Long tsLong;
    private String role,org;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        FirebaseMessaging.getInstance().subscribeToTopic("notification");
        FirebaseInAppMessaging.getInstance().setAutomaticDataCollectionEnabled(true);
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        Bundle bundle = new Bundle();
        assert user != null;
        DocumentReference docRef = firebaseFirestore.collection("Users").document(user.getUid());
        fab = findViewById(R.id.fab);
        fab.setEnabled(false);
        fab.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(EventActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(EventActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(EventActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                showBottomSheetDialog();
            }

        });

        tsLong = System.currentTimeMillis()/1000;
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Document found in the offline cache
                DocumentSnapshot document = task.getResult();
                role = document.getString("role");
                org = document.getString("organizer");
                if (role!=null){
                    fab.setEnabled(true);
                    Log.d("False", "onComplete: "+role);
                }
            } else {
                Log.d("EventActivity", "Cached get failed: ", task.getException());
            }
        });
        bottomNavigationView  = findViewById(R.id.bottomNavigationView);

        getSupportFragmentManager().beginTransaction().replace(R.id.container,homeFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.home:
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "0");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "HomeFragment");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,homeFragment).commit();

                    return true;
                case R.id.search:
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "SearchFragment");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,searchFragment).commit();
                    return true;
                case R.id.registrations:
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "2");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "RegistrationFragment");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,registratonFragment).commit();
                    return true;
                case R.id.settings:
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "3");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "SettingsFragment");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,settingsFragment).commit();
                    return true;
            }

            return false;
        });
    }

    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.post_data_bottom_sheet);
        TextInputEditText eventName, organizer, whatsapp, eventDetails, paymentLink;
        eventName = bottomSheetDialog.findViewById(R.id.editEventName);
        organizer = bottomSheetDialog.findViewById(R.id.editOrganizer);
        paymentLink = bottomSheetDialog.findViewById(R.id.editPaymentLink);
        TextInputLayout payText = bottomSheetDialog.findViewById(R.id.txtPayment);
        Objects.requireNonNull(organizer).setText(org);
        whatsapp = bottomSheetDialog.findViewById(R.id.editWhatsappGroup);
        eventDetails = bottomSheetDialog.findViewById(R.id.editPosterInfo);
        ProgressBar pro = bottomSheetDialog.findViewById(R.id.progress_bar);
        ExtendedFloatingActionButton btnPostData = bottomSheetDialog.findViewById(R.id.publishEvent);
        TextView chooseImage = bottomSheetDialog.findViewById(R.id.poster);
        assert chooseImage != null;
        chooseImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(EventActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(EventActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(EventActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                openFileChooser();
            }
        });
        RadioGroup identityGroup = bottomSheetDialog.findViewById(R.id.radioGroup);
        identityGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.dealerGrp:
                        payText.setVisibility(View.GONE);
                        break;
                    case R.id.ansPaper:
                        payText.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
        assert btnPostData != null;
        btnPostData.setOnClickListener(view -> {

            RadioButton radioButton = bottomSheetDialog.findViewById(Objects.requireNonNull(identityGroup).getCheckedRadioButtonId());
            String identify = Objects.requireNonNull(radioButton).getText().toString();
            String amount = paymentLink.getText().toString();
            if (mImageUri == null || TextUtils.isEmpty(Objects.requireNonNull(eventName).getText()) || TextUtils.isEmpty(Objects.requireNonNull(organizer).getText()) || TextUtils.isEmpty(Objects.requireNonNull(whatsapp).getText()) || TextUtils.isEmpty(Objects.requireNonNull(eventDetails).getText())) {
                Toast.makeText(getApplicationContext(), "Fill the detail first!", Toast.LENGTH_SHORT).show();
            } else {

                assert pro != null;
                pro.setVisibility(View.VISIBLE);
                if (mImageUri != null) {
                    StorageReference ref = storageRef.child(System.currentTimeMillis() + "" + "." + getFileExtension(mImageUri) + ".jpg");
                    UploadTask image_path = ref.putFile(mImageUri);
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
                            hos.put("banner", downloadUri.toString());
                            hos.put("docId", tsLong.toString());
                            hos.put("eventName", Objects.requireNonNull(eventName.getText()).toString());
                            hos.put("organizer", Objects.requireNonNull(organizer.getText()).toString());
                            hos.put("w_group", Objects.requireNonNull(whatsapp.getText()).toString());
                            hos.put("longDesc", Objects.requireNonNull(eventDetails.getText()).toString());
                            hos.put("payment", String.valueOf(Integer.parseInt(amount) * 100));
                            hos.put("postedBy", user.getDisplayName());
                            hos.put("role", role);
                            hos.put("type", identify);
                            hos.put("timestamp", FieldValue.serverTimestamp());
                            hos.put("pubEmail", user.getEmail());
                            firebaseFirestore.collection("Events").document(tsLong.toString())
                                    .set(hos, SetOptions.merge());
                            pro.setVisibility(View.GONE);
                            Toast.makeText(EventActivity.this, "Published", Toast.LENGTH_SHORT).show();

                            bottomSheetDialog.dismiss();
                        } else {
                            Toast.makeText(EventActivity.this, "Failed to post!!!", Toast.LENGTH_SHORT).show();
                            pro.setVisibility(View.GONE);
                            bottomSheetDialog.dismiss();
                        }
                    });
                }
            }

        });
        bottomSheetDialog.show();
    }
        private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Poster"),PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Toast.makeText(this, "Selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser()==null){
            startActivity(new Intent(EventActivity.this,WelcomeActivity.class));
            finish();
        }
    }
}