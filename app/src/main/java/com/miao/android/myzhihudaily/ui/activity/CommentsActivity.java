package com.miao.android.myzhihudaily.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.miao.android.myzhihudaily.R;
import com.miao.android.myzhihudaily.adapter.CommentsAdapter;
import com.miao.android.myzhihudaily.bean.Comment;
import com.miao.android.myzhihudaily.util.Api;
import com.miao.android.myzhihudaily.util.UtilFunctions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/4.
 */
public class CommentsActivity extends AppCompatActivity {

    private RecyclerView rvComments;
    private List<Comment> list = new ArrayList<Comment>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        UtilFunctions.setTheme(CommentsActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        initViews();

        String id = getIntent().getStringExtra("id");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                Api.COMMENTS + id + "/" + "long-comments", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    JSONArray array = jsonObject.getJSONArray("comments");
                    if (array.length()!=0) {
                        for (int i = array.length() - 1; i >= 0; i--) {
                            JSONObject object = array.getJSONObject(i);
                            Comment item = new Comment(object.getString("avatar"),
                                    object.getString("author"),
                                    object.getString("content"),
                                    object.getString("time"));
                            list.add(item);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        Volley.newRequestQueue(CommentsActivity.this.getApplicationContext()).add(request);

        JsonObjectRequest otherRequest = new JsonObjectRequest(Request.Method.GET,
                Api.COMMENTS + id + "/" + "short-comments", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    JSONArray array = jsonObject.getJSONArray("comments");
                    if (array.length() != 0) {
                        for (int j = array.length() - 1; j >= 0; j--) {
                            JSONObject object = array.getJSONObject(j);
                            Comment item = new Comment(object.getString("avatar"),
                                    object.getString("author"),
                                    object.getString("content"),
                                    object.getString("time"));
                            list.add(item);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                CommentsAdapter adapter = new CommentsAdapter(CommentsActivity.this, list);
                rvComments.setAdapter(adapter);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        Volley.newRequestQueue(CommentsActivity.this.getApplicationContext()).add(otherRequest);
    }

    private void initViews() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        rvComments = (RecyclerView) findViewById(R.id.rv_comments);
        rvComments.setLayoutManager(new LinearLayoutManager(CommentsActivity.this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
