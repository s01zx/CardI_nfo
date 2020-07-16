package com.solz.cardinfo.Data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public interface CardDao {

    @Query("SELECT * FROM cards")
    List<Card> loadAllTasks();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTask(Card cardEntry);

    @Delete
    void deleteTask(Card cardEntry);
}
