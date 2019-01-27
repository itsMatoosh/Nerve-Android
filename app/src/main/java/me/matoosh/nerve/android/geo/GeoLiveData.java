package me.matoosh.nerve.android.geo;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.lifecycle.LiveData;

/**
 * Live data with current geographic information.
 */
public class GeoLiveData extends LiveData<Address> {
    @Override
    protected void onActive() {
        
    }

    @Override
    protected void onInactive() {
        super.onInactive();
    }

    /**
     * Task to get address info from location.
     */
    public class GeoInfoTask extends AsyncTask<Location, Void, Address> {
        private Geocoder geocoder;

        @Override
        protected void onPreExecute() {
            geocoder = new Geocoder(null, Locale.getDefault());
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
    }
}
