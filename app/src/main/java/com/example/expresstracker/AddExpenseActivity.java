package com.example.expresstracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.expresstracker.db.AppDatabase;
import com.example.expresstracker.db.Expense;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddExpenseActivity extends BaseActivity {

    private EditText titleEditText, amountEditText, categoryEditText;
    private MaterialButtonToggleGroup typeToggleGroup;
    private Button addExpenseButton, cancelButton;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private boolean isEditMode = false;
    private int expenseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add Transaction");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        titleEditText = findViewById(R.id.edit_text_title);
        amountEditText = findViewById(R.id.edit_text_amount);
        categoryEditText = findViewById(R.id.edit_text_category);
        typeToggleGroup = findViewById(R.id.toggle_type);
        addExpenseButton = findViewById(R.id.button_add_expense);
        cancelButton = findViewById(R.id.button_cancel);

        if (getIntent().hasExtra("expense_id")) {
            isEditMode = true;
            expenseId = getIntent().getIntExtra("expense_id", -1);
            titleEditText.setText(getIntent().getStringExtra("expense_title"));
            amountEditText.setText(String.valueOf(getIntent().getDoubleExtra("expense_amount", 0)));
            categoryEditText.setText(getIntent().getStringExtra("expense_category"));

            String type = getIntent().getStringExtra("expense_type");
            if ("income".equalsIgnoreCase(type)) {
                typeToggleGroup.check(R.id.btn_income);
            } else {
                typeToggleGroup.check(R.id.btn_expense);
            }

            addExpenseButton.setText("Update");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Transaction");
            }
        }

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

        int checkedId = typeToggleGroup.getCheckedButtonId();
        String type = (checkedId == R.id.btn_income) ? "income" : "expense";

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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = sdf.format(new Date());

        Expense newExpense = new Expense();
        newExpense.setTitle(title);
        newExpense.setAmount(amount);
        newExpense.setCategory(category);
        newExpense.setDate(date);
        newExpense.setType(type);

        executorService.execute(() -> {
            if (isEditMode) {
                newExpense.setId(expenseId);
                db.expenseDao().updateExpense(newExpense);
                runOnUiThread(() -> {
                    Toast.makeText(AddExpenseActivity.this, "Transaction updated", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                db.expenseDao().insertExpense(newExpense);
                runOnUiThread(() -> {
                    Toast.makeText(AddExpenseActivity.this, "Transaction saved", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
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
