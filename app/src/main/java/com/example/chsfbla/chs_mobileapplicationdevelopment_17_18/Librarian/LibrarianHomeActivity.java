package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.Librarian;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.DefaultBarcodeScanner;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class LibrarianHomeActivity extends AppCompatActivity {
    ViewPager viewPager;

    final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private long totalTimeCheckedOut;
    private int numCheckinsForDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_librarian_home);

        //initialize views by id.
        viewPager = (ViewPager) findViewById(R.id.librarianViewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.librarianTabLayout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.librarianToolbar);

        //set title of toolbar
        toolbar.setTitle("Librarian Console");
        setSupportActionBar(toolbar);
        viewPager.setAdapter(new PageAdapter(getSupportFragmentManager()));

        //add 2 tabs to the librarian console
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

        //create bottom fab button for checking in books.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.librarianCheckInFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when the check in button is pressed the app goes to a new screen for scanning the barcode and checking in the book.
                Intent i = new Intent(getApplicationContext(), DefaultBarcodeScanner.class);
                startActivityForResult(i, 0);
            }
        });

        //when the librarian console is first opened up the first screen that should be shown should be the AccountManager tab (first tab).
        viewPager.setCurrentItem(0); //set the viewpager to the first tab
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        //listener to load tabs.
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            final String barcode = data.getStringExtra("Result");

            //firebase code for checking in.


            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //if the book that we are trying to check in is not part of the library (the child does not exist)
                    if (!dataSnapshot.child("Barcodes").hasChild(barcode)) {
                        Toast.makeText(LibrarianHomeActivity.this, "Sorry, this book isn't part of your library! Please add it on the web portal if you'd like!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    //get isbn corresponding to the isbn scanned.
                    String ISBN = dataSnapshot.child("Barcodes").child(barcode).child("ISBN").getValue().toString();

                    //if the book is not checked out then we know the user already returned it.
                    if (!dataSnapshot.child("Barcodes").child(barcode).child("CheckedOut").exists()) {
                        //alert the user that the book is already returned.
                        final Snackbar existsSnackbar = Snackbar.make(viewPager, "Book is already returned", Snackbar.LENGTH_LONG);
                        existsSnackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                existsSnackbar.dismiss();
                            }
                        }).show();
                        return;
                    }

                    //the book needs to be returned at this point.

                    final String UID = dataSnapshot.child("Barcodes").child(barcode).child("CheckedOut").getValue().toString();

                    //get the time the book has been out for (for statistical purposes).
                    final long time = Long.parseLong(dataSnapshot.child("Users").child(UID).child("BooksCheckedOut").child(barcode).getValue().toString());
                    final long timeCheckedOut = System.currentTimeMillis() - time;

                    //get title of the book checked out.
                    String title = dataSnapshot.child("Books").child(ISBN).child("Title").getValue().toString();

                    //alert the user that the book was returned.
                    final Snackbar snackbar = Snackbar.make(viewPager, "Successfully returned \"" + title + "\"", Snackbar.LENGTH_LONG);

                    final DatabaseReference df = reference.child("Users").child(UID).child("BooksCheckedOut").child(barcode);
                    df.removeValue();
                    reference.child("Barcodes").child(barcode).child("CheckedOut").removeValue(); //remove the value since the user is returning the book.

                    StatisticUtils.getRunningBookCheckoutTime(new StatisticsCallback() {
                        @Override
                        public void onCallback(int value) {
                        }

                        @Override
                        public void onCallback(long value) {
                            totalTimeCheckedOut = value;
                            //increment the running total time that a book was checked out for statistical purposes.
                            reference.child("Statistics").child("AverageBookCheckoutTime").child("RunningTime").setValue(value + timeCheckedOut);
                        }

                        @Override
                        public void onCallback(ArrayList<String[]> value) {
                        }

                        @Override
                        public void onCallback(HashMap<String, Integer> value) {
                        }
                    });

                    //in short the below code gets the returns per day, and updates the values to correspond with the book being turned in.
                    Calendar c = Calendar.getInstance();
                    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                    final String currDay = days[c.get(Calendar.DAY_OF_WEEK) - 1];

                    StatisticUtils.getCheckinsPerDay(new StatisticsCallback() {
                        @Override
                        public void onCallback(int value) {
                        }

                        @Override
                        public void onCallback(long value) {
                        }

                        @Override
                        public void onCallback(ArrayList<String[]> value) {
                        }

                        @Override
                        public void onCallback(HashMap<String, Integer> value) {
                            if (value.containsKey(currDay)) {
                                numCheckinsForDay = value.get(currDay);
                                //increment the books returned in on this day.
                                reference.child("Statistics").child("CheckinsPerDay").child(currDay).setValue(value.get(currDay) + 1);
                            }
                            else {
                                numCheckinsForDay = 0;
                                //if no books were checked out on this day, then we need to create the child day and set the value of 1 for today (to correspond with the book we are returning).
                                reference.child("Statistics").child("CheckinsPerDay").child(currDay).setValue(1);
                            }
                        }
                    });

                    //decrements the amount of books checked out currently.
                    StatisticUtils.getNumberOfBooksCheckedOutCurrently(new StatisticsCallback() {
                        @Override
                        public void onCallback(int value) {
                            reference.child("Statistics").child("NumberOfBooksCheckedOutCurrently").setValue(value - 1);
                        }

                        @Override
                        public void onCallback(long value) {
                        }

                        @Override
                        public void onCallback(ArrayList<String[]> value) {
                        }

                        @Override
                        public void onCallback(HashMap<String, Integer> value) {
                        }
                    });

                    //add a undo button in case the librarian returned a book on accident.
                    snackbar.setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //add all of the val's back to the database since user pressed cancel.
                            df.setValue(time);
                            reference.child("Barcodes").child(barcode).child("CheckedOut").setValue(UID); //add value back
                            reference.child("Statistics").child("AverageBookCheckoutTime").child("RunningTime").setValue(totalTimeCheckedOut);
                            reference.child("Statistics").child("CheckinsPerDay").child(currDay).setValue(numCheckinsForDay);
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }


    }

    //Page Adapter the tab view layout.
    private class PageAdapter extends FragmentPagerAdapter {

        PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new LibrarianAccountManagerFragment();
                case 1:
                    return new LibrarianStatisticsFragment();
                default:
                    return new LibrarianAccountManagerFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return LibrarianAccountManagerFragment.getName();
                case 1:
                    return LibrarianStatisticsFragment.getName();
                default:
                    return LibrarianAccountManagerFragment.getName();
            }
        }
    }
}



