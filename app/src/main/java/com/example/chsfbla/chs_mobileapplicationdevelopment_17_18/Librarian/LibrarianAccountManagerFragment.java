package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.Librarian;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class LibrarianAccountManagerFragment extends Fragment {

    //store an arraylist of accounts.
    ArrayList<AccountManagerItem> accounts = new ArrayList<>();

    //create an adapter for the list view.
    ArrayAdapter<AccountManagerItem> adapter;

    //store the context for use later on.
    Context context;

    ListView listView;

    public LibrarianAccountManagerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_librarian_account_manager, container, false);
    }


    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //initialize the context.
        context = this.getContext();

    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //initialize views here.

        //initialize the listView.
        listView = (ListView) view.findViewById(R.id.AccountManagerListView);
        //load the cards into the listView from Firebase.
        loadCards();




    }

    private void loadCards() {

        //initialize the adapter with the context and set the data source as the accounts array list.
        adapter = new LibrarianAccountManagerArrayAdapter(context, 0, accounts);


        //create a database reference so we can access the firebase database.
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        //set a listener so we can loop over values in the database.
        database.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //loop over the dataSnapshot children.
                for(DataSnapshot d : dataSnapshot.getChildren()) {

                    //get the name, and status as linked under the name child.
                    String name = d.child("Name").getValue().toString();
                    String status = d.child("Status").getValue().toString();

                    //if the user is a librarian, we don't need to add their checkout data.
                    if (status.equals("Librarian")) continue;

                    //only if the user has books checked out, get the value otherwise initialize it to 0.
                    int booksCheckedOut = 0;
                    if(d.hasChild("BooksCheckedOut")) { //prevents a crash.
                        booksCheckedOut = (int) d.child("BooksCheckedOut").getChildrenCount();
                    }

                    //only if the user has books on hold, get the value otherwise initialize it to 0.
                    int booksOnHold = 0;
                    if(d.hasChild("BooksOnHold")) { //prevents a crash.
                        booksOnHold = (int) d.child("BooksOnHold").getChildrenCount();
                    }

                    //only if the user has overdue books, get the value otherwise initialize it to 0.
                    int overdueBookCount = 0;
                    for(DataSnapshot books : d.child("BooksCheckedOut").getChildren()) {

                        //compute the (potential) overdue time a book has been out for.
                        long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - books.getValue(Long.class));

                        //if days is more than 1 for a teacher then it is overdue.
                        //if days is more than 1 for a student then it is overdue.
                        if(days > 1) {
                            //increment overdueBookCount only if the book has been due for over 1 day.
                            overdueBookCount++;
                        }
                    }

                    //add the new account information to the database.
                    accounts.add(new AccountManagerItem(name, status, booksCheckedOut, booksOnHold, overdueBookCount));

                }

                //notify the adapter that we have new data so we can update the UI.
                adapter.notifyDataSetChanged();
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public static String getName() {
        return "Account Manager";
    }

}



//Create a data wrapper class to encapsulate the AccountManager Data from Firebase.
class AccountManagerItem {
    private String name;
    private String status;
    private int checkedOut;
    private int numOnHold;
    private int overdue;

    //constructor initializes all of the default values passed in.
    public AccountManagerItem(String name, String status, int checkedOut, int numOnHold, int overdue) {
        this.name = name;
        this.status = status;
        this.checkedOut = checkedOut;
        this.numOnHold = numOnHold;
        this.overdue = overdue;
    }

    //typical getters and setters.
    public String getName() {
        return name;
    }

    public int getOverdue() {
        return overdue;
    }

    public void setOverdue(int overdue) {
        this.overdue = overdue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCheckedOut() {
        return checkedOut;
    }

    public void setCheckedOut(int checkedOut) {
        this.checkedOut = checkedOut;
    }

    public int getNumOnHold() {
        return numOnHold;
    }

    public void setNumOnHold(int numOnHold) {
        this.numOnHold = numOnHold;
    }

}

//create custom array adapter.
class LibrarianAccountManagerArrayAdapter extends ArrayAdapter<AccountManagerItem> {
    private Context context;
    private List<AccountManagerItem> accountList;

    public LibrarianAccountManagerArrayAdapter(Context context, int resource, List<AccountManagerItem> accountList) {
        super(context, resource, accountList);

        this.context = context;
        this.accountList = accountList;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AccountManagerItem account = accountList.get(position);

        //create the layout inflator to create view cards.
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.librarian_profile_card, null);

        //initialize parts of the card.
        TextView name = (TextView) view.findViewById(R.id.librarianProfileName);
        TextView status = (TextView) view.findViewById(R.id.librarianProfileStatus);
        TextView checkedOut = (TextView) view.findViewById(R.id.librarianProfileCheckedOut);
        TextView overdue = (TextView) view.findViewById(R.id.librarianProfileOverdue);
        TextView onHold = (TextView) view.findViewById(R.id.librarianProfileHolds);

        //set the various parts of the card to data from the Account object.
        name.setText(account.getName() + "");
        status.setText(account.getStatus() + "");
        checkedOut.setText(account.getCheckedOut() + "");
        overdue.setText(account.getOverdue() + "");
        onHold.setText(account.getNumOnHold() + "");

        return view;
    }



}