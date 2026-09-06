package com.ntvdm.m3launcher;

import android.view.View;
import java.lang.reflect.Method;

// moving here so dumbass 1.x doesnt cry with its verifyerror

public class ScaleAnimatorHelper {
    public static boolean applySmoothScale(View child, float targetScale) {
        try {
            int sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
            if (sdkVersion >= 11) {
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {*/
                Method setPivotX = View.class.getMethod("setPivotX", float.class);
                Method setPivotY = View.class.getMethod("setPivotY", float.class);
                setPivotX.invoke(child, child.getWidth() / 2f);
                setPivotY.invoke(child, child.getHeight()); // anchor to bottom

                Object animator = child.getClass().getMethod("animate").invoke(child);
                if (animator != null) {
                    Class<?> propAnimClass = animator.getClass();
                    propAnimClass.getMethod("scaleX", float.class).invoke(animator, targetScale);
                    propAnimClass.getMethod("scaleY", float.class).invoke(animator, targetScale);
                    Method setDuration = propAnimClass.getMethod("setDuration", long.class);
                    setDuration.invoke(animator, 200L);
                    propAnimClass.getMethod("start").invoke(animator);
                }
                return true;
            }
        } catch (Throwable t) {
            // fallback if anything goes wrong
        }
        return false;
    }

    public static boolean applyAdapterScale(View view, float targetScale, float pivotX, float pivotY) {
        try {
            int sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
            if (sdkVersion >= 11) {
                /*if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {*/
                Method setPivotX = View.class.getMethod("setPivotX", float.class);
                Method setPivotY = View.class.getMethod("setPivotY", float.class);
                Method setScaleX = View.class.getMethod("setScaleX", float.class);
                Method setScaleY = View.class.getMethod("setScaleY", float.class);

                setPivotX.invoke(view, pivotX);
                setPivotY.invoke(view, pivotY);
                setScaleX.invoke(view, targetScale);
                setScaleY.invoke(view, targetScale);
                return true;
            }
        } catch (Throwable t) {
            // fallback
        }
        return false;
    }
}