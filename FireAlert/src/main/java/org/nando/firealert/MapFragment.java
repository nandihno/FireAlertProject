package org.nando.firealert;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.nando.firealert.adapters.MapsAlertsInfoAdapter;
import org.nando.firealert.pojo.RssItem;
import org.nando.firealert.task.QldRssTask;
import org.nando.firealert.utils.ConnectivityUtils;
import org.nando.firealert.utils.GeoUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by fernandoMac on 24/10/2013.
 */
public class MapFragment extends Fragment  implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener, GoogleMap.OnInfoWindowClickListener {

    private MapView mapView;
    private GoogleMap map;
    private Location location;
    private LocationClient locationClient;
    private HashMap<Marker,RssItem> markerMap = new HashMap<Marker, RssItem>();

    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(10000)         // 10 seconds
            .setFastestInterval(5000)    // 5 sec
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            MapsInitializer.initialize(getActivity());
        } catch(GooglePlayServicesNotAvailableException e) {
           ConnectivityUtils.downloadGooglePlayServices(getActivity());
        }
        View rootView = inflater.inflate(R.layout.map_fragment,container,false);
        Button button = (Button) rootView.findViewById(R.id.refreshBtn);
        mapView = (MapView) rootView.findViewById(R.id.mapViewJp);
        mapView.onCreate(savedInstanceState);
        map = mapView.getMap();
        map.setMyLocationEnabled(true);
        map.setOnInfoWindowClickListener(this);
        GeoUtils.loadBrisbaneArea(map,4);
        button.setOnClickListener(this);
        presentInfo();
        return rootView;


    }

    private void presentInfo() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Attention");
        Resources res = getResources();
        String val = res.getString(R.string.info);
        alertDialog.setMessage(val);
        alertDialog.setPositiveButton("Understood",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                callRssFeed();
            }
        });
        alertDialog.show();

    }

    private void callRssFeed() {
        QldRssTask task  = new QldRssTask(this);
        String qldUrl = "http://www.ruralfire.qld.gov.au/bushfirealert/bushfireAlert.xml";
        String nswUrl = "http://www.rfs.nsw.gov.au/feeds/majorIncidents.xml";
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,qldUrl,nswUrl);


    }

    public void displayResults(ArrayList<RssItem> items) {
        map.clear();
        markerMap.clear();

        if(!items.isEmpty()) {
            for(RssItem item:items) {
                float colorVal = 0;
                if(item.alertLevel.equalsIgnoreCase("Alert Level: Advice.") || item.alertLevel.equalsIgnoreCase("Alert Level: Advice") ) {
                    colorVal = BitmapDescriptorFactory.HUE_AZURE;
                }
                if(item.alertLevel.equalsIgnoreCase("Alert Level: Emergency Warning.") || item.alertLevel.equalsIgnoreCase("Alert Level: Emergency Warning")) {
                    colorVal = BitmapDescriptorFactory.HUE_ROSE;
                }
                if(item.alertLevel.equalsIgnoreCase("Alert Level: Watch and Act.") || item.alertLevel.equalsIgnoreCase("Alert Level: Watch and Act")) {
                    colorVal = BitmapDescriptorFactory.HUE_YELLOW;
                }
                if(item.alertLevel.equalsIgnoreCase("Alert Level: Permitted Burn.") || item.alertLevel.equalsIgnoreCase("Alert Level: Permitted Burn")) {
                    colorVal = BitmapDescriptorFactory.HUE_CYAN;
                }

                Marker marker = map.addMarker(createMarkerOptions(new LatLng(item.latLng.latitude,item.latLng.longitude),item.pubDate,item.description,colorVal));
                markerMap.put(marker,item);
            }
            map.setInfoWindowAdapter(new MapsAlertsInfoAdapter(getActivity(),markerMap));
            GeoUtils.loadBrisbaneArea(map,4);
        }


    }

    private MarkerOptions createMarkerOptions(LatLng position,String title,String snippet,float hueValue) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(position);
        markerOptions.title(title);
        markerOptions.snippet(snippet);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hueValue));

        return markerOptions;
    }


    @Override
    public void onConnected(Bundle bundle) {
        locationClient.requestLocationUpdates(REQUEST,this);
        location = locationClient.getLastLocation();
        if(location == null) {
            ConnectivityUtils.showGPSSettingsAlert(getActivity());
        }

    }

    @Override
    public void onDisconnected() {
        locationClient.disconnect();

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.refreshBtn) {
            callRssFeed();
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    public void onResume() {
        super.onResume();
        setupLocationClientIfNeeded();
        locationClient.connect();
        mapView.onResume();
    }

    public void onPause() {
        super.onPause();
        locationClient.disconnect();
        mapView.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    public void onStart() {
        super.onStart();
        setupLocationClientIfNeeded();
        locationClient.connect();

    }



    @Override
    public void onStop() {

        if (locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
        }
        locationClient.disconnect();
        super.onStop();
    }

    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }



    private void setupLocationClientIfNeeded() {
        if(locationClient == null) {
            locationClient = new LocationClient(getActivity(),this,this);
        }
    }




}
