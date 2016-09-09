package com.miao.android.myzhihudaily.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.miao.android.myzhihudaily.db.DatabaseHelper;
import com.miao.android.myzhihudaily.util.Api;
import com.miao.android.myzhihudaily.util.NetworkState;
import com.miao.android.myzhihudaily.util.UtilFunctions;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/8/29.
 */
public class ZhihuReadActivity extends AppCompatActivity {

    private WebView webViewRead;
    private FloatingActionButton fab;
    private ImageView ivFirstImg;
    private TextView tvCopyRight;
    private CollapsingToolbarLayout toolbarLayout;
    private SharedPreferences sp;
    private AlertDialog dialog;
    private String id;
    private RequestQueue queue;
    private String shareUrl = null;
    private int likes = 0;
    private int comments = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        if (UtilFunctions.getThemeState(ZhihuReadActivity.this) == 0){
            setTheme(R.style.DayTheme);
        } else {
            setTheme(R.style.NightTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhihu_read);
        initViews();

        sp = getSharedPreferences("user_settings", MODE_PRIVATE);

        dialog = new AlertDialog.Builder(ZhihuReadActivity.this).create();
        dialog.setView(getLayoutInflater().inflate(R.layout.loading_layout, null));
        dialog.show();

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        final String title = intent.getStringExtra("title");
        final String image = intent.getStringExtra("image");

        setCollapsingToolbarLayoutTitle(title);

        queue = Volley.newRequestQueue(getApplicationContext());

        //能够和js交互
        webViewRead.getSettings().setJavaScriptEnabled(true);
        //缩放,设置为不能缩放可以防止页面上出现放大和缩小的图标
        webViewRead.getSettings().setBuiltInZoomControls(false);
        //缓存
        webViewRead.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        //开启DOM storage API功能
        webViewRead.getSettings().setDomStorageEnabled(true);
        //开启application Cache功能
        webViewRead.getSettings().setAppCacheEnabled(false);

        if (sp.getBoolean("in_app_browser",false)) {
            //不调用第三方浏览器即可进行页面反应
            webViewRead.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    webViewRead.loadUrl(url);
                    return true;
                }
            });

            // 设置在本WebView内可以通过按下返回上一个html页面
            webViewRead.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        if (i == KeyEvent.KEYCODE_BACK && webViewRead.canGoBack()) {
                            webViewRead.goBack();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        // 设置是否加载图片，true不加载，false加载图片
        webViewRead.getSettings().setBlockNetworkImage(sp.getBoolean("no_picture_mode", false));

        //添加网络判断代码
        if (!NetworkState.networkConnected(ZhihuReadActivity.this)) {

            ivFirstImg.setImageResource(R.drawable.no_img);
            ivFirstImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

            String parseByTheme = null;
            if (UtilFunctions.getThemeState(ZhihuReadActivity.this) == 0) {
                parseByTheme = "<body>\n";
            }else {
                parseByTheme = "<body style=\"background-color:#212b30\">\n";
            }

            if (loadContentFromDB(id) == null || loadContentFromDB(id).isEmpty()) {
                Snackbar.make(fab, R.string.wrong_process,Snackbar.LENGTH_SHORT).show();
            }else {
                String css = "<link rel=\"stylesheet\" herf=\"file:///android_asset/zhihu.css\" type=\"text/css\">";
                String html = "<DOCTYPE html>\n"
                        + "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                        + "<head>\n"
                        + "\t<meta charset=\"utf-8\" />"
                        + css
                        + "\n</head>\n"
                        + parseByTheme
                        + loadContentFromDB(id).replace("<div class=\"img-place-holder\">", "")
                        + "</body></html>";

                webViewRead.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);

                if (dialog.isShowing()){
                    dialog.dismiss();
                }
            }
        }else {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Api.NEWS + id, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        if (jsonObject.isNull("body")) {
                            webViewRead.loadUrl(jsonObject.getString("share_url"));
                            ivFirstImg.setImageResource(R.drawable.no_img);
                            ivFirstImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

                            shareUrl = jsonObject.getString("share_url");
                        } else {
                            shareUrl = jsonObject.getString("share_url");

                            if (!jsonObject.isNull("image")) {
                                Glide.with(ZhihuReadActivity.this)
                                        .load(jsonObject.getString("image"))
                                        .centerCrop()
                                        .into(ivFirstImg);
                                tvCopyRight.setText(jsonObject.getString("image_source"));
                            } else if (image == null) {
                                ivFirstImg.setImageResource(R.drawable.no_img);
                                ivFirstImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            } else {
                                Glide.with(ZhihuReadActivity.this)
                                        .load(jsonObject.getString("image"))
                                        .into(ivFirstImg);
                            }

                            String css = "<link rel=\"stylesheet\" href=\"file:///android_asset/zhihu.css\" type=\"test/css\">";
                            String content = jsonObject.getString("body").replace("<div class=\"img-place-holder\">", "");
                            content = content.replace("<div class=\"headline\">", "");

                            String parseByTheme = null;
                            if (UtilFunctions.getThemeState(ZhihuReadActivity.this) == 0) {
                                parseByTheme = "<body>\n";
                            } else {
                                parseByTheme = "<body style=\"background-color:#212b30\">\n";
                            }

                            String html = "<DOCTYPE html>\n"
                                    + "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                                    + "<head>\n"
                                    + "\t<meta charset=\"utf-8\" />"
                                    + css
                                    + "\n</head>\n"
                                    + parseByTheme
                                    + content
                                    + "</body></html>";

                            webViewRead.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Snackbar.make(fab, R.string.wrong_process, Snackbar.LENGTH_SHORT).show();

                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            });
            queue.add(request);

            JsonObjectRequest otherRequest = new JsonObjectRequest(Request.Method.GET,
                    Api.STORY_EXTRA + id, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    if (!jsonObject.isNull("comments")) {
                        try {
                            likes = jsonObject.getInt("popularity");
                            comments = jsonObject.getInt("comments");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Snackbar.make(fab, R.string.wrong_process, Snackbar.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Snackbar.make(fab,R.string.wrong_process,Snackbar.LENGTH_SHORT).show();
                }
            });
            queue.add(otherRequest);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent shareIntent = new Intent().setAction(Intent.ACTION_SEND).setType("text/plain");
                    String shareText = title + " " + shareUrl + getString(R.string.share_extra);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)));
                }catch (ActivityNotFoundException e) {
                    Snackbar.make(fab,R.string.wrong_process,Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String loadContentFromDB(String id) {
        String content = null;
        DatabaseHelper dbHelper = new DatabaseHelper(ZhihuReadActivity.this, "History.db", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("Contents", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(cursor.getColumnIndex("id")).equals(id)) {
                    content = cursor.getString(cursor.getColumnIndex("content"));
                }
            }while (cursor.moveToNext());
        }
        cursor.close();

        return content;
    }

    private void setCollapsingToolbarLayoutTitle(String title) {
        toolbarLayout.setTitle(title);
        toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBarPlus1);
        toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBarPlus1);
    }

    private void initViews() {
        webViewRead = (WebView) findViewById(R.id.web_read);
        webViewRead.setScrollbarFadingEnabled(true);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ivFirstImg = (ImageView) findViewById(R.id.head_img);
        tvCopyRight = (TextView) findViewById(R.id.tv_copyright);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_zhihu_read, menu);
        return true;
    }

    // 在onPrepareOptionsMenu方法中更新数据
    // 如果是在create方法中更新，那么只会创建一次，可能获取不到最新的数据
    // 而在这个方法中去更新，可以保证每次都是最新的
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        String temp = getResources().getString(R.string.likes) + ":" + likes;
        menu.findItem(R.id.action_like).setTitle(temp);
        temp = getResources().getString(R.string.comments) + ":" + comments;
        menu.findItem(R.id.action_comments).setTitle(temp);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        if (id == R.id.action_comments) {
            startActivity(new Intent(ZhihuReadActivity.this, CommentsActivity.class).putExtra("id", this.id));
        }

        if (id == R.id.action_open_in_browser) {
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Api.ZHIHU_DAILY_BASE_URL + this.id)));
        }
        return super.onOptionsItemSelected(item);
    }
}
