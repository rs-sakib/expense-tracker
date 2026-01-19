package com.example.expresstracker.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class Expense {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public double amount;
    public String category;
    public String date;
    public String type; // "income" or "expense"
}
