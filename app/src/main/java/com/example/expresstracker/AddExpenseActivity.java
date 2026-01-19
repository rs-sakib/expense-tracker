package com.example.expresstracker;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.expresstracker.db.AppDatabase;
import com.example.expresstracker.db.Expense;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddExpenseActivity extends BaseActivity {

    private EditText titleEditText, amountEditText, categoryEditText;
    private AutoCompleteTextView typeAutoCompleteTextView;
    private Button addExpenseButton, cancelButton;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = AppDatabase.getDatabase(getApplicationContext());

        titleEditText = findViewById(R.id.edit_text_title);
        amountEditText = findViewById(R.id.edit_text_amount);
        categoryEditText = findViewById(R.id.edit_text_category);
        typeAutoCompleteTextView = findViewById(R.id.auto_complete_text_view_type);
        addExpenseButton = findViewById(R.id.button_add_expense);
        cancelButton = findViewById(R.id.button_cancel);

        String[] transactionTypes = getResources().getStringArray(R.array.transaction_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                transactionTypes);
        typeAutoCompleteTextView.setAdapter(adapter);

        addExpenseButton.setOnClickListener(v -> saveExpense());
        cancelButton.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void saveExpense() {
        String title = titleEditText.getText().toString().trim();
        String amountStr = amountEditText.getText().toString().trim();
        String category = categoryEditText.getText().toString().trim();
        String type = typeAutoCompleteTextView.getText().toString().trim();

        if (title.isEmpty() || amountStr.isEmpty() || category.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = sdf.format(new Date());

        Expense newExpense = new Expense();
        newExpense.setTitle(title);
        newExpense.setAmount(amount);
        newExpense.setCategory(category);
        newExpense.setDate(date);
        newExpense.setType(type.toLowerCase());

        executorService.execute(() -> {
            db.expenseDao().insertExpense(newExpense);
            runOnUiThread(() -> {
                Toast.makeText(AddExpenseActivity.this, "Expense saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
