package org.nando.firealert;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import com.google.android.gms.maps.model.LatLng;
import com.bugsense.trace.BugSenseHandler;

import org.nando.firealert.utils.ConnectivityUtils;


public class MainActivity extends Activity {

    static MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.yellow_gradient));
        }
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean weHaveGoog = ConnectivityUtils.weHaveGoogleServices(this);
        if(weHaveGoog && enabled) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.animator.fade_in,android.R.animator.fade_out);
            mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragmentQld);
            ft.show(mapFragment);
            ft.commit();
        }
        else {
            if(!weHaveGoog) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Warning");
                alertDialog.setMessage("You dont have Google play services install please download it from the PlayStore");
                alertDialog.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms"));
                        startActivity(marketIntent);
                    }
                });
                alertDialog.show();

            }
            if(!enabled) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Warning");
                alertDialog.setMessage("Please enable GPS settings");
                alertDialog.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
                alertDialog.show();
            }
        }
    }

    protected void onStop() {
        super.onStop();
        System.out.println("stopping!");


    }

    protected void onStart() {
        super.onStart();

    }




}
