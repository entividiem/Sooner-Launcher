package com.ntvdm.m3launcher;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import java.util.List;

public class LauncherAdapter extends BaseAdapter {
    private Context mContext;
    private List<LauncherItem> mItems;

    public LauncherAdapter(Context context, List<LauncherItem> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public int getCount() { return mItems.size(); }
    @Override
    public Object getItem(int position) { return mItems.get(position); }
    @Override
    public long getItemId(int position) { return position; }

    private int dpToPx(int dp) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);

            int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;

            int totalMargin = dpToPx(50);  // icon scale, change if it looks weird
            int iconWidth = (screenWidth - totalMargin) / 5; // how many rows (5 is best duh)

            int iconHeight = dpToPx(60); // height of the icons, would leave it

            imageView.setLayoutParams(new Gallery.LayoutParams(iconWidth, iconHeight));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            imageView = (ImageView) convertView;
        }

        LauncherItem item = mItems.get(position);
        if (item.iconDrawable != null) {
            imageView.setImageDrawable(item.iconDrawable);
        } else {
            imageView.setImageResource(item.iconResId);
        }

        int selectedPosition = ((Gallery) parent).getSelectedItemPosition();
        float targetScale = (position == selectedPosition) ? 1.0f : 0.75f;

        // if new use the newer setscale

         /* if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            imageView.setPivotX(dpToPx(60) / 2f);
            imageView.setPivotY(dpToPx(60)); // anchor to bottom
            imageView.setScaleX(targetScale);
            imageView.setScaleY(targetScale);
        } else */
         if (!ScaleAnimatorHelper.applyAdapterScale(imageView, targetScale, dpToPx(60) / 2f, dpToPx(60))) {
            // old one for 4.1-
            ScaleAnimation snapAnim = new ScaleAnimation(targetScale, targetScale,
                    targetScale, targetScale,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                    ScaleAnimation.RELATIVE_TO_SELF, 1.0f);
            snapAnim.setDuration(0);
            snapAnim.setFillAfter(true);
            imageView.startAnimation(snapAnim);
        }

        imageView.setTag(targetScale);

        return imageView;
    }
}