package me.matoosh.nerve.android.geo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.lifecycle.LiveData;

/**
 * Live data for monitoring the device current location.
 */
public class LocationLiveData extends LiveData<Location> implements LocationListener {

    private Context context;

    public LocationLiveData(Context context) {
        this.context = context;
    }

    @Override
    protected void onActive() {
        //Start listening for user location.
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //Register the listener with the Location Manager to receive location updates
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }

        //Getting the last known location.
        this.setValue(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));

        super.onActive();
    }

    @Override
    protected void onInactive() {
        //Stop listening for user location.
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this);

        super.onInactive();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.setValue(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }

    @Override
    public void onProviderEnabled(String s) { }

    @Override
    public void onProviderDisabled(String s) { }
}
