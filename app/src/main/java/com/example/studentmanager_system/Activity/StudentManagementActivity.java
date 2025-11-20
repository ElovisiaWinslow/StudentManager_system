// app/src/main/java/com/example/studentmanager_system/Activity/StudentManagementActivity.java
package com.example.studentmanager_system.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.activity.OnBackPressedCallback;
import com.example.studentmanager_system.R;
import com.google.android.material.card.MaterialCardView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 学生管理页面 - 一级子页面
 */
public class StudentManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_management);

        // 注册新的返回监听器替代已弃用的onBackPressed()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 回到首页，结束当前管理页面
                Intent intent = new Intent(StudentManagementActivity.this, studentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        setupClickListeners();
    }

    // 添加到 StudentManagementActivity 类中
    private String getCurrentStudentId() {
        android.content.SharedPreferences prefs = getSharedPreferences("login_info", MODE_PRIVATE);
        return prefs.getString("student_id", null);
    }

    private void setupClickListeners() {
        // 课程表卡片点击事件
        MaterialCardView selectInfoCard = findViewById(R.id.card_select_info);
        if (selectInfoCard != null) {
            selectInfoCard.setOnClickListener(v -> {
                // 进入课程表
                Intent intent = new Intent(StudentManagementActivity.this, ScheduleActivity.class);
                intent.putExtra("studentId", getCurrentStudentId());
                startActivity(intent);
            });
        }

        // 在 StudentManagementActivity.java 的 setupClickListeners 方法中更新选课系统卡片点击事件
        MaterialCardView selectCourseCard = findViewById(R.id.card_select_course);
        if (selectCourseCard != null) {
            selectCourseCard.setOnClickListener(v -> {
                // 进入选课系统功能
                Intent intent = new Intent(StudentManagementActivity.this, CourseSelectionActivity.class);
                intent.putExtra("studentId", getCurrentStudentId()); // 需要实现 getCurrentStudentId() 方法
                startActivity(intent);
            });
        }

        // 导出数据功能
        MaterialCardView changePasswordCard = findViewById(R.id.card_export_data);
        if (changePasswordCard != null) {
            changePasswordCard.setOnClickListener(v -> {
                // 进入导出数据功能
                Intent intent = new Intent(StudentManagementActivity.this, ExportProfileActivity.class);
                intent.putExtra("studentId", getCurrentStudentId());
                startActivity(intent);
            });
        }

        // 已选课程卡片点击事件
        MaterialCardView selectedCoursesCard = findViewById(R.id.card_selected_courses);
        if (selectedCoursesCard != null) {
            selectedCoursesCard.setOnClickListener(v -> {
                // 进入已选课程功能
                Intent intent = new Intent(StudentManagementActivity.this, SelectedCoursesActivity.class);
                intent.putExtra("studentId", getCurrentStudentId()); // 需要实现 getCurrentStudentId() 方法
                startActivity(intent);
            });
        }

        // 底部导航栏 - 首页按钮
        LinearLayout navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // 直接回首页，结束当前页面
                Intent intent = new Intent(StudentManagementActivity.this, studentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 底部导航栏 - 管理按钮（当前页面，不需要处理）

        // 底部导航栏 - 我的按钮
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // 跳转到个人资料页面
                Intent intent = new Intent(StudentManagementActivity.this, StudentProfileActivity.class);
                intent.putExtra("studentId", getCurrentStudentId());
                startActivity(intent);
            });
        }
    }
}
