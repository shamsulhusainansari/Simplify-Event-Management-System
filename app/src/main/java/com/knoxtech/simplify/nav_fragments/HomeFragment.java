package com.knoxtech.simplify.nav_fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.knoxtech.simplify.DetailsActivity;
import com.knoxtech.simplify.R;
import com.knoxtech.simplify.model.Data;
import com.knoxtech.simplify.settings.InfoActivity;
import com.knoxtech.simplify.settings.LeaderboardActivity;
import com.knoxtech.simplify.settings.MyEventActivity;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //private FirestoreRecyclerAdapter adapter;
    private FirestorePagingAdapter<Data, DataViewHolder> adapter;
    private FirebaseFirestore firebaseFirestore;
    private String userId,org;
    private int i = 0;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
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

        //... constructor
        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("TAG", "meet a IOOBE in RecyclerView");
            }
        }
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        TextView profileName = view.findViewById(R.id.userName);
        CircleImageView profileImage = view.findViewById(R.id.profile_image);
        Picasso.get().load(Objects.requireNonNull(firebaseAuth.getCurrentUser().getPhotoUrl()).toString()).into(profileImage);
        profileName.setText("Home, ".concat(Objects.requireNonNull(firebaseAuth.getCurrentUser().getDisplayName())));
        RecyclerView mFirestoreList = view.findViewById(R.id.dashboard_recycler);

        profileImage.setOnClickListener(view17 -> {
            final Dialog dialog = new Dialog(view17.getContext(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.custom_mini_profile);

            RelativeLayout minirelative = dialog.findViewById(R.id.mini_relative);

            CircleImageView pic = dialog.findViewById(R.id.miniProfilePic);
            Picasso.get().load(Objects.requireNonNull(firebaseAuth.getCurrentUser().getPhotoUrl()).toString()).into(pic);

            TextView name = dialog.findViewById(R.id.mini_name);
            name.setText(firebaseAuth.getCurrentUser().getDisplayName());
            TextView email = dialog.findViewById(R.id.mini_email);
            email.setText(firebaseAuth.getCurrentUser().getEmail());
            MaterialButton my_event  = dialog.findViewById(R.id.my_events);
            dialog.findViewById(R.id.privacy).setOnClickListener(view16 -> {
                Uri uri = Uri.parse("http://simplify-b9f88.web.app/Privacy.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            });
            dialog.findViewById(R.id.leaderBoard).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(requireActivity(), LeaderboardActivity.class));
                    dialog.dismiss();
                }
            });
            dialog.findViewById(R.id.privacy).setOnClickListener(view15 -> {
                Uri uri = Uri.parse("http://simplify-b9f88.web.app/terms.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            });
            DocumentReference docRef = firebaseFirestore.collection("Users").document(Objects.requireNonNull(firebaseAuth.getUid()));
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        org = document.getString("organizer");
                        if (org==null){
                            my_event.setVisibility(View.GONE);
                        }else {

                            my_event.setVisibility(View.VISIBLE);

                        }
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            });
            my_event.setOnClickListener(view1 -> {
                dialog.dismiss();
                if (ContextCompat.checkSelfPermission(view17.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(view17.getContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {

                    Intent i = new Intent(requireActivity(), MyEventActivity.class);
                    i.putExtra("uid", userId);
                    i.putExtra("identify",)
                    startActivity(i);
                }
            });
            MaterialButton signOut  = dialog.findViewById(R.id.sign_out);
            signOut.setOnClickListener(view1 -> {
                dialog.dismiss();
                startActivity(new Intent(requireActivity(), InfoActivity.class));
            });



            MaterialButton share  = dialog.findViewById(R.id.share_app);
            share.setOnClickListener(view12 -> {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.knoxtech.simplify");
                intent.setType("text/plain");
                startActivity(intent);
            });
            minirelative.setOnClickListener(view13 -> dialog.dismiss());
            dialog.show();
        });
        Query query = firebaseFirestore.collection("Events").orderBy("timestamp", Query.Direction.DESCENDING);
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(2)
                .setPageSize(2)
                .build();

        FirestorePagingOptions<Data> options=new FirestorePagingOptions.Builder<Data>()
                .setQuery(query,config,Data.class)
                .build();

        adapter=new FirestorePagingAdapter<Data, HomeFragment.DataViewHolder>(options) {
            @NonNull
            @Override
            public HomeFragment.DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_dashboard,parent,false);
                return new HomeFragment.DataViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull HomeFragment.DataViewHolder holder, int position, @NonNull Data model) {
                Picasso.get().load(model.getBanner()).into(holder.bannerImage);
                holder.itemView.setOnClickListener(view14 -> {
                    i++;
                    Handler handler=new Handler();
                    handler.postDelayed(() -> {
                        if (i==1){
                            Intent i = new Intent(requireContext(), DetailsActivity.class);
                            i.putExtra("eventName", model.getEventName());
                            i.putExtra("longDesc", model.getLongDesc());
                            i.putExtra("organizer", model.getOrganizer());
                            i.putExtra("postedBy", model.getPostedBy());
                            i.putExtra("role", model.getRole());
                            i.putExtra("banner", model.getBanner());
                            i.putExtra("docId", model.getDocId());
                            i.putExtra("w_group", model.getW_group());
                            i.putExtra("type",model.getType());
                            i.putExtra("payment",model.getPayment());
                            i.putExtra("api",model.getApi());
                            startActivity(i);
                        }else if (i==2){
                            firebaseFirestore.collection("Events/"+model.getDocId()+"/Likes").document(userId).get().addOnCompleteListener(task -> {
                                if (!task.getResult().exists()){
                                    holder.animationLike.setVisibility(View.VISIBLE);
                                    holder.animationLike.setAnimationFromUrl("https://assets9.lottiefiles.com/packages/lf20_2ngkezwf.json");
                                    holder.animationLike.playAnimation();
                                    Map<String,Object> likesMap = new HashMap<>();
                                    likesMap.put("timestamp", FieldValue.serverTimestamp());
                                    firebaseFirestore.collection("Events/"+model.getDocId()+"/Likes").document(userId).set(likesMap);
                                    Handler handler1 = new Handler();
                                    handler1.postDelayed(() -> holder.animationLike.setVisibility(View.GONE), 1000);

                                }else {
                                    holder.animationLike.setVisibility(View.VISIBLE);
                                    holder.animationLike.setAnimationFromUrl("https://assets8.lottiefiles.com/packages/lf20_yv1cyyys.json");
                                    holder.animationLike.playAnimation();
                                    firebaseFirestore.collection("Events/"+model.getDocId()+"/Likes").document(userId).delete();
                                    Handler handler1 = new Handler();
                                    handler1.postDelayed(() -> holder.animationLike.setVisibility(View.GONE), 1000);

                                }
                            });
                        }
                        i=0;
                    },500);
                });
                holder.likeBtn.setOnClickListener(v -> firebaseFirestore.collection("Events/"+model.getDocId()+"/Likes").document(userId).get().addOnCompleteListener(task -> {
                    if (!task.getResult().exists()){
                        holder.animationLike.setVisibility(View.VISIBLE);
                        holder.animationLike.setAnimationFromUrl("https://assets9.lottiefiles.com/packages/lf20_2ngkezwf.json");
                        holder.animationLike.playAnimation();
                        Map<String,Object> likesMap = new HashMap<>();
                        likesMap.put("timestamp", FieldValue.serverTimestamp());
                        firebaseFirestore.collection("Events/"+model.getDocId()+"/Likes").document(userId).set(likesMap);
                        Handler handler = new Handler();
                        handler.postDelayed(() -> holder.animationLike.setVisibility(View.GONE), 1000);
                    }else {
                        holder.animationLike.setVisibility(View.VISIBLE);
                        holder.animationLike.setAnimationFromUrl("https://assets8.lottiefiles.com/packages/lf20_yv1cyyys.json");
                        holder.animationLike.playAnimation();
                        firebaseFirestore.collection("Events/"+model.getDocId()+"/Likes").document(userId).delete();
                        Handler handler = new Handler();
                        handler.postDelayed(() -> holder.animationLike.setVisibility(View.GONE), 1000);
                    }
                }));
                firebaseFirestore.collection("Events/"+model.getDocId()+"/Likes").addSnapshotListener((value, error) -> {

                    if (!Objects.requireNonNull(value).isEmpty()){
                        int count = value.size();
                        holder.updateLikesCount(count);
                    }else{
                        holder.updateLikesCount(0);
                    }
                });
                firebaseFirestore.collection("Events/"+model.getDocId()+"/Likes").document(userId).addSnapshotListener((value, error) -> {
                    if (Objects.requireNonNull(value).exists()){
                        holder.likeBtn.setImageDrawable(ResourcesCompat.getDrawable(requireActivity().getResources(),R.drawable.fav_red_24,null));
                    }else{
                        holder.likeBtn.setImageDrawable(ResourcesCompat.getDrawable(requireActivity().getResources(),R.drawable.fav_grey_24,null));
                    }
                });
            }
        };
        mFirestoreList.setHasFixedSize(true);
        mFirestoreList.setLayoutManager(new WrapContentLinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        mFirestoreList.setAdapter(adapter);
        return view;
    }
    private static class DataViewHolder extends RecyclerView.ViewHolder{
        private final ImageView bannerImage;
        private final ImageView likeBtn;
        private final View mView;
        private final LottieAnimationView animationLike;
        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            bannerImage=itemView.findViewById(R.id.portrait_banner);
            likeBtn=itemView.findViewById(R.id.likeBtn);
            animationLike = itemView.findViewById(R.id.likeAnimation);


        }


        public void updateLikesCount(int count) {
            TextView likeCount = mView.findViewById(R.id.likeCount);
            likeCount.setText("# ".concat(String.valueOf(count)).concat(" Likes"));
        }
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