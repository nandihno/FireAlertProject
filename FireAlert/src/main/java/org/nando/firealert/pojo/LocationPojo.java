package org.nando.firealert.pojo;

/**
 * Created by fernandoMac on 24/10/2013.
 */
public class LocationPojo {

    public LocationPojo(){

    }

    public LocationPojo(double alongtitude, double alatitude) {
        this.longtitude = alongtitude;
        this.latitude = alatitude;
    }

    public LocationPojo(double alongtitude, double alatitude,String anAddressName) {
        this.longtitude = alongtitude;
        this.latitude = alatitude;
        this.addressName = anAddressName;
    }

    public double longtitude;
    public double latitude;
    public String addressName = "";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationPojo that = (LocationPojo) o;

        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longtitude, longtitude) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(longtitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
