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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class MainActivity extends BaseActivity {

    private AppDatabase db;
    private ExpenseAdapter adapter;
    private RecyclerView recyclerView;
    private TextView totalIncome, totalExpenses, balance, emptyView;
    private ExtendedFloatingActionButton fab;

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
                adapter = new ExpenseAdapter(expenses);
                recyclerView.setAdapter(adapter);
                calculateSummary(expenses);
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
