package com.jiuj.absen;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.jiuj.absen.Adapter.AbsenAdapter;
import com.jiuj.absen.Adapter.AbsenList;
import com.jiuj.absen.Database.DatabaseHelper;
import com.karan.churi.PermissionManager.PermissionManager;

import java.util.ArrayList;

public class MainAbsen extends AppCompatActivity {
    private SQLiteDatabase db=null;
    private DatabaseHelper dbx=null;
    FloatingActionButton fab;
    private static String sTgl = "";
    Activity activ;
    PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_absen);

        dbx = new DatabaseHelper(this);
        db = dbx.getWritableDatabase();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainAbsen.this, ActivityAbsen.class);
                startActivity(i);
                finish();
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            permissionManager = new PermissionManager() {};
            permissionManager.checkAndRequestPermissions(this);
        }

        displayLv();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode,permissions,grantResults);
    }

    public void displayLv(){
        final ArrayList<AbsenList> imageArry = new ArrayList<AbsenList>();
        AbsenAdapter adapter;
        String selectQuery = "";
        selectQuery = "SELECT * from device_absen";
        final Cursor csr = db.rawQuery(selectQuery, null);
        if (csr.moveToFirst()) {
            do
            {
                AbsenList KFL = new AbsenList(csr.getString(0),csr.getString(1),csr.getString(2), csr.getString(2));
                imageArry.add(KFL);
            } while (csr.moveToNext());
        }
        adapter = new AbsenAdapter(this, R.layout.list_item, imageArry);
        ListView dataList = (ListView) this.findViewById(R.id.listview);
        dataList.setAdapter(adapter);
    }
}
