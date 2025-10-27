package com.example.studentmanager_system.Util;

import android.app.Activity;
import android.view.View;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class FullScreenUtils {
    private final WindowInsetsControllerCompat controller;

    // 构造方法接收 Activity（用于获取 Window）和 DecorView
    public FullScreenUtils(Activity activity, View decorView) {
        controller = new WindowInsetsControllerCompat(
                activity.getWindow(), // 传入 Activity 的 Window（解决类型不匹配）
                decorView
        );
        // 设置“滑动显示系统栏后自动隐藏”（粘性沉浸式）
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
    }

    // 隐藏状态栏 + 导航栏
    public void hideSystemBars() {
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
        }
    }

    // 显示状态栏 + 导航栏（可选）
    public void showSystemBars() {
        if (controller != null) {
            controller.show(WindowInsetsCompat.Type.systemBars());
        }
    }
}