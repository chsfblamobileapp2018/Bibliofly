package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.Librarian;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */

/**
 * -Most frequently checked out...
 * -Most number of holds - indicate that we have a shortage of books
 * -number of total books checked out currently
 * -which book has the most backlog (holds / numCopies)
 * -students who check out the most books
 * -graph of check-out vs day for a single school week
 * -graph of check-in vs day for a single school week
 * -average time a book is checked out for.
 * <p>
 * option 1
 * ---
 * --running total of book time out
 * -- total number of books out
 * <p>
 * time/books
 * <p>
 * <p>
 * option 2
 * ---
 * ---isbn - time out
 * ---isbn - time out
 * <p>
 * 1) allow us to give averages for book isbn
 * 2) averages for total books out.
 */


public class LibrarianStatisticsFragment extends Fragment {

    private static int totalBooksCheckedOut;

    Context context;


    private BarChart checkInBarGraph;
    private BarChart checkOutBarGraph;

    private TextView statisticsTotalBooksCheckedOutValue;
    private TextView statisticsCurrentlyCheckedOutValue;
    private TextView statisticsAverageCheckoutTimeValue;

    private ImageView statisticsMostPopularBooksImage;
    private TextView statisticsMostPopularBooksValue;
    private TextView statisticsMostPopularBooksSubtitle;

    private ImageView statisticsMostPopularBooksImage2;
    private TextView statisticsMostPopularBooksValue2;
    private TextView statisticsMostPopularBooksSubtitle2;

    private ImageView statisticsMostPopularBooksImage3;
    private TextView statisticsMostPopularBooksValue3;
    private TextView statisticsMostPopularBooksSubtitle3;

    private ImageView statisticsMostRequestedBooksImage;
    private TextView statisticsMostRequestedBooksValue;
    private TextView statisticsMostRequestedBooksSubtitle;

    private ImageView statisticsMostRequestedBooksImage2;
    private TextView statisticsMostRequestedBooksValue2;
    private TextView statisticsMostRequestedBooksSubtitle2;

    private ImageView statisticsMostRequestedBooksImage3;
    private TextView statisticsMostRequestedBooksValue3;
    private TextView statisticsMostRequestedBooksSubtitle3;

    private TextView statisticsMostActiveReaderValue;
    private TextView statisticsMostActiveReaderSubtitle;

    private TextView statisticsMostActiveReaderValue2;
    private TextView statisticsMostActiveReaderSubtitle2;

    private TextView statisticsMostActiveReaderValue3;
    private TextView statisticsMostActiveReaderSubtitle3;


    final ArrayList<String> labels = new ArrayList<>(Arrays.asList("Mon", "Tue", "Wed", "Thurs", "Fri"));


    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

