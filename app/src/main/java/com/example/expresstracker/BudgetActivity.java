package com.example.expresstracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.expresstracker.db.AppDatabase;
import com.example.expresstracker.db.Budget;
import com.example.expresstracker.db.Expense;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BudgetActivity extends BaseActivity {

    private AppDatabase db;
    private EditText budgetAmount;
    private Button setBudgetBtn;
    private TextView currentBudget, spent, remaining, budgetStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Monthly Budget");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        budgetAmount = findViewById(R.id.budget_amount);
        setBudgetBtn = findViewById(R.id.set_budget_btn);
        currentBudget = findViewById(R.id.current_budget);
        spent = findViewById(R.id.spent);
        remaining = findViewById(R.id.remaining);
        budgetStatus = findViewById(R.id.budget_status);

        setBudgetBtn.setOnClickListener(v -> setBudget());
        loadBudget();
    }

    private void setBudget() {
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

            runOnUiThread(() -> {
                currentBudget.setText(String.format("$%.2f", budgetAmt));
                spent.setText(String.format("$%.2f", finalTotalSpent));
                remaining.setText(String.format("$%.2f", finalRemainingAmt));

                String status;
                if (budgetAmt == 0) {
                    status = "No budget set for this month";
                } else if (percentage < 50) {
                    status = "On track - Good job!";
                } else if (percentage < 80) {
                    status = "Watch spending - Getting close";
                } else if (percentage < 100) {
                    status = "Near limit - Be careful!";
                } else {
                    status = "Over budget!";
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
