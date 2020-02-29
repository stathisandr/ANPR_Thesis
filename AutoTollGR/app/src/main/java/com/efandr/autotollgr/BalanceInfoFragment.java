package com.efandr.autotollgr;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class BalanceInfoFragment extends Fragment {

    public static final int NONE = 0;
    public static final int VISA = 1;
    public static final int MASTERCARD = 2;
    public static final int DISCOVER = 3;
    public static final int AMEX = 4;

    public static final String VISA_PREFIX = "4";
    public static final String MASTERCARD_PREFIX = "51,52,53,54,55,";
    public static final String DISCOVER_PREFIX = "6011";
    public static final String AMEX_PREFIX = "34,37,";

    private TextView cardholder, cardcvv, cardnumber, cardexp;

    private ImageView cardbank;

    private String userID;
    private FirebaseUser user;
    protected DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    private CreditCard UsersCard;




    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public BalanceInfoFragment() {
        // Required empty public constructor
    }

    public static BalanceInfoFragment newInstance(String param1, String param2) {
        BalanceInfoFragment fragment = new BalanceInfoFragment();
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
        View myFragmentView = inflater.inflate(R.layout.fragment_balance_info, container, false);

        cardcvv = (TextView) myFragmentView.findViewById(R.id.card_cvv);
        cardexp = (TextView) myFragmentView.findViewById(R.id.card_validity);
        cardholder = (TextView) myFragmentView.findViewById(R.id.card_holder_name);
        cardnumber = (TextView) myFragmentView.findViewById(R.id.card_card_number);

        cardbank = (ImageView) myFragmentView.findViewById(R.id.card_Type);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        userID = user.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        usersCardInfo(databaseReference);

        return myFragmentView;

    }

    private void usersCardInfo(DatabaseReference database){
        database.child("Cards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                // shake hands with each of them.
                for (DataSnapshot child : children) {
                    if(child.child("userid").getValue().equals(userID)){
                        String cvv = (String) child.child("cardcvv").getValue();
                        String holder = (String) child.child("cardholder").getValue();
                        String number = (String) child.child("cardnumber").getValue();
                        String expired = (String) child.child("cardexpireday").getValue();
                        String userid = (String) child.child("userid").getValue();
                        UsersCard = new CreditCard(holder, number, cvv, expired,userid);
                    }
                }
                cardholder.setText(UsersCard.getCardholder());
                cardnumber.setText(UsersCard.getCardnumber().substring(0,4)+"-"+UsersCard.getCardnumber().substring(4,8)+"-"+UsersCard.getCardnumber().substring(8,12)+"-"+UsersCard.getCardnumber().substring(12,16));
                cardexp.setText(UsersCard.getCardexpireday().substring(0,2)+"/"+UsersCard.getCardexpireday().substring(2,4));
                cardcvv.setText(UsersCard.getCardcvv());
                isValid(UsersCard.getCardnumber());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public static int getCardType(String cardNumber) {

        if (cardNumber.substring(0, 1).equals(VISA_PREFIX))
            return VISA;
        else if (MASTERCARD_PREFIX.contains(cardNumber.substring(0, 2) + ","))
            return MASTERCARD;
        else if (AMEX_PREFIX.contains(cardNumber.substring(0, 2) + ","))
            return AMEX;
        else if (cardNumber.substring(0, 4).equals(DISCOVER_PREFIX))
            return DISCOVER;

        return NONE;
    }

    public boolean isValid(String cardNumber) {
        if (!TextUtils.isEmpty(cardNumber) && cardNumber.length() >= 4)
            if (getCardType(cardNumber) == VISA && ((cardNumber.length() == 13 || cardNumber.length() == 16)))
                cardbank.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_visa));
            else if (getCardType(cardNumber) == MASTERCARD && cardNumber.length() == 16)
                cardbank.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_mastercard));
            else if (getCardType(cardNumber) == AMEX && cardNumber.length() == 15)
                cardbank.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_amex));
            else if (getCardType(cardNumber) == DISCOVER && cardNumber.length() == 16)
                cardbank.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_discover));
        return false;
    }
}
