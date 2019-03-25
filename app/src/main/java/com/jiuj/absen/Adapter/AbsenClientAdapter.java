package com.jiuj.absen.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiuj.absen.ActivityImageView;
import com.jiuj.absen.R;

import java.util.ArrayList;

public class AbsenClientAdapter extends ArrayAdapter<AbsenClientList>
{
    Context context;
    int layoutResourceId;
    private SQLiteDatabase db = null;
    ArrayList<AbsenClientList> data = new ArrayList<AbsenClientList>();

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
}
