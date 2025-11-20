// app/src/main/java/com/example/studentmanager_system/Activity/TeacherManagementActivity.java
package com.example.studentmanager_system.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.activity.OnBackPressedCallback;
import com.example.studentmanager_system.R;
import com.google.android.material.card.MaterialCardView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 教师管理页面 - 一级子页面
 */
public class TeacherManagementActivity extends AppCompatActivity {

    private String teacherId; // 教师ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_management);

        // 获取从教师主页传递的教师ID
        Intent intent = getIntent();
        teacherId = intent.getStringExtra("teacherId");

        // 注册新的返回监听器替代已弃用的onBackPressed()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 回到首页，结束当前管理页面
                Intent intent = new Intent(TeacherManagementActivity.this, teacherActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        setupClickListeners();
    }

    private void setupClickListeners() {
        // 查询个人授课表卡片点击事件
        MaterialCardView selectInfoCard = findViewById(R.id.card_select_info);
        if (selectInfoCard != null) {
            selectInfoCard.setOnClickListener(v -> {
                // 进入授课表功能
                Intent intent = new Intent(TeacherManagementActivity.this, TeacherScheduleActivity.class);
                intent.putExtra("teacherId", teacherId);
                startActivity(intent);
            });
        }

        // 学生成绩管理卡片点击事件
        MaterialCardView manageGradesCard = findViewById(R.id.card_manage_grades);
        if (manageGradesCard != null) {
            manageGradesCard.setOnClickListener(v -> {
                // 进入学生成绩管理功能
                Intent intent = new Intent(TeacherManagementActivity.this, TeacherGradeManagementActivity.class);
                intent.putExtra("teacherId", teacherId);
                startActivity(intent);
            });
        }

        // 导出数据卡片点击事件
        MaterialCardView exportDataCard = findViewById(R.id.card_export_data);
        if (exportDataCard != null) {
            exportDataCard.setOnClickListener(v -> {
                // 进入导出数据功能
                Intent intent = new Intent(TeacherManagementActivity.this, TeacherExportProfileActivity.class);
                intent.putExtra("teacherId", teacherId);
                startActivity(intent);
            });
        }

        // TeacherManagementActivity.java 中的 courseInfoCard 点击事件
        MaterialCardView courseInfoCard = findViewById(R.id.card_course_info);
        if (courseInfoCard != null) {
            courseInfoCard.setOnClickListener(v -> {
                // 进入课程信息功能
                Intent intent = new Intent(TeacherManagementActivity.this, TeacherCourseStudentsActivity.class);
                intent.putExtra("teacherId", teacherId);
                startActivity(intent);
            });
        }

        // 底部导航栏 - 首页按钮
        LinearLayout navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // 直接回首页，结束当前页面
                Intent intent = new Intent(TeacherManagementActivity.this, teacherActivity.class);
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
                // 跳转到教师个人资料页面
                Intent intent = new Intent(TeacherManagementActivity.this, TeacherProfileActivity.class);
                intent.putExtra("teacherId", teacherId);
                startActivity(intent);
            });
        }
    }
}
