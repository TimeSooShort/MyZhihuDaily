package com.miao.android.myzhihudaily.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.miao.android.myzhihudaily.bean.Theme;
import com.miao.android.myzhihudaily.ui.fragment.PagerFragment;

import java.util.List;

/**
 * Created by Administrator on 2016/9/7.
 */
public class ThemePagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private List<Theme> list;

    public ThemePagerAdapter(FragmentManager fm, Context context, List<Theme> list) {
        super(fm);
        mContext = context;
        this.list = list;
    }

    @Override
    public Fragment getItem(int position) {
        return PagerFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return list.get(position).getName();
    }
}
