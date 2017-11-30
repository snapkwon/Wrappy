package net.wrappy.im.GethService.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sonntht on 30/10/2017.
 */

public class TransactionHistoryRepo {
    private TransactionHistory history;

    public TransactionHistoryRepo(){
        history = new TransactionHistory();
    }


    public static String createTable(){
        return "CREATE TABLE IF NOT EXISTS "  + TransactionHistory.TABLE  +
                " (`address` TEXT NOT NULL, " +
                "`tokenAddress` TEXT NOT NULL, " +
                "`chain` TEXT NOT NULL, " +
                "`block` INTEGER NOT NULL, " +
                "`isToken` INTEGER NOT NULL, " +
                "`type` INTEGER NOT NULL, " +
                "`secondAddress` TEXT NOT NULL, " +
                "`balance` TEXT NOT NULL, " +
                "`time` TEXT NOT NULL, " +
                "PRIMARY KEY(`address`, `chain`, `tokenAddress`, `isToken`))";
    }


    public int insert(Balance balance) {
        SQLiteDatabase db = WalletDatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();

        values.put(history.KEY_address, history.getAddress());
        values.put(history.KEY_chain, history.getChain());
        values.put(history.KEY_tokenAddress, history.getTokenAddress());
        values.put(history.KEY_block, history.getBlock());
        values.put(history.KEY_isToken, history.getIsToken());
        values.put(history.KEY_type, history.getType());
        values.put(history.KEY_secondAddress, history.getSecondAddress());
        values.put(history.KEY_balance, balance.getBalance());
        values.put(history.KEY_time, history.getTime());

        // Inserting Row
        int ret = (int)db.insertWithOnConflict(Balance.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        WalletDatabaseManager.getInstance().closeDatabase();

        return ret;
    }

    public void delete( ) {
        SQLiteDatabase db = WalletDatabaseManager.getInstance().openDatabase();
        db.delete(TransactionHistory.TABLE,null,null);
        WalletDatabaseManager.getInstance().closeDatabase();
    }

    public List<TransactionHistory> getHistory(TransactionHistory TransactionHistory){
        List<TransactionHistory> histories = new ArrayList<>();

        String whereClause = " "+TransactionHistory.KEY_address+" = '" + TransactionHistory.getAddress() + "' and " +
                ""+TransactionHistory.KEY_chain+" = '" + TransactionHistory.getChain() + "' and  " +
                ""+TransactionHistory.KEY_tokenAddress+" = '" + TransactionHistory.getTokenAddress() + "' and  " +
                ""+TransactionHistory.KEY_block+" <= " + TransactionHistory.getBlock() + " and  " +
                ""+TransactionHistory.KEY_isToken+" = " + TransactionHistory.getIsToken();

        SQLiteDatabase db = WalletDatabaseManager.getInstance().openDatabase();
        String selectQuery =  " SELECT * FROM " + TransactionHistory.TABLE + " WHERE " + whereClause;

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TransactionHistory history = new TransactionHistory();
                TransactionHistory.setType(cursor.getInt(cursor.getColumnIndex(TransactionHistory.KEY_type)));
                TransactionHistory.setBlock(cursor.getLong(cursor.getColumnIndex(TransactionHistory.KEY_block)));
                TransactionHistory.setSecondAddress(cursor.getString(cursor.getColumnIndex(TransactionHistory.KEY_secondAddress)));
                TransactionHistory.setBalance(cursor.getString(cursor.getColumnIndex(TransactionHistory.KEY_balance)));
                TransactionHistory.setTime(cursor.getString(cursor.getColumnIndex(TransactionHistory.KEY_time)));
                histories.add(history);
            } while (cursor.moveToNext());
        }
        cursor.close();
        WalletDatabaseManager.getInstance().closeDatabase();

        return histories;
    }

    public long getStartBlock(TransactionHistory TransactionHistory){
            long block = 0;
            String whereClause = " "+TransactionHistory.KEY_address+" = '" + TransactionHistory.getAddress() + "' and " +
                    ""+TransactionHistory.KEY_chain+" = '" + TransactionHistory.getChain() + "' and  " +
                    ""+TransactionHistory.KEY_tokenAddress+" = '" + TransactionHistory.getTokenAddress() + "'";

            SQLiteDatabase db = WalletDatabaseManager.getInstance().openDatabase();
            String selectQuery =  " SELECT MAX("+TransactionHistory.KEY_block+") FROM " + TransactionHistory.TABLE + " WHERE " + whereClause + " ORDER BY "+TransactionHistory.KEY_block+" DESC";

            Cursor cursor = db.rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    block = cursor.getLong(cursor.getColumnIndex(TransactionHistory.KEY_block));
                } while (cursor.moveToNext());
            }
            cursor.close();
            WalletDatabaseManager.getInstance().closeDatabase();

            return block + 1;

    }
}
