package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.StudentTeacher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.Book;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.DefaultBarcodeScanner;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.Librarian.StatisticUtils;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.Librarian.StatisticsCallback;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.LoginActivity;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;


public class StudentTeacherHomeActivity extends AppCompatActivity implements StudentTeacherMapFragment.OnFragmentInteractionListener, StudentTeacherBookFragment.OnFragmentInteractionListener, StudentTeacherProfileFragment.OnFragmentInteractionListener {

    TabLayout tabLayout;
    ViewPager viewPager;
    Toolbar toolbar;
    CoordinatorLayout coordinatorLayout;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference();
    FloatingSearchView searchView;
    FloatingActionButton fab;
    static ArrayList<Book> suggestions = new ArrayList<>();
    static ArrayList<String> list = new ArrayList<String>();
    int position;
    Bitmap image = null;

    String title;
    String author;
    static String barcode;
    boolean allClearToCheckOut = false;

    private int numberOfBooks;
    private int numCheckoutsPerDay;

    final int VOICE_SEARCH_CODE = 3012;

    private static final int NOTIFICATION_ID = 12345;
    public static String link = "http://s3.amazonaws.com/pix.iemoji.com/images/emoji/apple/ios-11/256/fire.png";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_teacher_home);

        searchView = (FloatingSearchView) findViewById(R.id.searchView);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        coordinatorLayout.bringToFront();

        fab = (FloatingActionButton) findViewById(R.id.checkoutFab);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        //set adapter to your ViewPager
        viewPager.setAdapter(new PageAdapter(getSupportFragmentManager()));

        //intialize the tab layout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        //add 3 new tabs.
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

        //default tab that it loads on is the middle tab
        viewPager.setCurrentItem(1);


        //create page listener to return the tab at a certain position.
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
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

        //set the behavior when the menu is clicked.
        searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.voice:
                        // Voice search
                        startVoiceRecognition();
                        searchView.setSearchFocused(true);
                        break;
                    case R.id.feedback:
                        //Send a bug report via email using email intent.
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/html");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"biblioflyfbla@gmail.com"});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Bibliofly Bug Report");
                        intent.putExtra(Intent.EXTRA_TEXT, "My bug...");

                        startActivity(Intent.createChooser(intent, "Send Email"));
                        break;
                    case R.id.logout:
                        //use our authentication database to sign out.
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.signOut();
                        //move user to sign in page once signed out.
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                        break;
                    default:
                        break;
                }
            }
        });

        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                updateSearches(newQuery);
            }

        });

        searchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                updateSearches(searchView.getQuery());
            }

            @Override
            public void onFocusCleared() {

            }
        });

        //manages search query suggestions.
        searchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon, TextView textView, SearchSuggestion item, int itemPosition) {
                String body = item.getBody();
                String htmlText = body;
                String query = searchView.getQuery();

                if (query.length() == 0) return;

                ArrayList<String> queryTokens = new ArrayList<>();

                StringTokenizer st = new StringTokenizer(query);
                while (st.hasMoreTokens()) {
                    queryTokens.add(st.nextToken().toLowerCase());
                }


                for (String currQuery : queryTokens) {
                    htmlText = htmlText.replaceAll("(?i)" + currQuery, "<font color=#999999>" + currQuery + "</font>");
                }


                textView.setText(Html.fromHtml(htmlText));
            }

        });

        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                searchView.setSearchFocused(false);
                Intent i = new Intent(getApplicationContext(), StudentTeacherBookDetailActivity.class);
                i.putExtra("Book", searchSuggestion);

                final Book book = (Book) searchSuggestion;

                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            URL url = new URL(book.url);
                            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, bStream);
                byte[] byteArray = bStream.toByteArray();

                i.putExtra("Image", byteArray);
                startActivity(i);
            }

            @Override
            public void onSearchAction(String currentQuery) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DefaultBarcodeScanner.class);
                startActivityForResult(i, 0);
            }
        });

    }


    //this method shows a notification that a book is overdue
    public void createNotification(PendingIntent pendingIntent) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.book_notification_image)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!")
                        .setContentIntent(pendingIntent); //Required on Gingerbread and below

    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void updateSearches(String query) {
        searchView.showProgress();

        final String newQuery = query;
        final DatabaseReference searchRef = database.getReference().child("Books");

        searchRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                suggestions.clear();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    String isbn = d.getKey();
                    String title = d.child("Title").getValue(String.class);
                    String author = d.child("Author").getValue(String.class);
                    String description = d.child("Description").getValue(String.class);
                    double rating = d.child("Rating").getValue(Double.class);
                    String url = d.child("URL").getValue(String.class);
                    String lQuery = newQuery.toLowerCase();
                    StringTokenizer st = new StringTokenizer(lQuery);

                    boolean allTokens = false;

                    while (st.hasMoreTokens()) {
                        String currToken = st.nextToken();
                        if (title.toLowerCase().contains(currToken) || author.toLowerCase().contains(currToken) || description.toLowerCase().contains(currToken)) {
                            allTokens = true;
                        } else {
                            allTokens = false;
                            break;
                        }
                    }

                    if (allTokens) {
                        suggestions.add(new Book(isbn, title, author, description, rating, url));
                    }


                }
                searchView.swapSuggestions(suggestions);
                searchView.hideProgress();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //starts android voice recognition Intent.
    public void startVoiceRecognition() {
        Intent intent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
        intent.putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form");
        intent.putExtra("android.speech.extra.PROMPT", "Speak Now");
        this.startActivityForResult(intent, VOICE_SEARCH_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == VOICE_SEARCH_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra("android.speech.extra.RESULTS");
            searchView.setSearchText(matches.get(0));
        } else if (requestCode == 0 && resultCode == RESULT_OK) {
            final String code = data.getStringExtra("Result");
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //book can't be checked out.
                    final Snackbar tooManyBooksSnackbar = Snackbar.make(coordinatorLayout, "You have too many books checked out", Snackbar.LENGTH_INDEFINITE);
                    tooManyBooksSnackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            tooManyBooksSnackbar.dismiss();
                        }
                    });

                    String status = dataSnapshot.child("Users").child(uid).child("Status").getValue().toString();
                    long numberOfBooksCheckedOut = dataSnapshot.child("Users").child(uid).child("BooksCheckedOut").getChildrenCount();

                    //quota of 3 books per student, 5 per teacher
                    if (status.equals("Student") && numberOfBooksCheckedOut >= 3) {
                        tooManyBooksSnackbar.show();
                        return;
                    } else if (status.equals("Teacher") && numberOfBooksCheckedOut >= 5) {
                        tooManyBooksSnackbar.show();
                        return;
                    }

                    DataSnapshot temp = dataSnapshot;
                    dataSnapshot = dataSnapshot.child("Barcodes");
                    if (dataSnapshot.hasChild(code))
                        dataSnapshot = dataSnapshot.child(code).child("ISBN");
                    else {
                        //if book that you want to check out isn't part of the library.
                        Toast.makeText(StudentTeacherHomeActivity.this, "Sorry, this book isn't part of your library! Talk to your librarian if you want to see it added!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    final String isbn = dataSnapshot.getValue().toString();
                    final String title = temp.child("Books").child(isbn).child("Title").getValue().toString();

                    DataSnapshot checkedOutSnapshot = temp.child("Barcodes").child(code).child("CheckedOut");

                    //book on hold, can't be checked out.
                    final Snackbar snackbar = Snackbar.make(coordinatorLayout, "You can't check out \"" + title + "\" because other users have it on hold!", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });

                    if (checkedOutSnapshot.exists() && checkedOutSnapshot.getValue().toString().equals(uid)) {
                        snackbar.show();
                        return;
                    }

                    HoldsUtil.hasHoldOnBook(isbn, new HoldsCallback() {
                        @Override
                        public void onCallback(final int holdPlace) {
                            HoldsUtil.numCopies(isbn, new HoldsCallback() {
                                @Override
                                public void onCallback(final int copies) {
                                    HoldsUtil.numHoldees(isbn, new HoldsCallback() {
                                        @Override
                                        public void onCallback(final int holdees) {
                                            if (holdPlace == -1) {
                                                if (copies > holdees) {
                                                    //ALL GOOD user can check out.
                                                    checkOutBook(code, isbn);
                                                } else {
                                                    snackbar.show();
                                                }
                                            } else if (holdPlace <= copies) {
                                                checkOutBook(code, isbn);
                                            }

                                        }


                                    });
                                }

                            });
                        }


                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void removeHold(final String isbn) {

        //access firebase database and remove the value under the Holds child.
        DatabaseReference holds = FirebaseDatabase.getInstance().getReference().child("Books").child(isbn).child("Holds");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        holds.child(uid).removeValue();

        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("BooksOnHold").child(isbn).removeValue();
        position = 1;
        holds.orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    String uid = d.getKey();
                    FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("BooksOnHold").child(isbn).setValue(position++);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    public void checkOutBook(final String code, final String isbn) {
        removeHold(isbn);
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        DatabaseReference book = ref.child("Books").child(isbn);

        book.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                author = dataSnapshot.child("Author").getValue(String.class);
                title = dataSnapshot.child("Title").getValue(String.class);

                ref.child("Barcodes").child(code).child("CheckedOut").setValue(uid);
                ref.child("Users").child(uid).child("BooksCheckedOut").child(code).setValue(System.currentTimeMillis());

                Calendar c = Calendar.getInstance();

                //days of the week to graph on bar chart.
                String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                final String currDay = days[c.get(Calendar.DAY_OF_WEEK) - 1];

                //used for the graph creation.
                StatisticUtils.getTotalBooksCheckedOut(new StatisticsCallback() {
                    @Override
                    public void onCallback(int value) {
                        ref.child("Statistics").child("TotalBooksCheckedOut").setValue(value + 1);
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
                //increment checkouts per day on the current day of the week.
                StatisticUtils.getCheckoutsPerDay(new StatisticsCallback() {
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
                        if (value.containsKey(currDay))
                            ref.child("Statistics").child("CheckoutsPerDay").child(currDay).setValue(value.get(currDay) + 1);
                        else
                            ref.child("Statistics").child("CheckoutsPerDay").child(currDay).setValue(1);
                    }
                });
                //increment number of books checked out currently.
                StatisticUtils.getNumberOfBooksCheckedOutCurrently(new StatisticsCallback() {
                    @Override
                    public void onCallback(int value) {
                        ref.child("Statistics").child("NumberOfBooksCheckedOutCurrently").setValue(value + 1);
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
                //add to most frequently checked out.
                StatisticUtils.getCheckoutFrequencyOfBook(isbn, new StatisticsCallback() {
                    @Override
                    public void onCallback(int value) {
                        ref.child("Statistics").child("MostFrequentlyCheckedOut").child(isbn).setValue(value + 1);
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
                //add to the number of books the student checked out.
                StatisticUtils.getNumberOfCheckoutsForStudent(uid, new StatisticsCallback() {
                    @Override
                    public void onCallback(int value) {
                        ref.child("Statistics").child("StudentWithMostCheckouts").child(uid).setValue(value + 1);
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

                //notify the user that the checkout was possible.
                final Snackbar snackbar = Snackbar.make(coordinatorLayout, "Successfully checked out \"" + title + "\"", Snackbar.LENGTH_INDEFINITE);

                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                }).show();

                getBookDueFromFirebase(code);
                //Book checked out, set a 2 week alarm.



                            /*
                             * Code for Setting the Alarm
                             */
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getBookDueFromFirebase(String code) {
        barcode = code;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String ISBN = dataSnapshot.child("Barcodes").child(barcode).child("ISBN").getValue().toString();
                String title = dataSnapshot.child("Books").child(ISBN).child("Title").getValue().toString();
                //get link for the ISBN
                String link = dataSnapshot.child("Books").child(ISBN).child("URL").getValue().toString();
                setDateNotification(getApplicationContext(), title, barcode, link);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    public static void setDateNotification(final Context c, final String title, final String bc, final String link) {
        final AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        final Intent notificationIntent = new Intent(c, AlarmReceiver.class);
        notificationIntent.putExtra("Barcode", bc);
        notificationIntent.putExtra("Title", title);
        notificationIntent.putExtra("User", FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
        notificationIntent.putExtra("link", link);

        //Check if they are a student/teacher and add appropriate # weeks
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()).child("Status");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PendingIntent broadcast = PendingIntent.getBroadcast(c, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.WEEK_OF_YEAR, 2);
                if (dataSnapshot.getValue().toString().equals("Teacher")) {
                    cal.add(Calendar.WEEK_OF_YEAR, 1);
                }
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public static void setDateNotificationOverdue(Context c, String title, String bc, String link) {
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent(c, AlarmReceiver.class);
        notificationIntent.putExtra("Barcode", bc);
        notificationIntent.putExtra("Title", title);
        notificationIntent.putExtra("User", FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
        notificationIntent.putExtra("link", link);
        PendingIntent broadcast = PendingIntent.getBroadcast(c, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);
    }


    //returns the correct fragment according to the position the user is on the screen.
    private class PageAdapter extends FragmentPagerAdapter {

        PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new StudentTeacherMapFragment();
                case 1:
                    return new StudentTeacherBookFragment();
                case 2:
                    return new StudentTeacherProfileFragment();
                default:
                    return new StudentTeacherBookFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return StudentTeacherMapFragment.getName();
                case 1:
                    return StudentTeacherBookFragment.getName();
                case 2:
                    return StudentTeacherProfileFragment.getName();
                default:
                    return StudentTeacherBookFragment.getName();
            }
        }
    }

    public String getLinkForBook(final String ISBN) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                dataSnapshot = dataSnapshot.child("Books");
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (dataSnapshot.getKey().toString().equals(ISBN)) {
                        d = d.child("URL");
                        link = d.getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return link;
    }


}
