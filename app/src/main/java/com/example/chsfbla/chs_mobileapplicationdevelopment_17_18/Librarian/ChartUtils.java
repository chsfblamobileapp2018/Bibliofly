package com.example.chsfbla.chs_mobileapplicationdevelopment_17_18.Librarian;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sreeharirammohan on 2/10/18.
 */

public class ChartUtils {

    // creates a bar graph with labels,
    public static void generateBarGraph(String label, BarChart barChart, ArrayList<String> labels, List<BarEntry> yData) {
        //Create a new BarDataSet object.
        BarDataSet dataset = new BarDataSet(yData, label);
        //remove description.
        barChart.getDescription().setEnabled(false);
        //add the standard material colors to the graph for stylistic purposes.
        dataset.setColors(ColorTemplate.MATERIAL_COLORS);
        //remove the lege`nd
        barChart.getLegend().setEnabled(false);

        //remove right axis and grid lines for stylistic purposes.
        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setDrawGridLines(false);

        //set the label count to the x - axis and format it properly with value formatter.
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        //create a new bar data object to be used by the barchart.
        BarData data = new BarData(dataset);
        barChart.setData(data);
    }
}
