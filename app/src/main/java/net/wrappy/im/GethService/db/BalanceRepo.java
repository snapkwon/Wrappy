package net.wrappy.im.GethService.db;

/**
 * Created by sonntht on 25/10/2017.
 */

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

public class BalanceRepo {
    private Balance balance;

    public BalanceRepo(){
        balance = new Balance();
    }


    public static String createTable(){
        return "CREATE TABLE IF NOT EXISTS "  + Balance.TABLE  +
                " (`address` TEXT NOT NULL, " +
                "`tokenAddress` TEXT NOT NULL, " +
                "`chain` TEXT NOT NULL, " +
                "`block` INTEGER NOT NULL, " +
                "`balance` TEXT NOT NULL, " +
                "`tokenBalance` TEXT NOT NULL, " +
                "PRIMARY KEY(`address`, `chain`, `tokenAddress`))";
    }


    public int insertOrUpdate(Balance balance) {
        SQLiteDatabase db = WalletDatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();

        values.put(Balance.KEY_address, balance.getAddress());
        values.put(Balance.KEY_chain, balance.getChain());
        values.put(Balance.KEY_tokenAddress, balance.getTokenAddress());
        values.put(Balance.KEY_block, balance.getBlock());
        values.put(Balance.KEY_balance, balance.getBalance());
        values.put(Balance.KEY_tokenBalance, balance.getTokenBalance());

        // Inserting Row
        int ret = (int)db.insertWithOnConflict(Balance.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (ret == -1) {
            String whereClause = " "+Balance.KEY_address+" = '" + balance.getAddress() + "' and " +
                    ""+Balance.KEY_chain+" = '" + balance.getChain() + "' and  " +
                    ""+Balance.KEY_tokenAddress+" = '" + balance.getTokenAddress() + "'";
            db.update(Balance.TABLE, values, whereClause, null);  // number 1 is the _id here, update to variable for your code
        }
        WalletDatabaseManager.getInstance().closeDatabase();

        return ret;
    }

    public void delete( ) {
        SQLiteDatabase db = WalletDatabaseManager.getInstance().openDatabase();
        db.delete(Balance.TABLE,null,null);
        WalletDatabaseManager.getInstance().closeDatabase();
    }

    public Balance getWalletInfo(Balance balance){
        String whereClause = " "+Balance.KEY_address+" = '" + balance.getAddress() + "' and " +
                ""+Balance.KEY_chain+" = '" + balance.getChain() + "' and  " +
                ""+Balance.KEY_tokenAddress+" = '" + balance.getTokenAddress() + "'";

        SQLiteDatabase db = WalletDatabaseManager.getInstance().openDatabase();
        String selectQuery =  " SELECT * FROM " + Balance.TABLE + " WHERE " + whereClause;

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                balance.setTokenBalance(cursor.getString(cursor.getColumnIndex(Balance.KEY_tokenBalance)));
                balance.setBlock(cursor.getLong(cursor.getColumnIndex(Balance.KEY_block)));
                balance.setBalance(cursor.getString(cursor.getColumnIndex(Balance.KEY_balance)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        WalletDatabaseManager.getInstance().closeDatabase();

        return balance;

    }

}








