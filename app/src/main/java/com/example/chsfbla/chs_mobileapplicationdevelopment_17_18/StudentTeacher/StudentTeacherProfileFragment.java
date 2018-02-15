package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.StudentTeacher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/*
    This is the StudentTeacherProfile Fragment. It is the rightmost screen in the home page, and has information about the
    current user, include their holds/checked out books and more.
 */
public class StudentTeacherProfileFragment extends Fragment {

    SwipeRefreshLayout swipeRefreshLayout;

    ScrollView scrollView;
    ListView listViewHolds;
    ListView listViewCheckedOut;
    ProgressBar progress;
    ImageView holdsShare;
    ImageView checkedOutShare;
    Context context;

    ArrayList<BookProfile> booksCheckedOut = new ArrayList<>();

    ArrayList<BookProfile> booksOnHold = new ArrayList<>();
    ArrayAdapter<BookProfile> adapterHolds;
    ArrayAdapter<BookProfile> adapterCheckedOut;
    ArrayList<String> holdsList = new ArrayList<>();
    ArrayList<String> checkedOutList = new ArrayList<>();

    public DatabaseReference database;
    public static String name = "Profile";

    int numCopies;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_teacher_profile, container, false);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // do your variables initialisations here except Views!!!

        context = getActivity().getApplicationContext();
    }

    public void loadData() {

        scrollView.setVisibility(View.INVISIBLE);

        //Clear our arraylists that hold the old data
        booksCheckedOut.clear();
        booksOnHold.clear();

        //Instantiate the two array adapters that connect the arraylists to listviews
        adapterHolds = new BookProfileArrayAdapter(context, 0, booksOnHold);
        adapterCheckedOut = new BookProfileArrayAdapter(context, 0, booksCheckedOut);

        database = FirebaseDatabase.getInstance().getReference();
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                booksCheckedOut.clear();
                booksOnHold.clear();
                name = "Profile";//dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Name").getValue().toString();

                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                //Create the datasnapshots which will have the references to the roots for user holds and user checked-out books
                DataSnapshot d = dataSnapshot.child("Users").child(uid).child("BooksCheckedOut");
                DataSnapshot d2 = dataSnapshot.child("Users").child(uid).child("BooksOnHold");
                String status = dataSnapshot.child("Users").child(uid).child("Status").getValue().toString();
                //Iterate through all checked-out books
                for (DataSnapshot book : d.getChildren()) {
                    //Get the barcode and date checked out from the child
                    final String barcode = book.getKey().toString();
                    long date = Long.parseLong(book.getValue().toString());

                    //Iterate through the Barcodes root and find which ISBN this Barcode is mapped to
                    String isbn = null;
                    for (DataSnapshot currBarcode : dataSnapshot.child("Barcodes").getChildren()) {
                        //If this is the right barcode, get the isbn value from the key and set it to the isbn variable
                        if (currBarcode.getKey().equals(barcode)) {
                            isbn = currBarcode.child("ISBN").getValue().toString();
                        }
                    }
                    //Iterate through the Books root and find the information about this specific ISBN
                    for (DataSnapshot currISBN : dataSnapshot.child("Books").getChildren()) {
                        //If this is the right ISBN, get the Title and date checked out, and add this info to the arraylist
                        if (currISBN.getKey().equals(isbn)) {
                            String title = currISBN.child("Title").getValue().toString();
                            String url = currISBN.child("URL").getValue().toString();
                            String author = currISBN.child("Author").getValue().toString();

                            title = (title.length() > 23) ? title.substring(0, 20) + "..." : title;

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(date);

                            calendar.add(Calendar.WEEK_OF_YEAR, (status.equals("Student")) ? 2 : 3);

                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH) + 1;
                            int day = calendar.get(Calendar.DAY_OF_MONTH);

                            String description = "Due on " + month + "/" + day + "/" + year;

                            booksCheckedOut.add(new BookProfile(url, title, author, description));
                            break;
                        }
                    }

                }
                //Iterate through to get information on holds
                for (DataSnapshot book : d2.getChildren()) { // Go through each hold the user has

                    //Find the isbn and the order in line that the user is in
                    final String isbn1 = book.getKey().toString();
                    final String order = book.getValue().toString();

                    DataSnapshot barcodes = dataSnapshot.child("Barcodes");

                    int copies = numCopies(isbn1, barcodes);
                    int checkedOut = numCheckedOut(isbn1, barcodes);

                    numCopies = copies - checkedOut;

                    //Iterate through the Books root to find the ISBN, at which point we will get the title and, along with the order, put this in the arraylist
                    for (DataSnapshot currISBN : dataSnapshot.child("Books").getChildren()) {
                        if (currISBN.getKey().toString().equals(isbn1)) {
                            String title = currISBN.child("Title").getValue().toString();
                            String url = currISBN.child("URL").getValue().toString();
                            String author = currISBN.child("Author").getValue().toString();

                            title = (title.length() > 23) ? title.substring(0, 20) + "..." : title;

                            String description = "You are in position #" + order;
                            if (Integer.valueOf(order) <= numCopies)
                                description += " (Available for pickup)";

                            booksOnHold.add(new BookProfile(url, title, author, description));
                            break;
                        }
                    }
                }
                //Notify the adapters that the arraylists have changed, and that they have to update info
                adapterHolds.notifyDataSetChanged();
                adapterCheckedOut.notifyDataSetChanged();

                //For each listview, set the adapter to their respective one
                listViewCheckedOut.setAdapter(adapterCheckedOut);
                listViewHolds.setAdapter(adapterHolds);
                progress.setVisibility(View.INVISIBLE);

                setListViewHeight(listViewCheckedOut);
                setListViewHeight(listViewHolds);

                scrollView.setVisibility(View.VISIBLE);

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });
    }

    private int numCheckedOut(final String isbn, DataSnapshot dataSnapshot) {
        int holdees = 0;
        //find the number of copies that are checked out
        for (DataSnapshot barcode : dataSnapshot.getChildren()) {
            if (barcode.child("ISBN").getValue().toString().equals(isbn)) {
                if (barcode.hasChild("CheckedOut") &&
                        !barcode.child("CheckedOut").getValue().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        && !barcode.child("CheckedOut").getValue().toString().isEmpty()) {
                    holdees++;
                }
            }
        }

        return holdees;
    }

    //check how many copies of an isbn exisit.
    public static int numCopies(final String isbn, DataSnapshot dataSnapshot) {
        int copies = 0;
        for (DataSnapshot barcode : dataSnapshot.getChildren()) {
            String tempISBN = barcode.child("ISBN").getValue().toString();
            if (isbn.equals(tempISBN)) copies++;
        }

        return copies;

    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize our views

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.profileSwipeRefresh);

        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        listViewHolds = (ListView) view.findViewById(R.id.profileHoldsListView);
        listViewCheckedOut = (ListView) view.findViewById(R.id.profileCheckedOutListView);
        progress = (ProgressBar) view.findViewById(R.id.progressBar2);
        holdsShare = (ImageView) view.findViewById(R.id.holdsShare);
        checkedOutShare = (ImageView) view.findViewById(R.id.checkedOutShare);

        //loading
        progress.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.INVISIBLE);

        loadData();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        // Holds share button pressed
        holdsShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holdsList.clear();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterator userData = dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()).child("BooksOnHold").getChildren().iterator();
                        while (userData.hasNext()) {
                            DataSnapshot book = (DataSnapshot) userData.next();
                            String isbn = book.getKey().toString();
                            String title = dataSnapshot.child("Books").child(isbn).child("Title").getValue().toString();
                            holdsList.add(title);
                        }

                        //make a message for social media.
                        String text = "I can't wait to read";
                        for (int i = 0; i < holdsList.size(); i++) {
                            if (i == holdsList.size() - 1) text += (" and");
                            text += (" \"" + holdsList.get(i) + "\",");
                            if (i == holdsList.size() - 1) {
                                text = text.substring(0, text.length() - 1);
                                text += ("!");
                            }
                            if (holdsList.size() <= 2) text = text.replaceAll(",", "");
                        }
                        text += " Use Bibliofly today!";


                        //let the user choose how they are going to share their information!
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, text);
                        startActivity(Intent.createChooser(intent, "Share"));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }
        });

        // Checkout share button pressed
        checkedOutShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkedOutList.clear();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterator userData = dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()).child("BooksCheckedOut").getChildren().iterator();
                        while (userData.hasNext()) {
                            DataSnapshot book = (DataSnapshot) userData.next();
                            String barcode = book.getKey().toString();
                            String isbn = dataSnapshot.child("Barcodes").child(barcode).child("ISBN").getValue().toString();
                            String title = dataSnapshot.child("Books").child(isbn).child("Title").getValue().toString();
                            checkedOutList.add(title);
                        }

                        //social media post creation.
                        String text = "I'm currently reading ";
                        for (int i = 0; i < checkedOutList.size(); i++) {
                            if (i == checkedOutList.size() - 1) text += (" and");
                            text += (" \"" + checkedOutList.get(i) + "\",");
                            if (i == checkedOutList.size() - 1) {
                                text = text.substring(0, text.length() - 1);
                                text += ("!");
                            }
                            if (checkedOutList.size() <= 2) text.replaceAll(",", "");

                        }
                        //create intent to share information about what the user is reading.
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, text + " Sign up for a Bibliofly account today!");
                        startActivity(Intent.createChooser(intent, "Share"));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    public static void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

    }

    public static String getName() {
        return name;
    }
}

