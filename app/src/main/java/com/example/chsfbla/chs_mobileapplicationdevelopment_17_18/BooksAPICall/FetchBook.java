/*This class is used, alongside with the NetworkUtils class, to facilitate HTTPS requests
to the Google Books API and parse the information.

First, the object is created( and the isbn for which the API will be called
is passed in through the constructor). Then, the execute() method is called on the FetchBook object;
since this is an AsyncTask type object, it will run the execute() method in the background
and call the onPostExecute() method after it has run. Further comments can be found inside
the clas itself.
 */

package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.BooksAPICall;

import android.os.AsyncTask;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;


public class FetchBook extends AsyncTask<String, Void, String> {
    //The following two variables are set to -1 intially so that our code can tell if the book does not exist
    public String titleText = "-1";
    public String authorText = "-1";
    public TextView t;
    public String isbn;
    public String description;
    public double rating;
    public String url;

    public FetchBook(TextView t1, String isbnString) {
        t = t1;
        isbn = isbnString;
    }


    public String getAuthor() {
        return authorText;
    }

    public String getTitleText() {
        return titleText;
    }

    @Override
    protected String doInBackground(String... params) {
        return NetworkUtils.getBookInfo(isbn);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        try {
            //Create a JSONObject out of the string that is passed in; it essentially puts it into JSON format.
            JSONObject jsonObject = new JSONObject(s);
            //Set a specific array to the values found under the 'items' tag of the JSON information
            JSONArray itemsArray = jsonObject.getJSONArray("items");

            //Parse the first information in the 'items' in JSON format, do the same for the volumeInfo object
            JSONObject book = itemsArray.getJSONObject(0);
            String title = null;
            String authors = null;
            JSONObject volumeInfo = book.getJSONObject("volumeInfo");
            //If there is a title tag, set the String title to that value

            try {
                title = volumeInfo.getString("title");
            } catch(Exception e) {
                noResultsFound();
                return;
            }

            //If there is an authors tag, find the first author and set authors to that value

            try {
                String[] all_authors = volumeInfo.getString("authors").split(",");
                authors = all_authors[0].substring(2, all_authors[0].length()-1);
            } catch(Exception e) {
                authors = "";
            }

            //If there is an description tag, find and set description to that value

            try {
                description = volumeInfo.getString("description");
            } catch(Exception e) {
                description = "";
            }

            //If there is an ratings tag, find and set rating to that value

            try {
                rating = Double.parseDouble(volumeInfo.getString("averageRating"));
            } catch(Exception e) {
                rating = 0;
            }

            /*If there is an image tag, find and set the url to that value; if not, use a blank book image that serves as
            a default.
            */

            try {
                url = volumeInfo.getJSONObject("imageLinks").getString("smallThumbnail");
            } catch(Exception e) {
                url = "https://images-na.ssl-images-amazon.com/images/I/3151L%2BxIwtL._SY445_.jpg";
                e.printStackTrace();
            }

            //Set the value of two text fields to the data received (in case the user wishes to see this information in a view).
            titleText = title;
            authorText = authors;

            // Add values to Firebase under the proper isbn tag
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Books");
            ref.child(isbn).child("Author").setValue(authors);
            ref.child(isbn).child("Title").setValue(title);
            ref.child(isbn).child("Description").setValue(description);
            ref.child(isbn).child("Rating").setValue(rating);
            ref.child(isbn).child("URL").setValue(url);
        } catch (Exception e) {

        }

    }

    private void noResultsFound() {
        //notify the developer that no results were found, for debugging purposes.
        //set the titleText to no results found so the user can know.
        titleText = "NO RESULTS FOUND";
    }
}
