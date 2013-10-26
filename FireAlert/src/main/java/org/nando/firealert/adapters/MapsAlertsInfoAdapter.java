package org.nando.firealert.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.nando.firealert.R;
import org.nando.firealert.pojo.RssItem;

import java.util.HashMap;

/**
 * Created by fernandoMac on 24/10/2013.
 */
public class MapsAlertsInfoAdapter implements GoogleMap.InfoWindowAdapter {

    private static LayoutInflater inflater = null;
    private HashMap<Marker,RssItem> map = new HashMap();

    public MapsAlertsInfoAdapter(Context ctx,HashMap<Marker,RssItem> aMap) {
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        map = aMap;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;

    }

    @Override
    public View getInfoContents(Marker marker) {
        View vi = inflater.inflate(R.layout.map_info_details,null);
        RssItem item = map.get(marker);
        TextView loc = (TextView) vi.findViewById(R.id.location);
        TextView pubDate = (TextView) vi.findViewById(R.id.pubDate);
        TextView alertLevel = (TextView) vi.findViewById(R.id.alertLevel);
        TextView currentStatus = (TextView) vi.findViewById(R.id.currentStatus);
        TextView details = (TextView) vi.findViewById(R.id.details);
        loc.setText(item.location);
        pubDate.setText(item.pubDate);
        alertLevel.setText(item.alertLevel);
        currentStatus.setText(item.currentStatus);
        details.setText(item.details);
        return vi;
    }


}
