package com.example.time;

import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;

public class FirstFragment extends Fragment {


    TextView textView;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }




    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);*/


                getRestaurantState(new Listener<String>(){
                    public void on(String result){
                        Looper.prepare();


                        ((TextView)getParentFragment().getView().findViewById(R.id.text_first)).setText(result);
/*
                       Toast toast = Toast. makeText(getContext(), result, Toast. LENGTH_LONG);
                        toast.setDuration(1000);
                        toast.show();
*/


                    }

                });
            }
        });
    }

    public interface Listener<T> {
        void on(T arg);
    }

    public void getRestaurantState(final Listener<String> onCompleteListener) {
        final String urlAddress="https://www.supaupload.co.uk/foodapp/api/getrestaurant";
        final String strRestaurantId="2";
        final StringBuilder sb = new StringBuilder();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int weekday = 0;
                String msg ="None";
                try {
                    URL url = new URL(urlAddress);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setRequestProperty("restaurantid", strRestaurantId);
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.connect();



                    int HttpResult =conn.getResponseCode();
                    if(HttpResult ==HttpURLConnection.HTTP_OK){
                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                conn.getInputStream(),"utf-8"));
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();

                        System.out.println(""+sb.toString());

                    }else{
                        System.out.println(conn.getResponseMessage());
                    }
                    JSONObject myObject = new JSONObject(sb.toString());
                    JSONArray weekarray = myObject.getJSONObject("result").getJSONArray("week");

                    Calendar calendar = Calendar.getInstance();
                    weekday = (calendar.get(Calendar.DAY_OF_WEEK)-1)%7-1;
                    if (weekday == -1)
                        weekday = 6;
                    JSONObject weekdayobj = weekarray.getJSONObject(weekday);
                    JSONObject nextweekdayobj = weekarray.getJSONObject((weekday+1)%7);
                    JSONObject beforeweekdayobj = weekarray.getJSONObject((weekday-1)<0?6:weekday-1);
                    String strStartTime = weekdayobj.getString("start_time");
                    String strEndTime = weekdayobj.getString("end_time");
                    String strNextStartTime=nextweekdayobj.getString("start_time");
                    String strBeforeEndTime=beforeweekdayobj.getString("end_time");
                    String strBeforeStartTime=beforeweekdayobj.getString("start_time");
                    SimpleDateFormat format = new SimpleDateFormat("hh:mm aa");

                    try {
                        Date date1 = Calendar.getInstance().getTime();
                        Date date2 = Calendar.getInstance().getTime();
                        Date date3 = Calendar.getInstance().getTime();
                        Date date4 = Calendar.getInstance().getTime();
                        date1 = format.parse(strStartTime);
                        date2 = format.parse(strEndTime);
                        date3 = format.parse(strBeforeEndTime);
                        date4 = format.parse(strBeforeStartTime);
                        Calendar c = Calendar.getInstance();
                        if (date2.before(date1))
                        {
                            c.setTime(date2);
                            c.add(Calendar.DAY_OF_YEAR, 1);


                            date2 = c.getTime();
                        }

                        Date currentTime = Calendar.getInstance().getTime();
                        currentTime.setYear(70);
                        currentTime.setMonth(0);
                        currentTime.setDate(1);

                        if ((currentTime.after(date1) &&  currentTime.before(date2)) || (currentTime.before(date3) && date3.before(date4))) //end < start

                        {
                            onCompleteListener.on("Restaurant is opened");

                        }
                        else {
                            onCompleteListener.on("Restaurant is closed. Will be opened at " + strNextStartTime);
                        }


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

}