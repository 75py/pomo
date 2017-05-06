package com.nagopy.android.pomo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

@SuppressWarnings("WeakerAccess")
public class UserSettings {

    private final Resources res;
    private final SharedPreferences sp;

    public UserSettings(Context context) {
        this(context.getResources(), PreferenceManager.getDefaultSharedPreferences(context));
    }

    public UserSettings(Resources res, SharedPreferences sp) {
        this.res = res;
        this.sp = sp;
    }

    public int getWorkMinutes() {
        return sp.getInt(res.getString(R.string.key_work_minutes), res.getInteger(R.integer.def_work_minutes)) * 60;
    }

    public int getShortBreakMinutes() {
        return sp.getInt(res.getString(R.string.key_short_break_minutes), res.getInteger(R.integer.def_short_break_minutes)) * 60;
    }

    public int getLongBreakMinutes() {
        return sp.getInt(res.getString(R.string.key_long_break_minutes), res.getInteger(R.integer.def_long_break_minutes)) * 60;
    }

    public boolean useVibration() {
        return sp.getBoolean(res.getString(R.string.key_use_vibration), res.getBoolean(R.bool.def_use_vibration));
    }

}
