package com.example.expresstracker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expresstracker.db.AppDatabase;
import com.example.expresstracker.db.Expense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private ExpenseAdapter adapter;
    private RecyclerView recyclerView;
    private TextView totalIncome, totalExpenses, balance;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDatabase(getApplicationContext());

        recyclerView = findViewById(R.id.expenses_recycler_view);
        totalIncome = findViewById(R.id.total_income);
        totalExpenses = findViewById(R.id.total_expenses);
        balance = findViewById(R.id.balance);
        fab = findViewById(R.id.fab_add_expense);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v -> {
            // TODO: Implement adding a new expense
        });

        loadExpenses();
    }

    private void loadExpenses() {
        new Thread(() -> {
            List<Expense> expenses = db.expenseDao().getAllExpenses();
            runOnUiThread(() -> {
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
            if ("income".equals(e.type)) {
                income += e.amount;
            } else {
                expense += e.amount;
            }
        }

        totalIncome.setText(String.format("Income: $%.2f", income));
        totalExpenses.setText(String.format("Expenses: $%.2f", expense));
        balance.setText(String.format("Balance: $%.2f", income - expense));
    }
}
