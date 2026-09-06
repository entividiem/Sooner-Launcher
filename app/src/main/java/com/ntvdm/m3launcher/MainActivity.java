package com.ntvdm.m3launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private Gallery mGallery;
    private TextView mAppLabel;
    private List<LauncherItem> mLauncherItems;
    private LauncherAdapter mAdapter;
    private boolean mShowAllApps = true;
    private boolean mShowClock = true;

    private static final String PREFS_NAME = "M3LauncherPrefs";
    private static final String PREF_SHOW_ALL_APPS = "show_all_apps";
    private static final String PREF_SHOW_CLOCK = "show_clock";

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); // all because of 1.0

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // sharedprefs for future?
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mShowAllApps = prefs.getBoolean(PREF_SHOW_ALL_APPS, true);
        mShowClock = prefs.getBoolean(PREF_SHOW_CLOCK, true);

        View clock = findViewById(R.id.clock);
        if (clock != null) {
            clock.setVisibility(mShowClock ? View.VISIBLE : View.GONE);
        }

        View root = findViewById(R.id.main_root);
        int sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);  // sdk instead of sdk.int because of 1.x again
        if (sdkVersion < 5) {
            Drawable systemWallpaper = getWallpaper();
            if (systemWallpaper != null && root != null) {
                root.setBackgroundDrawable(systemWallpaper);
            }
        }

        mAppLabel = (TextView) findViewById(R.id.app_label);
        mGallery = (Gallery) findViewById(R.id.launcher_gallery);

        int spacingPx = dpToPx(15);
        mGallery.setSpacing(0);

        initLauncherItems();

        mAdapter = new LauncherAdapter(this, mLauncherItems);
        mGallery.setAdapter(mAdapter);

        mGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAppLabel.setText(mLauncherItems.get(position).label);

                // for arrow visibility
                ImageView leftArrow = (ImageView) findViewById(R.id.arrow_left);
                ImageView rightArrow = (ImageView) findViewById(R.id.arrow_right);

                // hide left arrow if at start
                leftArrow.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);

                // hide right arrow if at end
                rightArrow.setVisibility(position == (mLauncherItems.size() - 1) ? View.INVISIBLE : View.VISIBLE);

                // scaling animation loop
                // scaling animation loop
                for (int i = 0; i < parent.getChildCount(); i++) {

                    View child = parent.getChildAt(i);
                    boolean isSelected = (child == view);
                    float targetScale = isSelected ? 1.0f : 0.75f;

                    // version check here too for the smmoooth scaling method | MOVED TO smth class
                    /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                        child.setPivotX(child.getWidth() / 2f);
                        child.setPivotY(child.getHeight()); // anchor to bottom
                        child.animate()
                                .scaleX(targetScale)
                                .scaleY(targetScale)
                                .setDuration(200)
                                .start();
                    } else */if (!ScaleAnimatorHelper.applySmoothScale(child, targetScale)) {
                        // ScaleAnimation for older
                        Float currentScaleTag = (child.getTag() instanceof Float) ? (Float) child.getTag() : 0.75f;

                        ScaleAnimation anim = new ScaleAnimation(
                                currentScaleTag, targetScale,
                                currentScaleTag, targetScale,
                                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                                ScaleAnimation.RELATIVE_TO_SELF, 1.0f
                        );
                        anim.setDuration(200);
                        anim.setFillAfter(true);
                        child.startAnimation(anim);
                        child.setTag(targetScale);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = mLauncherItems.get(position).intent;
                if (intent != null) {
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        // fallback handling
                    }
                }
            }
        });

        // extend the screen
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // pass it to gallery
                return mGallery.dispatchTouchEvent(event);
            }
        });
    }

    // menu picker stuff

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 1, 0, "Wallpaper settings");
        menu.add(0, 2, 0, "Toggle clock");
        menu.add(0, 3, 0, "Toggle All Apps");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            // trigger system wallpaper picker
            Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
            startActivity(Intent.createChooser(intent, "Select wallpaper :)"));
            return true;
        } else if (item.getItemId() == 2) {
            mShowClock = !mShowClock;
            View clock = findViewById(R.id.clock);
            if (clock != null) {
                clock.setVisibility(mShowClock ? View.VISIBLE : View.GONE);
            }
            // show clock
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putBoolean(PREF_SHOW_CLOCK, mShowClock)
                    .commit();
            return true;
        } else if (item.getItemId() == 3) {
            mShowAllApps = !mShowAllApps;
            // show other crap
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putBoolean(PREF_SHOW_ALL_APPS, mShowAllApps)
                    .commit();
            initLauncherItems();
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // menu key function (duh)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initLauncherItems() {
        if (mLauncherItems == null) {
            mLauncherItems = new ArrayList<LauncherItem>();
        } else {
            mLauncherItems.clear();
        }

        // the dubious 5 sooner apps
        mLauncherItems.add(new LauncherItem("Applications", R.drawable.app_all_apps, new Intent().setClassName(this, "com.ntvdm.m3launcher.AllAppsActivity")));
        mLauncherItems.add(new LauncherItem("Contacts", R.drawable.icon_contacts, new Intent(Intent.ACTION_VIEW, android.provider.Contacts.People.CONTENT_URI)));

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));
        mLauncherItems.add(new LauncherItem("Browser", R.drawable.icon_browser, browserIntent));

        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0"));
        mLauncherItems.add(new LauncherItem("Maps", R.drawable.icon_maps, mapsIntent));

        mLauncherItems.add(new LauncherItem("Dev Tools", R.drawable.icon_dev, new Intent(android.provider.Settings.ACTION_SETTINGS)));

        // show all apps after (apparently what the original did but who care)
        if (mShowAllApps) {
            PackageManager pm = getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> installedApps = pm.queryIntentActivities(mainIntent, 0);

            for (ResolveInfo info : installedApps) {
                String label = info.loadLabel(pm).toString();
                Drawable icon = info.loadIcon(pm);

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setComponent(new android.content.ComponentName(
                        info.activityInfo.applicationInfo.packageName,
                        info.activityInfo.name));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                mLauncherItems.add(new LauncherItem(label, icon, intent));
            }
        }
    }
}