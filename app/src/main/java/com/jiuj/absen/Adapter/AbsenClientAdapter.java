package com.jiuj.absen.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiuj.absen.ActivityImageView;
import com.jiuj.absen.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class AbsenClientAdapter extends ArrayAdapter<AbsenClientList>
{
    Context context;
    int layoutResourceId;
    private SQLiteDatabase db = null;
    ArrayList<AbsenClientList> data = new ArrayList<AbsenClientList>();
    String encodedImage = "";

    public AbsenClientAdapter(Context context, int layoutResourceId, ArrayList<AbsenClientList> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        AbsenClientAdapter.ImageHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.list_item, null);
            holder = new AbsenClientAdapter.ImageHolder();
            holder.txtTitle = (TextView) row.findViewById(R.id.txtTitle);
            holder.txtSub = (TextView) row.findViewById(R.id.txtSub);
            holder.txtDetail = (TextView) row.findViewById(R.id.txtDetail);
            holder.txtRef = (TextView) row.findViewById(R.id.txtTime);
            holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
            row.setTag(holder);
        } else {
            holder = (AbsenClientAdapter.ImageHolder) row.getTag();
        }

        final AbsenClientList picture = data.get(position);
        byte[] decodedString = Base64.decode(picture._image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        String a = picture._title;
        String[] separated = a.split(" ");
        holder.txtSub.setText(picture._title.toUpperCase());
        //holder.txtSub.setText(separated[1]);
        holder.txtTitle.setText(picture._noref);
        holder.txtDetail.setText(picture._addr);
        if("".equalsIgnoreCase(picture._image)){
            holder.imgIcon.setImageResource(R.drawable.noimage_new);
        }else{
            //encodedImage = getByteArrayFromImageURL(picture._image);
            holder.imgIcon.setImageBitmap(decodedByte);
        }

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String norefx = picture._noref;
                String namex = picture._title;
                String imgxx = "";
                byte[] imgx = Base64.decode(picture._image, Base64.DEFAULT);
                if("".equalsIgnoreCase(picture._image)){
                    imgxx = "";
                }else{
                    imgxx = picture._image;
                }
                Intent i = new Intent(getContext(),ActivityImageView.class);
                i.putExtra("key", norefx);
                i.putExtra("key2", imgx);
                i.putExtra("key3", namex);
                i.putExtra("key4", imgxx);
                i.putExtra("glat", picture._lat);
                i.putExtra("glong", picture._lng);
                i.putExtra("addr", picture._addr);
                context.startActivity(i);
                //((Activity)context).finish();
            }
        });
        return row;
    }

    static class ImageHolder {
        ImageView imgIcon;
        TextView txtTitle, txtSub, txtDetail, txtRef;
    }

    private String getByteArrayFromImageURL(String url) {
        try {
            URL imageUrl = new URL(url);
            URLConnection ucon = imageUrl.openConnection();
            InputStream is = ucon.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, read);
            }
            baos.flush();
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            Log.d("Error", e.toString());
        }
        return null;
    }
}
