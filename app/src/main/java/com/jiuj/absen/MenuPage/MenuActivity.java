package com.jiuj.absen.MenuPage;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;

import java.util.ArrayList;
import java.util.Arrays;

import com.jiuj.absen.ActivityAbsen;
import com.jiuj.absen.ActivityAbsenListClient;
import com.jiuj.absen.ActivityCalendar3;
import com.jiuj.absen.ActivityImageView;
import com.jiuj.absen.ActivityMaps;
import com.jiuj.absen.ActivityMock;
import com.jiuj.absen.AppController;
import com.jiuj.absen.Database.DatabaseHelper;
import com.jiuj.absen.R;
import com.jiuj.absen.Receiver.ConnectivityReceiver;
import com.jiuj.absen.Receiver.NetworkChangeReceiver;
import com.jiuj.absen.Utils.PrefManager;

import cn.pedant.SweetAlert.SweetAlertDialog;
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout;
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView;
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle;

public class MenuActivity extends AppCompatActivity implements DuoMenuView.OnMenuClickListener {
    private static final String TAG = MenuActivity.class.getName();
    public static Activity aMenu;
    private ArrayList<String> mTitles = new ArrayList<>();
    private MenuAdapter mMenuAdapter;
    private ViewHolder mViewHolder;
    SQLiteDatabase db;
    DatabaseHelper dbx;
    PrefManager session;
    TextView navUsername, navSub;
    NetworkChangeReceiver myReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_new);
        aMenu = this;
        mTitles = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.menuOptions)));
        mViewHolder = new ViewHolder();
        dbx = new DatabaseHelper(this);
        db = dbx.getWritableDatabase();
        session = new PrefManager(this);
        myReceiver= new NetworkChangeReceiver();
        changeHeader();
        handleToolbar();
        handleMenu();
        handleDrawer();
        //AppController.getInstance().setConnectivityListener(this);
        goToFragment(new MainFragment(), false);
        //mMenuAdapter.setViewSelected(0, true);
        //setTitle(mTitles.get(0));
        setTitle("MENU");
        session.createAcvtivity(TAG);
    }

    private void handleToolbar() {
        setSupportActionBar(mViewHolder.mToolbar);
    }

    private void handleDrawer() {
        DuoDrawerToggle duoDrawerToggle = new DuoDrawerToggle(this,
                mViewHolder.mDuoDrawerLayout,
                mViewHolder.mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        mViewHolder.mDuoDrawerLayout.setDrawerListener(duoDrawerToggle);
        duoDrawerToggle.syncState();
        //mViewHolder.mDuoDrawerLayout.openDrawer();
    }

    private void handleMenu() {
        mMenuAdapter = new MenuAdapter(mTitles);
        mViewHolder.mDuoMenuView.setOnMenuClickListener(this);
        mViewHolder.mDuoMenuView.setAdapter(mMenuAdapter);
    }

    @Override
    public void onFooterClicked() {
        //Toast.makeText(this, "onFooterClicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onHeaderClicked() {
        //Toast.makeText(this, "onHeaderClicked", Toast.LENGTH_SHORT).show();
    }

    private void goToFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.add(R.id.container, fragment).commit();
    }

    @Override
    public void onOptionClicked(int position, Object objectClicked) {
        setTitle(mTitles.get(position));
        mMenuAdapter.setViewSelected(position, true);
        switch (position) {
            case 0:
                Intent i = new Intent(MenuActivity.this, ActivityMaps.class);
                startActivity(i);
                overridePendingTransition(R.anim.enter, R.anim.exit);
                finish();
                break;

            case 1:
                //Intent a = new Intent(MenuActivity.this, ActivityAbsenListClient.class);
                Intent a = new Intent(MenuActivity.this, ActivityCalendar3.class);
                startActivity(a);
                finish();
                break;

            default:
                //goToFragment(new MainFragment(), false);
                break;
        }
        mViewHolder.mDuoDrawerLayout.closeDrawer();
    }

    private class ViewHolder {
        private DuoDrawerLayout mDuoDrawerLayout;
        private DuoMenuView mDuoMenuView;
        private Toolbar mToolbar;

        ViewHolder() {
            mDuoDrawerLayout = (DuoDrawerLayout) findViewById(R.id.drawer);
            //mDuoMenuView = (DuoMenuView) mDuoDrawerLayout.getMenuView();
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            mDuoMenuView = (DuoMenuView) mDuoDrawerLayout.getMenuView();

        }
    }

    private void changeHeader(){
        View headerView = mViewHolder.mDuoMenuView.getHeaderView();
        navUsername = (TextView) headerView.findViewById(R.id.custom_title);
        navSub = (TextView) headerView.findViewById(R.id.custom_sub_title);
        //navUsername.setText("Ricky Balnase");
        //navSub.setText("Android Programmer");
        //getUser();
        getUserPref();
    }

    @Override
    public void onBackPressed() {
        DuoDrawerLayout mDuoDrawerLayout = (DuoDrawerLayout) findViewById(R.id.drawer);
        if (mDuoDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDuoDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            showAlert();
            //super.onBackPressed();
        }
    }

    public void showAlert(){
        new AwesomeInfoDialog(this)
                .setTitle(R.string.app_name)
                .setMessage("Anda yakin akan keluar dari aplikasi ?")
                .setColoredCircle(R.color.dialogWarningBackgroundColor)
                .setDialogIconAndColor(R.drawable.ic_dialog_warning, R.color.white)
                .setCancelable(false)
                .setPositiveButtonText(getString(R.string.dialog_yes_button))
                .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                .setPositiveButtonTextColor(R.color.white)
                .setNegativeButtonText(getString(R.string.dialog_no_button))
                .setNegativeButtonbackgroundColor(R.color.colorAccent)
                .setNegativeButtonTextColor(R.color.white)
                .setPositiveButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        session.logoutUser();
                        finish();
                    }
                })
                .setNegativeButtonClick(new Closure() {
                    @Override
                    public void exec() {
                    }
                })
                .show();
    }

    private void getUser(){
        String selectQuery = "select userid, nama from device_login";
        Cursor csr = db.rawQuery(selectQuery, null);
        if (csr != null && csr.moveToFirst()) {
            navSub.setText(csr.getString(0));
            navUsername.setText(csr.getString(1));
        }
    }

    private void getUserPref(){
        navSub.setText(session.getKEY_NIK());
        navUsername.setText(session.getKEY_Name());
    }

    private void displayPromptInternet(boolean isConnected){
        String msg;
        if (isConnected) {
            msg = "Good! Connected to Internet";
        } else {
            msg = "Sorry! Not connected to internet";
        }
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE).setTitleText("");
        sweetAlertDialog.setContentText(msg);
        sweetAlertDialog.setConfirmText("OK");
        sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        });
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(myReceiver, filter);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(myReceiver);
    }

}
