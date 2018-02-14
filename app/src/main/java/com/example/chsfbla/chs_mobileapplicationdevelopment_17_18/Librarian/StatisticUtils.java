package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.Librarian;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sreeharirammohan on 2/8/18.
 */

public class StatisticUtils {

    private static DatabaseReference reference = FirebaseDatabase.getInstance().getReference();


    //gets the number of checkouts per day and returns it in a hasmap of key-val pairs (keys are days and values are the # checked out on that day).
    public static void getCheckoutsPerDay(final StatisticsCallback callback) {
        reference.child("Statistics").child("CheckoutsPerDay").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                updateDayData(dataSnapshot);

                HashMap<String, Integer> checkoutsPerDayHashMap = new HashMap<>();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    checkoutsPerDayHashMap.put(d.getKey(), d.getValue(Integer.class));
                }
                callback.onCallback(checkoutsPerDayHashMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //gets the number of checkins per day and returns it in a hasmap of key-val pairs (keys are days and values are the # checked out on that day).
    public static void getCheckinsPerDay(final StatisticsCallback callback) {
        reference.child("Statistics").child("CheckinsPerDay").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                updateDayData(dataSnapshot);

                HashMap<String, Integer> checkinsPerDayHashMap = new HashMap<>();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    checkinsPerDayHashMap.put(d.getKey(), d.getValue(Integer.class));
                }
                callback.onCallback(checkinsPerDayHashMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private static void updateDayData(final DataSnapshot dataSnapshot) {
        //if the database has no day data for one of the days, create the child to prevent a crash.
        if (!dataSnapshot.child("Monday").exists()) reference.child("Statistics").child("Monday").setValue(0);
        if (!dataSnapshot.child("Tuesday").exists()) reference.child("Statistics").child("Tuesday").setValue(0);
        if (!dataSnapshot.child("Wednesday").exists()) reference.child("Statistics").child("Wednesday").setValue(0);
        if (!dataSnapshot.child("Thursday").exists()) reference.child("Statistics").child("Thursday").setValue(0);
        if (!dataSnapshot.child("Friday").exists()) reference.child("Statistics").child("Friday").setValue(0);
    }

    //returns the number of copies for a particular ISBN
    public static void getNumberOfCopiesOfBook(final String isbn, final StatisticsCallback callback) {
        reference.child("Barcodes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int numCopies = 0;
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (d.child("ISBN").getValue().toString().endsWith(isbn)) numCopies++;
                }

                callback.onCallback(numCopies);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //returns the top 3 books checked out.
    public static void getMostFrequentlyCheckedOut(final StatisticsCallback callback) {

        reference.child("Statistics").child("MostFrequentlyCheckedOut").orderByValue().limitToLast(3).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String[]> mostFrequentBooksCheckedOut = new ArrayList<String[]>();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    mostFrequentBooksCheckedOut.add(0, new String[]{d.getKey(), d.getValue().toString()});
                }

                callback.onCallback(mostFrequentBooksCheckedOut);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //returns the most frequently checked out books.
    public static void getCheckoutFrequencyOfBook(final String isbn, final StatisticsCallback callback) {
        reference.child("Statistics").child("MostFrequentlyCheckedOut").child(isbn).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    reference.child("Statistics").child("MostFrequentlyCheckedOut").child(isbn).setValue(0);
                    callback.onCallback(0);
                } else {
                    callback.onCallback(dataSnapshot.getValue(Integer.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //returns the total books checked out (running total).
    public static void getTotalBooksCheckedOut(final StatisticsCallback callback) {
        reference.child("Statistics").child("TotalBooksCheckedOut").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    reference.child("Statistics").child("TotalBooksCheckedOut").setValue(0);
                    callback.onCallback(0);
                } else {
                    callback.onCallback(Integer.parseInt(dataSnapshot.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //returns the student with the most books checked out.
    public static void getStudentsWithMostBooksCheckedOut(final StatisticsCallback callback) {
        reference.child("Statistics").child("StudentWithMostCheckouts").orderByValue().limitToLast(3).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String[]> studentsWithMostBooksCheckedOut = new ArrayList<String[]>();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    studentsWithMostBooksCheckedOut.add(0, new String[]{d.getKey(), d.getValue().toString()});

                }

                callback.onCallback(studentsWithMostBooksCheckedOut);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //given a students uid this method returns how many books that student has checked out.
    public static void getNumberOfCheckoutsForStudent(final String uid, final StatisticsCallback callback) {
        reference.child("Statistics").child("StudentWithMostCheckouts").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    reference.child("Statistics").child("StudentWithMostCheckouts").child(uid).setValue(0);
                    callback.onCallback(0);
                } else {
                    callback.onCallback(dataSnapshot.getValue(Integer.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //returns the most number of holds currently.
    public static void getMostNumberOfHolds(final StatisticsCallback callback) {
        reference.child("Statistics").child("MostNumberOfHolds").orderByValue().limitToLast(3).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) reference.child("Statistics").child("MostNumberOfHolds").setValue(0);
                else {
                    ArrayList<String[]> mostNumberOfHolds = new ArrayList<String[]>();
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        mostNumberOfHolds.add(0, new String[]{d.getKey().toString(), d.getValue().toString()});
                    }

                    callback.onCallback(mostNumberOfHolds);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //returns the number of holds that a book has (max).
    public static void getNumberOfHoldsForBook(final String isbn, final StatisticsCallback callback) {
        reference.child("Statistics").child("MostNumberOfHolds").child(isbn).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    reference.child("Statistics").child("MostNumberOfHolds").child(isbn).setValue(0);
                    callback.onCallback(0);
                }
                else {
                    callback.onCallback(dataSnapshot.getValue(Integer.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    //backlog is defined as holds - copies, this method returns the backlog for a book.
    //note return means that the method calls a callback which can be used later
    public static void getBacklog(final StatisticsCallback callback) {
        reference.child("Statistics").child("Backlog").orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String[]> backlog = new ArrayList<String[]>();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (backlog.size() > 2) {
                        break;
                        //max size 3
                    }
                    backlog.add(new String[]{d.getKey().toString(), d.getValue().toString()});
                }
                callback.onCallback(backlog);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //returns the checkout time that a book was taken.
    public static void getRunningBookCheckoutTime(final StatisticsCallback callback) {
        reference.child("Statistics").child("AverageBookCheckoutTime").child("RunningTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    reference.child("Statistics").child("AverageBookCheckoutTime").child("RunningTime").setValue(0);
                    callback.onCallback((long) 0);
                } else {
                    callback.onCallback(Long.parseLong(dataSnapshot.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    //returns total books
    public static void getTotalBooksInLibrary(final StatisticsCallback callback) {
        reference.child("Statistics").child("TotalBooksInLibrary").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    reference.child("Statistics").child("TotalBooksInLibrary").setValue(0);
                    callback.onCallback(0);
                } else {
                    callback.onCallback(Integer.parseInt(dataSnapshot.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //returns the number of books currently out.
    public static void getNumberOfBooksCheckedOutCurrently(final StatisticsCallback callback) {
        reference.child("Statistics").child("NumberOfBooksCheckedOutCurrently").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    reference.child("Statistics").child("NumberOfBooksCheckedOutCurrently").setValue(0);
                    callback.onCallback(0);
                } else {
                    callback.onCallback(Integer.parseInt(dataSnapshot.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
