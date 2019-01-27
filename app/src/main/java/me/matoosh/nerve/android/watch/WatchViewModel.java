package me.matoosh.nerve.android.watch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The watch module viewmodel
 */
public class WatchViewModel extends ViewModel {
    private MutableLiveData<String> currentCity;
}
