
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
public class HoldReceiver extends WakefulBroadcastReceiver {
    String isbn = "";
    String title = "";
    String user = "";
    String imageLink = "";
    boolean bookExists = false;
    public Context c;
    long time;
    @Override
    public void onReceive(final Context context, Intent intent) {

        Log.v("OnReceive", "Holds");

        c = context;
        //Check if the intent that was received has an extra tag "Barcode" (passed in through .putExtra() in a different activity
        if(intent.hasExtra("ISBN")){
            //Get the barcode, title, user, and imageLink (since we know that the intent has the proper tags) - then, initialize attributes
            isbn = intent.getStringExtra("ISBN");
            title = intent.getStringExtra("Title");
            user = intent.getStringExtra("User");
            imageLink = intent.getStringExtra("link");
        }

        //The following code determines whether to send the notification or not


    }

    public void vibrate() {
        Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
    }
}
