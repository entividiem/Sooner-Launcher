package com.ntvdm.m3launcher;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class LauncherItem {
    public String label;
    public int iconResId = 0;
    public Drawable iconDrawable = null;
    public Intent intent;

    // Constructor for static/hardcoded items
    public LauncherItem(String label, int iconResId, Intent intent) {
        this.label = label;
        this.iconResId = iconResId;
        this.intent = intent;
    }

    // Constructor for dynamic apps
    public LauncherItem(String label, Drawable iconDrawable, Intent intent) {
        this.label = label;
        this.iconDrawable = iconDrawable;
        this.intent = intent;
    }
}