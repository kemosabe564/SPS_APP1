//package com.example.myapplication2;
//
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.content.ContentValues;
//import android.util.Log;
//
//
//public class MyDbHandler extends SQLiteOpenHelper {
//    private static final int DATABASE_VERSION=1;
//    private static final String DATABASE_NAME="AllSensors.db";
//
//    //Table SensorData
//    private static final String TABLE_NAME="SensorData";
//    private static final String COLUMN_ID="_id";
//    private static final String COLUMN_DEVICEID="deviceId";
//    private static final String COLUMN_MODEL="modelName";
//    private static final String COLUMN_TIMESTAMP="timestamp";
//    private static final String COLUMN_ACCVAL0="accval0";
//    private static final String COLUMN_ACCVAL1="accval1";
//    private static final String COLUMN_ACCVAL2="accval2";
//    private static final String COLUMN_SSID="ssid";
//    private static final String COLUMN_RSSI="rssi";
//    private static final String COLUMN_LOCTIME="localTime";
//    private static final String COLUMN_CELL="cellNumber";
//    private static final String COLUMN_ACTIVITY="action";
//    private String TAG="com.example.georgia.sps_app1";
//
//
//    //Constructor
//    public MyDbHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
//        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
//    }
//
//
//    //Override methods
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        String query ="CREATE TABLE "+ TABLE_NAME + "(" + COLUMN_ID +
//                " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_DEVICEID +" TEXT NULL,"+
//                COLUMN_TIMESTAMP + " TEXT NULL," + COLUMN_MODEL + " TEXT NULL," +
//                COLUMN_ACCVAL0  + " TEXT NULL," +
//                COLUMN_ACCVAL1 + " TEXT NULL," + COLUMN_ACCVAL2 + " TEXT NULL," +
//                COLUMN_SSID + " TEXT NULL," + COLUMN_RSSI + " TEXT NULL," + COLUMN_LOCTIME +" TEXT NULL," + COLUMN_CELL +
//                " TEXT NULL," + COLUMN_ACTIVITY +" TEXT NULL" + ");";
//        Log.i(TAG,query);
//        db.execSQL(query);
//    }
//
//    @Override
//    //Upgrade the table
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME );
//        onCreate(db);
//    }
//
//    //Methods
//    //Add a new row to the database
//    public void addRow(SensorsTable snst){
//        SQLiteDatabase db=this.getWritableDatabase();
//        // Use ContentValues to add a row in the table
//        ContentValues values = new ContentValues();
//        //Add values for each column
//        values.put(COLUMN_DEVICEID,snst.get_deviceId());
//        values.put(COLUMN_TIMESTAMP,snst.get_tmst());
//        values.put(COLUMN_MODEL,snst.get_modelName());
//        values.put(COLUMN_ACCVAL0,snst.get_accVal0());
//        values.put(COLUMN_ACCVAL1,snst.get_accVal1());
//        values.put(COLUMN_ACCVAL2,snst.get_accVal2());
//        values.put(COLUMN_SSID,snst.get_SSID());
//        values.put(COLUMN_RSSI,snst.get_RSSI());
//        values.put(COLUMN_LOCTIME,snst.getLocalTime());
//        values.put(COLUMN_CELL,snst.getCellNo());
//        values.put(COLUMN_ACTIVITY,snst.getAction());
//        long g=db.insert(TABLE_NAME, null, values);
//        Log.i(TAG,String.valueOf(g));
//
//        if(g!=-1){
//            Log.i(TAG,"Row added");
//        }
//        else{
//            Log.i(TAG,"Row not added");
//        }
//        db.close();
//    }
//
//
//
//    public void deleteAll(){
//        SQLiteDatabase db=getWritableDatabase();
//        String query="DELETE FROM "+ TABLE_NAME;
//        db.execSQL(query);
//    }
//
//    public String DatabaseToString(){
//        int sa;
//        String dbString="";
//        SQLiteDatabase db = getReadableDatabase();
//        String query = "SELECT * FROM " + TABLE_NAME;
//
//        //Cursor that points to a row of the table
//        Cursor c = db.rawQuery(query, null);
//
//        //Move to first row
//        if(c.moveToFirst()){
//            Log.i(TAG,"There are records in the table");
//        }
//        else{
//            dbString="No records in the table!";
//            return dbString;
//        }
//
//        do{
//            sa=c.getInt(c.getColumnIndex(COLUMN_ID));
//            dbString +=String.valueOf(sa);
//            dbString+=" , ";
//            dbString+=c.getString(c.getColumnIndex(COLUMN_DEVICEID));
//            dbString+=" , ";
//            dbString+=c.getString(c.getColumnIndex(COLUMN_TIMESTAMP));
//            dbString+=" , ";
//            dbString+=c.getString(c.getColumnIndex(COLUMN_MODEL));
//            dbString+=" , ";
//            dbString+=c.getString(c.getColumnIndex(COLUMN_ACCVAL0));
//            dbString+=" , ";
//            dbString+=c.getString(c.getColumnIndex(COLUMN_ACCVAL1));
//            dbString+=" , ";
//            dbString+=c.getString(c.getColumnIndex(COLUMN_ACCVAL2));
//            dbString+=" , ";
//            dbString+=c.getString(c.getColumnIndex(COLUMN_SSID));
//            dbString+=" , ";
//            dbString+=c.getString(c.getColumnIndex(COLUMN_RSSI));
//            dbString+=" , ";
//            dbString+=c.getString(c.getColumnIndex(COLUMN_LOCTIME));
//            dbString+=" , ";
//            dbString+=c.getString(c.getColumnIndex(COLUMN_CELL));
//            dbString+=" , ";
//            dbString+=c.getString(c.getColumnIndex(COLUMN_ACTIVITY));
//            dbString+="\n";
//        }while(c.moveToNext());
//        c.close();
//        return dbString;
//
//    }
//}
