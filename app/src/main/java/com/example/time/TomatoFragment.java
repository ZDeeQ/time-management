package com.example.time;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pavlospt.CircleView;

import lecho.lib.hellocharts.model.Viewport;



public class TomatoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public static TextView goalText;
    public TomatoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TomatoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TomatoFragment newInstance(String param1, String param2) {
        TomatoFragment fragment = new TomatoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    CircleView invest;//投资
    CircleView rest;//休息
    int investTime = 25;//投资时间配置
    int restTime = 5;//休息时间配置
    static boolean isBegin = false;//番茄是否开始
    CountDownTimer timer;//倒计时器
    static boolean finished = false;//完成番茄
    String status = null;//当前状态
    static View root;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_tomato, container, false);
        invest =(CircleView) root.findViewById(R.id.investCircle);
        rest = (CircleView)root.findViewById(R.id.restCircle);
        //投资按钮事件
        invest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBegin == false && finished == false){
                    beginInvest();
                    isBegin = true;
                }
                else if (isBegin && !finished){
                    showAlert();
                }
                else {
                    inputTime();
                }
            }
        });
        //休息事件按钮
        rest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBegin==false){
                    if (finished)
                        invest.setSubtitleColor(invest.getTitleColor());
                    beginRest();
                    isBegin = true;
                }
                else showAlert();
            }
        });
        //配置时间
        root.findViewById(R.id.tomatoSetting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBegin){
                    Toast.makeText(root.getContext(),"需要停止番茄才能设置哦",Toast.LENGTH_SHORT).show();
                }
                else {
                    showSettingDialog();
                }
            }
        });


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isBegin && !finished)
        {
            if (status.equals("invest"))
            {
                invest.setSubtitleText("投入");
                invest.setTitleText("请稍等");
            }
            else if (status.equals("rest")){
                invest.setSubtitleText("休息");
                invest.setTitleText("请稍等");
                invest.setFillColor(rest.getSubtitleColor());
                rest.setVisibility(View.INVISIBLE);
            }
        }
        else if (finished){
            invest.setTitleText("提交");
            invest.setSubtitleColor(invest.getFillColor());
        }
    }

    private void inputTime() {
        //TODO
        finished = false;



        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.invest_time_two,null);
        goalText=(TextView)contentView.findViewById(R.id.goal_textView);

        RecyclerView investRecycler=(RecyclerView)contentView.findViewById(R.id.investtime_recycler);

        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayout.HORIZONTAL);
        investRecycler.setLayoutManager(layoutManager);
        final GoalAdapterSimpleTwo goalAdapterSimpleTwo=new GoalAdapterSimpleTwo(TodayFragment.goalList);
        investRecycler.setAdapter(goalAdapterSimpleTwo);

        final PopupWindow popWnd = new PopupWindow (getActivity());


        Button sureButton=(Button)contentView.findViewById(R.id.sure_button);

        sureButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!goalText.getText().toString().equals("点击下方目标")) {
                    Goal goal = TodayFragment.goalList.get(goalAdapterSimpleTwo.getPosition());
                    int second = investTime;
                    float hour = (float) second / 60;

                    goal.setTodayTime(goal.getTotalTime() + hour);
                    goal.setTotalTime(goal.getTotalTime() + hour);
                    SQLiteDatabase db = MainActivity.dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("everyday_invest", goal.getTodayTime());
                    db.update("goal", values, "name=?" + "and " + "date=?"
                            , new String[]{goal.getName(), MainActivity.getDateString()});
                    TodayFragment.goalAdapter.notifyDataSetChanged();
                    TodayFragment.renewBar();

                   // EverydayFragment.refresh();
                   // StatisticsFragment.refresh();

                    Toast.makeText(getActivity(), "添加成功", Toast.LENGTH_SHORT).show();
                    popWnd.dismiss();
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




    }

    private void showSettingDialog() {
        AlertDialog.Builder alertDialog = new  AlertDialog.Builder(root.getContext());
        final String[] type = {"番茄时间","休息时间"};
        final String[] tomatoTime = {"25","30","35","40","45","50"};
        final String[] restTime = {"5","10","15","20","25","30"};
        alertDialog.setTitle("配置类型");
        alertDialog.setItems(type, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        showTimeSelectDialog(tomatoTime,0);
                        break;
                    case 1:
                        showTimeSelectDialog(restTime,1);
                        break;
                }
            }
        }).create().show();

    }



    private void showTimeSelectDialog(final String[] time, final int type) {
        AlertDialog.Builder timeSelect = new AlertDialog.Builder(root.getContext()).setTitle("选择时间");
        timeSelect.setItems(time, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (type==0){
                    investTime = Integer.parseInt(time[i]);
                    invest.setSubtitleText(time[i]+":00");
                }
                else
                    restTime = Integer.parseInt(time[i]);
            }
        }).create().show();
    }


    private void showAlert() {
        final int green = invest.getFillColor();
        final AlertDialog alertDialog = new AlertDialog.Builder(root.getContext()).create();
        alertDialog.setTitle("确认放弃吗？");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stopInvest();
                isBegin = false;
                init();
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }



    private void beginRest() {
        status = "rest";
        invest.setTitleText(restTime+":00");
        invest.setSubtitleText("休息");
        invest.setFillColor(rest.getSubtitleColor());
        rest.setVisibility(View.INVISIBLE);
        timer = new CountDownTimer(restTime*60*1000,1000) {
            @Override
            public void onTick(long l) {
                String minute = Long.toString(l/(60*1000));
                String second;
                if ((l%(60*1000))<1000) second = "00";
                else if ((l%(60*1000))<10000) second = "0" + Long.toString(l%(60*1000)).charAt(0);
                else second = Long.toString(l%(60*1000)).substring(0,2);
                invest.setTitleText(minute + ":" + second);
            }

            @Override
            public void onFinish() {
                final int blue = invest.getFillColor();
                Vibrator vibrator = (Vibrator)root.getContext().getSystemService(root.getContext().VIBRATOR_SERVICE);
                vibrator.vibrate(1000);
                if (!finished){
                    init();
                }
                else {
                    invest.setFillColor(blue);
                    invest.setTitleText("提交");
                    invest.setSubtitleColor(blue);
                    rest.setVisibility(View.VISIBLE);
                }
                isBegin = false;
                status = null;
            }

        }.start();
    }

    private void stopInvest() {
        timer.cancel();
        init();
    }

    private void beginInvest() {
        invest.setTitleText(investTime+":00");
        invest.setSubtitleText("投入");
        status = "invest";
            timer = new CountDownTimer(investTime*60*1000,1000) {

            @Override
            public void onTick(long l) {
                String minute = Long.toString(l/(60*1000));
                String second;
                if ((l%(60*1000))<1000) second = "00";
                else if ((l%(60*1000))<10000) second = "0" + Long.toString(l%(60*1000)).charAt(0);
                else second = Long.toString(l%(60*1000)).substring(0,2);
                invest.setTitleText(minute + ":" + second);
            }

            @Override
            public void onFinish() {
                Vibrator vibrator = (Vibrator)root.getContext().getSystemService(root.getContext().VIBRATOR_SERVICE);
                vibrator.vibrate(1000);
                invest.setTitleText("提交");
                invest.setSubtitleColor(invest.getFillColor());
                isBegin = false;
                finished = true;
                status = null;
            }
        }.start();
    }

    private void init(){
        final int blue = invest.getFillColor();
        invest.setTitleText("投资");
        invest.setSubtitleText(investTime+":00");
        invest.setFillColor(blue);
        rest.setVisibility(View.VISIBLE);

    }


}
