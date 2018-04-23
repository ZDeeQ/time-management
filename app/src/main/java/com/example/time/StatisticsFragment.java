package com.example.time;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.aigestudio.datepicker.cons.DPMode;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatisticsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StatisticsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StatisticsFragment newInstance(String param1, String param2) {
        StatisticsFragment fragment = new StatisticsFragment();
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

    private static List<AxisValue> mAxisXValues = new ArrayList<>();
    private static List<PointValue> mPointValues = new ArrayList<>();
    private String[] weekday = {"日","一","二","三","四","五","六"};//X轴的标注
    private static float[] score;//图表的数据点
    private static LineChartView lineChart;
    private ImageView popDatePicker;
    private static MyDatabaseHelper db=MainActivity.dbHelper;
    private static View root;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_statistics,container,false);

        lineChart = (LineChartView)root.findViewById(R.id.chart);
        String today = new SimpleDateFormat("yyyy-M-d").format(new Date(System.currentTimeMillis()));

        //开始绘制折线图
        setWeekData(today);
        float total = getInvestSum();
        float avg = Float.parseFloat(new DecimalFormat("#.00").format(total/7.0));
        TextView totalInput = (TextView)root.findViewById(R.id.totalInput);
        TextView avgInput =(TextView) root.findViewById(R.id.avrInput);
        totalInput.setText(total+"h");
        avgInput.setText(avg+"h");
        getAxisLables();
        getAxisPoints();//
        initLineChart();//
        //绘制完毕

//        initDatabase();//完成时删除即可！

        popDatePicker = (ImageView)root.findViewById(R.id.showDate);
        popDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //选择日期
                cn.aigestudio.datepicker.views.DatePicker picker = new cn.aigestudio.datepicker.views.DatePicker(root.getContext());
                final AlertDialog ad = new AlertDialog.Builder(root.getContext()).setView(picker).create();
                picker.setDate(2018,3);
                picker.setMode(DPMode.SINGLE);
                picker.setOnDatePickedListener(new cn.aigestudio.datepicker.views.DatePicker.OnDatePickedListener() {
                    @Override
                    public void onDatePicked(String date) {
                        //读取当日时间分配并刷新
                        setWeekData(date);
                        float total = getInvestSum();
                        float avg = Float.parseFloat(new DecimalFormat("#.00").format(total/7.0));
                        TextView totalInput = (TextView)root.findViewById(R.id.totalInput);
                        TextView avgInput =(TextView) root.findViewById(R.id.avrInput);
                        totalInput.setText(total+"h");
                        avgInput.setText(avg+"h");
                        ad.dismiss();
                        getAxisPoints();//
                        initLineChart();//
                    }
                });
                ad.show();
            }
        });


        return root;
    }


//    @Override
//    public void onStart() {
//        super.onStart();
//        String today = new SimpleDateFormat("yyyy-M-d").format(new Date(System.currentTimeMillis()));
//        setWeekData(today);
//        float total = getInvestSum();
//        float avg = Float.parseFloat(new DecimalFormat("#.00").format(total/7.0));
//        TextView totalInput = (TextView)root.findViewById(R.id.totalInput);
//        TextView avgInput =(TextView) root.findViewById(R.id.avrInput);
//        totalInput.setText(total+"h");
//        avgInput.setText(avg+"h");
//        getAxisPoints();//
//        initLineChart();//
//    }

    private static float getInvestSum() {
        float sum =0;
        for (int i=0;i<score.length;i++){
            sum += score[i];
        }

        return  Float.parseFloat(new DecimalFormat("#.00").format(sum));
    }

    private static void setWeekData(String date) {
        //得到date所在周第一天和最后一天
        Calendar calendar = Calendar.getInstance();
        int year = Integer.parseInt(date.split("-")[0]);
        int month = Integer.parseInt(date.split("-")[1])-1;
        int day = Integer.parseInt(date.split("-")[2]);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.DATE,day);
        System.out.println(calendar.getTime());
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
        String weekStart = simpleDateFormat.format(calendar.getTime());
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
        String weekEnd = simpleDateFormat.format(calendar.getTime());
        System.out.println("weekStart"+weekStart);
        System.out.println("weekEnd"+weekEnd);

        SQLiteDatabase readDB = db.getReadableDatabase();
        Cursor c = readDB.rawQuery("select name,date,everyday_invest from goal where date between ? and ?",new String[]{weekStart,weekEnd});
        score = new float[]{0,0,0,0,0,0,0};
        while (c.moveToNext()){
            int weekDay = getWeekDay(c.getString(c.getColumnIndex("date")));
            System.out.println("date "+c.getString(c.getColumnIndex("date")));
            if (!c.getString(c.getColumnIndex("name")).equals("浪费")&&
                    !c.getString(c.getColumnIndex("name")).equals("固定")&&
                    !c.getString(c.getColumnIndex("name")).equals("未知")&&
                    !c.getString(c.getColumnIndex("name")).equals("睡眠")){
                score[weekDay] += c.getFloat(c.getColumnIndex("everyday_invest"));
            }
        }
    }



    private static int getWeekDay(String date) {
        int year = Integer.parseInt(date.split("-")[0]);
        int month = Integer.parseInt(date.split("-")[1]);
        int day = Integer.parseInt(date.split("-")[2]);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month-1);
        calendar.set(Calendar.DATE,day);
        return calendar.get(Calendar.DAY_OF_WEEK)-1;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * 设置X 轴的显示
     */
    private void getAxisLables(){
        for (int i = 0; i < weekday.length; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(weekday[i]));
        }
    }
    /**
     * 图表的每个点的显示
     */
    private static void getAxisPoints(){
        mPointValues.clear();
        for (int i = 0; i < score.length; i++) {
            mPointValues.add(new PointValue(i, score[i]));
        }
    }

    private static void initLineChart(){
        Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色（橙色）

        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(false);//曲线是否平滑，即是曲线还是折线
        line.setFilled(false);//是否填充曲线的面积
        line.setHasLabels(true);//曲线的数据坐标是否加上备注
        line.setHasLabelsOnlyForSelected(false);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(false);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.BLACK);  //设置字体颜色
        //axisX.setName("date");  //表格名称
        axisX.setTextSize(6);//设置字体大小
        axisX.setMaxLabelChars(7); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
        //data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线

        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        Axis axisY = new Axis().setHasLines(true);  //Y轴
        axisY.setName("");//y轴标注
        axisY.setMaxLabelChars(6);
        List<AxisValue> yList = new ArrayList<>();
        axisY.setTextSize(10);//设置字体大小
        for(int i=0;i<=24;i++){
            AxisValue value = new AxisValue(i);
            value.setLabel(Integer.toString(i));
            yList.add(value);
        }
        axisY.setValues(yList);
       // data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边


        //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(false);
