package com.example.myalarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final Button set_alarm = findViewById(R.id.set_alarm);
        set_alarm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGetReq();
            }
        });
    }

    private void sendGetReq() {
        ZonedDateTime today = ZonedDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
        final String start__gte = dtf.format(today);
        final String start__lt = dtf.format(today.plusDays(1));
        final String api_key = "3d02cd67d97f6f64f5a4f2ba970d562f9ec722ec";
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("clist.by")
                .appendPath("api")
                .appendPath("v1")
                .appendPath("json")
                .appendPath("contest")
                .appendQueryParameter("start__gte", start__gte)
                .appendQueryParameter("start__lt", start__lt)
                .appendQueryParameter("username", "idc")
                .appendQueryParameter("api_key", api_key);
        final String url = builder.build().toString();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray contests = response.getJSONArray("objects");
                            TextView textView = findViewById(R.id.textView);
                            textView.setText("");
                            for(int i = 0; i < contests.length(); ++i){
                                JSONObject contest = contests.getJSONObject(i);
                                final String href = contest.getString("href");
                                final String start = contest.getString("start") + "+00:00";
                                textView.append(contest.getString("event") + "\n");
                                textView.append(href + "\n");
                                ZonedDateTime zdt = ZonedDateTime.parse(start).withZoneSameInstant(ZoneId.systemDefault());
                                textView.append(zdt.toString() + "\n\n");
                                System.out.println(zdt.toString());
                                Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
                                alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
                                alarmIntent.putExtra(AlarmClock.EXTRA_MESSAGE, href);
                                alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, zdt.getHour());
                                alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, zdt.getMinute());
                                startActivity(alarmIntent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(req);
    }
}
