package com.jiuj.absen.Adapter;

public class AbsenClientList {
    String _title;
    String _image;
    String _noref;
    String _addr;
    String _lat;
    String _lng;
    String _status;

    public AbsenClientList(String noref, String title, String image, String address, String glat, String glng, String status) {
        this._noref = noref;
        this._title = title;
        this._image = image;
        this._addr = address;
        this._lat = glat;
        this._lng = glng;
        this._status = status;
    }
}
