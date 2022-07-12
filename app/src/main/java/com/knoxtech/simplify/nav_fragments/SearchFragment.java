package com.knoxtech.simplify.nav_fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.knoxtech.simplify.R;
import com.knoxtech.simplify.model.Data;
import com.knoxtech.simplify.model.PersonRecyclerAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    private PersonRecyclerAdapter mAdapter;
    public SearchFragment() {
        // Required empty public constructor
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
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
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
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_search, container, false);

        RecyclerView searchRecycler = view.findViewById(R.id.searchRecycler);
        searchRecycler.setLayoutManager(new HomeFragment.WrapContentLinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        searchRecycler.setHasFixedSize(true);
        Query query = mDb.collection("Users").limit(10);
        FirestoreRecyclerOptions<Data> options=new FirestoreRecyclerOptions.Builder<Data>()
                .setQuery(query,Data.class)
                .build();
        mAdapter = new PersonRecyclerAdapter(options);
        searchRecycler.setAdapter(mAdapter);
        TextInputEditText searchBox = view.findViewById(R.id.searchBox);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("SearchFragment", "Searchbox has changed to: " + s.toString());
                Query query;
                if (s.toString().isEmpty()) {
                    query = mDb.collection("Users")
                            .orderBy("phone", Query.Direction.ASCENDING).limit(10);
                } else {
                    query = mDb.collection("Users")
                            .whereEqualTo("email", s.toString());
                }
                FirestoreRecyclerOptions<Data> options = new FirestoreRecyclerOptions.Builder<Data>()
                        .setQuery(query, Data.class)
                        .build();
                mAdapter.updateOptions(options);
                mAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}