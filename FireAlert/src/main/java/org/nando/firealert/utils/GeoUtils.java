package org.nando.firealert.utils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.nando.firealert.pojo.LocationPojo;

/**
 * Created by fernandoMac on 24/10/2013.
 */
public class GeoUtils {

    public static double RANGE_IN_METERS = 500.0;
    public static final LatLng BRISBANE_LT_LNG = new LatLng(-27.4710107,153.0234489);

    /**
     * Calculates the end-point from a given source at a given range (meters)
     * and bearing (degrees). This methods uses simple geometry equations to
     * calculate the end-point.
     *
     * @param point
     *            Point of origin
     * @param range
     *            Range in meters
     * @param bearing
     *            Bearing in degrees
     * @return End-point from the source given the desired range and bearing.
     */
    public static LocationPojo calculateDerivedPosition(LocationPojo point,
                                                        double range, double bearing)
    {
        double EarthRadius = 6371000; // m

        double latA = Math.toRadians(point.latitude);
        double lonA = Math.toRadians(point.longtitude);
        double angularDistance = range / EarthRadius;
        double trueCourse = Math.toRadians(bearing);

        double lat = Math.asin(
                Math.sin(latA) * Math.cos(angularDistance) +
                        Math.cos(latA) * Math.sin(angularDistance)
                                * Math.cos(trueCourse));

        double dlon = Math.atan2(
                Math.sin(trueCourse) * Math.sin(angularDistance)
                        * Math.cos(latA),
                Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));

        double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);


        LocationPojo newPoint = new LocationPojo();
        newPoint.latitude = lat;
        newPoint.longtitude = lon;

        return newPoint;

    }


    public static double getDistanceBetweenTwoPoints(LocationPojo p1, LocationPojo p2) {
        double R = 6371000; // m
        double dLat = Math.toRadians(p2.latitude - p1.latitude);
        double dLon = Math.toRadians(p2.longtitude - p1.longtitude);
        double lat1 = Math.toRadians(p1.latitude);
        double lat2 = Math.toRadians(p2.latitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
                * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;

        return d;
    }

    public static void loadBrisbaneArea(GoogleMap map,int zoomLevel) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(BRISBANE_LT_LNG)
                .zoom(zoomLevel)
                .bearing(0)
                .tilt(30)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }


}
