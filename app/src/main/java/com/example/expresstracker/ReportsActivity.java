package com.example.expresstracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.expresstracker.db.AppDatabase;
import com.example.expresstracker.db.Expense;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends BaseActivity {

    private AppDatabase db;
    private TextView monthlyReport, yearlyReport;
    private Button generateMonthlyBtn, generateYearlyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Reports");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        monthlyReport = findViewById(R.id.monthly_report);
        yearlyReport = findViewById(R.id.yearly_report);
        generateMonthlyBtn = findViewById(R.id.generate_monthly_btn);
        generateYearlyBtn = findViewById(R.id.generate_yearly_btn);

        generateMonthlyBtn.setOnClickListener(v -> generateMonthlyReport());
        generateYearlyBtn.setOnClickListener(v -> generateYearlyReport());

        generateMonthlyReport();
        generateYearlyReport();
    }

    private void generateMonthlyReport() {
        new Thread(() -> {
            List<Expense> expenses = db.expenseDao().getAllExpenses();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            String currentMonth = sdf.format(Calendar.getInstance().getTime());

            double income = 0, expense = 0;
            int count = 0;

            for (Expense e : expenses) {
                if (e.getDate().startsWith(currentMonth)) {
                    if ("income".equalsIgnoreCase(e.getType())) {
                        income += e.getAmount();
                    } else {
                        expense += e.getAmount();
                    }
                    count++;
                }
            }

            double finalIncome = income;
            double finalExpense = expense;
            int finalCount = count;

            runOnUiThread(() -> {
                String report = String.format(
                        "Month: %s\n\nTotal Income: $%.2f\nTotal Expenses: $%.2f\nNet: $%.2f\nTransactions: %d",
                        currentMonth, finalIncome, finalExpense, finalIncome - finalExpense, finalCount);
                monthlyReport.setText(report);
                Toast.makeText(this, "Monthly report generated", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void generateYearlyReport() {
        new Thread(() -> {
            List<Expense> expenses = db.expenseDao().getAllExpenses();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
            String currentYear = sdf.format(Calendar.getInstance().getTime());

            double income = 0, expense = 0;
            int count = 0;

            for (Expense e : expenses) {
                if (e.getDate().startsWith(currentYear)) {
                    if ("income".equalsIgnoreCase(e.getType())) {
                        income += e.getAmount();
                    } else {
                        expense += e.getAmount();
                    }
                    count++;
                }
            }

            double finalIncome = income;
            double finalExpense = expense;
            int finalCount = count;

            runOnUiThread(() -> {
                String report = String.format(
                        "Year: %s\n\nTotal Income: $%.2f\nTotal Expenses: $%.2f\nNet: $%.2f\nTransactions: %d",
                        currentYear, finalIncome, finalExpense, finalIncome - finalExpense, finalCount);
                yearlyReport.setText(report);
                Toast.makeText(this, "Yearly report generated", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
