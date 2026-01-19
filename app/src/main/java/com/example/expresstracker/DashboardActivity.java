package com.example.expresstracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;

import com.example.expresstracker.db.AppDatabase;
import com.example.expresstracker.db.Expense;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends BaseActivity {

    private AppDatabase db;
    private TextView todayExpense, weekExpense, monthExpense, yearExpense;
    private CardView analyticsCard, reportsCard, budgetCard, transactionsCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        todayExpense = findViewById(R.id.today_expense);
        weekExpense = findViewById(R.id.week_expense);
        monthExpense = findViewById(R.id.month_expense);
        yearExpense = findViewById(R.id.year_expense);

        analyticsCard = findViewById(R.id.analytics_card);
        reportsCard = findViewById(R.id.reports_card);
        budgetCard = findViewById(R.id.budget_card);
        transactionsCard = findViewById(R.id.transactions_card);

        analyticsCard.setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));
        reportsCard.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        budgetCard.setOnClickListener(v -> startActivity(new Intent(this, BudgetActivity.class)));
        transactionsCard.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

        loadDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        new Thread(() -> {
            List<Expense> allExpenses = db.expenseDao().getAllExpenses();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar cal = Calendar.getInstance();

            String today = sdf.format(cal.getTime());

            cal.add(Calendar.DAY_OF_YEAR, -7);
            String weekAgo = sdf.format(cal.getTime());

            cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String monthStart = sdf.format(cal.getTime());

            cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_YEAR, 1);
            String yearStart = sdf.format(cal.getTime());

            double todayTotal = 0, weekTotal = 0, monthTotal = 0, yearTotal = 0;

            for (Expense expense : allExpenses) {
                if ("expense".equalsIgnoreCase(expense.getType())) {
                    if (expense.getDate().equals(today)) {
                        todayTotal += expense.getAmount();
                    }
                    if (expense.getDate().compareTo(weekAgo) >= 0) {
                        weekTotal += expense.getAmount();
                    }
                    if (expense.getDate().compareTo(monthStart) >= 0) {
                        monthTotal += expense.getAmount();
                    }
                    if (expense.getDate().compareTo(yearStart) >= 0) {
                        yearTotal += expense.getAmount();
                    }
                }
            }

            double finalTodayTotal = todayTotal;
            double finalWeekTotal = weekTotal;
            double finalMonthTotal = monthTotal;
            double finalYearTotal = yearTotal;

            runOnUiThread(() -> {
                todayExpense.setText(String.format("$%.2f", finalTodayTotal));
                weekExpense.setText(String.format("$%.2f", finalWeekTotal));
                monthExpense.setText(String.format("$%.2f", finalMonthTotal));
                yearExpense.setText(String.format("$%.2f", finalYearTotal));
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
