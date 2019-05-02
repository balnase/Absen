package com.jiuj.absen.SampleCalendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jiuj.absen.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HwAdapter extends BaseAdapter {
    private LinearLayout linearLayout;
    private ArrayList<String> items;
    private GregorianCalendar selectedDate;
    private String gridvalue;
    private ListView listTeachers;
    private ArrayList<Dialogpojo> alCustom=new ArrayList<Dialogpojo>();
    private Activity context;
    private java.util.Calendar month;
    public static List<String> day_string;
    public GregorianCalendar pmonth;
    public GregorianCalendar pmonthmaxset;
    public ArrayList<HomeCollection>  date_collection_arr;
    int firstDay;
    int maxWeeknumber;
    int maxP;
    int calMaxP;
    int mnthlength;
    DateFormat df;
    TextView eventDay;
    TextView tvEvents;
    String itemvalue, curentDateString, formattedDate, dateToday ;

    public HwAdapter(Activity context, GregorianCalendar monthCalendar,ArrayList<HomeCollection> date_collection_arr) {
        this.date_collection_arr=date_collection_arr;
        HwAdapter.day_string = new ArrayList<String>();
        Locale.setDefault(Locale.US);
        month = monthCalendar;
        selectedDate = (GregorianCalendar) monthCalendar.clone();
        this.context = context;
        month.set(GregorianCalendar.DAY_OF_MONTH, 1);
        this.items = new ArrayList<String>();
        df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        formattedDate = df.format(c);
        curentDateString = df.format(selectedDate.getTime());
        refreshDays();
    }

    public int getCount() {
        return day_string.size();
    }

    public Object getItem(int position) {
        return day_string.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        TextView dayView;
        if (convertView == null) { // if it's not recycled, initialize some
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.cal_item, null);
        }

        linearLayout = (LinearLayout) v.findViewById(R.id.linMain);
        /*
        tvEvents = new TextView(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams
                ((int) LinearLayout.LayoutParams.WRAP_CONTENT,(int) LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,-5,0,0);
        tvEvents.setLayoutParams(lp);
        tvEvents.setGravity(Gravity.CENTER);
        tvEvents.setBackgroundResource(R.drawable.bg_holiday);
        tvEvents.setTextColor(context.getResources().getColor(R.color.colorWhite));
        tvEvents.setTextSize(6);
        tvEvents.setVisibility(View.VISIBLE);
        linearLayout.addView(tvEvents);
        */
        dayView = (TextView) v.findViewById(R.id.date);
        eventDay = (TextView) v.findViewById(R.id.date2);
        String[] separatedTime = day_string.get(position).split("-");
        gridvalue = separatedTime[2].replaceFirst("^0*", "");
        if ((Integer.parseInt(gridvalue) > 1) && (position < firstDay)) {
            dayView.setTextColor(Color.parseColor("#A9A9A9"));
            dayView.setClickable(false);
            dayView.setFocusable(false);
        } else if ((Integer.parseInt(gridvalue) < 7) && (position > 28)) {
            dayView.setTextColor(Color.parseColor("#A9A9A9"));
            dayView.setClickable(false);
            dayView.setFocusable(false);
        } else {
            dayView.setTextColor(Color.parseColor("#696969"));
        }

        if (day_string.get(position).equals(formattedDate)) {
            v.setBackgroundColor(Color.parseColor("#ff5ccc78"));
            v.setBackgroundResource(R.drawable.bg_today);
            //Toast.makeText(context, formattedDate,Toast.LENGTH_LONG).show();
        } else {
            v.setBackgroundColor(Color.parseColor("#ffffff"));
        }
        dayView.setText(gridvalue);
        String date = day_string.get(position);
        if (date.length() == 1) {
            date = "0" + date;
        }
        String monthStr = "" + (month.get(GregorianCalendar.MONTH) + 1);
        if (monthStr.length() == 1) {
            monthStr = "0" + monthStr;
        }
        setEventView(v, position,dayView);
        return v;
    }

    public void refreshDays() {
        items.clear();
        day_string.clear();
        Locale.setDefault(Locale.US);
        pmonth = (GregorianCalendar) month.clone();
        firstDay = month.get(GregorianCalendar.DAY_OF_WEEK);
        maxWeeknumber = month.getActualMaximum(GregorianCalendar.WEEK_OF_MONTH);
        mnthlength = maxWeeknumber * 7;
        maxP = getMaxP(); // previous month maximum day 31,30....
        calMaxP = maxP - (firstDay - 1);// calendar offday starting 24,25 ...
        pmonthmaxset = (GregorianCalendar) pmonth.clone();
        pmonthmaxset.set(GregorianCalendar.DAY_OF_MONTH, calMaxP + 1);
        for (int n = 0; n < mnthlength; n++) {
            itemvalue = df.format(pmonthmaxset.getTime());
            pmonthmaxset.add(GregorianCalendar.DATE, 1);
            day_string.add(itemvalue);
        }
    }

    private int getMaxP() {
        int maxP;
        if (month.get(GregorianCalendar.MONTH) == month.getActualMinimum(GregorianCalendar.MONTH)) {
            pmonth.set((month.get(GregorianCalendar.YEAR) - 1), month.getActualMaximum(GregorianCalendar.MONTH), 1);
        } else {
            pmonth.set(GregorianCalendar.MONTH, month.get(GregorianCalendar.MONTH) - 1);
        }
        maxP = pmonth.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        return maxP;
    }

    public void setEventView(View v,int pos,TextView txt){
        int len=HomeCollection.date_collection_arr.size();
        for (int i = 0; i < len; i++) {
            HomeCollection cal_obj=HomeCollection.date_collection_arr.get(i);
            String date=cal_obj.date;
            int len1=day_string.size();
            if (len1>pos) {
                if (day_string.get(pos).equals(date)) {
                    if ((Integer.parseInt(gridvalue) > 1) && (pos < firstDay)) {
                    } else if ((Integer.parseInt(gridvalue) < 7) && (pos > 28)) {
                    } else {
                        //v.setBackgroundColor(Color.parseColor("#F71B05"));
                        if("Attendance".equalsIgnoreCase(cal_obj.description)){
                            eventDay.setText(cal_obj.description);
                            v.setBackgroundResource(R.drawable.bg_attendance);
                            txt.setTextColor(Color.parseColor("#ffffff"));
                        }else{
                            eventDay.setText(cal_obj.name);
                            v.setBackgroundResource(R.drawable.bg_holiday);
                            txt.setTextColor(Color.parseColor("#ffffff"));
                        }

                        //txt.setTextColor(Color.parseColor("#696969"));
                        //txt.setTextColor(Color.parseColor("#ffffff"));
                    }
                }
            }}
    }

    public void getPositionList(String date,final Activity act){
        int len= HomeCollection.date_collection_arr.size();
        JSONArray jbarrays=new JSONArray();
        for (int j=0; j<len; j++){
            if (HomeCollection.date_collection_arr.get(j).date.equals(date)){
                HashMap<String, String> maplist = new HashMap<String, String>();
                maplist.put("hnames",HomeCollection.date_collection_arr.get(j).name);
                maplist.put("hsubject",HomeCollection.date_collection_arr.get(j).subject);
                maplist.put("descript",HomeCollection.date_collection_arr.get(j).description);
                JSONObject json1 = new JSONObject(maplist);
                jbarrays.put(json1);
            }
        }
        if (jbarrays.length()!=0) {
            final Dialog dialogs = new Dialog(context);
            dialogs.setContentView(R.layout.dialog_inform);
            listTeachers = (ListView) dialogs.findViewById(R.id.list_teachers);
            ImageView imgCross = (ImageView) dialogs.findViewById(R.id.img_cross);
            listTeachers.setAdapter(new DialogAdaptorStudent(context, getMatchList(jbarrays + "")));
            imgCross.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogs.dismiss();
                }
            });
            dialogs.show();
        }
    }

    private ArrayList<Dialogpojo> getMatchList(String detail) {
        try {
            JSONArray jsonArray = new JSONArray(detail);
            alCustom = new ArrayList<Dialogpojo>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                Dialogpojo pojo = new Dialogpojo();
                pojo.setTitles(jsonObject.optString("hnames"));
                pojo.setSubjects(jsonObject.optString("hsubject"));
                pojo.setDescripts(jsonObject.optString("descript"));
                alCustom.add(pojo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return alCustom;
    }
}

