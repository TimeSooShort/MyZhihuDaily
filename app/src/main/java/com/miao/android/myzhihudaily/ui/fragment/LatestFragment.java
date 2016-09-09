package com.miao.android.myzhihudaily.ui.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.miao.android.myzhihudaily.R;
import com.miao.android.myzhihudaily.adapter.LatestPostAdapter;
import com.miao.android.myzhihudaily.bean.LatestPost;
import com.miao.android.myzhihudaily.db.DatabaseHelper;
import com.miao.android.myzhihudaily.interfaces.OnRecyclerViewOnClickListener;
import com.miao.android.myzhihudaily.ui.activity.ZhihuReadActivity;
import com.miao.android.myzhihudaily.util.Api;
import com.miao.android.myzhihudaily.util.DividerItemDecoration;
import com.miao.android.myzhihudaily.util.NetworkState;
import com.rey.material.app.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/8/28.
 */
public class LatestFragment extends Fragment {

    private static final String TAG = "LatestFragment";
    private RecyclerView rvLatestNews;
    private LinearLayoutManager linearLayoutManage;
    private SwipeRefreshLayout refresh;
    private FloatingActionButton fab;
    private List<LatestPost> list = new ArrayList<LatestPost>();
    private SQLiteDatabase db;
    private RequestQueue queue;
    private DatabaseHelper dbHelper;
    private LatestPostAdapter adapter;
    private SharedPreferences sp;

    // 2013.5.20是知乎日报api首次上线
    private int year = 2013;
    private int month = 5;
    private int day = 20;

    // 用于记录加载更多的次数
    private int groupCount = -1;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        linearLayoutManage = new LinearLayoutManager(getActivity());
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        dbHelper = new DatabaseHelper(getActivity(), "History.db", null, 1);
        db = dbHelper.getWritableDatabase();

        sp = getActivity().getSharedPreferences("user_settings", Context.MODE_PRIVATE);

        deleteTimeoutPosts();

