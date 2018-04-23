package com.example.time;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;
/**
 * Created by liutang on 2018/3/27.
 */

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder>{
    private List<Goal> goalList;
    static class ViewHolder extends RecyclerView.ViewHolder{
        boolean start;
        View goalView;
        ImageView goalImage;
        TextView goalName;
        Chronometer timer;
        TextView timeCount;
        ImageView timerImage;
        public ViewHolder(View view){
            super(view);
            start=false;
            goalView=view;
            goalImage=(ImageView)view.findViewById(R.id.goal_image);
            goalName=(TextView)view.findViewById(R.id.goal_name);
            timer=(Chronometer)view.findViewById(R.id.timer);
            timeCount=(TextView)view.findViewById(R.id.time_count);
            timerImage=(ImageView)view.findViewById(R.id.timer_image);
        }
    }
    public GoalAdapter(List<Goal> goalList){
        this.goalList=goalList;
    }
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            final View view= LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.goal_item,parent,false);
            final ViewHolder viewHolder=new GoalAdapter.ViewHolder(view);
            ;
            viewHolder.goalView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(final View v) {

                    int position=(int)view.getTag();
                    final Goal goal=goalList.get(position);

                    if(goal.getName().equals("固定")||goal.getName().equals("浪费")||goal.getName().equals("睡眠")) {
                        final AlertDialog.Builder inputDialog =
                                new AlertDialog.Builder(MainActivity.mainActivity);
                        final EditText editText = new EditText(MainActivity.mainActivity);
                        editText.setText(goal.getName());
                        editText.setSelection(goal.getName().length());

                        inputDialog.setTitle("修改名字").setView(editText);
                        inputDialog.setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String name = editText.getText().toString();
                                        SQLiteDatabase db = MainActivity.dbHelper.getWritableDatabase();
                                        ContentValues values = new ContentValues();
                                        values.put("name", name);
                                        db.update("goal_info", values, "name=?", new String[]{goal.getName()});
                                        db.update("goal", values, "name=?", new String[]{goal.getName()});
                                        goal.setName(name);
                                        GoalAdapter.this.notifyDataSetChanged();
                                    }
                                });
                        /*
                        inputDialog.setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                */
                        inputDialog.show();
                    }
                    else {
                        Activity mainAcitivity = (Activity) parent.getContext();
                        Intent intent = new Intent(mainAcitivity, AddGoalActivity.class);
                        intent.putExtra("position", position);
                        mainAcitivity.startActivity(intent);
                    }
                }
            });
        final ImageView timerImage= viewHolder.timerImage;
        timerImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Chronometer timer=viewHolder.timer;
                if(!viewHolder.start) {
                    viewHolder.timerImage.setImageResource(R.drawable.stop);
                    viewHolder.start=true;
                    timer.setBase(SystemClock.elapsedRealtime());
                    int hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60);
                    timer.setFormat("0"+String.valueOf(hour)+":%s");
                    Vibrator vibrator = (Vibrator)MainActivity.mainActivity.getSystemService(MainActivity.mainActivity.VIBRATOR_SERVICE);
                    vibrator.vibrate(200);

                    timer.start();
                    int position=(int)view.getTag();
                    Toast.makeText(MainActivity.mainActivity,"开始为"+goalList.get(position).getName()+"计时",Toast.LENGTH_SHORT).show();
                }
                else{
                    viewHolder.timerImage.setImageResource(R.drawable.start);
                    viewHolder.start=false;
                    String[] t=timer.getText().toString().split(":");
                    int second=Integer.parseInt(t[0])*60+Integer.parseInt(t[1]);
                    if(second!=0) {
                        float hour = (float) second / 60;
                        Goal goal = goalList.get((int) view.getTag());
                        goal.setTodayTime(goal.getTodayTime() + hour);
                        goal.setTotalTime(goal.getTotalTime() + hour);
                        SQLiteDatabase db = MainActivity.dbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("everyday_invest", goal.getTodayTime());
                        Cursor cursor=db.rawQuery("select * from goal where name="+"\""+goal.getName()+"\""
                                +" and "+"date="+"\""+MainActivity.getDateString()+"\"",null);
                        if(cursor.getCount()!=0) {
                            db.update("goal", values, "name=?" + " and " + "date=?"
                                    , new String[]{goal.getName(), MainActivity.getDateString()});

                        }
                        else{
                            values.put("name",goal.getName());
                            values.put("date",MainActivity.getDateString());
                            db.insert("goal",null,values);

                        }
                        int position=(int)view.getTag();

                        Toast.makeText(MainActivity.mainActivity,goalList.get(position).getName()+"计时完成",Toast.LENGTH_SHORT).show();

                        notifyDataSetChanged();
                    }
                    else{
                        Toast.makeText(MainActivity.mainActivity,"过滤开启，不保存一分钟以下记录！",Toast.LENGTH_SHORT).show();
                    }
                    timer.setBase(SystemClock.elapsedRealtime());
                    timer.stop();


                }
            }
        });
        return viewHolder;
    }
    public void onBindViewHolder(GoalAdapter.ViewHolder holder, int position) {
        Goal goal=goalList.get(position);
        holder.goalImage.setImageResource((goal.getGoalImageID()));
        holder.goalName.setText(goal.getName());

        DecimalFormat df=new DecimalFormat("0.00");
        String timeCount="总计:"+df.format(goal.getTotalTime())
                +"h"+"\n"+"今日:"+df.format(goal.getTodayTime())+"h";
        holder.timeCount.setText(timeCount);
        holder.goalView.setTag(position);


    }
    public int getItemCount() {
        return goalList.size();
    }
}
