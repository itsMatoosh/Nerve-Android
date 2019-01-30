package me.matoosh.nerve.android.watch;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import me.matoosh.nerve.android.geo.AddressLiveData;
import me.matoosh.nerve.android.geo.LocationLiveData;

/**
 * The watch module viewmodel
 */
public class WatchViewModel extends ViewModel {
    private MutableLiveData<Boolean> isWatchModuleVisible;
    private LocationLiveData userLocation;
    private AddressLiveData userAddress;

    public MutableLiveData<Boolean> isWatchModuleVisible() {
        if(isWatchModuleVisible == null) {
            isWatchModuleVisible = new MutableLiveData<>();
            isWatchModuleVisible.setValue(false);
        }

        return isWatchModuleVisible;
    }

    public LocationLiveData getUserLocation(Context context) {
        if(userLocation == null) {
            userLocation = new LocationLiveData(context);
        }

        return userLocation;
    }

    public AddressLiveData getUserAddress(Context context) {
        if(userAddress == null) {
            userAddress = new AddressLiveData(context);
        }

        return userAddress;
    }
}