        // 获取当前日期的前一天
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,-1);

        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_latest, container, false);

        initViews(view);

        if (!NetworkState.networkConnected(getActivity())) {
            showNoNetwork();
            loadFromDB();
        }else {
            load(null);
        }

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!list.isEmpty()) {
                    list.clear();
                }
                adapter.notifyDataSetChanged();

                if (!NetworkState.networkConnected(getActivity())) {
                    showNoNetwork();
                    loadFromDB();
                }else {
                    load(null);
                }

                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_MONTH, -1);

                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);

                groupCount = -1;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final DatePickerDialog dialog = new DatePickerDialog(getActivity());

                // 给dialog设置初始日期,即默认被选中的日期
                dialog.date(day, month, year);

                Calendar calendar = Calendar.getInstance();
                // 最小日期设置为2013年5月20日，知乎日报的诞生日期为2013年5月19日，
                // 如果传入的日期小于19，那么将会出现错误
                calendar.set(2013,5,20);

                // 通过calendar给dialog设置最大和最小日期
                // 其中最大日期为当前日期的前一天
                dialog.dateRange(calendar.getTimeInMillis(), Calendar.getInstance()
                        .getTimeInMillis() - 24*60*60*1000);
                dialog.show();

                dialog.positiveAction(R.string.positive);
                dialog.negativeAction(R.string.negative);

                dialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        year = dialog.getYear();
                        month = dialog.getMonth();
                        day = dialog.getDay();

                        load(parseDate(dialog.getDate()));

                        dialog.dismiss();
                    }
                });

                dialog.negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });

        rvLatestNews.setOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isSlidingToLast = false;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = manager.getItemCount();

                    if (lastVisibleItem == (totalItemCount - 1) && isSlidingToLast) {
                        loadMore();
                    }
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                isSlidingToLast = dy > 0;
            }
        });

        return view;
    }


    private void load(final String date) {

        refresh.post(new Runnable() {
            @Override
            public void run() {
                refresh.setRefreshing(true);
            }
        });

        String url;
        if (date == null) {
            url = Api.LATEST;
        }else {
            url = Api.HISTORY + date;
        }

        if (!list.isEmpty()) {
            list.clear();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (!jsonObject.getString("date").isEmpty()) {
                        JSONArray array = jsonObject.getJSONArray("stories");
                        for (int i = 0; i < array.length(); i++) {
                            JSONArray images = array.getJSONObject(i).getJSONArray("images");
                            String type = array.getJSONObject(i).getString("type");
                            String title = array.getJSONObject(i).getString("title");
                            String id = array.getJSONObject(i).getString("id");
                            List<String> imageUrlList = new ArrayList<String>();
                            for (int j = 0; j < images.length(); j++) {
                                String imageUrl = images.getString(j);
                                imageUrlList.add(imageUrl);
                            }
                            LatestPost item = new LatestPost(title, imageUrlList, type, id);
                            list.add(item);
                            if (!queryIDExists("LatestPosts", id)) {
                                ContentValues values = new ContentValues();
                                values.put("id", Integer.valueOf(id));
                                values.put("title", title);
                                values.put("type", Integer.valueOf(type));
                                values.put("img_url", imageUrlList.get(0));

                                if (date == null) {
                                    String d = jsonObject.getString("date");
                                    values.put("date", Integer.valueOf(d));
                                    storeContent(id, d);
                                } else {
                                    values.put("date", Integer.valueOf(date));
                                    storeContent(id, date);
                                }

                                db.insert("LatestPosts", null, values);
                                values.clear();
                            }
                        }
                    }

                    adapter = new LatestPostAdapter(getActivity(), list);
                    rvLatestNews.setAdapter(adapter);
                    adapter.setItemClickListener(new OnRecyclerViewOnClickListener() {
                        @Override
                        public void OnItemClick(View v, int position) {
                            Intent intent = new Intent(getActivity(), ZhihuReadActivity.class);
                            intent.putExtra("id", list.get(position).getId());
                            intent.putExtra("title", list.get(position).getTitle());
                            startActivity(intent);
                        }
                    });

                    if (refresh.isRefreshing()) {
                        refresh.post(new Runnable() {
                            @Override
                            public void run() {
                                refresh.setRefreshing(false);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (refresh.isRefreshing()) {
                    Snackbar.make(fab, R.string.wrong_process,Snackbar.LENGTH_SHORT).show();
                    refresh.setRefreshing(false);
                }
            }
        });
        request.setTag(TAG);
        queue.add(request);
    }

    private boolean queryIDExists(String tabName, String id) {
        Cursor cursor = db.query(tabName,null,null,null,null,null,null);
        if (cursor.moveToFirst()){
            do {
                if (id.equals(String.valueOf(cursor.getInt(cursor.getColumnIndex("id"))))) {
                    return true;
                }
            }while (cursor.moveToNext());
        }
        cursor.close();
        return false;
    }

    private void storeContent(final String id, final String date) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Api.NEWS + id,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        if (queryIDExists("LatestPosts", id)) {
                            ContentValues values = new ContentValues();

                            try {
                                if (jsonObject.isNull("body")) {
                                    values.put("id", Integer.valueOf(id));
                                    values.put("content", jsonObject.getString("body"));
                                    values.put("date", Integer.valueOf(date));
                                    db.insert("Contents", null, values);
                                    values.clear();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "onErrorResponse: Failed", volleyError);
            }
        });

        request.setTag(TAG);
        queue.add(request);
    }

    private void initViews(View view) {
        rvLatestNews = (RecyclerView) view.findViewById(R.id.rv_main);
        rvLatestNews.setLayoutManager(linearLayoutManage);
        rvLatestNews.addItemDecoration(new DividerItemDecoration(getActivity(),
                LinearLayoutManager.VERTICAL));

        refresh = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setRippleColor(getResources().getColor(R.color.colorPrimaryDark));

        //设置下拉刷新的按钮的颜色
        refresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //设置手指在屏幕上下拉多少距离开始刷新
        refresh.setDistanceToTriggerSync(300);
        //设置下拉刷新按钮的背景颜色
        refresh.setProgressBackgroundColorSchemeColor(Color.WHITE);
        //设置下拉刷新按钮的大小
        refresh.setSize(SwipeRefreshLayout.DEFAULT);
    }

    public void showNoNetwork() {
        Snackbar.make(fab, R.string.no_network_connected, Snackbar.LENGTH_SHORT)
                .setAction(R.string.go_to_set, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                }).show();
    }

    /**
     * 从数据库中加载已经保存的数据
     */
    private void loadFromDB() {

        refresh.post(new Runnable() {
            @Override
            public void run() {
                refresh.setRefreshing(true);
            }
        });

        Cursor cursor = db.query("LatestPosts", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndex("title"));
                List<String> list = new ArrayList<String>();
                list.add(cursor.getString(cursor.getColumnIndex("img_url")));
                String id = String.valueOf(cursor.getInt(cursor.getColumnIndex("id")));
                String type = String.valueOf(cursor.getInt(cursor.getColumnIndex("type")));

                if ((title != null) && (list.get(0) != null) && (!id.equals("")) && (!type.equals(""))) {
                    LatestPost item = new LatestPost(title, list, type, id);
                    this.list.add(item);
                }
            }while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new LatestPostAdapter(getActivity(), list);
        rvLatestNews.setAdapter(adapter);
        adapter.setItemClickListener(new OnRecyclerViewOnClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                Intent intent = new Intent(getActivity(), ZhihuReadActivity.class);
                intent.putExtra("id", list.get(position).getId());
                intent.putExtra("title", list.get(position).getTitle());
                startActivity(intent);
            }
        });
        refresh.post(new Runnable() {
            @Override
            public void run() {
                refresh.setRefreshing(false);
            }
        });
    }

    /**
     * 将long类date转换为String类型
     * @param date date
     * @return String date
     */
    private String parseDate(long date) {
        String sDate;
        Date d = new Date(date + 24*60*60*1000);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        sDate = format.format(d);

        return sDate;
    }

    private void loadMore() {

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date d = new Date(year-1900, month, day - groupCount);
        final String date = format.format(d);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Api.HISTORY + date,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {

                        try {
                            if (!jsonObject.getString("date").isEmpty()) {
                                JSONArray array = jsonObject.getJSONArray("stories");

                                for (int i = 0; i < array.length(); i++) {
                                    JSONArray images = array.getJSONObject(i).getJSONArray("images");
                                    String id = array.getJSONObject(i).getString("id");
                                    String type = array.getJSONObject(i).getString("type");
                                    String title = array.getJSONObject(i).getString("title");
                                    List<String> stringList = new ArrayList<String>();
                                    for (int j = 0; j < images.length(); j++) {
                                        String imgUrl = images.getString(j);
                                        stringList.add(imgUrl);
                                    }

                                    LatestPost item = new LatestPost(title, stringList, type, id);

                                    list.add(item);

                                    if (!queryIDExists("LatestPosts", id)) {
                                        ContentValues values = new ContentValues();
                                        values.put("id", Integer.valueOf(id));
                                        values.put("title", title);
                                        values.put("type", Integer.valueOf(type));
                                        values.put("img_url", stringList.get(0));

                                        if (date == null) {
                                            String d = jsonObject.getString("date");
                                            values.put("date", Integer.valueOf(d));
                                            storeContent(id, d);
                                        } else {
                                            values.put("date", Integer.valueOf(date));
                                            storeContent(id, date);
                                        }

                                        db.insert("LatestPosts", null, values);

                                        values.clear();
                                    }
                                }
                            }

                            adapter.notifyDataSetChanged();

                            groupCount++;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (refresh.isRefreshing()) {
                    Snackbar.make(fab, R.string.wrong_process,Snackbar.LENGTH_SHORT).show();
                    refresh.post(new Runnable() {
                        @Override
                        public void run() {
                            refresh.setRefreshing(false);
                        }
                    });
                }
            }
        });

        request.setTag(TAG);
        queue.add(request);
    }

    private void deleteTimeoutPosts(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,-2);

        String[] whereArgs = {parseDate(c.getTimeInMillis())};

        db.delete("LatestPosts","date<?",whereArgs);
        db.delete("Contents","date<?",whereArgs);
    }
}
