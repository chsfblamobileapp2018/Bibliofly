package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.StudentTeacher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.Book;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.Librarian.StatisticUtils;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.Librarian.StatisticsCallback;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klinker.android.sliding.SlidingActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

/*
This is the StudentTeacherBookDetail activity; it is the screen that is opened whenever a user clicks to see more information
on a particular book in the main fragmented screen. It provides information about the title, author, rating, description, image,
 hold status, and more
 */
public class StudentTeacherBookDetailActivity extends SlidingActivity {
    int position = -1;

    ProgressBar detailProgressBar;
    Button detailPlaceHoldButton;
    ImageView detailHoldImageView;
    TextView detailHoldPlacedLabel;
    TextView detailAuthor;
    ImageView detailAuthorImageView;
    TextView detailDescriptionLabel;
    TextView detailDescription;
    TextView detailRatingLabel;
    RatingBar detailRating;

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference holds;
    DatabaseReference userHold;
    boolean holdPlaced;

    private int numCopies;

    //the following init method is comparable to the onCreate() method of our other activites
    @Override
    public void init(Bundle savedInstanceState) {
        //Create a book object, and initialize it to a Parcelable object (since Book is a SearchSuggestion and SearchSuggestion is a Parcelable)
        final Book book = getIntent().getParcelableExtra("Book");
        //Set database references that we will use later; we do this here because we need to set them based on user id and book isbn
        holds = reference.child("Books").child(book.isbn).child("Holds");
        userHold = reference.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("BooksOnHold");

        setTitle(book.title);
        /*Get the data that represents the Image in byte array form (since you can't put a Bitmap extra).
            Then, convert it to a Bitmap
         */
        byte[] byteArray = getIntent().getByteArrayExtra("Image");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
        Bitmap original = image.copy(image.getConfig(), true);
        /*the following lines of code alters the color scheme of the top bar of the app and the buttons in our app
          to match the book cover's color
         */
        image = darkenBitMap(image);
        image = blur(image);
        setImage(image);
        Palette p = Palette.from(original).generate();
        int def = 0xffffff;
        final int primaryColor = p.getDominantColor(def);
        int primaryColorDark = manipulateColor(primaryColor, 0.5f);

        if (primaryColor != def) {
            setPrimaryColors(primaryColor, primaryColorDark);
        }

        setContent(R.layout.activity_student_teacher_book_detail);
        //initialize views that will be used later
        detailProgressBar = (ProgressBar) findViewById(R.id.detailProgressBar);
        detailPlaceHoldButton = (Button) findViewById(R.id.detailPlaceHoldButton);
        detailHoldImageView = (ImageView) findViewById(R.id.detailHoldImageView);
        detailHoldPlacedLabel = (TextView) findViewById(R.id.detailHoldPlacedLabel);
        detailAuthor = (TextView) findViewById(R.id.detailAuthor);
        detailAuthorImageView = (ImageView) findViewById(R.id.detailAuthorImageView);
        detailDescriptionLabel = (TextView) findViewById(R.id.detailDescriptionLabel);
        detailDescription = (TextView) findViewById(R.id.detailDescription);
        detailRatingLabel = (TextView) findViewById(R.id.detailRatingLabel);
        detailRating = (RatingBar) findViewById(R.id.detailRating);
        //save the color of our button so that if the user holds and un-holds, we can keep this color scheme
        final int buttonColor = detailPlaceHoldButton.getCurrentTextColor();

        //adjust the color scheme based on the values we extracted earlier - then, set the text/value for each view
        detailHoldImageView.setColorFilter(primaryColorDark);

        detailAuthor.setText(book.author);
        detailAuthorImageView.setColorFilter(primaryColorDark);

        detailDescriptionLabel.setTextColor(primaryColorDark);
        detailDescription.setText(book.description);

        detailRatingLabel.setText(String.valueOf(book.rating));
        detailRating.setRating((float) book.rating);


        //Show a progress bar until the data is loaded
        show(detailProgressBar);

        //Hide the following items (they should only be shown if a hold is placed)
        hide(detailPlaceHoldButton);
        hide(detailHoldImageView);
        hide(detailHoldPlacedLabel);


        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        holds.orderByValue().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //The following code finds which position the current user is in line for the book
                int i = 1;
                position = -1;
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (d.getKey().equals(uid)) {
                        position = i;
                        break;
                    }
                    i++;
                }
                //Show the hold views, and hide the progress bar
                hide(detailProgressBar);
                show(detailPlaceHoldButton);
                show(detailHoldImageView);
                //If the user has no hold on the book, hide hold views and set a boolean holdPlaced to false (and vice versa)
                if (position == -1) {
                    holdPlaced = false;
                    detailPlaceHoldButton.setText("Place a Hold");
                    detailPlaceHoldButton.setTextColor(buttonColor);
                    detailHoldImageView.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                    hide(detailHoldPlacedLabel);

                } else {
                    holdPlaced = true;
                    detailPlaceHoldButton.setText("Cancel Hold");
                    detailPlaceHoldButton.setTextColor(getResources().getColor(R.color.error));
                    detailHoldImageView.setImageResource(R.drawable.ic_bookmark_black_24dp);
                    show(detailHoldPlacedLabel);
                    detailHoldPlacedLabel.setText("Hold placed. You are " + position + suffix(position) + " in line.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        detailPlaceHoldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user has placed a hold and they click the button, they intend to remove their hold, so we remove their uid from the hold list
                if (holdPlaced) {
                    holds.child(uid).removeValue();
                    userHold.child(book.isbn).removeValue();
                    //set notification
                    holds.orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            position = 1;
                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                String uid = d.getKey();
                                reference.child("Users").child(uid).child("BooksOnHold").child(book.isbn).setValue(position);
                                position++;
                            }

                            //Hold canceled so we need to decrement the value in firebase.
                            StatisticUtils.getNumberOfHoldsForBook(book.isbn, new StatisticsCallback() {
                                @Override
                                public void onCallback(int value) {
                                    reference.child("Statistics").child("MostNumberOfHolds").child(book.isbn).setValue(value - 1);
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
                            StatisticUtils.getNumberOfCopiesOfBook(book.isbn, new StatisticsCallback() {
                                @Override
                                public void onCallback(int value) {
                                    numCopies = value;
                                    StatisticUtils.getNumberOfHoldsForBook(book.isbn, new StatisticsCallback() {
                                        @Override
                                        public void onCallback(int value) {
                                            double databaseValue = (value == 0) ? 0 : ((double) numCopies) / value;
                                            reference.child("Statistics").child("Backlog").child(book.isbn).setValue(databaseValue);
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
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            DataSnapshot temp = dataSnapshot;
                            if (dataSnapshot.child("Users").child(uid).hasChild("BooksCheckedOut")) {
                                dataSnapshot = dataSnapshot.child("Users").child(uid).child("BooksCheckedOut");

                                Iterator barcodes = dataSnapshot.getChildren().iterator();
                                while (barcodes.hasNext()) {
                                    String bc = ((DataSnapshot) (barcodes.next())).getKey().toString();

                                    if (temp.child("Barcodes").child(bc).child("ISBN").getValue().toString().equals(book.isbn)) {
                                        Toast.makeText(StudentTeacherBookDetailActivity.this, "Book already checked out",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                            }

                            //this means that the user wants to PLACE a hold - add their UID as the key, and the current time as the value
                            holds.child(uid).setValue("" + System.currentTimeMillis());
                            setHoldNotification(getApplicationContext(), book.title, book.isbn, book.url);
                            //find their current position in line, just like how do we did before
                            holds.orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    int i = 1;
                                    position = -1;
                                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                                        if (d.getKey().equals(uid)) {
                                            position = i;
                                            break;
                                        }
                                        i++;
                                    }
                                    userHold.child(book.isbn).setValue(position);

                                    //Update Number of Holds for that Book by incrementing
                                    StatisticUtils.getNumberOfHoldsForBook(book.isbn, new StatisticsCallback() {
                                        @Override
                                        public void onCallback(int value) {
                                            reference.child("Statistics").child("MostNumberOfHolds").child(book.isbn.toString()).setValue(value + 1);
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
                                    StatisticUtils.getNumberOfCopiesOfBook(book.isbn, new StatisticsCallback() {
                                        @Override
                                        public void onCallback(int value) {
                                            numCopies = value;
                                            StatisticUtils.getNumberOfHoldsForBook(book.isbn, new StatisticsCallback() {
                                                @Override
                                                public void onCallback(int value) {
                                                    double databaseValue = (value == 0) ? 0 : ((double) numCopies) / value;
                                                    reference.child("Statistics").child("Backlog").child(book.isbn).setValue(databaseValue);
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

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    //a simple function to convert any integer to a string with a suffix after ("st", "nd", etc)
    private String suffix(int position) {

        int lastDigit = position % 10;
        int tensDigit = (position / 10) % 10;

        if (tensDigit == 1) return "th";

        switch (lastDigit) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public void show(View v) {
        v.setVisibility(View.VISIBLE);
    }

    public void hide(View v) {
        v.setVisibility(View.INVISIBLE);
    }

    //this is a function that blurs an image to create a more aesthetic look/portray the cover as a background, not foreground
    public Bitmap blur(Bitmap image) {
        if (image == null) return null;

        Bitmap outputBitmap = Bitmap.createBitmap(image);
        final RenderScript renderScript = RenderScript.create(this);
        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        //Use the Intrinsic Gausian blur filter on the entire image, and return the editted bitmap
        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        intrinsicBlur.setRadius(1);
        intrinsicBlur.setInput(tmpIn);
        intrinsicBlur.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }

    //this is a function that darkens an image to create a more aesthetic look/portray the cover as a background, not foreground
    private Bitmap darkenBitMap(Bitmap bm) {

        Canvas canvas = new Canvas(bm);
        //The Color.RED value and 0xFF7F7F7F value are used to create a dark filter
        Paint p = new Paint(Color.RED);
        ColorFilter filter = new LightingColorFilter(0xFF7F7F7F, 0x00000000);    // darken
        p.setColorFilter(filter);
        canvas.drawBitmap(bm, new Matrix(), p);

        return bm;
    }

    //This method changes the rgb values by a scalar to either darken/lighten the image
    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));

    }

    public static void setHoldNotification(final Context c, final String title, final String isbn, final String link) {
        if(FirebaseAuth.getInstance().getCurrentUser() == null) return;
        final AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        final Intent notificationIntent = new Intent(c, AlarmReceiver.class);
        notificationIntent.putExtra("ISBN", isbn);
        notificationIntent.putExtra("Title", title);

        notificationIntent.putExtra("User", FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
        notificationIntent.putExtra("link", link);
        Log.v("Hold Notif", title);
        //Check if they are a student/teacher and add appropriate # weeks
        PendingIntent broadcast = PendingIntent.getBroadcast(c, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        //cal.add(Calendar.SECOND, 10);
        Log.v("HoldNotifTime", "" + cal.getTimeInMillis());
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);


    }

}
