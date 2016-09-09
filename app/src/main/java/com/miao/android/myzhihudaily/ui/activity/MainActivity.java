package com.miao.android.myzhihudaily.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.miao.android.myzhihudaily.R;
import com.miao.android.myzhihudaily.ui.fragment.HotPostFragment;
import com.miao.android.myzhihudaily.ui.fragment.LatestFragment;
import com.miao.android.myzhihudaily.ui.fragment.ThemeFragment;
import com.miao.android.myzhihudaily.util.UtilFunctions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        UtilFunctions.setTheme(MainActivity.this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        navigationView.setCheckedItem(R.id.nav_home);
        LatestFragment fragment = new LatestFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_content, fragment).commit();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_copy_right) {
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
            dialog.setTitle(R.string.action_settings);
            dialog.setMessage(getString(R.string.copy_right));
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.go_it),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            dialog.show();

            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            changeFragment(new LatestFragment());
            toolbar.setTitle(getString(R.string.app_name));

        } else if (id == R.id.nav_theme_post) {
            changeFragment(new ThemeFragment());
            toolbar.setTitle(item.getTitle());

        } else if (id == R.id.nav_hot_post) {
            changeFragment(new HotPostFragment());
            toolbar.setTitle(item.getTitle());

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_change_theme) {
            if (UtilFunctions.getThemeState(MainActivity.this) == 0) {
                UtilFunctions.setThemeState(MainActivity.this, 1);
            }else {
                UtilFunctions.setThemeState(MainActivity.this, 0);
            }
            this.finish();
            this.startActivity(this.getIntent());

        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changeFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_content, fragment).commit();
    }
}
