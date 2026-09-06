package com.hieu.buoi8;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CounterViewModel extends ViewModel {

    private static final String PREFS_NAME = "counter_prefs";
    private static final String KEY_COUNT = "count";

    private final MutableLiveData<Integer> count = new MutableLiveData<>(0);

    public LiveData<Integer> getCount() {
        return count;
    }

    // TODO 1: Load count từ SharedPreferences và set vào LiveData
    public void loadCount(SharedPreferences prefs) {
        // code here:
        int savedValue = prefs.getInt(KEY_COUNT, 0);
        count.setValue(savedValue);
    }

    // TODO 2: Tăng count lên 1 và lưu vào SharedPreferences
    public void increment(SharedPreferences prefs) {
        // code here:
        int currentValue = count.getValue();
        count.setValue(currentValue + 1);
        saveCount(prefs, currentValue + 1);
    }

    // TODO 3: Giảm count xuống 1 và lưu vào SharedPreferences, không giảm quá 0
    public void decrement(SharedPreferences prefs) {
        int current = count.getValue() == null ? 0 : count.getValue();
        int newValue = Integer.max(current - 1, 0);
        count.setValue(newValue);
        saveCount(prefs, newValue);
    }

    // TODO 4: Reset to 0 and save it
    public void reset(SharedPreferences prefs) {
        // ...
        count.setValue(0);
        saveCount(prefs, 0);
    }

    // Helper method để lưu count vào SharedPreferences
    private void saveCount(SharedPreferences prefs, int value) {
        prefs.edit().putInt(KEY_COUNT, value).apply();
    }
}
