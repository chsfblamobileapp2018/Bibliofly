/*
    This is the NetworkUtils class; it goes hand in hand with the FetchBook class, and is triggered
    in the doInBackground() method of the FetchBook class. This class builds the HTTPS request and
    handles the connection between the app and the Google Books API. It sends the raw data to the
    FetchBook class, which parses the data.
 */
package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.BooksAPICall;


import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {

    //Base URL for the Books API
    private static final String BOOK_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
    private static final String QUERY_PARAM = "q";
    private static final String MAX_RESULTS = "maxResults";
    private static final String PRINT_TYPE = "printType";

    static String getBookInfo(String queryString) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJSONString = null;

        try {
            //Build up the book URL, limiting the results to 1 book - then, open the connection
            Uri buildURI = Uri.parse(BOOK_BASE_URL).buildUpon().appendQueryParameter(QUERY_PARAM, queryString)
                    .appendQueryParameter(MAX_RESULTS, "1")
                    .appendQueryParameter(PRINT_TYPE, "books")
                    .build();
            URL requestURL = new URL(buildURI.toString());
            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Read the input string using inputStream and BufferedReader
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                //The stream had no information, so return immediately
                return null;
            }
            //Set our data string to the information in the StringBuffer
            bookJSONString = buffer.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            //End the connection, close the reader, and return our data!
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return bookJSONString;
        }

    }

}