package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18;

import android.os.Parcel;
import android.os.Parcelable;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;


/*
This is the Book class, which implements the SearchSuggestion interface so that it can be used in the searchbar at the
top of the middle screen of the home page. The attributes it contains are: title, author, description, rating, url (thumbnail
url), and isbn, which are the results/aspects that are shown in the BookDetail activity
 */

public class Book implements SearchSuggestion {

    public final String title;
    public final String author;
    public final String description;
    public final double rating;
    public final String url;
    public final String isbn;

    //This constructor has individual parameters for each attribute that the class has
    public Book(String isbn, String title, String author, String description, double rating, String url) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.description = description;
        this.rating = rating;
        this.url = url;
    }
    /*This constructor takes in a parcel (which is formatted properly) and initializes the attributes; it is needed for
    functionality with the SearchSuggestion interface, just like all @Override methods below
    */
    public Book(Parcel parcel) {
        this.isbn = parcel.readString();
        this.title = parcel.readString();
        this.author = parcel.readString();
        this.description = parcel.readString();
        this.rating = parcel.readDouble();
        this.url = parcel.readString();
    }

    @Override
    public String getBody() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(isbn);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(description);
        dest.writeDouble(rating);
        dest.writeString(url);

    }
    //Required field for the interface to be able to create a new Book object from a passed in parcel
    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {

        @Override
        public Book createFromParcel(Parcel parcel) {
            return new Book(parcel);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[0];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) return false;
        Book o = (Book) obj;

        return isbn.equals(o.isbn);
    }

    @Override
    public String toString() {
        return getBody() + " //////////// " + url;
    }
}
