package com.example.studentmanager_system.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import com.google.android.material.card.MaterialCardView;
import com.example.studentmanager_system.R;

/**
 * 教师管理主页面 - 二级子页面
 */
public class AdminManageTeacherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_teachers);

        // 注册新的返回监听器替代已弃用的onBackPressed()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 直接销毁当前页面，返回上一个页面
                finish();
            }
        });

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // 初始化搜索相关组件
        EditText etSearch = findViewById(R.id.et_search);
        Button btnSearch = findViewById(R.id.btn_search);

        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                String keyword = etSearch.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    // 启动教师信息页面并传递搜索关键字
                    Intent intent = new Intent(AdminManageTeacherActivity.this, teacherinfoActivity.class);
                    intent.putExtra("search_keyword", keyword);
                    startActivity(intent);
                }
            });
        }
    }

    private void setupClickListeners() {
        setupBottomNavigation();
        setupTeacherManagementCards();
    }

    private void setupTeacherManagementCards() {
        // 添加跳转到添加教师信息页面的点击事件
        MaterialCardView addTeacherCard = findViewById(R.id.card_add_teacher);
        if (addTeacherCard != null) {
            addTeacherCard.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManageTeacherActivity.this, add_teacherinfoActivity.class);
                // 传递标识，表示是新增教师而不是修改教师
                intent.putExtra("haveData", "false");
                startActivity(intent);
            });
        }

        // 添加跳转到教师信息总览页面的点击事件
        MaterialCardView queryTeacherCard = findViewById(R.id.card_query_teacher);
        if (queryTeacherCard != null) {
            queryTeacherCard.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManageTeacherActivity.this, teacherinfoActivity.class);
                startActivity(intent);
            });
        }

        // 添加跳转到教师数据管理页面的点击事件
        MaterialCardView batchOperationCard = findViewById(R.id.card_batch_teacher_operation);
        if (batchOperationCard != null) {
            batchOperationCard.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManageTeacherActivity.this, DataManagementActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupBottomNavigation() {
        // 首页按钮
        LinearLayout navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // 直接回首页，结束所有中间页面
                Intent intent = new Intent(AdminManageTeacherActivity.this, adminActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                // 不需要手动finish，因为CLEAR_TOP会清理栈
            });
        }

        // 管理按钮
        LinearLayout navManage = findViewById(R.id.nav_manage);
        if (navManage != null) {
            navManage.setOnClickListener(v -> {
                // 回到管理页面，结束当前教师管理页面
                Intent intent = new Intent(AdminManageTeacherActivity.this, AdminManagementActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }
}
