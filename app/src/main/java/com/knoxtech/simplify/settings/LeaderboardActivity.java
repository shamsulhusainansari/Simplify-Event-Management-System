package com.knoxtech.simplify.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.knoxtech.simplify.DetailsActivity;
import com.knoxtech.simplify.R;
import com.knoxtech.simplify.model.Data;
import com.knoxtech.simplify.nav_fragments.HomeFragment;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class LeaderboardActivity extends AppCompatActivity {

    private FirestorePagingAdapter<Data, DataViewHolder> adapter;
    private FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        firebaseFirestore = FirebaseFirestore.getInstance();

        RecyclerView mFirestoreList = findViewById(R.id.leaderRecycler);

        Query query = firebaseFirestore.collection("LeaderBoard").orderBy("timestamp", Query.Direction.DESCENDING).orderBy("rank", Query.Direction.ASCENDING);
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(7)
                .setPageSize(7)
                .build();

        FirestorePagingOptions<Data> options=new FirestorePagingOptions.Builder<Data>()
                .setQuery(query,config,Data.class)
                .build();


        adapter=new FirestorePagingAdapter<Data, DataViewHolder>(options) {
            @NonNull
            @Override
            public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_layout,parent,false);
                return new DataViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull DataViewHolder holder, int position, @NonNull Data model) {
                Picasso.get().load(model.getProfilePicture()).into(holder.leaderImage);
                holder.leaderName.setText(model.getName());
                holder.leaderClg.setText(model.getClgName());
                holder.leaderBranch.setText("#Rank ".concat(model.getRank()));
                holder.leaderOrg.setText(model.getEventName().concat(" | ").concat(model.getOrganizer()));

            }

        };

        mFirestoreList.setHasFixedSize(true);
        mFirestoreList.setLayoutManager(new HomeFragment.WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mFirestoreList.setAdapter(adapter);
    }
    private static class DataViewHolder extends RecyclerView.ViewHolder{

        private final CircleImageView leaderImage;
        private final View mView;
        private final TextView leaderName,leaderClg,leaderOrg,leaderBranch;
        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            leaderImage=itemView.findViewById(R.id.src_profile);
            leaderName=itemView.findViewById(R.id.leaderName);
            leaderClg = itemView.findViewById(R.id.leaderClg);
            leaderBranch = itemView.findViewById(R.id.leaderBranch);
            leaderOrg = itemView.findViewById(R.id.organizerLeader);

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