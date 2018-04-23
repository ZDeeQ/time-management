package com.example.time;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener,EverydayFragment.OnFragmentInteractionListener,StatisticsFragment.OnFragmentInteractionListener{
    private List<Fruit> fruitList=new ArrayList<>();
    private  TodayFragment todayFragment;
    private  TomatoFragment tomatoFragment;
    private  EverydayFragment everydayFragment;
    private  StatisticsFragment statisticsFragment;
    private BottomNavigationBar bottomNavigationBar;
    private int lastSelectedPosition = 0;
    private Fragment currentFragment;
    public  static MyDatabaseHelper dbHelper;
    private List<Goal> goalList=new ArrayList<>();
    public static Activity mainActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity=this;


        dbHelper=new MyDatabaseHelper(this,"time.db",null,1);
        dbHelper.getWritableDatabase();

        SQLiteDatabase db=dbHelper.getWritableDatabase();
        Cursor cursor=db.rawQuery("select * from goal_info",null);

        if(cursor.getCount()==0){
            ContentValues values=new ContentValues();
            values.put("name","浪费");
            db.insert("goal_info",null,values);
            values.put("date",getDateString());
            db.insert("goal",null,values);
            values.clear();
            values.put("name","睡眠");
            db.insert("goal_info",null,values);
            values.put("date",getDateString());
            db.insert("goal",null,values);
            values.clear();
            values.put("name","固定");
            db.insert("goal_info",null,values);
            values.put("date",getDateString());
            db.insert("goal",null,values);
        }

        initGoals();
        TodayFragment.goalList=this.goalList;



        todayFragment=new TodayFragment();
        tomatoFragment=new TomatoFragment();
        everydayFragment=new EverydayFragment();
        statisticsFragment=new StatisticsFragment();

        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        bottomNavigationBar
                .setTabSelectedListener(this)
                .setMode(BottomNavigationBar.MODE_FIXED)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .setActiveColor("#FF107FFD")
                .setInActiveColor("#A9A9A9")
                .setBarBackgroundColor("#F5F5F5");
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.today, "今天"))
                .addItem(new BottomNavigationItem(R.drawable.tomato, "番茄"))
                .addItem(new BottomNavigationItem(R.drawable.everyday, "每天"))
                .addItem(new BottomNavigationItem(R.drawable.statistic, "统计"))
                .setFirstSelectedPosition(lastSelectedPosition )
                .initialise();
        setDefaultFragment();

    }

    public void onTabSelected(int position) {
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        switch(position){
            case 0:
                showFragment(todayFragment);
                break;
            case 1:
                showFragment(tomatoFragment);
                break;
            case 2:
               showFragment(everydayFragment);
                break;
            case 3:
               showFragment(statisticsFragment);
                break;
            default:
                break;
        }

    }

    public void setDefaultFragment(){
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        transaction.add(R.id.outer_framelayout,todayFragment);
        transaction.show(todayFragment);
        currentFragment=todayFragment;

        transaction.commit();
    }
    private void showFragment(Fragment fg) {
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        //如果之前没有添加过
        if(fg instanceof EverydayFragment) {
            everydayFragment = new EverydayFragment();
            fg=everydayFragment;
        }
        if(fg instanceof StatisticsFragment){
            statisticsFragment=new StatisticsFragment();
            fg=statisticsFragment;
        }

        if(!fg.isAdded()){

            transaction
                    .hide(currentFragment)
                    .add(R.id.outer_framelayout,fg);
        }else{
            transaction
                    .hide(currentFragment)
                    .show(fg);
        }
        currentFragment = fg;
        transaction.commit();
    }
    public void onTabUnselected(int position) {

    }

    public void onTabReselected(int position) {

    }


    public void initFruits(){
        String[] data={"苹果","香蕉","橘子","番茄","黄瓜","土豆","梨子","甘蔗"
                ,"西瓜","菠萝","桃子","桂圆","辣椒","火龙果","猕猴桃","红枣"};
        for(String s:data) {
            fruitList.add(new Fruit(s,R.drawable.me));
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void initGoals(){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        Cursor cursor=db.rawQuery("select name from goal_info",null);
        if(cursor.moveToFirst()){
            do {
                String s = cursor.getString(cursor.getColumnIndex("name"));
                float totalTime=0;
                float totdayTime=0;

                Cursor cursor1=db.rawQuery("select sum(everyday_invest) from goal where name="+"\""+s+"\"",null);
                if(cursor1.moveToFirst());
                    totalTime=cursor1.getFloat(0);

                String date=getDateString();
                cursor1=db.rawQuery("select everyday_invest from goal where name="+"\""+s+"\""
                        +" and "+"date="+"\""+date+"\"",null);
                if(cursor1.getCount()!=0) {
                    if (cursor1.moveToFirst()) ;
                    totdayTime = cursor1.getFloat(cursor1.getColumnIndex("everyday_invest"));
                }
                cursor1.close();
                if(s.equals("浪费"))
                    goalList.add(new Goal(s,totalTime,totdayTime,R.drawable.waste));
                else if(s.equals("睡眠"))
                    goalList.add(new Goal(s,totalTime,totdayTime,R.drawable.sleep));
                else if(s.equals("固定"))
                    goalList.add(new Goal(s,totalTime,totdayTime,R.drawable.solid));
                else
                    goalList.add(0,new Goal(s,totalTime,totdayTime,R.drawable.lamp));
            }while(cursor.moveToNext());
        }
        cursor.close();
        /*
        Cursor cursor1=db.rawQuery("select * from goal where name="+"\""+"浪费"+"\"",null);
        cursor1.moveToFirst();
        Toast.makeText(MainActivity.this,cursor1.getString(cursor1.getColumnIndex("name")),Toast.LENGTH_SHORT).show();
        Toast.makeText(MainActivity.this,cursor1.getString(cursor1.getColumnIndex("date")),Toast.LENGTH_SHORT).show();
        Toast.makeText(MainActivity.this,cursor1.getString(cursor1.getColumnIndex("everyday_invest")),Toast.LENGTH_SHORT).show();
        cursor1.close();
        */
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /*
    public void onBackPressed() {

        //final TextView textView = new TextView(MainActivity.this);
       //textView.setText("退出后将停止计时！");
       //textView.setGravity(TextView.TEXT_ALIGNMENT_CENTER);


        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("   警告:退出后将停止计时!");

        inputDialog.setPositiveButton("退出",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();;
                    }
                });
        inputDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        inputDialog.show();

    }
    */


    public static String getDateString(){
        Calendar cal= Calendar.getInstance();
        int year=cal.get(Calendar.YEAR);
        int month=cal.get(Calendar.MONTH);
        int day=cal.get(Calendar.DAY_OF_MONTH);
        String dataString=year+"-"+(++month)+"-"+day;
        return dataString;
    }

}
