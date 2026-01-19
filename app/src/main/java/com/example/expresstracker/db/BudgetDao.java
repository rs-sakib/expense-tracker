package com.example.expresstracker.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BudgetDao {

    @Insert
    void insertBudget(Budget budget);

    @Update
    void updateBudget(Budget budget);

    @Query("SELECT * FROM budget ORDER BY month DESC")
    List<Budget> getAllBudgets();

    @Query("SELECT * FROM budget WHERE month = :month LIMIT 1")
    Budget getBudgetForMonth(String month);

    @Query("SELECT * FROM budget WHERE month = :month AND category = :category LIMIT 1")
    Budget getBudgetForMonthAndCategory(String month, String category);

    @Query("DELETE FROM budget WHERE id = :id")
    void deleteBudget(int id);
}
