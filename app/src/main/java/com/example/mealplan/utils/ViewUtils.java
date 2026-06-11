package com.example.mealplan.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;

public class ViewUtils {

    public static ObjectAnimator startShimmer(View view) {
        if (view == null) return null;
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.35f);
        anim.setDuration(700);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.start();
        return anim;
    }

    public static void stopShimmer(ObjectAnimator anim, View view) {
        if (anim != null) anim.cancel();
        if (view != null) view.setAlpha(1f);
    }
}