//Wrapper class for book information.
class BookProfile {
    String url;
    String title;
    String author;
    String description;

    public BookProfile(String url, String title, String author, String description) {
        this.url = url;
        this.title = title;
        this.author = author;
        this.description = description;
    }
}

//adapter which manages the data in the profile fragment list view.
class BookProfileArrayAdapter extends ArrayAdapter<BookProfile> {

    private Context context;
    private List<BookProfile> bookList;

    public BookProfileArrayAdapter(Context context, int resource, List<BookProfile> bookList) {
        super(context, resource, bookList);

        this.context = context;
        this.bookList = bookList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //inflates a card and populates/adds the proper information

        BookProfile book = bookList.get(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.book_profile, null);

        //initializes the views on the card.
        ImageView bookProfileImage = (ImageView) view.findViewById(R.id.bookProfileImage);
        TextView bookProfileTitle = (TextView) view.findViewById(R.id.bookProfileTitle);
        TextView bookProfileAuthor = (TextView) view.findViewById(R.id.bookProfileAuthor);
        TextView bookProfileDescription = (TextView) view.findViewById(R.id.bookProfileDescription);

        //loading book image async with Glide loading library.
        Glide.with(context).load(book.url).into(bookProfileImage);

        //adding proper data to views.
        bookProfileTitle.setText(book.title);
        bookProfileAuthor.setText(book.author);
        bookProfileDescription.setText(book.description);

        return view;
    }
}
