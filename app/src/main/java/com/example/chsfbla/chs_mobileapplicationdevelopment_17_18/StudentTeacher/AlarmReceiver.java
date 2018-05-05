
package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.StudentTeacher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Vibrator;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
/*
    This is the AlarmReceiver receiver. It is the main component of our notifications functionality. When the PendingIntent is
    started, this AlarmReceiver receives the intent and:
    1) checks if the book that had been checked out still is not returned
    2) if the book has not been received, then send a notification (don't do so otherwise)
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    String barcode = "";
    String title = "";
    String isbn = "";
    String user = "";
    String imageLink = "";
    boolean bookExists = false;
    boolean bookOverdue = false;
    public Context c;
    long time;
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.v("AlarmReceiver", "ButHolds");

        c = context;
        //Check if the intent that was received has an extra tag "Barcode" (passed in through .putExtra() in a different activity
        if(intent.hasExtra("Barcode")){
            //Get the barcode, title, user, and imageLink (since we know that the intent has the proper tags) - then, initialize attributes
            barcode = intent.getStringExtra("Barcode");
            title = intent.getStringExtra("Title");
            user = intent.getStringExtra("User");
            imageLink = intent.getStringExtra("link");

            //The following code determines whether to send the notification or not

            //bookExists = true;
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(user);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DataSnapshot temp = dataSnapshot;
                    //If the user has any books checked out, set the pointer of this snapshot to that root in the database
                    if(dataSnapshot.hasChild("BooksCheckedOut")) dataSnapshot = dataSnapshot.child("BooksCheckedOut");
                    //For each child in the BooksCheckedOut root, check if the key has the same tag as the barcode (as per the format of our database)
                    Iterator children = dataSnapshot.getChildren().iterator();
                    while(children.hasNext()){
                        DataSnapshot child = (DataSnapshot) children.next();
                        //The user has NOT returned the book; thus, set the boolean bookExists to true
                        if(child.getKey().toString().equals(barcode)) {
                            bookExists = true;
                            time = (long) (child.getValue());

                            int numWeeks;

                            if (temp.child("Status").getValue().toString().equals("Student")) numWeeks = 2;
                            else numWeeks = 3;

                            long oneDay = 24*60*60*1000;

                            if(System.currentTimeMillis()>time+numWeeks*7*oneDay + oneDay - 1000*60){
                                bookOverdue = true;
                                time = System.currentTimeMillis()-(time + numWeeks*7*24*60*60*1000 + oneDay - 1000*60);
                            }

                            break;
                        }

                    }
                    //If the book hasn't been returned yet, the following code will send the notification
                    if(bookExists) {

                        /*
                         * The below code uses the Glide Framework to load the book image from the imageLink URL
                         * and then uses the image for the notification icon (non-mandatory, but useful, functionality)
                         */
                        if(bookOverdue){
                            Glide
                                    .with(c)
                                    .load(imageLink)
                                    .asBitmap()
                                    .into(new SimpleTarget<Bitmap>(300, 300) {
                                        @Override
                                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                            //the activity that should open (when the notificaiton is clicked) is the home page
                                            Intent notificationIntent = new Intent(c, StudentTeacherHomeActivity.class);

                                            //Ceate a TaskStackBuilder to simulate navigation history (if the user presses the back button, they can actually go back)
                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(c);
                                            stackBuilder.addParentStack(StudentTeacherHomeActivity.class);
                                            stackBuilder.addNextIntent(notificationIntent);
                                            //Get the pending intent and store it
                                            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                                    /*Create a Notification, using a Notification.Builder object
                                        1) pass in the resource Bitmap for the icon of the notification
                                        2) the intent that should be opened is the one stored in the PendingIntent, so pass that in
                                        3) set the priority to high so it will pop up on the screen
                                     */

                                            String text ="The book \"" + title + "\" was due ";
                                            text += (int) (time/(1000*60*60*24));
                                            text += " days ago";
                                            Notification.Builder builder = new Notification.Builder(c);
                                            Notification notification = builder.setContentTitle("Bibliofly")
                                                    .setContentText(text)
                                                    .setTicker("New Message Alert!")
                                                    .setSmallIcon(R.mipmap.app_logo)
                                                    .setLargeIcon(resource)
                                                    .setDefaults(Notification.DEFAULT_ALL)
                                                    .setPriority(Notification.PRIORITY_HIGH)
                                                    .setContentIntent(pendingIntent)
                                                    .setStyle(new Notification.BigTextStyle()
                                                            .bigText(text))
                                                    .build();
                                            //Let the phone's notification manager know that we have a new message
                                            NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                                            notificationManager.notify(0, notification);
                                            //vibrate the phone to alert the user
                                            vibrate();
                                        }
                                    });
                        }
                        else {
                            Glide
                                    .with(c)
                                    .load(imageLink)
                                    .asBitmap()
                                    .into(new SimpleTarget<Bitmap>(300, 300) {
                                        @Override
                                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                            //the activity that should open (when the notificaiton is clicked) is the home page
                                            Intent notificationIntent = new Intent(c, StudentTeacherHomeActivity.class);

                                            //Ceate a TaskStackBuilder to simulate navigation history (if the user presses the back button, they can actually go back)
                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(c);
                                            stackBuilder.addParentStack(StudentTeacherHomeActivity.class);
                                            stackBuilder.addNextIntent(notificationIntent);
                                            //Get the pending intent and store it
                                            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                                    /*Create a Notification, using a Notification.Builder object
                                        1) pass in the resource Bitmap for the icon of the notification
                                        2) the intent that should be opened is the one stored in the PendingIntent, so pass that in
                                        3) set the priority to high so it will pop up on the screen
                                     */
                                            Notification.Builder builder = new Notification.Builder(c);
                                            Notification notification = builder.setContentTitle("Bibliofly")
                                                    .setContentText("A book is due: " + title)
                                                    .setTicker("New Message Alert!")
                                                    .setSmallIcon(R.mipmap.app_logo)
                                                    .setLargeIcon(resource)
                                                    .setDefaults(Notification.DEFAULT_ALL)
                                                    .setPriority(Notification.PRIORITY_HIGH)
                                                    .setContentIntent(pendingIntent)
                                                    .setStyle(new Notification.BigTextStyle()
                                                            .bigText("A book is due: " + title))
                                                    .build();

                                            //Let the phone's notification manager know that we have a new message
                                            NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                                            notificationManager.notify(0, notification);
                                            //vibrate the phone to alert the user
                                            vibrate();
                                        }
                                    });
                        }
                        StudentTeacherHomeActivity.setDateNotificationOverdue(c, title, barcode, imageLink);
                    } else {
                        //do nothing because the user already returned the book.
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
        else if(intent.hasExtra("ISBN")){
            Log.v("ISBN_Exists", "hey!");
            //Get the barcode, title, user, and imageLink (since we know that the intent has the proper tags) - then, initialize attributes
            isbn = intent.getStringExtra("ISBN");
            title = intent.getStringExtra("Title");
            user = intent.getStringExtra("User");
            imageLink = intent.getStringExtra("link");

            //bookExists = true;
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(user);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DataSnapshot temp = dataSnapshot;
                    //If the user has any books checked out, set the pointer of this snapshot to that root in the database
                    if(dataSnapshot.hasChild("BooksOnHold")) dataSnapshot = dataSnapshot.child("BooksOnHold");
                    //For each child in the BooksCheckedOut root, check if the key has the same tag as the barcode (as per the format of our database)
                    Iterator children = dataSnapshot.getChildren().iterator();
                    while(children.hasNext()){
                        final DataSnapshot child = (DataSnapshot) children.next();
                        //The user has NOT returned the book; thus, set the boolean bookExists to true
                        if(child.getKey().toString().equals(isbn)) {
//                            if(Integer.parseInt(child.getValue().toString())==1/*<value*/)
//                                bookExists = true;
                            HoldsUtil.numCopies(isbn, new HoldsCallback() {
                                @Override
                                public void onCallback(int value) {
                                    Log.v("NumCopies", value + "");
                                    if(Integer.parseInt(child.getValue().toString())<=value)
                                        bookExists = true;

                                    if(bookExists) {

                                        /*
                                         * The below code uses the Glide Framework to load the book image from the imageLink URL
                                         * and then uses the image for the notification icon (non-mandatory, but useful, functionality)
                                         */

                                        Glide
                                                .with(c)
                                                .load(imageLink)
                                                .asBitmap()
                                                .into(new SimpleTarget<Bitmap>(300, 300) {
                                                    @Override
                                                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                                        //the activity that should open (when the notificaiton is clicked) is the home page
                                                        Intent notificationIntent = new Intent(c, StudentTeacherHomeActivity.class);

                                                        //Ceate a TaskStackBuilder to simulate navigation history (if the user presses the back button, they can actually go back)
                                                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(c);
                                                        stackBuilder.addParentStack(StudentTeacherHomeActivity.class);
                                                        stackBuilder.addNextIntent(notificationIntent);
                                                        //Get the pending intent and store it
                                                        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                                    /*Create a Notification, using a Notification.Builder object
                                        1) pass in the resource Bitmap for the icon of the notification
                                        2) the intent that should be opened is the one stored in the PendingIntent, so pass that in
                                        3) set the priority to high so it will pop up on the screen
                                     */
                                                        Notification.Builder builder = new Notification.Builder(c);
                                                        Notification notification = builder.setContentTitle("Bibliofly")
                                                                .setContentText("Your hold is available: " + title)
                                                                .setTicker("New Message Alert!")
                                                                .setSmallIcon(R.mipmap.app_logo)
                                                                .setLargeIcon(resource)
                                                                .setDefaults(Notification.DEFAULT_ALL)
                                                                .setPriority(Notification.PRIORITY_HIGH)
                                                                .setContentIntent(pendingIntent)
                                                                .setStyle(new Notification.BigTextStyle()
                                                                        .bigText("A book is available: " + title))
                                                                .build();

                                                        //Let the phone's notification manager know that we have a new message
                                                        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                                                        notificationManager.notify(0, notification);
                                                        //vibrate the phone to alert the user
                                                        vibrate();
                                                    }
                                                });
                                        //StudentTeacherHomeActivity.setDateNotificationOverdue(c, title, isbn, imageLink);
                                    } else {
                                        //do nothing because the user already returned the book.
                                    }

                                }
                            });
                            StudentTeacherBookDetailActivity.setHoldNotification(c,title,isbn,imageLink);

                        }

                    }
                    Log.v("Outside", "Hey!");
                    //If the book hasn't been returned yet, the following code will send the notification

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }


    }

    public void vibrate() {
        Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
    }
}
