package com.efandr.autotollgr;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    ListView purchaseListView;
    ArrayList<Purchase> purchaseList;

    PurchaseListAdapter purchaseListAdapter;

    private FirebaseUser user;

    private String userID;

    protected DatabaseReference databaseReference;

    private FirebaseAuth mAuth;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View myFragmentView = inflater.inflate(R.layout.fragment_history, container, false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        userID = user.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();


        purchaseListView = (ListView) myFragmentView.findViewById(R.id.purchase_list_view);

        purchaseList = new ArrayList<>();

        getUsersPurchases();

        return myFragmentView;

    }

    private void showUsersPurchases(Context context){
        purchaseListAdapter = new PurchaseListAdapter(context,R.layout.listview_row,purchaseList);
        purchaseListView.setAdapter(purchaseListAdapter);
    }

    private void getUsersPurchases() {

        databaseReference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                // shake hands with each of them.
                for (DataSnapshot child : children) {
                    if(child.child("userid").getValue().equals(userID)){
                        final String LicencePlate = child.getKey();
                        databaseReference.child("Purchase").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                // shake hands with each of them.
                                for (DataSnapshot child : children) {
                                    if(child.getKey().equals(LicencePlate)){
                                        databaseReference.child("Purchase").child(LicencePlate).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                                // shake hands with each of them.
                                                for (DataSnapshot child : children) {
                                                    String day = child.child("day").getValue().toString();
                                                    float cost = Float.parseFloat(child.child("cost").getValue().toString());
                                                    String time = child.child("time").getValue().toString();
                                                    purchaseList.add(new Purchase(day,cost,time));

                                                }
                                                showUsersPurchases(getContext());
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
