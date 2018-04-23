package com.example.time;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import org.w3c.dom.Text;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.aigestudio.datepicker.cons.DPMode;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EverydayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EverydayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EverydayFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private static int[] colors = {0xFFFF8C69, 0xFF6495ED, 0xFFE32636, 0xFF800000, 0xFF808000}; //各个区域的颜色

    private static String[] areaNames = {"学习","浪费","固定","未知","睡眠"}; //区域名称

    private static float[] areaTime = {0,0,0,0,0}; //各区域时间，测试用

    private static ArrayList<PieData> pieData = new ArrayList<>();

    private ImageView popTextArea;
    private ImageView popDatepicker;
    static View root;
    static MyDatabaseHelper db=MainActivity.dbHelper;

    public EverydayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EverydayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EverydayFragment newInstance(String param1, String param2) {
        EverydayFragment fragment = new EverydayFragment();
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_everyday,container,false);

//        initDatabase();//完成时删除即可！！
        final String today = new SimpleDateFormat("yyyy-M-d").format(new Date(System.currentTimeMillis())).toString();
        System.out.println(today);
        //得到总结
        String s = getSummary(today);
        TextView tv = (TextView)root.findViewById(R.id.summaryText);
        if (s!=null) tv.setText("总结:"+s);
        else tv.setText("");
        //
        setData(today);
        final PieChart pieChart =(PieChart) root.findViewById(R.id.pieChart);
        pieChart.setCenterText("");
        pieChart.setPieData(pieData);
        //扇形图有关处理完毕
        final TextView dateTv = (TextView)root.findViewById(R.id.dateText);
        dateTv.setText(new SimpleDateFormat("yyyy年M月d日").format(new Date(System.currentTimeMillis())).toString()+"时间分配");

        //处理左上角和右上角
        popTextArea = (ImageView)root.findViewById(R.id.popTextArea);
        popDatepicker = (ImageView)root.findViewById(R.id.popDatepicker);
        popTextArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popTextArea.setBackgroundColor(0x303F9F);
                String d;
                d = dateTv.getText().toString().split("年")[0]+"-"+dateTv.getText().toString().split("年")[1].split("月")[0]+"-"+dateTv.getText().toString().split("年")[1].split("月")[1].split("日")[0];
                if (d.equals(today)){
                    final View summaryView = inflater.inflate(R.layout.summary_view,null);

                    //创建summary输入框
                    final AlertDialog inputSummary = new AlertDialog.Builder(root.getContext()).setView(summaryView).create();
                    //处理summaryView的事件
                    //左上角内总结载入
                    final TextView summaryText = (TextView)root.findViewById(R.id.summaryText);

                    final EditText summaryEditText =(EditText) summaryView.findViewById(R.id.summaryEditText);
                    if (summaryText.getText().toString()!=null &&!summaryText.getText().toString().equals("") ) summaryEditText.setText(summaryText.getText().toString().split("总结:")[1]);

                    //取消时返回
                    summaryView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            inputSummary.dismiss();
                        }
                    });
                    //确认时存储值
                    summaryView.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (summaryEditText.getText().toString()!=null && !summaryEditText.getText().toString().equals("") ){
                                summaryText.setText("总结:"+summaryEditText.getText());
                                storeSummary(summaryText.getText().toString().substring(3),today);
                            }
                            else summaryText.setText("");
                            inputSummary.dismiss();
                        }
                    });
                    inputSummary.show();
                }
                else Toast.makeText(root.getContext(),"非今日不能写总结哦~",Toast.LENGTH_SHORT).show();



            }
        });

        popDatepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cn.aigestudio.datepicker.views.DatePicker picker = new cn.aigestudio.datepicker.views.DatePicker(root.getContext());
                final AlertDialog ad = new AlertDialog.Builder(root.getContext()).setView(picker).create();
                picker.setDate(Integer.parseInt(today.split("-")[0]),Integer.parseInt(today.split("-")[1]));
                picker.setMode(DPMode.SINGLE);
                picker.setOnDatePickedListener(new cn.aigestudio.datepicker.views.DatePicker.OnDatePickedListener() {
                    @Override
                    public void onDatePicked(String date) {
                        //读取当日时间分配并刷新
                        if (!setData(date)) {
                            Toast.makeText(root.getContext(),"当日无数据",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            pieChart.setPieData(pieData);
                            System.out.println("I am here"+date);
                            String[] mDate = date.split("-");
                            dateTv.setText(mDate[0]+"年"+mDate[1]+"月"+mDate[2]+"日"+"时间分配");
                            String s;
                            s = getSummary(date);
                            System.out.println(s);
                            TextView tv = (TextView)root.findViewById(R.id.summaryText);
                            if (s!=null) tv.setText("总结:"+s);
                            else tv.setText("");

                        }
                        ad.dismiss();
                    }
                });
                ad.show();
            }
        });

        return root;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
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
        pieData.clear();
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

    private void  initDatabase(){
//        for (int i=0;i<5;i++){
//            PieData pd = new PieData();
//            pd.setName(areaNames[i]);
//            pd.setValue(areaTime[i]);
//            pd.setColor(colors[i]);
//            pieData.add(pd);
//        }

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
        writeDB.insert("goal",null,contentValues);//3-30

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
        writeDB.close();



    }

