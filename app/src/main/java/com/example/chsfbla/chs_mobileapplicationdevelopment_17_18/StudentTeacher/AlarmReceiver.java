
package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.StudentTeacher;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import javax.sql.DataSource;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
/*
    This is the AlarmReceiver receiver. It is the main component of our notifications functionality. When the PendingIntent is
    started, this AlarmReceiver receives the intent and:
    1) checks if the book that had been checked out still is not returned
    2) if the book has not been received, then send a notification (don't do so otherwise)
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    String barcode = "";
    String title = "";
    String user = "";
    String imageLink = "";
    boolean bookExists = false;
    boolean bookOverdue = false;
    public Context c;
    long time;
    @Override
    public void onReceive(final Context context, Intent intent) {

        Log.e("SetDateNotification", "AlarmReceiver onReceive()");

        c = context;
        //Check if the intent that was received has an extra tag "Barcode" (passed in through .putExtra() in a different activity
        if(intent.hasExtra("Barcode")){
            //Get the barcode, title, user, and imageLink (since we know that the intent has the proper tags) - then, initialize attributes
            barcode = intent.getStringExtra("Barcode");
            title = intent.getStringExtra("Title");
            user = intent.getStringExtra("User");
            imageLink = intent.getStringExtra("link");
            Log.e("Barcode, Title, User",barcode+title+user);
            Log.e("Image Link", imageLink);
        }

        //The following code determines whether to send the notification or not

        //bookExists = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(user);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //If the user has any books checked out, set the pointer of this snapshot to that root in the database
                if(dataSnapshot.hasChild("BooksCheckedOut")) dataSnapshot = dataSnapshot.child("BooksCheckedOut");
                Log.e("dataSnapshot", dataSnapshot.toString());
                //For each child in the BooksCheckedOut root, check if the key has the same tag as the barcode (as per the format of our database)
                Iterator children = dataSnapshot.getChildren().iterator();
                while(children.hasNext()){
                    DataSnapshot child = (DataSnapshot) children.next();
                    //The user has NOT returned the book; thus, set the boolean bookExists to true
                    if(child.getKey().toString().equals(barcode)) {
                        bookExists = true;
                        time = (long) (child.getValue());
                        if(System.currentTimeMillis()>time+2*7*24*60*60*1000){
                            bookOverdue = true;
                            time = System.currentTimeMillis()-(time+2*7*24*60*60*1000);
                        }

                        break;
                    }

                    Log.e("CHILD",child.getKey().toString());
                    Log.e("BookExists",String.valueOf(bookExists));
                }
                //If the book hasn't been returned yet, the following code will send the notification
                if(bookExists) {

                    /*
                     * The below code uses the Glide Framework to load the book image from the imageLink URL
                     * and then uses the image for the notification icon (non-mandatory, but useful, functionality)
                     */
                    if(bookOverdue){
                        Log.e("Before Glide", "Here");
                        Glide
                                .with(c)
                                .load(imageLink)
                                .asBitmap()
                                .into(new SimpleTarget<Bitmap>(300, 300) {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                        Log.e("After Glide", "Start of method");
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
                                        Log.e("Inside", "Creating Notif");
                                        Notification.Builder builder = new Notification.Builder(c);
                                        Notification notification = builder.setContentTitle("Bibliofly")
                                                .setContentText(text)
                                                .setTicker("New Message Alert!")
                                                .setSmallIcon(R.mipmap.app_logo)
                                                .setLargeIcon(resource)
                                                .setDefaults(Notification.DEFAULT_ALL)
                                                .setPriority(Notification.PRIORITY_HIGH)
                                                .setContentIntent(pendingIntent).build();
                                        //Let the phone's notification manager know that we have a new message
                                        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                                        notificationManager.notify(0, notification);
                                        //vibrate the phone to alert the user
                                        vibrate();
                                    }
                                });
                    }
                    else {
                        Log.e("Before Glide", "Here");
                        Glide
                                .with(c)
                                .load(imageLink)
                                .asBitmap()
                                .into(new SimpleTarget<Bitmap>(300, 300) {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                        Log.e("After Glide", "Start of method");
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
                                        Log.e("Inside", "Creating Notif");
                                        Notification.Builder builder = new Notification.Builder(c);
                                        Notification notification = builder.setContentTitle("Bibliofly")
                                                .setContentText("A book is due: " + title)
                                                .setTicker("New Message Alert!")
                                                .setSmallIcon(R.mipmap.app_logo)
                                                .setLargeIcon(resource)
                                                .setDefaults(Notification.DEFAULT_ALL)
                                                .setPriority(Notification.PRIORITY_HIGH)
                                                .setContentIntent(pendingIntent).build();
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

    public void vibrate() {
        Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
    }
}
