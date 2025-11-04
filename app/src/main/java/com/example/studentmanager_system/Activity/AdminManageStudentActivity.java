package com.example.studentmanager_system.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;
import java.util.Calendar;

/**
 * 学生管理主页面 - 二级子页面
 */
public class AdminManageStudentActivity extends AppCompatActivity {
    private myDatabaseHelper dbHelper;
    private TextView tvTotalStudents, tvTotalClasses, tvWarningCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_students);

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
        loadStatisticsData();
        setupClickListeners();
    }

    private void initViews() {
        // 初始化统计数据显示控件
        tvTotalStudents = findViewById(R.id.tv_total_students);
        tvTotalClasses = findViewById(R.id.tv_new_this_month);
        tvWarningCount = findViewById(R.id.tv_graduated);

        // 初始化搜索相关组件
        EditText etSearch = findViewById(R.id.et_search);
        Button btnSearch = findViewById(R.id.btn_search);

        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                String keyword = etSearch.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    // 启动学生信息页面并传递搜索关键字
                    Intent intent = new Intent(AdminManageStudentActivity.this, studentinfoActivity.class);
                    intent.putExtra("search_keyword", keyword);
                    startActivity(intent);
                }
            });
        }
    }

    private void loadStatisticsData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int currentAcademicYear = getCurrentAcademicYear();

        // 学生总数
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + myDatabaseHelper.STUDENT_TABLE, null);
        if (cursor.moveToFirst()) {
            tvTotalStudents.setText(String.valueOf(cursor.getInt(0)));
        }
        cursor.close();

        // 班级总数
        cursor = db.rawQuery("SELECT COUNT(DISTINCT class) FROM " + myDatabaseHelper.STUDENT_TABLE, null);
        if (cursor.moveToFirst()) {
            tvTotalClasses.setText(String.valueOf(cursor.getInt(0)));
        }
        cursor.close();

        // 学分预警学生数（当前学年的大四学生中已完成学分<100）
        int seniorGrade = currentAcademicYear - 3; // 大四学生年级
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + myDatabaseHelper.STUDENT_TABLE +
                        " WHERE grade = ? AND completedCredits < 100",
                new String[]{String.valueOf(seniorGrade)});

        if (cursor.moveToFirst()) {
            tvWarningCount.setText(String.valueOf(cursor.getInt(0)));
        } else {
            tvWarningCount.setText("0");
        }
        cursor.close();
    }

    private int getCurrentAcademicYear() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH); // 0-based, so September is 8

        // 如果当前月份小于9月(即还没到新学年)，则属于上一学年
        if (currentMonth < 8) { // 8 represents September
            return currentYear - 1;
        } else {
            return currentYear;
        }
    }

    private void setupClickListeners() {
        setupBottomNavigation();
        setupStudentManagementCards();
    }

    private void setupStudentManagementCards() {
        // 添加跳转到添加学生信息页面的点击事件
        CardView addStudentCard = findViewById(R.id.card_add_student);
        if (addStudentCard != null) {
            addStudentCard.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManageStudentActivity.this, add_studentinfoActivity.class);
                // 传递标识，表示是新增学生而不是修改学生
                intent.putExtra("haveData", "false");
                startActivity(intent);
            });
        }

        // 添加跳转到学生信息总览页面的点击事件
        CardView queryStudentCard = findViewById(R.id.card_query_student);
        if (queryStudentCard != null) {
            queryStudentCard.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManageStudentActivity.this, studentinfoActivity.class);
                startActivity(intent);
            });
        }

        // 添加跳转到数据管理页面的点击事件
        CardView batchOperationCard = findViewById(R.id.card_batch_operation);
        if (batchOperationCard != null) {
            batchOperationCard.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManageStudentActivity.this, DataManagementActivity.class);
                startActivity(intent);
            });
        }

        // 在setupStudentManagementCards方法中添加以下代码
        CardView dataStatisticsCard = findViewById(R.id.card_data_statistics);
        if (dataStatisticsCard != null) {
            dataStatisticsCard.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManageStudentActivity.this, StudentStatisticsActivity.class);
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
                Intent intent = new Intent(AdminManageStudentActivity.this, adminActivity.class);
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
                Intent intent = new Intent(AdminManageStudentActivity.this, AdminManagementActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回此页面时重新加载统计数据
        loadStatisticsData();
    }
}
