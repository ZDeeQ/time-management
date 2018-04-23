package com.example.time;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

public class AddGoalActivity extends AppCompatActivity {
    private Calendar cal;
    private int year,month,day;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);
        getDate();
        final SQLiteDatabase db=MainActivity.dbHelper.getWritableDatabase();

        ImageView backImageView=(ImageView)findViewById(R.id.back_image);
        Button saveButton=(Button)findViewById(R.id.save_button);

        final EditText editGoalName=(EditText)findViewById(R.id.goal_name_edit) ;
        final EditText editCourage=(EditText)findViewById(R.id.encourage_edit);

        final ImageView needTimeImage=(ImageView)findViewById(R.id.needtime_edit);
        final TextView needTimeText=(TextView)findViewById(R.id.needtime_textview);
        final ImageView startDateImage=(ImageView)findViewById(R.id.startdate_edit);
        final TextView startDateText=(TextView)findViewById(R.id.startdate_textview);
        ImageView endDateImage=(ImageView)findViewById(R.id.enddate_edit);
        final TextView endDateText=(TextView)findViewById(R.id.enddate_textview) ;
        final TextView everydatTimeTextView=(TextView)findViewById(R.id.everydaytime_textview);
        Button deleteButton=(Button)findViewById(R.id.delete_button);
        final int positon=getIntent().getIntExtra("position",-1);

        if(positon!=-1){
            String goalName=TodayFragment.goalList.get(positon).getName();
            Cursor cursor=db.rawQuery("select * from goal_info where name="+"\""+goalName+"\"",null);
            cursor.moveToFirst();

            editGoalName.setText(goalName);
            editGoalName.setSelection(goalName.length());
            String encourage=cursor.getString(cursor.getColumnIndex(("encourage")));
            editCourage.setText(encourage);
            editCourage.setSelection(encourage.length());
            int needTime=cursor.getInt(cursor.getColumnIndex("pre_invest"));
            needTimeText.setText(needTime+"小时");
            startDateText.setText(cursor.getString(cursor.getColumnIndex("start_date")));
            endDateText.setText(cursor.getString(cursor.getColumnIndex("end_date")));
            cursor.close();
            everydatTimeTextView.setText(calBetweenTime(needTime,startDateText.getText().toString(),endDateText.getText().toString()));

        }
        else{
            deleteButton.setText("取消");
            startDateText.setText(MainActivity.getDateString());
            endDateText.setText(MainActivity.getDateString());
            everydatTimeTextView.setText(needTimeText.getText());
        }





        backImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                /*
                Cursor cursor=db.rawQuery("select * from goal_info",null);

                if(cursor.moveToFirst()){
                    do {
                        String name = cursor.getString(cursor.getColumnIndex("name"));
                        Toast.makeText(AddGoalActivity.this, name, Toast.LENGTH_SHORT).show();
                    }while(cursor.moveToNext());
                }
                cursor.close();
                */
                finish();
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                /*
                String insert="insert into goal_info(name,encourage,pre_invest,start_date,end_date,finished) values("
                        +goalName+","+encourage+","+needTime+","+startDate+","+endDate+","+0+")";


                db.execSQL(insert);
                */
                String goalName = editGoalName.getText().toString();
                if(goalName==null||goalName.isEmpty()){
                    Toast.makeText(AddGoalActivity.this,"请输入目标",Toast.LENGTH_SHORT).show();
                }else {
                    String encourage = editCourage.getText().toString();
                    String[] s = needTimeText.getText().toString().split("小");
                    int needTime = Integer.parseInt(s[0]);
                    String startDate = startDateText.getText().toString();
                    String endDate = endDateText.getText().toString();
                    ContentValues values = new ContentValues();
                    values.put("name", goalName);
                    values.put("encourage", encourage);
                    values.put("pre_invest", needTime);
                    values.put("start_date", startDate);
                    values.put("end_date", endDate);
                    ContentValues values1 = new ContentValues();
                    values1.put("name", goalName);
                    if (positon != -1) {
                        Goal goal = TodayFragment.goalList.get(positon);


                        db.update("goal_info", values, "name=?", new String[]{goal.getName()});
                        db.update("goal", values1, "name=?", new String[]{goal.getName()});
                        goal.setName(goalName);

                    } else {
                        values1.put("date", MainActivity.getDateString());
                        db.insert("goal_info", null, values);
                        db.insert("goal", null, values1);
                        TodayFragment.goalList.add(0, new Goal(goalName, 0, 0, R.drawable.lamp));
                    }
                    TodayFragment.goalAdapter.notifyDataSetChanged();
                    Toast.makeText(AddGoalActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });


        needTimeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(AddGoalActivity.this);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                AlertDialog.Builder inputDialog =
                        new AlertDialog.Builder(AddGoalActivity.this);
                inputDialog.setTitle("修改需要时间").setView(editText);
                inputDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                needTimeText.setText(editText.getText()+"小时");
                                String[] n=needTimeText.getText().toString().split("小");

                                everydatTimeTextView.setText(calBetweenTime(Integer.parseInt(n[0])
                                        ,startDateText.getText().toString(),endDateText.getText().toString()));
                            }
                        });
                inputDialog.show();
            }
        });
        startDateImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final String s=startDateText.getText().toString();
                DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker arg0, int year, int month, int day) {

                        startDateText.setText(year + "-" + (++month) + "-" + day);
                        if(compareDate(startDateText.getText().toString(),endDateText.getText().toString())) {

                            String[] n = needTimeText.getText().toString().split("小");

                            everydatTimeTextView.setText(calBetweenTime(Integer.parseInt(n[0])
                                    , startDateText.getText().toString(), endDateText.getText().toString()));
                        }
                        else{
                            startDateText.setText(s);
                            Toast.makeText(AddGoalActivity.this,"开始时间必须小于截止时间",Toast.LENGTH_SHORT).show();
                        }

                    }
                };
                DatePickerDialog dialog=new DatePickerDialog(AddGoalActivity.this,DatePickerDialog.THEME_HOLO_LIGHT,listener,year,month,day);
                dialog.show();
            }
        });
        endDateImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker arg0, int year, int month, int day) {
                        String s=endDateText.getText().toString();
                        endDateText.setText(year+"-"+(++month)+"-"+day);
                        if(compareDate(startDateText.getText().toString(),endDateText.getText().toString())) {

                            String[] n=needTimeText.getText().toString().split("小");

                            everydatTimeTextView.setText(calBetweenTime(Integer.parseInt(n[0])
                                    ,startDateText.getText().toString(),endDateText.getText().toString()));
                        }
                        else{
                            endDateText.setText(s);
                            Toast.makeText(AddGoalActivity.this,"开始时间必须小于截止时间",Toast.LENGTH_SHORT).show();
                        }

                    }
                };
                DatePickerDialog dialog=new DatePickerDialog(AddGoalActivity.this,DatePickerDialog.THEME_HOLO_LIGHT,listener,year,month,day);

                dialog.show();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(positon!=-1){
                    db.delete("goal_info","name=?",new String[]{TodayFragment.goalList.get(positon).getName()});
                    db.delete("goal","name=?",new String[]{TodayFragment.goalList.get(positon).getName()});
                    TodayFragment.goalList.remove(positon);
                    TodayFragment.goalAdapter.notifyDataSetChanged();
                    TodayFragment.renewBar();
                    Toast.makeText(AddGoalActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

    }

    private String calBetweenTime(int needTime,String startTime,String endTime){
        int betweenDay=0;
        float betweenTime;
        String[] st=startTime.split("-"),et=endTime.split("-");
        int sYear=Integer.parseInt(st[0]),sMonth=Integer.parseInt(st[1]),sDay=Integer.parseInt(st[2]);
        int eYear=Integer.parseInt(et[0]),eMonth=Integer.parseInt(et[1]),eDay=Integer.parseInt(et[2]);

        if(sYear==eYear){
            if(sMonth==eMonth)
                betweenDay=eDay-sDay;
            else {
                betweenDay += calMonthDay(sYear, sMonth) - sDay;
                for (int i =sMonth+1;i<eMonth;i++)
                    betweenDay+=calMonthDay(sYear,i);
                betweenDay += eDay;
            }
        }
        else {
            betweenDay += calMonthDay(sYear, sMonth) - sDay;
            for (int i = sMonth + 1; i <= 12; i++)
                betweenDay += calMonthDay(sYear, i);
            for (int i = 1; i < eMonth; i++)
                betweenDay += calMonthDay(eYear, i);
            betweenDay += eDay;
            if ((eYear - sYear) != 1) {
                for (int i = sYear + 1; i < eYear; i++)
                    if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
                        betweenDay += 366;
                    }
                    else{
                    betweenDay+=365;
                    }
            }
        }
            betweenTime = needTime / (float)(betweenDay + 1);
            DecimalFormat df = new DecimalFormat("0.00");
            return df.format(betweenTime)+"小时";

    }
    private int calMonthDay(int year,int month){
        switch (month){
            case 2:
                if((year%4==0&&year%100!=0)||(year%400==0))
                    return 29;
                return 28;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
                default:return 31;
        }
    }
    private boolean compareDate(String startTime,String endTime){
        String[] st=startTime.split("-"),et=endTime.split("-");
        int sYear=Integer.parseInt(st[0]),sMonth=Integer.parseInt(st[1]),sDay=Integer.parseInt(st[2]);
        int eYear=Integer.parseInt(et[0]),eMonth=Integer.parseInt(et[1]),eDay=Integer.parseInt(et[2]);
        if(sYear<eYear){
            return  true;
        }
        else if(sYear>eYear){
            return false;
        }
        else{
            if(sMonth<eMonth){
                return true;
            }
            else if(sMonth>eMonth){
                return false;
            }
            else{
                if(sDay<=eDay)
                    return true;
                else
                    return false;
            }
        }
    }
    /*
    private int calDay(String date){
        String[] d=date.split("-");
        int dYear=Integer.parseInt(d[0]),dMonth=Integer.parseInt(d[1]),dDay=Integer.parseInt(d[2]);
        dDay+=365;
        if((dYear%4==0&&dYear%100!=0)||(dYear%400==0))
            dDay++;
        if(dMonth==4||dMonth==6||dMonth==9||dMonth==11)
    }
    */
    public void getDate(){
        cal=Calendar.getInstance();
        year=cal.get(Calendar.YEAR);
        month=cal.get(Calendar.MONTH);
        day=cal.get(Calendar.DAY_OF_MONTH);
    }

}
