package com.benjaminwan.composelocation.loc;

import android.annotation.SuppressLint;
import android.location.Location;

@SuppressLint("ParcelCreator")
public class HalopayLocation extends Location {
    private static final int TYPE_GSM = 0;
    private static final int TYPE_CDMA = 1;
    private static final int TYPE_NONE = -1;
    private static final int NONE_CELLID = -1;
    private static final int NONE_LACID = -1;
    private int type;
    private int cellId;
    private int lacId;
    private boolean isCorrected = false;
    private String addrStr;
    private String province;
    private String city;
    private String district;
    private boolean hasPoi;
    private String poi;
    private int satelliteNumber;
    private float radius;
    private int dataSource;
    private String coordinateSys;

    public HalopayLocation(Location location) {
        super(location);
        // TODO Auto-generated constructor stub
    }

    public HalopayLocation(String provider) {
        super(provider);
    }

    public HalopayLocation(String provider, int type, int cellId, int lacId) {
        super(provider);
        this.type = type;
        this.cellId = cellId;
        this.lacId = lacId;
    }

    public String getCoordinateSystem() {
        return this.coordinateSys;
    }

    public void setCoordinateSystem(String data) {
        this.coordinateSys = data;
    }

    public int getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(int data) {
        this.dataSource = data;
    }

    public String getAddrStr() {
        return this.addrStr;
    }

    public void setAddrStr(String addrStr) {
        this.addrStr = addrStr;
    }

    public String getProvince() {
        return this.province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return this.district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public boolean isHasPoi() {
        return this.hasPoi;
    }

    public void setHasPoi(boolean hasPoi) {
        this.hasPoi = hasPoi;
    }

    public String getPoi() {
        return this.poi;
    }

    public void setPoi(String poi) {
        this.poi = poi;
    }

    public float getRadius() {
        return this.radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getSatelliteNumber() {
        return this.satelliteNumber;
    }

    public void setSatelliteNumber(int satelliteNumber) {
        this.satelliteNumber = satelliteNumber;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        if ((type != 0) && (type != 1)) {
            type = -1;
        }
        this.type = type;
    }

    public int getCellId() {
        return this.cellId;
    }

    public void setCellId(int cellId) {
        if (cellId < 0) {
            cellId = -1;
        }
        this.cellId = cellId;
    }

    public int getLacId() {
        return this.lacId;
    }

    public void setLacId(int lacId) {
        if (lacId < 0) {
            lacId = -1;
        }
        this.lacId = lacId;
    }

    public boolean isCorrected() {
        return this.isCorrected;
    }

    public void setCorrected(boolean isCorrected) {
        this.isCorrected = isCorrected;
    }

    public String getTypeReadable() {
        String cardType = "";
        switch (this.type) {
            case 0:
                cardType = "GSM";
                break;
            case 1:
                cardType = "CDMA";
                break;
            case -1:
        }

        return cardType;
    }

    public float getFilteredDistance(HalopayLocation dest) {
        float distance = distanceTo(dest);

        if (distance > 10.0F) {
            return distance;
        }

        return 0.0F;
    }
}
