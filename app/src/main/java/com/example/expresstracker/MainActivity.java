package com.example.expresstracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expresstracker.db.AppDatabase;
import com.example.expresstracker.db.Expense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends BaseActivity {

    private AppDatabase db;
    private ExpenseAdapter adapter;
    private RecyclerView recyclerView;
    private TextView totalIncome, totalExpenses, balance, emptyView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = AppDatabase.getDatabase(getApplicationContext());

        recyclerView = findViewById(R.id.recycler_view);
        totalIncome = findViewById(R.id.total_income_textview);
        totalExpenses = findViewById(R.id.total_expense_textview);
        balance = findViewById(R.id.balance_textview);
        emptyView = findViewById(R.id.empty_view);
        fab = findViewById(R.id.add_expense_fab);

        // Quick Actions
        findViewById(R.id.action_analytics)
                .setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));
        findViewById(R.id.action_reports)
                .setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        findViewById(R.id.action_budget).setOnClickListener(v -> startActivity(new Intent(this, BudgetActivity.class)));
        findViewById(R.id.action_more)
                .setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddExpenseActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses();
    }

    private void loadExpenses() {
        new Thread(() -> {
            List<Expense> expenses = db.expenseDao().getAllExpenses();
            runOnUiThread(() -> {
                if (expenses.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
                adapter = new ExpenseAdapter(expenses, this::onExpenseClick);
                recyclerView.setAdapter(adapter);
                calculateSummary(expenses);
            });
        }).start();
    }

    private void onExpenseClick(Expense expense) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Transaction Options")
                .setItems(new String[] { "Edit", "Delete" }, (dialog, which) -> {
                    if (which == 0) {
                        // Edit
                        Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                        intent.putExtra("expense_id", expense.getId());
                        intent.putExtra("expense_title", expense.getTitle());
                        intent.putExtra("expense_amount", expense.getAmount());
                        intent.putExtra("expense_category", expense.getCategory());
                        intent.putExtra("expense_type", expense.getType());
                        startActivity(intent);
                    } else {
                        // Delete
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Delete Transaction")
                                .setMessage("Are you sure you want to delete this transaction?")
                                .setPositiveButton("Yes", (d, w) -> deleteExpense(expense))
                                .setNegativeButton("No", null)
                                .show();
                    }
                })
                .show();
    }

    private void deleteExpense(Expense expense) {
        new Thread(() -> {
            db.expenseDao().deleteExpense(expense);
            runOnUiThread(() -> {
                android.widget.Toast.makeText(this, "Transaction deleted", android.widget.Toast.LENGTH_SHORT).show();
                loadExpenses();
            });
        }).start();
    }

    private void calculateSummary(List<Expense> expenses) {
        double income = 0;
        double expense = 0;

        for (Expense e : expenses) {
            if ("income".equalsIgnoreCase(e.getType())) {
                income += e.getAmount();
            } else {
                expense += e.getAmount();
            }
        }

        totalIncome.setText(String.format("$%.2f", income));
        totalExpenses.setText(String.format("$%.2f", expense));
        balance.setText(String.format("$%.2f", income - expense));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_dashboard) {
            startActivity(new Intent(this, DashboardActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
