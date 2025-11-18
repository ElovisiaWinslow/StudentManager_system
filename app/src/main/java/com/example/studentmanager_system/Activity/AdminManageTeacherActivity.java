// app/src/main/java/com/example/studentmanager_system/Activity/AdminManageTeacherActivity.java
package com.example.studentmanager_system.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import com.google.android.material.card.MaterialCardView;
import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * 教师管理主页面 - 二级子页面
 */
public class AdminManageTeacherActivity extends AppCompatActivity {

    private myDatabaseHelper dbHelper;
    private TextView tvTotalTeachers, tvNewTeachersThisMonth, tvRetiredTeachers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_teachers);

        // 初始化数据库帮助类
        dbHelper = myDatabaseHelper.getInstance(this);

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
        loadStatisticsData(); // 加载统计数据
    }

    private void initViews() {
        // 初始化统计文本视图
        tvTotalTeachers = findViewById(R.id.tv_total_teachers);
        tvNewTeachersThisMonth = findViewById(R.id.tv_new_teachers_this_month);
        tvRetiredTeachers = findViewById(R.id.tv_retired_teachers);

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

    /**
     * 加载统计数据
     */
    private void loadStatisticsData() {
        // 统计教师总数
        int totalTeachers = getTotalTeachers();
        tvTotalTeachers.setText(String.valueOf(totalTeachers));

        // 统计学院数量（去重）
        int collegesCount = getDistinctCollegesCount();
        tvNewTeachersThisMonth.setText(String.valueOf(collegesCount));

        // 统计课程数量（去重）
        int coursesCount = getDistinctCoursesCount();
        tvRetiredTeachers.setText(String.valueOf(coursesCount));
    }

    /**
     * 获取教师总数
     * @return 教师总数
     */
    private int getTotalTeachers() {
        android.database.Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + myDatabaseHelper.TEACHER_TABLE, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * 获取不重复的学院数量
     * @return 学院数量
     */
    private int getDistinctCollegesCount() {
        android.database.Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT college FROM " + myDatabaseHelper.TEACHER_TABLE + " WHERE college IS NOT NULL AND college != ''", null);

        Set<String> colleges = new HashSet<>();
        while (cursor.moveToNext()) {
            colleges.add(cursor.getString(0));
        }
        cursor.close();
        return colleges.size();
    }

    /**
     * 获取不重复的课程数量
     * @return 课程数量
     */
    private int getDistinctCoursesCount() {
        android.database.Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT DISTINCT name FROM " + myDatabaseHelper.COURSE_TABLE, null);

        int count = cursor.getCount();
        cursor.close();
        return count;
    }
}
