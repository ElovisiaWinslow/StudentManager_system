package com.example.studentmanager_system.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import com.example.studentmanager_system.R;

/**
 * 学生管理主页面 - 二级子页面
 */
public class AdminMangeStudentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mange_students);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // 初始化视图组件
    }

    private void setupClickListeners() {
        setupBottomNavigation();
    }


    private void setupBottomNavigation() {
        // 首页按钮
        LinearLayout navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // 直接回首页，结束所有中间页面
                Intent intent = new Intent(AdminMangeStudentActivity.this, adminActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                // 不需要手动finish，因为CLEAR_TOP会清理栈
            });
        }

        // 管理按钮
        LinearLayout navManage = findViewById(R.id.nav_manage);
        if (navManage != null) {
            navManage.setOnClickListener(v -> {
                // 回到管理页面，结束当前学生管理页面
                Intent intent = new Intent(AdminMangeStudentActivity.this, AdminManagementActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    /**
     * 处理返回键按下事件
     * 在学生管理页面按下返回：回到管理页面，结束当前页面
     */
    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        // 回到管理页面，结束当前学生管理页面
        Intent intent = new Intent(this, AdminManagementActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}