package com.example.studentmanager_system.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;
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
        updateStatistics();
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

        // 浏览课程卡片点击事件
        MaterialCardView browseCourseCard = findViewById(R.id.card_browse_course);
        if (browseCourseCard != null) {
            browseCourseCard.setOnClickListener(v -> {
                // TODO: 实现浏览课程功能
                // 示例跳转到课程浏览页面
                Intent intent = new Intent(AdminManagementActivity.this, courseinfoActivity.class);
                startActivity(intent);
            });
        }

        // 数据操作卡片点击事件
        MaterialCardView courseDataOperationCard = findViewById(R.id.card_course_data_operation);
        if (courseDataOperationCard != null) {
            courseDataOperationCard.setOnClickListener(v -> {
                // TODO: 实现数据操作功能
                // 示例跳转到课程数据操作页面
                Intent intent = new Intent(AdminManagementActivity.this, DataManagementActivity.class);
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

        // 我的按钮点击事件
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // 这里可以跳转到关于南京邮电大学的介绍页面
                Intent intent = new Intent(AdminManagementActivity.this, NjuptInfoActivity.class);
                startActivity(intent);
            });
        }

        // 底部导航栏 - 管理按钮（当前页面，不需要处理）
    }

    // 更新统计数据
    private void updateStatistics() {
        myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 查询学生总数
        Cursor studentCursor = db.rawQuery("SELECT COUNT(*) FROM " + myDatabaseHelper.STUDENT_TABLE, null);
        int studentCount = 0;
        if (studentCursor.moveToFirst()) {
            studentCount = studentCursor.getInt(0);
        }
        studentCursor.close();

        // 查询教师总数
        Cursor teacherCursor = db.rawQuery("SELECT COUNT(*) FROM " + myDatabaseHelper.TEACHER_TABLE, null);
        int teacherCount = 0;
        if (teacherCursor.moveToFirst()) {
            teacherCount = teacherCursor.getInt(0);
        }
        teacherCursor.close();

        // 查询班级数量
        Cursor classCursor = db.rawQuery("SELECT COUNT(DISTINCT class) FROM " + myDatabaseHelper.STUDENT_TABLE, null);
        int classCount = 0;
        if (classCursor.moveToFirst()) {
            classCount = classCursor.getInt(0);
        }
        classCursor.close();

        // 更新UI显示
        TextView studentCountText = findViewById(R.id.student_count_text);
        TextView teacherCountText = findViewById(R.id.teacher_count_text);
        TextView classCountText = findViewById(R.id.class_count_text);

        if (studentCountText != null) {
            studentCountText.setText(String.valueOf(studentCount));
        }
        if (teacherCountText != null) {
            teacherCountText.setText(String.valueOf(teacherCount));
        }
        if (classCountText != null) {
            classCountText.setText(String.valueOf(classCount));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatistics();
    }
}
