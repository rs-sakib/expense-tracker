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
    private OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
    }

    public ExpenseAdapter(List<Expense> expenses, OnExpenseClickListener listener) {
        this.expenses = expenses;
        this.listener = listener;
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

        if ("income".equalsIgnoreCase(expense.getType())) {
            holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.income_color));
            holder.icon.setImageResource(R.drawable.ic_arrow_up);
            holder.icon.setBackgroundResource(R.drawable.income_background);
            holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.income_color));
        } else {
            holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.expense_color));
            holder.icon.setImageResource(R.drawable.ic_arrow_down);
            holder.icon.setBackgroundResource(R.drawable.expense_background);
            holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.expense_color));
        }

        holder.itemView.setOnClickListener(v -> listener.onExpenseClick(expense));
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
        android.widget.ImageView icon;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            amount = itemView.findViewById(R.id.item_amount);
            category = itemView.findViewById(R.id.item_category);
            date = itemView.findViewById(R.id.item_date);
            icon = itemView.findViewById(R.id.item_icon);
        }
    }
}
