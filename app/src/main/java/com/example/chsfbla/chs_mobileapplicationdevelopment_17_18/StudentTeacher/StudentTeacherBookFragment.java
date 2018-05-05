package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.StudentTeacher;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.Book;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.R;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.RecyclerViewClickListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class StudentTeacherBookFragment extends Fragment {
    public ArrayList<Book> list = new ArrayList<>();
    RecyclerView recyclerView;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    DatabaseReference database;
    Bitmap image = null;

    RecyclerViewClickListener listener;

    static FragmentActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Listen for clicks in the main recyclerview
        listener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                //Create an Intent to the BookDetail Activity, and pass in the info about the specific Book that was clicked
                Intent i = new Intent(getContext(), StudentTeacherBookDetailActivity.class);
                final Book book = list.get(position);
                i.putExtra("Book", book);

                //Get the image from the book's image url
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            URL url = new URL(book.url);
                            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                //Start the process above with this line of code
                thread.start();

                try {
                    //wait for the thread to die
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Convert the image from before into a byte array so that it can be passed into the new intent
                ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, bStream);
                byte[] byteArray = bStream.toByteArray();

                i.putExtra("Image", byteArray);
                startActivity(i);
            }
        };

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_teacher_book, container, false);


    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize our database reference
        database = FirebaseDatabase.getInstance().getReference();
        activity = getActivity();

    }

    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        // initialise our views and set various attributes/layouts/listeners
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        update();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();
            }
        });

    }

    private void update() {
        database.child("Books").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                recyclerView.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                //Iterate through all children in the Books root to get information for each ISBN
                Iterator i = dataSnapshot.getChildren().iterator();
                while(i.hasNext()){
                    //Get the ISBN from the key of the child, and then proceed to find the title, author, description, image url, and rating
                    String isbnStr = ((DataSnapshot) i.next()).getKey();
                    String title = ((dataSnapshot).child(isbnStr).child("Title").getValue().toString());
                    String author = ((dataSnapshot).child(isbnStr).child("Author").getValue().toString());
                    String description = ((dataSnapshot).child(isbnStr).child("Description").getValue().toString());
                    String url = ((dataSnapshot).child(isbnStr).child("URL").getValue().toString());
                    float rating = Float.parseFloat(dataSnapshot.child(isbnStr).child("Rating").getValue().toString());
                    //add the book to the list, which will put it on the screen in the recycler view
                    list.add(new Book(isbnStr, title, author, description, rating, url));
                }

                showCards();
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        swipeRefreshLayout.setRefreshing(false);
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void showCards() {
        BookAdapter bookAdapter = new BookAdapter(list, listener);
        recyclerView.setAdapter(bookAdapter);
    }

    public static String getName() { return "Explore"; }

}

class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private ArrayList<Book> books;
    private RecyclerViewClickListener mListener;
    //Default constructor
    BookAdapter(ArrayList<Book> books, RecyclerViewClickListener listener) {
        this.books = books;
        mListener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    @Override
    public void onBindViewHolder(BookViewHolder bookViewHolder, int i) {
        //Set each field to its corresponding attribute
        Book book = books.get(i);
        bookViewHolder.title.setText(book.title);
        bookViewHolder.author.setText(book.author);
        bookViewHolder.description.setText(book.description);
        bookViewHolder.ratingBar.setRating((float) book.rating);
        //Load the proper image into the imageView using the Glide framework
        Glide.with(StudentTeacherBookFragment.activity.getApplicationContext())
                .load(book.url)
                .into(bookViewHolder.bookImage);
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        //Inflate the view using the proper xml layout
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.book_card, viewGroup, false);

        return new BookViewHolder(itemView, mListener);
    }

    static class BookViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cardView;
        TextView title;
        TextView author;
        TextView description;
        RatingBar ratingBar;
        ImageView bookImage;

        private RecyclerViewClickListener mListener;

        BookViewHolder(View v, RecyclerViewClickListener mListener) {
            super(v);
            //instantiation of views
            cardView = (CardView)       v.findViewById(R.id.cardView);
            title =  (TextView)         v.findViewById(R.id.bookTitle);
            author = (TextView)         v.findViewById(R.id.bookAuthor);
            description = (TextView)    v.findViewById(R.id.bookDescription);
            ratingBar = (RatingBar)     v.findViewById(R.id.ratingBar);
            bookImage = (ImageView)     v.findViewById(R.id.bookImageView);

            this.mListener = mListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v, getAdapterPosition());
        }
    }
}
