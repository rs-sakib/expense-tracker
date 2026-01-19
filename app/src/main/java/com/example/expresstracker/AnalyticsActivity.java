package com.example.expresstracker;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.core.content.ContextCompat;

import com.example.expresstracker.db.AppDatabase;
import com.example.expresstracker.db.Expense;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsActivity extends BaseActivity {

    private AppDatabase db;
    private TextView topCategory, topCategoryAmount, totalTransactions, avgTransaction;
    private PieChart pieChart;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        topCategory = findViewById(R.id.top_category);
        topCategoryAmount = findViewById(R.id.top_category_amount);
        totalTransactions = findViewById(R.id.total_transactions);
        avgTransaction = findViewById(R.id.avg_transaction);
        pieChart = findViewById(R.id.pie_chart);
        lineChart = findViewById(R.id.line_chart);

        loadAnalytics();
    }

    private void loadAnalytics() {
        new Thread(() -> {
            List<Expense> expenses = db.expenseDao().getAllExpenses();

            Map<String, Double> categoryTotals = new HashMap<>();
            Map<String, Double> dailyExpenses = new HashMap<>();
            double totalExpense = 0;
            int expenseCount = 0;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (Expense expense : expenses) {
                if ("expense".equalsIgnoreCase(expense.getType())) {
                    String category = expense.getCategory();
                    categoryTotals.put(category,
                            categoryTotals.getOrDefault(category, 0.0) + expense.getAmount());

                    String date = expense.getDate();
                    dailyExpenses.put(date,
                            dailyExpenses.getOrDefault(date, 0.0) + expense.getAmount());

                    totalExpense += expense.getAmount();
                    expenseCount++;
                }
            }

            String topCat = "";
            double topAmount = 0;

            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                if (entry.getValue() > topAmount) {
                    topAmount = entry.getValue();
                    topCat = entry.getKey();
                }
            }

            String finalTopCat = topCat;
            double finalTopAmount = topAmount;
            int finalExpenseCount = expenseCount;
            double finalTotalExpense = totalExpense;

            runOnUiThread(() -> {
                topCategory.setText(finalTopCat.isEmpty() ? "N/A" : finalTopCat);
                topCategoryAmount.setText(String.format("$%.2f", finalTopAmount));
                totalTransactions.setText(String.valueOf(finalExpenseCount));
                avgTransaction
                        .setText(finalExpenseCount > 0 ? String.format("$%.2f", finalTotalExpense / finalExpenseCount)
                                : "$0.00");

                setupPieChart(categoryTotals, finalTotalExpense);
                setupLineChart(dailyExpenses);
            });
        }).start();
    }

    private void setupPieChart(Map<String, Double> categoryTotals, double total) {
        if (categoryTotals.isEmpty()) {
            pieChart.setNoDataText("No expense data available");
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        int colorPrimary = ContextCompat.getColor(this, R.color.primary);
        int colorSecondary = ContextCompat.getColor(this, R.color.secondary);
        int colorPrimaryLight = ContextCompat.getColor(this, R.color.primary_light);
        int colorSecondaryLight = ContextCompat.getColor(this, R.color.secondary_light);

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            float percentage = (float) ((entry.getValue() / total) * 100);
            entries.add(new PieEntry(percentage, entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Use primary and secondary colors with shades
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(colorPrimary);
        colors.add(colorSecondary);
        colors.add(colorPrimaryLight);
        colors.add(colorSecondaryLight);
        dataSet.setColors(colors);

        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f);

        // Thin stroke
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f%%", value);
            }
        });

        pieChart.setData(data);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(70f); // Thin ring
        pieChart.setTransparentCircleRadius(75f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Expenses\nby Category");
        pieChart.setCenterTextSize(14f);
        pieChart.setDescription(null);
        pieChart.setDrawEntryLabels(false);

        // Legend with bullet points
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setForm(Legend.LegendForm.CIRCLE); // Bullet points
        legend.setFormSize(10f);
        legend.setTextSize(12f);

        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void setupLineChart(Map<String, Double> dailyExpenses) {
        if (dailyExpenses.isEmpty()) {
            lineChart.setNoDataText("No expense data available");
            return;
        }

        ArrayList<Entry> entries = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Get last 7 days
        for (int i = 6; i >= 0; i--) {
            cal.add(Calendar.DAY_OF_YEAR, i == 6 ? -6 : 1);
            String date = sdf.format(cal.getTime());
            float amount = dailyExpenses.containsKey(date) ? dailyExpenses.get(date).floatValue() : 0f;
            entries.add(new Entry(6 - i, amount));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Daily Expenses");
        dataSet.setColor(ContextCompat.getColor(this, R.color.primary));
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.secondary));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(this, R.color.primary_light));
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.setDescription(null);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
