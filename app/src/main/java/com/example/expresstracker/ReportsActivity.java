package com.example.expresstracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.expresstracker.db.AppDatabase;
import com.example.expresstracker.db.Expense;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends BaseActivity {

    private AppDatabase db;

    // Monthly Report Views
    private TextView monthlyTitle, monthlyTransactionCount, monthlyIncomeAmount, monthlyExpenseAmount, monthlyNetAmount;
    private View generateMonthlyBtn;

    // Yearly Report Views
    private TextView yearlyTitle, yearlyTransactionCount, yearlyIncomeAmount, yearlyExpenseAmount, yearlyNetAmount;
    private View generateYearlyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reports");
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        // Initialize Monthly Views
        monthlyTitle = findViewById(R.id.monthly_title);
        monthlyTransactionCount = findViewById(R.id.monthly_transaction_count);
        monthlyIncomeAmount = findViewById(R.id.monthly_income_amount);
        monthlyExpenseAmount = findViewById(R.id.monthly_expense_amount);
        monthlyNetAmount = findViewById(R.id.monthly_net_amount);
        generateMonthlyBtn = findViewById(R.id.generate_monthly_btn);

        // Initialize Yearly Views
        yearlyTitle = findViewById(R.id.yearly_title);
        yearlyTransactionCount = findViewById(R.id.yearly_transaction_count);
        yearlyIncomeAmount = findViewById(R.id.yearly_income_amount);
        yearlyExpenseAmount = findViewById(R.id.yearly_expense_amount);
        yearlyNetAmount = findViewById(R.id.yearly_net_amount);
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
            SimpleDateFormat titleFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            String currentMonthKey = sdf.format(cal.getTime());
            String titleText = titleFormat.format(cal.getTime());

            double income = 0, expense = 0;
            int count = 0;

            for (Expense e : expenses) {
                if (e.getDate().startsWith(currentMonthKey)) {
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
                monthlyTitle.setText(titleText);
                monthlyTransactionCount.setText(finalCount + " Transactions");
                monthlyIncomeAmount.setText(String.format("$%.2f", finalIncome));
                monthlyExpenseAmount.setText(String.format("$%.2f", finalExpense));
                monthlyNetAmount.setText(String.format("$%.2f", finalIncome - finalExpense));

                // Color Net Amount based on value
                if (finalIncome - finalExpense >= 0) {
                    monthlyNetAmount.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    monthlyNetAmount.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                }

                Toast.makeText(this, "Monthly report updated", Toast.LENGTH_SHORT).show();
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
                yearlyTitle.setText(currentYear);
                yearlyTransactionCount.setText(finalCount + " Transactions");
                yearlyIncomeAmount.setText(String.format("$%.2f", finalIncome));
                yearlyExpenseAmount.setText(String.format("$%.2f", finalExpense));
                yearlyNetAmount.setText(String.format("$%.2f", finalIncome - finalExpense));

                // Color Net Amount based on value
                if (finalIncome - finalExpense >= 0) {
                    yearlyNetAmount.setTextColor(getResources().getColor(android.R.color.holo_green_dark)); // Use
                                                                                                            // standard
                                                                                                            // colors or
                                                                                                            // app
                                                                                                            // colors
                } else {
                    yearlyNetAmount.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                }

                Toast.makeText(this, "Yearly report updated", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
