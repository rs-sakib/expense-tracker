package com.example.expresstracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expresstracker.db.Expense;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses;

    public ExpenseAdapter(List<Expense> expenses) {
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_list_item, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.title.setText(expense.getTitle());
        holder.amount.setText(String.format("$%.2f", expense.getAmount()));
        holder.category.setText(expense.getCategory());
        holder.date.setText(expense.getDate());

        // Set color based on type
        int color;
        if ("income".equalsIgnoreCase(expense.getType())) {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.income_color);
        } else {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.expense_color);
        }

        holder.typeIndicator.setBackgroundColor(color);
        holder.amount.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView amount;
        TextView category;
        TextView date;
        View typeIndicator;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            amount = itemView.findViewById(R.id.item_amount);
            category = itemView.findViewById(R.id.item_category);
            date = itemView.findViewById(R.id.item_date);
            typeIndicator = itemView.findViewById(R.id.item_type_indicator);
        }
    }
}
