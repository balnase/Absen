package com.jiuj.absen.Adapter;

public class AbsenClientList {
    String _title;
    String _image;
    String _noref;
    String _addr;

    public AbsenClientList(String noref, String title, String image, String address) {
        this._noref = noref;
        this._title = title;
        this._image = image;
        this._addr = address;
    }
}
