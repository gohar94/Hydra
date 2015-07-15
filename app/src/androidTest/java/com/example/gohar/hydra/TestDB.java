package com.example.gohar.hydra;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.example.gohar.hydra.data.ResultsDBHelper;

/**
 * Created by Gohar on 16/07/15.
 */
public class TestDB extends AndroidTestCase {
    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(ResultsDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new ResultsDBHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }
}
