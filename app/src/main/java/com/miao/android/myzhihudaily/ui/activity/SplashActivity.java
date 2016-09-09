package com.miao.android.myzhihudaily.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.miao.android.myzhihudaily.R;
import com.miao.android.myzhihudaily.util.Api;
import com.miao.android.myzhihudaily.util.NetworkState;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/9/8.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private SharedPreferences sp;
    private RequestQueue queue;
    private ImageView ivWelcome;
    private TextView tvWelcomeName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = getSharedPreferences("user_settings", MODE_PRIVATE);

        if (sp.getBoolean("load_splash", false)) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else {
            queue = Volley.newRequestQueue(getApplicationContext());
            setContentView(R.layout.activity_splash);
            initViews();

            if (NetworkState.networkConnected(SplashActivity.this)) {
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Api.START_IMAGE,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                try {
                                    if (jsonObject.getString("img").isEmpty() || jsonObject.isNull("img")) {
                                        ivWelcome.setImageResource(R.drawable.welcome);
                                        tvWelcomeName.setText(R.string.welcome_to_zhihuDaily);
                                    } else {
                                        Glide.with(SplashActivity.this)
                                                .load(jsonObject.getString("img"))
                                                .asBitmap()
                                                .error(R.drawable.no_img)
                                                .into(ivWelcome);
                                        tvWelcomeName.setText(jsonObject.getString("text"));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        ivWelcome.setImageResource(R.drawable.welcome);
                        tvWelcomeName.setText(R.string.welcome_to_zhihuDaily);
                    }
                });
                request.setTag(TAG);
                queue.add(request);
            }else {
                ivWelcome.setImageResource(R.drawable.welcome);
                tvWelcomeName.setText(R.string.welcome_to_zhihuDaily);
            }

            final Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            Timer timer = new Timer();
            TimerTask timeTask = new TimerTask() {
                @Override
                public void run() {
                    startActivity(intent);
                }
            };
            timer.schedule(timeTask, 1000*3);
        }
    }

    private void initViews() {
        ivWelcome = (ImageView) findViewById(R.id.iv_welcome);
        tvWelcomeName = (TextView) findViewById(R.id.tv_welcome_name);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (queue != null){
            queue.cancelAll(TAG);
        }
    }
}
