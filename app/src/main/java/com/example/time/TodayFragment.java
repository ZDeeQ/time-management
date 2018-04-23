package com.example.time;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.service.quicksettings.Tile;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by liutang on 2018/3/26.
 */

public class TodayFragment extends Fragment {
    @Nullable

    public static List<Goal> goalList=new ArrayList<>();

    public static GoalAdapter goalAdapter;

    public static TextView goalText;

    private static ProgressBar investBar,wasteBar;

    private static TextView investHint;
    private static TextView wasteHint;
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        final View view=inflater.inflate(R.layout.today_fragment,container,false);

        investBar=(ProgressBar)view.findViewById(R.id.invest_progressbar);
        wasteBar=(ProgressBar)view.findViewById(R.id.waste_progressbar);
        investHint=(TextView)view.findViewById(R.id.invest_hint);
        wasteHint=(TextView)view.findViewById(R.id.waste_hint);
        renewBar();

        Time t=new Time();
        t.setToNow();
        int hour=t.hour;
        int minute=t.minute;

        double restTime=24-(hour+minute/60.0);
        DecimalFormat df=new DecimalFormat("0.0");
        TextView resttimeText=(TextView)view.findViewById(R.id.rest_time);
        resttimeText.setText(df.format(restTime)+"h");

        ImageView goalInfo=(ImageView)view.findViewById(R.id.goal_info);
        goalInfo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),GoalInfo.class);
                startActivity(intent);
            }
        });

       final RecyclerView recyclerView=(RecyclerView)view.findViewById(R.id.recycle_view);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        goalAdapter = new GoalAdapter(goalList);
        recyclerView.setAdapter(goalAdapter);

        final LinearLayout todayTitleBar=(LinearLayout)view.findViewById(R.id.today_titlebar);
        FloatingActionButton addFab=(FloatingActionButton)view.findViewById(R.id.add_fab);
        FloatingActionButton investFab=(FloatingActionButton)view.findViewById(R.id.invest_fab);

        addFab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {



                startActivity(new Intent(getActivity(),AddGoalActivity.class));



            }
        });

        investFab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                View contentView = inflater.inflate(R.layout.invest_time,null);

                final EditText investEditText=(EditText)contentView.findViewById(R.id.invest_editText);
                goalText=(TextView)contentView.findViewById(R.id.goal_textView);


                RecyclerView investRecycler=(RecyclerView)contentView.findViewById(R.id.investtime_recycler);

                LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
                layoutManager.setOrientation(LinearLayout.HORIZONTAL);
                investRecycler.setLayoutManager(layoutManager);
                final GoalAdapterSimple goalAdapterSimple=new GoalAdapterSimple(goalList);
                investRecycler.setAdapter(goalAdapterSimple);

                final PopupWindow popWnd = new PopupWindow (getActivity());


                Button sureButton=(Button)contentView.findViewById(R.id.sure_button);

                sureButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        if(!goalText.getText().toString().equals("点击下方目标")) {
                            Goal goal = goalList.get(goalAdapterSimple.getPosition());
                            int second = Integer.parseInt(investEditText.getText().toString());
                            if(second>=0&&second<=1440) {
                                float hour = (float) second / 60;

                                goal.setTodayTime(goal.getTodayTime() + hour);
                                goal.setTotalTime(goal.getTotalTime() + hour);
                                SQLiteDatabase db = MainActivity.dbHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("everyday_invest", goal.getTodayTime());


                                Cursor cursor = db.rawQuery("select * from goal where name=" + "\"" + goal.getName() + "\""
                                        + " and " + "date=" + "\"" + MainActivity.getDateString() + "\"", null);
                                if (cursor.getCount() != 0) {
                                    db.update("goal", values, "name=?" + " and " + "date=?"
                                            , new String[]{goal.getName(), MainActivity.getDateString()});
                                    Log.i("MainActivity", "fuckfuckfuck");
                                } else {
                                    values.put("name", goal.getName());
                                    values.put("date", MainActivity.getDateString());
                                    db.insert("goal", null, values);
                                    Log.i("MainActivity", "好的好的好的");
                                }

                                goalAdapter.notifyDataSetChanged();
                                renewBar();
                                // EverydayFragment.refresh();
                                // StatisticsFragment.refresh();

                                Toast.makeText(getActivity(), "添加成功", Toast.LENGTH_SHORT).show();
                                popWnd.dismiss();
                            }
                            else{
                                Toast.makeText(getActivity(), "一天只有24小时", Toast.LENGTH_SHORT).show();
                            }

                        }
                        else{
                            Toast.makeText(getActivity(), "请先选择目标", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                popWnd.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                popWnd.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

                popWnd.setContentView(contentView);
                popWnd.setFocusable(true);
                popWnd.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.CENTER,0,0);

                InputMethodManager imm=(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);

            }
        });

        return view;
    }


    public static void renewBar(){

        SQLiteDatabase db=MainActivity.dbHelper.getWritableDatabase();
        int todayInvest=0,todayWaste=0;
        DecimalFormat df=new DecimalFormat("0.0");
        Cursor cursor=db.rawQuery("select avg(everyday_invest) from goal where date="+"\""+MainActivity.getDateString()+"\""
                +"and name!=\"固定\""+"and name!=\"睡眠\""+"and name!=\"浪费\"",null);
        if(cursor.moveToFirst()) {
            float invest=cursor.getFloat(0);
            investHint.setText(df.format(invest)+"h投资");
            todayInvest = (int)invest*1000 / 24;
        }
        cursor=db.rawQuery("select everyday_invest from goal where name=\"浪费\"",null);
        if(cursor.moveToFirst()) {
            float waste=cursor.getFloat(0);
            wasteHint.setText(df.format(waste)+"h浪费");
            todayWaste = (int) waste * 1000 / 24;
        }
        cursor.close();
        investBar.setProgress(todayInvest);
        wasteBar.setProgress(todayWaste);
    }

}