    public LibrarianStatisticsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_librarian_statistics, container, false);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = this.getContext();

        checkInBarGraph = (BarChart) view.findViewById(R.id.checkIn);
        checkOutBarGraph = (BarChart) view.findViewById(R.id.checkOut);

        statisticsTotalBooksCheckedOutValue = (TextView) view.findViewById(R.id.statisticsTotalBooksCheckedOutValue);
        statisticsCurrentlyCheckedOutValue = (TextView) view.findViewById(R.id.statisticsCurrentlyCheckedOutValue);
        statisticsAverageCheckoutTimeValue = (TextView) view.findViewById(R.id.statisticsAverageCheckoutTimeValue);

        statisticsMostPopularBooksImage = (ImageView) view.findViewById(R.id.statisticsMostPopularBooksImage);
        statisticsMostPopularBooksValue = (TextView) view.findViewById(R.id.statisticsMostPopularBooksValue);
        statisticsMostPopularBooksSubtitle = (TextView) view.findViewById(R.id.statisticsMostPopularBooksSubtitle);

        statisticsMostPopularBooksImage2 = (ImageView) view.findViewById(R.id.statisticsMostPopularBooksImage2);
        statisticsMostPopularBooksValue2 = (TextView) view.findViewById(R.id.statisticsMostPopularBooksValue2);
        statisticsMostPopularBooksSubtitle2 = (TextView) view.findViewById(R.id.statisticsMostPopularBooksSubtitle2);

        statisticsMostPopularBooksImage3 = (ImageView) view.findViewById(R.id.statisticsMostPopularBooksImage3);
        statisticsMostPopularBooksValue3 = (TextView) view.findViewById(R.id.statisticsMostPopularBooksValue3);
        statisticsMostPopularBooksSubtitle3 = (TextView) view.findViewById(R.id.statisticsMostPopularBooksSubtitle3);


        statisticsMostRequestedBooksImage = (ImageView) view.findViewById(R.id.statisticsMostRequestedBooksImage);
        statisticsMostRequestedBooksValue = (TextView) view.findViewById(R.id.statisticsMostRequestedBooksValue);
        statisticsMostRequestedBooksSubtitle = (TextView) view.findViewById(R.id.statisticsMostRequestedBooksSubtitle);

        statisticsMostRequestedBooksImage2 = (ImageView) view.findViewById(R.id.statisticsMostRequestedBooksImage2);
        statisticsMostRequestedBooksValue2 = (TextView) view.findViewById(R.id.statisticsMostRequestedBooksValue2);
        statisticsMostRequestedBooksSubtitle2 = (TextView) view.findViewById(R.id.statisticsMostRequestedBooksSubtitle2);

        statisticsMostRequestedBooksImage3 = (ImageView) view.findViewById(R.id.statisticsMostRequestedBooksImage3);
        statisticsMostRequestedBooksValue3 = (TextView) view.findViewById(R.id.statisticsMostRequestedBooksValue3);
        statisticsMostRequestedBooksSubtitle3 = (TextView) view.findViewById(R.id.statisticsMostRequestedBooksSubtitle3);


        statisticsMostActiveReaderValue = (TextView) view.findViewById(R.id.statisticsMostActiveReaderValue);
        statisticsMostActiveReaderSubtitle = (TextView) view.findViewById(R.id.statisticsMostActiveReaderSubtitle);

        statisticsMostActiveReaderValue2 = (TextView) view.findViewById(R.id.statisticsMostActiveReaderValue2);
        statisticsMostActiveReaderSubtitle2 = (TextView) view.findViewById(R.id.statisticsMostActiveReaderSubtitle2);

        statisticsMostActiveReaderValue3 = (TextView) view.findViewById(R.id.statisticsMostActiveReaderValue3);
        statisticsMostActiveReaderSubtitle3 = (TextView) view.findViewById(R.id.statisticsMostActiveReaderSubtitle3);


        StatisticUtils.getTotalBooksCheckedOut(new StatisticsCallback() {
            @Override
            public void onCallback(int value) {
                statisticsTotalBooksCheckedOutValue.setText("" + value);
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
        StatisticUtils.getNumberOfBooksCheckedOutCurrently(new StatisticsCallback() {
            @Override
            public void onCallback(int value) {
                statisticsCurrentlyCheckedOutValue.setText("" + value);
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
        StatisticUtils.getRunningBookCheckoutTime(new StatisticsCallback() {
            @Override
            public void onCallback(int value) {

            }

            @Override
            public void onCallback(long value) {
                final long runningTotal = value;
                StatisticUtils.getTotalBooksCheckedOut(new StatisticsCallback() {
                    @Override
                    public void onCallback(int value) {

                        int numWeeks;
                        int numDays;

                        if (value == 0) {
                            numWeeks = 0;
                            numDays = 0;
                        } else {

                            long averageTime = runningTotal / value;

                            numWeeks = (int) (averageTime / (1000 * 60 * 60 * 24 * 7));
                            averageTime %= (1000 * 60 * 60 * 24 * 7);

                            numDays = (int) (averageTime / (1000 * 60 * 60 * 24));
                        }
                        statisticsAverageCheckoutTimeValue.setText((numWeeks == 0) ? numDays + " days" : numWeeks + " weeks " + numDays + " days");
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
            public void onCallback(ArrayList<String[]> value) {

            }

            @Override
            public void onCallback(HashMap<String, Integer> value) {

            }
        });

        StatisticUtils.getMostFrequentlyCheckedOut(new StatisticsCallback() {
            @Override
            public void onCallback(int value) {

            }

            @Override
            public void onCallback(long value) {

            }

            @Override
            public void onCallback(final ArrayList<String[]> value) {
                reference.child("Books").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int n = 0;
                        for (String[] data : value) {
                            String isbn = data[0];
                            String url = dataSnapshot.child(isbn).child("URL").getValue().toString();
                            String title = dataSnapshot.child(isbn).child("Title").getValue().toString();
                            switch (n) {
                                case 0:
                                    Glide.with(context).load(url).into(statisticsMostPopularBooksImage);
                                    statisticsMostPopularBooksValue.setText(title);
                                    statisticsMostPopularBooksSubtitle.setText(data[1] + " checkouts");
                                    break;
                                case 1:
                                    Glide.with(context).load(url).into(statisticsMostPopularBooksImage2);
                                    statisticsMostPopularBooksValue2.setText(title);
                                    statisticsMostPopularBooksSubtitle2.setText(data[1] + " checkouts");
                                    break;
                                default:
                                    Glide.with(context).load(url).into(statisticsMostPopularBooksImage3);
                                    statisticsMostPopularBooksValue3.setText(title);
                                    statisticsMostPopularBooksSubtitle3.setText(data[1] + " checkouts");
                                    break;
                            }
                            n++;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCallback(HashMap<String, Integer> value) {

            }
        });

        StatisticUtils.getMostNumberOfHolds(new StatisticsCallback() {
            @Override
            public void onCallback(int value) {

            }

            @Override
            public void onCallback(long value) {

            }

            @Override
            public void onCallback(final ArrayList<String[]> value) {
                reference.child("Books").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int n = 0;
                        for (String[] data : value) {
                            String isbn = data[0];
                            String url = dataSnapshot.child(isbn).child("URL").getValue().toString();
                            String title = dataSnapshot.child(isbn).child("Title").getValue().toString();
                            switch (n) {
                                case 0:
                                    Glide.with(getContext()).load(url).into(statisticsMostRequestedBooksImage);
                                    statisticsMostRequestedBooksValue.setText(title);
                                    statisticsMostRequestedBooksSubtitle.setText(data[1] + " holds");
                                    break;
                                case 1:
                                    Glide.with(getContext()).load(url).into(statisticsMostRequestedBooksImage2);
                                    statisticsMostRequestedBooksValue2.setText(title);
                                    statisticsMostRequestedBooksSubtitle2.setText(data[1] + " holds");
                                    break;
                                default:
                                    Glide.with(getContext()).load(url).into(statisticsMostRequestedBooksImage3);
                                    statisticsMostRequestedBooksValue3.setText(title);
                                    statisticsMostRequestedBooksSubtitle3.setText(data[1] + " holds");
                                    break;
                            }
                            n++;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCallback(HashMap<String, Integer> value) {

            }
        });

        StatisticUtils.getStudentsWithMostBooksCheckedOut(new StatisticsCallback() {
            @Override
            public void onCallback(int value) {

            }

            @Override
            public void onCallback(long value) {

            }

            @Override
            public void onCallback(final ArrayList<String[]> value) {
                reference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int n = 0;
                        for (String[] data : value) {
                            String uid = data[0];
                            String name = dataSnapshot.child(uid).child("Name").getValue().toString();
                            switch (n) {
                                case 0:
                                    statisticsMostActiveReaderValue.setText(name);
                                    statisticsMostActiveReaderSubtitle.setText(data[1] + " checkouts");
                                    break;
                                case 1:
                                    statisticsMostActiveReaderValue2.setText(name);
                                    statisticsMostActiveReaderSubtitle2.setText(data[1] + " checkouts");
                                    break;
                                default:
                                    statisticsMostActiveReaderValue3.setText(name);
                                    statisticsMostActiveReaderSubtitle3.setText(data[1] + " checkouts");
                                    break;
                            }
                            n++;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCallback(HashMap<String, Integer> value) {

            }
        });


        generateGraph("CheckInGraph", checkInBarGraph);
        generateGraph("CheckOutGraph", checkOutBarGraph);
    }

    /**
     * Precondition: type is either "CheckInGraph" or "CheckOutGraph"
     * Postcondition: graph is created.
     *
     * @param type
     */
    private void generateGraph(String type, final BarChart graph) {

        if (type.equals("CheckInGraph")) {
            StatisticUtils.getCheckinsPerDay(new StatisticsCallback() {
                @Override
                public void onCallback(int value) {

                }

                @Override
                public void onCallback(long value) {

                }

                @Override
                public void onCallback(ArrayList<String[]> value) {

                }

                @Override
                public void onCallback(HashMap<String, Integer> value) {
                    List<BarEntry> entries = new ArrayList<>();
                    try {
                        entries.add(new BarEntry(0f, value.get("Monday")));
                    } catch (NullPointerException npe) {
                        entries.add(new BarEntry(0f, 0));
                    }
                    try {
                        entries.add(new BarEntry(1f, value.get("Tuesday")));
                    } catch (NullPointerException npe) {
                        entries.add(new BarEntry(1f, 0));
                    }
                    try {
                        entries.add(new BarEntry(2f, value.get("Wednesday")));
                    } catch (NullPointerException npe) {
                        entries.add(new BarEntry(2f, 0));
                    }
                    try {
                        entries.add(new BarEntry(3f, value.get("Thursday")));
                    } catch (NullPointerException npe) {
                        entries.add(new BarEntry(3f, 0));
                    }
                    try {
                        entries.add(new BarEntry(4f, value.get("Friday")));
                    } catch (NullPointerException npe) {
                        entries.add(new BarEntry(4f, 0));
                    }

                    ChartUtils.generateBarGraph("Books Returned", graph, labels, entries);
                }
            });
        } else {
            StatisticUtils.getCheckoutsPerDay(new StatisticsCallback() {
                @Override
                public void onCallback(int value) {

                }

                @Override
                public void onCallback(long value) {

                }

                @Override
                public void onCallback(ArrayList<String[]> value) {

                }

                @Override
                public void onCallback(HashMap<String, Integer> value) {
                    List<BarEntry> entries = new ArrayList<>();
                    try {
                        entries.add(new BarEntry(0f, value.get("Monday")));
                    } catch (NullPointerException npe) {
                        entries.add(new BarEntry(0f, 0));
                    }
                    try {
                        entries.add(new BarEntry(1f, value.get("Tuesday")));
                    } catch (NullPointerException npe) {
                        entries.add(new BarEntry(1f, 0));
                    }
                    try {
                        entries.add(new BarEntry(2f, value.get("Wednesday")));
                    } catch (NullPointerException npe) {
                        entries.add(new BarEntry(2f, 0));
                    }
                    try {
                        entries.add(new BarEntry(3f, value.get("Thursday")));
                    } catch (NullPointerException npe) {
                        entries.add(new BarEntry(3f, 0));
                    }
                    try {
                        entries.add(new BarEntry(4f, value.get("Friday")));
                    } catch (NullPointerException npe) {
                        entries.add(new BarEntry(4f, 0));
                    }
                    ChartUtils.generateBarGraph("Books Checked Out", graph, labels, entries);
                }
            });
        }


    }


    public static String getName() {
        return "Statistics";
    }
}

