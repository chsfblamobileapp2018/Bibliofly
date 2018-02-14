package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.Librarian;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sreeharirammohan on 2/7/18.
 */

public interface StatisticsCallback {
    void onCallback(int value);
    abstract void onCallback(long value);
    void onCallback(ArrayList<String[]> value);
    void onCallback(HashMap<String, Integer> value);

}
