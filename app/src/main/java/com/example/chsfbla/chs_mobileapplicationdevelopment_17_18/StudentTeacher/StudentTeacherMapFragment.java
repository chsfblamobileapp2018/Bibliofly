package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.StudentTeacher;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.R;

import java.util.ArrayList;

/*
This is the StudentTeacherMapFragment.
The purpose is to load all of the ImageViews that correspond to each different portion of the library.
 */
public class StudentTeacherMapFragment extends Fragment {

    private AlertDialog.Builder builder;

    private float lastX = 0;
    private float lastY = 0;
    private int color = 0;

    private ArrayList<ImageView> arr = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_teacher_map, container, false);
    }

    //We use this method instead of the onCreate() method because instantiating Views in a Fragment onCreate() can cause crashes
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Intitialize all ImageViews
        ImageView work_bench_1_image = (ImageView) view.findViewById(R.id.work_bench_1);
        ImageView work_bench_2_image = (ImageView) view.findViewById(R.id.work_bench_2);
        ImageView front_desk = (ImageView) view.findViewById(R.id.front_desk);
        ImageView PC_Computers = (ImageView) view.findViewById(R.id.PC_Computers);
        ImageView book_display = (ImageView) view.findViewById(R.id.book_displays);
        ImageView seating_area = (ImageView) view.findViewById(R.id.seating_area);
        ImageView work_bench_3 = (ImageView) view.findViewById(R.id.work_bench_3);
        ImageView work_bench_4 = (ImageView) view.findViewById(R.id.work_bench_4);
        ImageView mac_lab = (ImageView) view.findViewById(R.id.mac_lab);
        ImageView offices = (ImageView) view.findViewById(R.id.offices);
        ImageView entrance = (ImageView) view.findViewById(R.id.entrance);
        ImageView work_cubicles = (ImageView) view.findViewById(R.id.work_cubicles);
        ImageView printer = (ImageView) view.findViewById(R.id.printer);
        ImageView independent_library = (ImageView) view.findViewById(R.id.independent_library);
        ImageView fiction_section = (ImageView) view.findViewById(R.id.fiction_section);
        ImageView biography_section = (ImageView) view.findViewById(R.id.biography_section);
        ImageView shelves = (ImageView) view.findViewById(R.id.shelves);
        //Add all of these imageViews to an arraylist
        arr.add(work_bench_1_image);
        arr.add(work_bench_2_image);
        arr.add(front_desk);
        arr.add(PC_Computers);
        arr.add(book_display);
        arr.add(seating_area);
        arr.add(work_bench_3);
        arr.add(work_bench_4);
        arr.add(mac_lab);
        arr.add(offices);
        arr.add(entrance);
        arr.add(work_cubicles);
        arr.add(printer);
        arr.add(independent_library);
        arr.add(fiction_section);
        arr.add(biography_section);
        arr.add(shelves);

        //For each ImageView, enable a click/touch listener
        for (ImageView i : arr) {
            i.setDrawingCacheEnabled(true);
            i.setOnClickListener(customListener);
            i.setOnTouchListener(customListener2);
        }
    }

    private final View.OnClickListener customListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Using the coordinates of the tap event, show an alert box with the proper info IF there is a landmark there
            for (int i = 0; i < arr.size(); i++) {
                if (isTransparent((int) lastX, (int) lastY, arr.get(i))) {
                    //Transparency means that there is no 'landmark' at the tap; take a look at the png images to understand how transparency = landmark
                } else {
                    //Find the id of the imageView that is not transparent at this mark - the id contains information that can be parsed to be the description in the alert
                    String id = getResources().getResourceName(arr.get(i).getId());
                    int start = id.lastIndexOf(":");
                    id = id.substring(start + 4);
                    int label = getResources().getIdentifier(id, "string", getActivity().getPackageName());
                    showAlert(getResources().getString(label));
                    break;
                }
            }
        }

    };

    private final View.OnTouchListener customListener2 = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //Get the tap event coordinates
            lastX = event.getX();
            lastY = event.getY();

            //Get the id and information that will be passed into the alert
            String id = getResources().getResourceName(v.getId());
            int lastDash = id.lastIndexOf("/");
            String name = id.substring(lastDash + 1);
            name = name.replaceAll("_", " ");

            return false;
        }

    };

    private boolean isTransparent(int x, int y, View v) {
        //Determine if the pixel at the specific coordinates (for a specific view) is transparent
        boolean returnValue = true;
        try {
            Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
            returnValue = bmp.getPixel((int) lastX, (int) lastY) == Color.TRANSPARENT;
        } catch (Exception e) {

        }

        return returnValue;
    }

    public void showAlert(String message) {
        //Create an Alert Dialog using the AlertDialog.Builder object
        if (builder != null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        //Set the proper information, and show the dialog.
        builder.setTitle("A place in the library:")
                .setMessage(message)
                .setPositiveButton("Back to map", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        builder = null;
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_map)
                .setCancelable(false)
                .show();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public static String getName() {
        return "Map";
    }
}
