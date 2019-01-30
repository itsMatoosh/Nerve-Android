package me.matoosh.nerve.android.geo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.lifecycle.LiveData;

/**
 * Live data with current geographic information.
 */
public class AddressLiveData extends LiveData<Address> implements LocationListener {
    private Context context;

    public AddressLiveData(Context context) {
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
        processLocationChange(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));

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
        processLocationChange(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void processLocationChange(Location location) {
        new GeoInfoTask(context).execute(location);
    }

    /**
     * Task to get address info from location.
     */
    public class GeoInfoTask extends AsyncTask<Location, Void, Address> {
        private Geocoder geocoder;
        private Context context;

        public GeoInfoTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            geocoder = new Geocoder(context, Locale.getDefault());
        }

        @Override
        protected Address doInBackground(Location... locations) {
            Location loc = locations[0];
            try {
                List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);

                return addresses.get(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Address address) {
            AddressLiveData.this.setValue(address);
            super.onPostExecute(address);
        }
    }
}
