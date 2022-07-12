package com.knoxtech.simplify;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.knoxtech.simplify.model.Data;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DetailsActivity extends AppCompatActivity implements PaymentResultListener {
    private String eventName,api,longDesc, organizer,postedBy,role,banner,w_group,name,branch,docId,shamsul,profilePicture,clgName,email,type,payment;
    private BitmapDrawable drawable;
    private Bitmap bitmap;
    private TextView event_Name,pub_Name;
    private ImageView bannerImage;
    private FirebaseAuth firebaseAuth;
    private String formattedDate;
    private FirebaseFirestore firebaseFirestore;
    private ProgressBar pro;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventName = extras.getString("eventName");
            longDesc = extras.getString("longDesc");
            organizer = extras.getString("organizer");
            postedBy = extras.getString("postedBy");
            role = extras.getString("role");
            banner = extras.getString("banner");
            w_group = extras.getString("w_group");
            docId = extras.getString("docId");
            type = extras.getString("type");
            api = extras.getString("api");
            payment = extras.getString("payment");
        } else {
            Toast.makeText(this, "Null String", Toast.LENGTH_SHORT).show();
        }
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        formattedDate = df.format(c);
        DocumentReference docRef = firebaseFirestore.collection("Users").document(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            Data user = documentSnapshot.toObject(Data.class);
            assert user != null;
            name = user.getName();
            branch = user.getBranch();
            clgName = user.getClgName();
            profilePicture = user.getProfilePicture();
            email = user.getEmail();
        });
        pro = findViewById(R.id.pro);
        bannerImage = findViewById(R.id.bannerImage);
        Picasso.get().load(banner).into(bannerImage);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                DetailsActivity.this, R.style.BottomSheetDialogTheme
        );
        final View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(
                        R.layout.bottom_sheet,
                        findViewById(R.id.bottom_sheet_layout)
                );

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {

                    Uri deepLink = null;
                    if (pendingDynamicLinkData!=null){
                        deepLink =pendingDynamicLinkData.getLink();

                        String refLink = deepLink.toString();
                        try {
                            refLink = refLink.substring(refLink.lastIndexOf("=")+1);
                            String ddId = refLink.substring(0,refLink.lastIndexOf("-"));

                            Log.e("DetailsActivity",refLink.substring(0,refLink.indexOf("-")));
                            shamsul = refLink.substring(0,refLink.indexOf("-"));

                        }catch (Exception e){
                            Log.e("DetailsActivity",e.getMessage());
                        }
                    }


                    ProgressBar progressBar = bottomSheetView.findViewById(R.id.progressDialog);
                    event_Name = bottomSheetView.findViewById(R.id.eventName);
                    event_Name.setText(eventName);
                    pub_Name = bottomSheetView.findViewById(R.id.publisherName);

                    Button btnApply = bottomSheetView.findViewById(R.id.btnApply);

                    TextView long_Desc = bottomSheetView.findViewById(R.id.longDesc);

                    try {
                        pub_Name.setText(organizer.concat(", ").concat(postedBy + " - " + role));
                        long_Desc.setText(Html.fromHtml(longDesc, Html.FROM_HTML_MODE_COMPACT));
                    }catch (Exception e){
                        DocumentReference refLinkDoc = firebaseFirestore.collection("Events").document(shamsul);
                        refLinkDoc.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {

                                    eventName = document.getString("eventName");
                                    longDesc = document.getString("longDesc");
                                    organizer = document.getString("organizer");
                                    postedBy = document.getString("postedBy");
                                    role = document.getString("role");
                                    banner = document.getString("banner");
                                    w_group = document.getString("w_group");
                                    docId = document.getString("docId");
                                    type = document.getString("type");
                                    payment = document.getString("payment");
                                    api = document.getString("api");
                                    pub_Name.setText(organizer.concat(", ").concat(postedBy + " - " + role));
                                    long_Desc.setText(Html.fromHtml(longDesc, Html.FROM_HTML_MODE_COMPACT));
                                    event_Name.setText(eventName);
//                                    if (payment!=null){
//                                        payLink.setText(payment);
//                                    }
                                    Picasso.get().load(banner).into(bannerImage);
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        });
                    }


                    btnApply.setOnClickListener(v -> {
                        if (type==null || type.equals("Paid")){
                            Checkout checkout = new Checkout();
                            checkout.setKeyID(api);
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("name",user.getDisplayName());
                                jsonObject.put("currency","INR");
                                jsonObject.put("amount",payment);
                                jsonObject.put("prefill.email",user.getEmail());

                                checkout.open(this, jsonObject);
                            }catch (Exception e){
                                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
                                Log.e("Error","Error in payment");

                            }
                        }else {
                            progressBar.setVisibility(View.VISIBLE);
                            String docStr = eventName.replaceAll("\\s", "");
                            Map<String, Object> registration = new HashMap<>();
                            registration.put("userId", Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
                            registration.put("eventName", eventName);
                            registration.put("organizer", organizer);
                            registration.put("w_group", w_group);
                            registration.put("name", name);
                            registration.put("branch", branch);
                            registration.put("profilePicture",profilePicture);
                            registration.put("date", formattedDate);
                            registration.put("clgName",clgName);
                            registration.put("email",email);
                            registration.put("timestamp", FieldValue.serverTimestamp());
                            registration.put("p_id", docStr + firebaseAuth.getCurrentUser().getUid());
                            firebaseFirestore.collection("Participants").document(docStr + Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())
                                    .set(registration)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(DetailsActivity.this, "Your registration is complete", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                        progressBar.setVisibility(View.GONE);
                                        if (w_group != null) {
                                            bottomSheetDialog.dismiss();
                                            Uri uri = Uri.parse(w_group);
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            startActivity(intent);
                                        } else {
                                            bottomSheetDialog.dismiss();
                                            Toast.makeText(this, "No group found", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(DetailsActivity.this, EventActivity.class));
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Error writing document", e);
                                        Toast.makeText(DetailsActivity.this, "Error" + e, Toast.LENGTH_SHORT).show();
                                        bottomSheetDialog.dismiss();
                                        progressBar.setVisibility(View.GONE);
                                    });
                        }
                    });
                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_COLLAPSED);
                    bottomSheetDialog.show();

                    findViewById(R.id.bottomBtn).setOnClickListener(v -> {
                        bottomSheetDialog.show();
                    });


                    findViewById(R.id.shareBtn).setOnClickListener(view -> {

                        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                                .setLink(Uri.parse("https://simplify56.page.link/"))
                                .setDynamicLinkDomain("simplify56.page.link")
                                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                                .buildDynamicLink();
                        Uri dynamicLinkUri = dynamicLink.getUri();
                        Log.e("main", "  Long refer " + dynamicLink.getUri());
                        @SuppressLint("UseCompatLoadingForDrawables") String sharelinktext = "https://simplify56.page.link/?" +
                                "link=http://simplify56.page.link/myrefer.php?docId=" + docId +"-" +
                                "&apn=" + getPackageName() +
                                "&st=" + eventName +
                                "&sd=" + Html.fromHtml(longDesc).toString() +
                                "&si=" + "https://firebasestorage.googleapis.com/v0/b/simplify-b9f88.appspot.com/o/SIMPLIFY%20%E2%80%93%2010.png?alt=media&token=f83296e5-ee42-4cc8-a2df-44c641842b8b";

                        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                                .setLongLink(Uri.parse(sharelinktext))
                                .buildShortDynamicLink()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Uri shortLink = task.getResult().getShortLink();
                                        Uri flowchartLink = task.getResult().getPreviewLink();
                                        Log.e("main ", "short link "+ Objects.requireNonNull(shortLink));
                                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                        StrictMode.setVmPolicy(builder.build());
                                        drawable=(BitmapDrawable) bannerImage.getDrawable();
                                        bitmap =drawable.getBitmap();
                                        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"Simplify",null);
                                        Uri uri = Uri.parse(bitmapPath);
                                        Intent intent;
                                        try {
                                            intent = new Intent();
                                            intent.setType("image/*");
                                            intent.putExtra(Intent.EXTRA_STREAM,uri);
                                            intent.setAction(Intent.ACTION_SEND);
                                            intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(longDesc).toString() + " " + shortLink);
                                            startActivity(intent);
                                        }catch (Exception e){
                                            throw  new RuntimeException(e);
                                        }
                                        startActivity(Intent.createChooser(intent,"Share Via"));
                                    } else {
                                        Log.e("main", " error "+task.getException() );
                                    }
                                });
                    });
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user==null){
            startActivity(new Intent(DetailsActivity.this,WelcomeActivity.class));
            finish();
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        Map<String, Object> hos = new HashMap<>();
        hos.put("paymentId", s);
        String docStr = eventName.replaceAll("\\s", "");
        hos.put("userId", user.getUid());
        hos.put("eventName", eventName);
        hos.put("organizer", organizer);
        hos.put("w_group", w_group);
        hos.put("profilePicture",profilePicture);
        hos.put("name", name);
        hos.put("branch", branch);
        hos.put("date", formattedDate);
        hos.put("clgName",clgName);
        hos.put("email",email);
        hos.put("timestamp", FieldValue.serverTimestamp());
        hos.put("p_id", docStr + user.getUid());
        firebaseFirestore.collection("Participants").document(docStr + user.getUid())
                .set(hos)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DetailsActivity.this, "Your registration is complete", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                    if (w_group != null) {
                        pro.setVisibility(View.GONE);
                        Uri uri = Uri.parse(w_group);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } else {
                        pro.setVisibility(View.GONE);
                        Toast.makeText(this, "No group found", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(DetailsActivity.this, EventActivity.class));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error writing document");
                    Toast.makeText(DetailsActivity.this, "Error" + e, Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show();
    }
}