package com.miao.android.myzhihudaily.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.miao.android.myzhihudaily.R;
import com.miao.android.myzhihudaily.adapter.ThemePostAdapter;
import com.miao.android.myzhihudaily.bean.ThemePost;
import com.miao.android.myzhihudaily.interfaces.OnRecyclerViewOnClickListener;
import com.miao.android.myzhihudaily.ui.activity.ZhihuReadActivity;
import com.miao.android.myzhihudaily.util.Api;
import com.miao.android.myzhihudaily.util.DividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/7.
 */
public class PagerFragment extends Fragment {

    private static final String ARGS_PAGE = "args_page";
    private static final String TAG = "PageFragment";
    private int pages;
    private RequestQueue queue;
    private ImageView ivTheme;
    private RecyclerView rvThemePosts;
    private TextView tvThemeDescription;
    private RelativeLayout header;

    private List<String> ids = new ArrayList<String>();
    private List<String> thumbnails = new ArrayList<String>();
    private ThemePostAdapter adapter;

    public static PagerFragment newInstance (int page) {
        Bundle args = new Bundle();
        args.putInt(ARGS_PAGE, page);
        PagerFragment fragment = new PagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pages = getArguments().getInt(ARGS_PAGE);
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view  = inflater.inflate(R.layout.theme_page, container, false);

        ivTheme = (ImageView) view.findViewById(R.id.iv_theme);
        rvThemePosts = (RecyclerView) view.findViewById(R.id.rv_theme_post);
        rvThemePosts.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvThemePosts.addItemDecoration(new DividerItemDecoration(getActivity(),
                LinearLayoutManager.VERTICAL));
        tvThemeDescription = (TextView) view.findViewById(R.id.tv_theme_description);
        header = (RelativeLayout) view.findViewById(R.id.header);

        JsonObjectRequest request1 = new JsonObjectRequest(Request.Method.GET, Api.THEMES,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            if (!jsonObject.getString("limit").isEmpty()) {
                                final JSONArray array = jsonObject.getJSONArray("others");
                                for (int i = 0; i < array.length(); i++) {
                                    String id = array.getJSONObject(i).getString("id");
                                    String thumbnail = array.getJSONObject(i).getString("thumbnail");
                                    ids.add(id);
                                    thumbnails.add(thumbnail);
                                }

                                final List<ThemePost> list = new ArrayList<ThemePost>();
                                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                                        Api.THEME + ids.get(pages), new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject jsonObject) {
                                        try {
                                            if (jsonObject.has("stories")) {
                                                Glide.with(getActivity())
                                                        .load(jsonObject.getString("image"))
                                                        .centerCrop()
                                                        .into(ivTheme);
                                                tvThemeDescription.setText(jsonObject.getString("description"));
                                                JSONArray array1 = jsonObject.getJSONArray("stories");
                                                for (int j = 0; j < array1.length(); j++) {
                                                    String[] strings;
                                                    if (array1.getJSONObject(j).isNull("images")) {
                                                        strings = null;
                                                    } else {
                                                        strings = new String[array1.getJSONObject(j)
                                                                .getJSONArray("images").length()];
                                                        JSONArray imgUrls = array1.getJSONObject(j)
                                                                .getJSONArray("images");
                                                        for (int q = 0; q < imgUrls.length(); q++) {
                                                            strings[q] = imgUrls.getString(q);
                                                        }
                                                    }
                                                    ThemePost themePost = new ThemePost(
                                                            array1.getJSONObject(j).getString("id"),
                                                            strings,
                                                            array1.getJSONObject(j).getString("title"));
                                                    list.add(themePost);
                                                }

                                                adapter = new ThemePostAdapter(getActivity(), list);
                                                rvThemePosts.setAdapter(adapter);
                                                adapter.setItemClickListener(new OnRecyclerViewOnClickListener() {
                                                    @Override
                                                    public void OnItemClick(View v, int position) {
                                                        Intent intent = new Intent(getActivity(), ZhihuReadActivity.class);
                                                        intent.putExtra("id", list.get(position).getId());
                                                        intent.putExtra("title", list.get(position).getTitle());
                                                        intent.putExtra("image", list.get(position).getFirstImg());
                                                        startActivity(intent);
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
                                        Snackbar.make(ivTheme, R.string.wrong_process, Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                                queue.add(request);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Snackbar.make(ivTheme,R.string.wrong_process,Snackbar.LENGTH_SHORT).show();
            }
        });
        request1.setTag(TAG);
        queue.add(request1);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (queue!=null) {
            queue.cancelAll(TAG);
        }
    }
}
