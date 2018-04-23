package com.example.time;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper{
    public static final  String CREATE_GOAL_INFO="create table goal_info("
            +"name text primary key,"
            +"encourage text,"
            +"pre_invest integer default 0,"
            +"start_date text,"
            +"end_date text,"
            +"finished integer default 0)";
    public static final String CREATE_GOAL="create table goal("
            +"name text,"
            +"date text,"
            +"everyday_invest real default 0.0,"
            +"primary key(name,date),"
            +"foreign key(name) references goal_info(name))";
    public static  final String CREATE_SUMMARY="create table summary("
            +"date text primary key,"
            +"summary text,"
            +"foreign key(date) references goal(date))";
    private Context mContext;

    public MyDatabaseHelper(Context context, String name,
                            SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
        mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_GOAL_INFO);
        db.execSQL(CREATE_GOAL);
        db.execSQL(CREATE_SUMMARY);
        //Toast.makeText(mContext,"create succeeded",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
