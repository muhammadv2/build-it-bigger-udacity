package com.arts.m3droid.empl.ui.requestedOffers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arts.m3droid.empl.Constants;
import com.arts.m3droid.empl.R;
import com.arts.m3droid.empl.model.Employee;
import com.arts.m3droid.empl.model.RequestedOffer;
import com.arts.m3droid.empl.utils.FirebaseFactory;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RequestedOffersFragment extends Fragment implements RequestedOffersAdapter.OnItemClicked {

    @BindView(R.id.rv_special_offers)
    RecyclerView rvSpecialOffers;

    private List<RequestedOffer> offerRequests;
    private Employee employee;
    private DatabaseReference offerUnAnsweredRef;

    public RequestedOffersFragment() {
        // Required empty public constructor
    }

    public static RequestedOffersFragment requestedOfferWithArgs(Employee employee) {
        RequestedOffersFragment f = new RequestedOffersFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(Constants.NODE_EMPLOYEE, employee);
        f.setArguments(args);
        return f;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requested_offers, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        offerRequests = new ArrayList<>();

        if (getArguments() != null)
            employee = getArguments().getParcelable(Constants.NODE_EMPLOYEE);

        loadDataFromFirebase();
    }

    private void loadDataFromFirebase() {
        offerUnAnsweredRef = FirebaseFactory.getDatabase().getReference(Constants.UNANSWERED_OFFERS);

        offerUnAnsweredRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RequestedOffer requestedOffer = dataSnapshot.getValue(RequestedOffer.class);

                if (requestedOffer == null) return;

                
                // Find if there emp number associated with the offer
                if (dataSnapshot.hasChild(Constants.NODE_EMP_NUMBER)) {
                    //If found check the current emp number before adding it to his offers
                    if (requestedOffer.getEmployeeKey().equals(employee.getEmpNumber())) {
                        requestedOffer.setCurrentNodeKey(dataSnapshot.getKey());
                        requestedOffer.setEmployeeKey(employee.getEmpNumber());
                        offerRequests.add(requestedOffer); // add this offer to this emp only)
                    }
                } else {
                    //There's no key add it directly to all
                    requestedOffer.setCurrentNodeKey(dataSnapshot.getKey());
                    requestedOffer.setEmployeeKey(Constants.NOT_SPECIFIED_EMP_NUM);
                    offerRequests.add(requestedOffer);
                }
                setUpRecyclerView();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUpRecyclerView() {
        rvSpecialOffers.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rvSpecialOffers.setLayoutManager(layoutManager);
        RequestedOffersAdapter adapter =
                new RequestedOffersAdapter(offerRequests, this);
        rvSpecialOffers.setAdapter(adapter);
    }


    @Override
    public void onClick(int position) {

        if (!isOnline()) {
            Toast.makeText(getContext(), "قبول العروض يتطلب وجود انترنت", Toast.LENGTH_LONG).show();
        }

        RequestedOffer requestedOffer = offerRequests.get(position);

        offerUnAnsweredRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    if (dataSnapshot1.getKey().equals(requestedOffer.getCurrentNodeKey())) {

                        RequestedOffer innerOffer = dataSnapshot1.getValue(RequestedOffer.class);

                        if (innerOffer.getEmployee() == null) {
                            offerUnAnsweredRef.child(requestedOffer.getCurrentNodeKey()).child(Constants.CAT_EMPLOYEE).
                                    setValue(employee.getUid());
                            Toast.makeText(getContext(), "welcome ", Toast.LENGTH_LONG).show();
                            offerRequests.remove(requestedOffer);
                            setUpRecyclerView();

                        } else {
                            Toast.makeText(getContext(), "Not urs ", Toast.LENGTH_LONG).show();
                            offerRequests.remove(requestedOffer);
                            setUpRecyclerView();
                        }
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public boolean isOnline() {

        ConnectivityManager connectivityManager =
                (ConnectivityManager) Objects.requireNonNull(getActivity()).getApplicationContext().
                        getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
