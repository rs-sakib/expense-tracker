package com.example.expresstracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import androidx.appcompat.widget.Toolbar;

import com.example.expresstracker.db.AppDatabase;
import com.example.expresstracker.db.Budget;
import com.example.expresstracker.db.Expense;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BudgetActivity extends BaseActivity {

    private AppDatabase db;
    private TextInputEditText budgetAmount;
    private Button setBudgetBtn;
    private TextView currentBudget, spent, remaining, budgetStatus;
    private LinearProgressIndicator budgetProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Budget");
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        budgetAmount = findViewById(R.id.budget_amount);
        setBudgetBtn = findViewById(R.id.set_budget_btn);
        currentBudget = findViewById(R.id.current_budget);
        spent = findViewById(R.id.spent);
        remaining = findViewById(R.id.remaining);
        budgetStatus = findViewById(R.id.budget_status);
        budgetProgress = findViewById(R.id.budget_progress);

        setBudgetBtn.setOnClickListener(v -> setBudget());
        loadBudget();
    }

    private void setBudget() {
        if (budgetAmount.getText() == null)
            return;
        String amountStr = budgetAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter budget amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            String currentMonth = sdf.format(Calendar.getInstance().getTime());

            new Thread(() -> {
                Budget existingBudget = db.budgetDao().getBudgetForMonth(currentMonth);

                if (existingBudget != null) {
                    existingBudget.setAmount(amount);
                    db.budgetDao().updateBudget(existingBudget);
                } else {
                    Budget budget = new Budget();
                    budget.setMonth(currentMonth);
                    budget.setAmount(amount);
                    budget.setCategory("General");
                    db.budgetDao().insertBudget(budget);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Budget set successfully", Toast.LENGTH_SHORT).show();
                    budgetAmount.setText("");
                    loadBudget();
                });
            }).start();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBudget() {
        new Thread(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            String currentMonth = sdf.format(Calendar.getInstance().getTime());

            Budget budget = db.budgetDao().getBudgetForMonth(currentMonth);
            List<Expense> expenses = db.expenseDao().getAllExpenses();

            double totalSpent = 0;
            for (Expense expense : expenses) {
                if (expense.getDate().startsWith(currentMonth) &&
                        "expense".equalsIgnoreCase(expense.getType())) {
                    totalSpent += expense.getAmount();
                }
            }

            double budgetAmt = budget != null ? budget.getAmount() : 0;
            double remainingAmt = budgetAmt - totalSpent;
            double percentage = budgetAmt > 0 ? (totalSpent / budgetAmt) * 100 : 0;

            double finalTotalSpent = totalSpent;
            double finalRemainingAmt = remainingAmt;
            int finalPercentage = (int) percentage;

            runOnUiThread(() -> {
                currentBudget.setText(String.format("$%.2f", budgetAmt));
                spent.setText(String.format("$%.2f", finalTotalSpent));
                remaining.setText(String.format("$%.2f", finalRemainingAmt));

                budgetProgress.setProgress(finalPercentage);
                if (finalPercentage > 100) {
                    budgetProgress.setIndicatorColor(Color.parseColor("#FF5252")); // Red if over
                } else {
                    budgetProgress.setIndicatorColor(Color.parseColor("#00E676")); // Green/Primary normally
                }

                String status;
                if (budgetAmt == 0) {
                    status = "Not Set";
                    budgetStatus.setTextColor(Color.GRAY);
                } else if (percentage < 50) {
                    status = "On Track";
                    budgetStatus.setTextColor(Color.parseColor("#00E676"));
                } else if (percentage < 80) {
                    status = "Spending";
                    budgetStatus.setTextColor(Color.parseColor("#FF9800"));
                } else if (percentage < 100) {
                    status = "Near Limit";
                    budgetStatus.setTextColor(Color.parseColor("#FF9800"));
                } else {
                    status = "Over Budget";
                    budgetStatus.setTextColor(Color.parseColor("#FF5252"));
                }
                budgetStatus.setText(status);
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBudget();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
