package com.example.expresstracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expresstracker.db.AppDatabase;
import com.example.expresstracker.db.Expense;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText titleEditText, amountEditText, categoryEditText;
    private RadioGroup typeRadioGroup;
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
        typeRadioGroup = findViewById(R.id.radio_group_type);
        addExpenseButton = findViewById(R.id.button_add_expense);
        cancelButton = findViewById(R.id.button_cancel);

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

        if (title.isEmpty() || amountStr.isEmpty() || category.isEmpty()) {
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

        int selectedTypeId = typeRadioGroup.getCheckedRadioButtonId();
        if (selectedTypeId == -1) {
            Toast.makeText(this, "Please select a transaction type", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedRadioButton = findViewById(selectedTypeId);
        String type = selectedRadioButton.getText().toString().equalsIgnoreCase("income") ? "income" : "expense";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = sdf.format(new Date());

        Expense newExpense = new Expense();
        newExpense.title = title;
        newExpense.amount = amount;
        newExpense.category = category;
        newExpense.date = date;
        newExpense.type = type;

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
