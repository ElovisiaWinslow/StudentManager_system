package com.example.studentmanager_system.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.activity.OnBackPressedCallback;
import com.example.studentmanager_system.R;
import com.google.android.material.card.MaterialCardView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 管理页面 - 一级子页面
 */
public class AdminManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_management);

        // 注册新的返回监听器替代已弃用的onBackPressed()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 回到首页，结束当前管理页面
                Intent intent = new Intent(AdminManagementActivity.this, adminActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // 初始化视图组件
    }

    private void setupClickListeners() {
        // 学生管理卡片点击事件
        MaterialCardView studentManagementCard = findViewById(R.id.card_student_management);
        if (studentManagementCard != null) {
            studentManagementCard.setOnClickListener(v -> {
                // 进入子页面，不结束当前页面
                Intent intent = new Intent(AdminManagementActivity.this, AdminManageStudentActivity.class);
                startActivity(intent);
            });
        }

        // 教师管理卡片点击事件
        MaterialCardView teacherManagementCard = findViewById(R.id.card_teacher_management);
        if (teacherManagementCard != null) {
            teacherManagementCard.setOnClickListener(v -> {
                // 进入教师管理子页面，不结束当前页面
                Intent intent = new Intent(AdminManagementActivity.this, AdminManageTeacherActivity.class);
                startActivity(intent);
            });
        }

        // 底部导航栏 - 首页按钮
        LinearLayout navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // 直接回首页，结束当前页面
                Intent intent = new Intent(AdminManagementActivity.this, adminActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 底部导航栏 - 管理按钮（当前页面，不需要处理）
    }
}
