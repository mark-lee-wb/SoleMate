package solemate.solemate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class databaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "solemate.db";
    public static final String patientLocationTable = "patient_location_table";
    public static final String patinetInfoTable = "patient_information_table";
    public static final String COL1_1 = "ID";
    public static final String COL1_2 = "PATIENT_ID";
    public static final String COL1_3 = "LATITUDE";
    public static final String COL1_4 = "LONGITUDE";
    public static final String COL1_5 = "TIMESTAMP";
    public static final String COL2_1 = "PATIENT_ID";
    public static final String COL2_2 = "NAME";
    public static final String COL2_3 = "PHONE";
    public static final String COL2_4 = "RELATIONSHIP";

    public databaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + patientLocationTable + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, PATIENT_ID INTEGER, LATITUDE REAL, LONGITUDE REAL, TIMESTAMP INTEGER)");
        sqLiteDatabase.execSQL("create table " + patinetInfoTable + " (PATIENT_ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, PHONE TEXT, RELATIONSHIP TEXT) ");
        System.out.println("lalaland_oncreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ patientLocationTable);
        onCreate(sqLiteDatabase);
    }

    public boolean insertLocationData(Integer patient_id,Double latitude,Double longitude, Long timeStamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1_2,patient_id);
        contentValues.put(COL1_3,latitude);
        contentValues.put(COL1_4,longitude);
        contentValues.put(COL1_5,timeStamp);
        long result = db.insert(patientLocationTable,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }


    public boolean insertPatientData(String name, String phone, String relationship) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2_2,name);
        contentValues.put(COL2_3,phone);
        contentValues.put(COL2_4,relationship);
        long result = db.insert(patientLocationTable,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getLastEntry() {
        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor res = db.rawQuery("select * from "+patientLocationTable,null);
        String selectQuery= "SELECT * FROM " + patientLocationTable+" ORDER BY ID DESC LIMIT 1";
        Cursor res = db.rawQuery(selectQuery, null);
        return res;
    }

    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(patientLocationTable, "ID = ?",new String[] {id});
    }
}