//    private void changeData(){
//        pieData.clear();
//        for (int i=0;i<5;i++){
//            PieData pd = new PieData();
//            pd.setName(areaNames[i]);
//            pd.setValue(i*2+1);
//            pd.setColor(colors[i]);
//            pieData.add(pd);
//        }
//    }

    //初始化扇形图
    private static boolean setData(String d){
        SQLiteDatabase readDB = db.getReadableDatabase();
        Cursor c = readDB.rawQuery("select name,everyday_invest from goal where date = ?"
                ,new String[]{d});
        if (!c.moveToNext()) {
            return false;
        }
        else {
            float investTime =0;
            float wasteTime =0;
            float fixedTime =0;
            float sleepTime=0;
            c.moveToFirst();
            do {
                switch (c.getString(c.getColumnIndex("name"))){
                    case "浪费":
                        wasteTime += c.getInt(c.getColumnIndex("everyday_invest"));
                        break;
                    case "固定":
                        fixedTime += c.getInt(c.getColumnIndex("everyday_invest"));
                        break;
                    case "睡眠":
                        sleepTime += c.getInt(c.getColumnIndex("everyday_invest"));
                        break;
                    default:
                        investTime += c.getInt(c.getColumnIndex("everyday_invest"));
                        break;
                }
            }while ((c.moveToNext()));
            c.close();
            areaTime = new float[]{investTime*60, wasteTime*60, fixedTime*60, (24-investTime-wasteTime-fixedTime-sleepTime)*60, sleepTime*60};
            pieData.clear();
            for (int i=0;i<5;i++){
                PieData pd = new PieData();
                pd.setName(areaNames[i]);
                pd.setValue(areaTime[i]);
                pd.setColor(colors[i]);
                pieData.add(pd);
            }

        }
        readDB.close();
        return true;
    }

    private String getSummary(String date){
        SQLiteDatabase readDB = db.getReadableDatabase();
        Cursor c = readDB.rawQuery("select summary from summary where date = ?",new String[]{date} );
        if (!c.moveToNext()) {
            c.close();
            readDB.close();
            return null;
        }
        else {
            c.moveToFirst();
            String s =c.getString(c.getColumnIndex("summary"));
            c.getString(c.getColumnIndex("summary"));
            c.close();
            readDB.close();
            return s;
        }
    }

    private void storeSummary(String text,String date){
        SQLiteDatabase readDB = db.getReadableDatabase();
        Cursor c = readDB.rawQuery("select summary from summary where date = ?",new String[]{date} );
        if (c.moveToNext()){
            readDB.execSQL("update summary set summary=? where date=?",new String[]{text,date});
        }
        else {
            readDB.execSQL("insert into summary(date,summary) values(?,?)",new String[]{date,text});
        }
        c.close();
    }

    public static void refresh(){
        final String today = new SimpleDateFormat("yyyy-M-d").format(new Date(System.currentTimeMillis())).toString();
        setData(today);
        final PieChart pieChart =(PieChart) root.findViewById(R.id.pieChart);
        pieChart.setCenterText("");
        pieChart.setPieData(pieData);
    }

}