//        lineChart.setZoomType(ZoomType.HORIZONTAL);
//        lineChart.setMaxZoom((float) 2);//最大方法比例
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);

        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right= 7;
        lineChart.setCurrentViewport(v);

    }


//完成时删除
    private void  initDatabase(){


        SQLiteDatabase writeDB = db.getWritableDatabase();
        writeDB.delete("goal",null,null);
        writeDB.delete("summary",null,null);
        ContentValues contentValues = new ContentValues();
        contentValues.put("name","看书");
        contentValues.put("date","2018-3-31");
        contentValues.put("everyday_invest","300");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("date","2018-3-31");
        contentValues.put("summary","今天不错！");
        writeDB.insert("summary",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","浪费");
        contentValues.put("date","2018-3-31");
        contentValues.put("everyday_invest","200");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","未知");
        contentValues.put("date","2018-3-31");
        contentValues.put("everyday_invest","500");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","固定");
        contentValues.put("date","2018-3-31");
        contentValues.put("everyday_invest","400");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","背单词");
        contentValues.put("date","2018-3-31");
        contentValues.put("everyday_invest","600");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","睡眠");
        contentValues.put("date","2018-3-31");
        contentValues.put("everyday_invest","1000");
        writeDB.insert("goal",null,contentValues);//3-31

        contentValues = new ContentValues();
        contentValues.put("name","编程");
        contentValues.put("date","2018-3-29");
        contentValues.put("everyday_invest","500");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("date","2018-3-29");
        contentValues.put("summary","今天不好！");
        writeDB.insert("summary",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","浪费");
        contentValues.put("date","2018-3-29");
        contentValues.put("everyday_invest","800");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","未知");
        contentValues.put("date","2018-3-29");
        contentValues.put("everyday_invest","0");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","固定");
        contentValues.put("date","2018-3-29");
        contentValues.put("everyday_invest","600");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","咕咕咕");
        contentValues.put("date","2018-3-29");
        contentValues.put("everyday_invest","200");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","睡眠");
        contentValues.put("date","2018-3-29");
        contentValues.put("everyday_invest","1200");
        writeDB.insert("goal",null,contentValues);//3-29

        contentValues = new ContentValues();
        contentValues.put("name","编程");
        contentValues.put("date","2018-4-1");
        contentValues.put("everyday_invest","700");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("date","2018-4-1");
        contentValues.put("summary","今天不好！");
        writeDB.insert("summary",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","浪费");
        contentValues.put("date","2018-4-1");
        contentValues.put("everyday_invest","500");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","未知");
        contentValues.put("date","2018-4-1");
        contentValues.put("everyday_invest","0");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","固定");
        contentValues.put("date","2018-4-1");
        contentValues.put("everyday_invest","600");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","咕咕咕");
        contentValues.put("date","2018-4-1");
        contentValues.put("everyday_invest","1200");
        writeDB.insert("goal",null,contentValues);
        contentValues = new ContentValues();
        contentValues.put("name","睡眠");
        contentValues.put("date","2018-4-1");
        contentValues.put("everyday_invest","1200");
        writeDB.insert("goal",null,contentValues);//4.1

        writeDB.close();



    }
    public static void refresh(){
        String today = new SimpleDateFormat("yyyy-M-d").format(new Date(System.currentTimeMillis()));
        setWeekData(today);
        float total = getInvestSum();
        float avg = Float.parseFloat(new DecimalFormat("#.00").format(total/7.0));
        TextView totalInput = (TextView)root.findViewById(R.id.totalInput);
        TextView avgInput =(TextView)root.findViewById(R.id.avrInput);
        totalInput.setText(total+"h");
        avgInput.setText(avg+"h");
        getAxisPoints();//
        initLineChart();//
    }
}