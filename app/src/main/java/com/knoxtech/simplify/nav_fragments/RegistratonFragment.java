package com.knoxtech.simplify.nav_fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.knoxtech.simplify.R;
import com.knoxtech.simplify.model.Data;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegistratonFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegistratonFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    String organizer;
    private FirestoreRecyclerAdapter adapter;
    public RegistratonFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegistratonFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegistratonFragment newInstance(String param1, String param2) {
        RegistratonFragment fragment = new RegistratonFragment();
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
        View view = inflater.inflate(R.layout.fragment_registraton, container, false);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        RecyclerView mFirestoreList = view.findViewById(R.id.reg_recycler);


        assert user != null;
        Query query = firebaseFirestore.collection("Participants").whereEqualTo("userId", user.getUid());
        //Log.i("Shamsul",""+organizer);

        FirestoreRecyclerOptions<Data> options=new FirestoreRecyclerOptions.Builder<Data>()
                .setQuery(query,Data.class)
                .build();

        adapter=new FirestoreRecyclerAdapter<Data, RegistratonFragment.DataViewHolder>(options) {
            @NonNull
            @Override
            public RegistratonFragment.DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.my_registrations,parent,false);
                return new RegistratonFragment.DataViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull RegistratonFragment.DataViewHolder holder, int position, @NonNull Data model) {
                holder.eventName.setText(model.getEventName().concat("  |  "));
                holder.name.setText(model.getName());
                holder.date.setText(model.getDate());

                holder.itemView.setOnClickListener(v -> {
                    if (model.getW_group()!=null){
                        Uri uri = Uri.parse(model.getW_group());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }else {
                        Toast.makeText(requireActivity(), "No group found", Toast.LENGTH_SHORT).show();
                    }
                });

                holder.wGroup.setOnClickListener(view1 -> {
                    if (model.getCertificate()==null){
                        Toast.makeText(view1.getContext(), "No Certificate Attached!", Toast.LENGTH_SHORT).show();
                    }else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(model.getCertificate()),"application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        view1.getContext().startActivity(intent);
                    }
                });


            }

        };

        mFirestoreList.setHasFixedSize(true);
        mFirestoreList.setLayoutManager(new RegistratonFragment.WrapContentLinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        mFirestoreList.setAdapter(adapter);
        return view;
    }

    private static class DataViewHolder extends RecyclerView.ViewHolder{

        TextView eventName, name,date,org;
        FloatingActionButton wGroup;
        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.reg_name);
            eventName=itemView.findViewById(R.id.reg_event_name);
            org = itemView.findViewById(R.id.reg_organizer);
            date = itemView.findViewById(R.id.reg_date);

            wGroup = itemView.findViewById(R.id.w_group);
            wGroup.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic__cd99b4095bb136a03fda869c0d38314));
            //wGroup.setColorFilter(Color.parseColor("#FFD700"));
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