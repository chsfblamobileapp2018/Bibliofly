package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.StudentTeacher;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class HoldsUtil {
    final private static DatabaseReference FINAL_REF = FirebaseDatabase.getInstance().getReference();
    static int holdPlace = -1;
    static int copies = 0;
    static int holdees = 0;

    //utility method for checking how many holds a book has.
    public static void hasHoldOnBook(final String isbn, final HoldsCallback callback){
        holdPlace = -1;
        DatabaseReference reference = FINAL_REF.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("BooksOnHold");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(isbn)){
                    holdPlace = Integer.parseInt(dataSnapshot.child(isbn).getValue().toString());
                }
                callback.onCallback(holdPlace);

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //check how many copies of an isbn exisit.
    public static void numCopies(final String isbn, final HoldsCallback callback){
        copies = 0;
        DatabaseReference reference = FINAL_REF.child("Barcodes");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator eachBarcode = dataSnapshot.getChildren().iterator();
                while(eachBarcode.hasNext()){
                    DataSnapshot tempBarcode = (DataSnapshot)(eachBarcode.next());
                    String tempISBN = tempBarcode.child("ISBN").getValue().toString();
                    if(isbn.equals(tempISBN)) copies++;
                }
                callback.onCallback(copies);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //figures out how many users have a hold on an isbn
    public static void numHoldees(final String isbn, final HoldsCallback callback){
        holdees = 0;
        FINAL_REF.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot d1 = dataSnapshot.child("Books").child(isbn).child("Holds");
                DataSnapshot d2 = dataSnapshot.child("Barcodes");
                holdees = (int) d1.getChildrenCount();
                Iterator barcodeIterator = d2.getChildren().iterator();

                while(barcodeIterator.hasNext()){
                    DataSnapshot barcode = (DataSnapshot) (barcodeIterator.next());
                    if(barcode.child("ISBN").getValue().toString().equals(isbn)){
                        if(barcode.hasChild("CheckedOut") &&
                                !barcode.child("CheckedOut").getValue().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                && !barcode.child("CheckedOut").getValue().toString().isEmpty()){
                            holdees++;
                        }
                    }
                }
                callback.onCallback(holdees);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void numCheckedOut(final String isbn, final HoldsCallback callback){
        holdees = 0;
        FINAL_REF.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot d1 = dataSnapshot.child("Books").child(isbn).child("Holds");
                DataSnapshot d2 = dataSnapshot.child("Barcodes");
                Iterator barcodeIterator = d2.getChildren().iterator();
                //find the number of copies that are checked out
                while(barcodeIterator.hasNext()){
                    DataSnapshot barcode = (DataSnapshot) (barcodeIterator.next());
                    if(barcode.child("ISBN").getValue().toString().equals(isbn)){
                        if(barcode.hasChild("CheckedOut") &&
                                !barcode.child("CheckedOut").getValue().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                && !barcode.child("CheckedOut").getValue().toString().isEmpty()){
                            holdees++;
                        }
                    }
                }
                callback.onCallback(holdees);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
