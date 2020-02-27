package com.efandr.autotollgr;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
public class AccountFragment extends Fragment {

    private EditText accountemail, accountlicenceplate, accountvehicletype, accountpassword;
    private TextView accountusername, userdrivein, usertotal;

    private String userID;

    public User Current_User;

    protected DatabaseReference databaseReference;

    private FirebaseAuth mAuth;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public AccountFragment() {
        // Required empty public constructor
    }

    public static AccountFragment newInstance(String param1, String param2){
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1,param1);
        args.putString(ARG_PARAM2,param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View myFragmentView = inflater.inflate(R.layout.fragment_account, container, false);

        accountusername = (TextView) myFragmentView.findViewById(R.id.account_username);
        accountemail = (EditText) myFragmentView.findViewById(R.id.account_email);
        accountlicenceplate = (EditText) myFragmentView.findViewById(R.id.account_licenceplate);
        accountvehicletype = (EditText) myFragmentView.findViewById(R.id.account_vehicletype);
        accountpassword = (EditText) myFragmentView.findViewById(R.id.account_password);

        userdrivein = (TextView) myFragmentView.findViewById(R.id.account_drive_in);
        usertotal = (TextView) myFragmentView.findViewById(R.id.account_total_spend);

        Button button = (Button) myFragmentView.findViewById(R.id.exit_button);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FirebaseAuth.getInstance().signOut();
                getActivity().finish();
                startActivity(new Intent(getActivity(),LoginActivity.class));
            }
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        userID = user.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        userInfo(databaseReference);

        userStatistics(databaseReference);

        return myFragmentView;
    }

    private void userInfo(DatabaseReference database){
        database.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                // shake hands with each of them.
                for (DataSnapshot child : children) {
                    if(child.child("userid").getValue().equals(userID)){
                        String email = (String) child.child("email").getValue();
                        String username = (String) child.child("username").getValue();
                        String licenceplate = (String) child.child("licenceplate").getValue();
                        String vehicletype = (String) child.child("vehicletype").getValue();
                        String userid = (String) child.child("userid").getValue();
                        String password = (String) child.child("password").getValue();
                        Current_User= new User(email,username,licenceplate,vehicletype,userid,password);
                    }
                }

                accountusername.setText(Current_User.getUsername());
                accountemail.setText(Current_User.getEmail());
                accountlicenceplate.setText(Current_User.getLicenceplate());
                accountvehicletype.setText(Current_User.getVehicletype());
                accountpassword.setText(Current_User.getPassword());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void userStatistics(final DatabaseReference database){
        database.child("Purchase").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                // shake hands with each of them.
                for (DataSnapshot child : children) {
                    if(child.getKey().equals(Current_User.getLicenceplate())){
                        database.child("Purchase").child(Current_User.getLicenceplate()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                // shake hands with each of them.
                                int driveincnt = 0;
                                float totalspend = 0.0f;
                                for (DataSnapshot child : children) {
                                    driveincnt++;
                                    totalspend = totalspend + Float.parseFloat((String) child.child("cost").getValue());

                                }
                                userdrivein.setText(String.valueOf(driveincnt));
                                usertotal.setText(Float.toString(totalspend)+"€");
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
