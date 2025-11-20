// app/src/main/java/com/example/studentmanager_system/Activity/StudentProfileActivity.java
package com.example.studentmanager_system.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.LinearLayout; // 添加导入

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Tools.Course;
import com.example.studentmanager_system.Util.myDatabaseHelper;

import java.util.List;

public class StudentProfileActivity extends AppCompatActivity {
    private myDatabaseHelper dbHelper;
    private String studentId;
    private TextView tvStudentId, tvStudentName, tvStudentSex, tvStudentNumber;
    private TextView tvStudentGrade, tvStudentClass, tvStudentRanking, tvCompletedCredits;
    private TextView tvSelectedCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        studentId = getIntent().getStringExtra("studentId");
        if (studentId == null) {
            Toast.makeText(this, "学生信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = myDatabaseHelper.getInstance(this);

        initViews();
        loadStudentInfo();
        loadSelectedCourses();
    }

    private void initViews() {
        // 初始化 TextView 组件
        tvStudentId = findViewById(R.id.tv_student_id);
        tvStudentName = findViewById(R.id.tv_student_name);
        tvStudentSex = findViewById(R.id.tv_student_sex);
        tvStudentNumber = findViewById(R.id.tv_student_number);
        tvStudentGrade = findViewById(R.id.tv_student_grade);
        tvStudentClass = findViewById(R.id.tv_student_class);
        tvStudentRanking = findViewById(R.id.tv_student_ranking);
        tvCompletedCredits = findViewById(R.id.tv_completed_credits);
        tvSelectedCourses = findViewById(R.id.tv_selected_courses);

        // 移除导出按钮的初始化代码

        // 初始化返回按钮并设置点击监听器
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            finish(); // 关闭当前活动，返回上一个页面
        });

        // 初始化底部导航栏按钮并设置点击监听器
        initBottomNavigation();
    }

    /**
     * 初始化底部导航栏点击事件
     */
    private void initBottomNavigation() {
        // 首页按钮点击事件
        LinearLayout navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // 跳转到学生主页
                Intent intent = new Intent(StudentProfileActivity.this, studentActivity.class);
                intent.putExtra("id", studentId); // 传递学生ID
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 管理按钮点击事件
        LinearLayout navManage = findViewById(R.id.nav_manage);
        if (navManage != null) {
            navManage.setOnClickListener(v -> {
                // 跳转到学生管理页面
                Intent intent = new Intent(StudentProfileActivity.this, StudentManagementActivity.class);
                intent.putExtra("studentId", studentId); // 传递学生ID
                startActivity(intent);
            });
        }

        // 我的按钮点击事件（当前页面，无需处理）
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // 当前已在个人资料页面，无需跳转
            });
        }
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void loadStudentInfo() {
        // 查询学生基本信息
        var db = dbHelper.getReadableDatabase();
        var cursor = db.query(
                myDatabaseHelper.STUDENT_TABLE,
                new String[]{"id", "name", "sex", "number", "grade", "class", "GPA", "completedCredits"},
                "id=?",
                new String[]{studentId},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            tvStudentId.setText("学号: " + cursor.getString(0));
            tvStudentName.setText("姓名: " + cursor.getString(1));
            tvStudentSex.setText(cursor.getString(2));
            tvStudentNumber.setText(cursor.getString(3));
            tvStudentGrade.setText(cursor.getString(4));
            tvStudentClass.setText(cursor.getString(5));

            // 格式化GPA显示，保留两位小数
            double gpa = cursor.getDouble(6);
            tvStudentRanking.setText(String.format("%.2f", gpa));

            tvCompletedCredits.setText(cursor.getString(7));
        } else {
            Toast.makeText(this, "未找到学生信息", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }


    @SuppressLint("SetTextI18n")
    private void loadSelectedCourses() {
        // 查询学生已选课程及成绩
        List<Course> selectedCourses = dbHelper.getSelectedCourses(studentId);

        StringBuilder courseInfo = new StringBuilder();
        if (selectedCourses.isEmpty()) {
            courseInfo.append("暂无已选课程");
        } else {
            courseInfo.append("已选课程 (").append(selectedCourses.size()).append("门):\n\n");
            for (int i = 0; i < selectedCourses.size(); i++) {
                Course course = selectedCourses.get(i);
                courseInfo.append((i + 1)).append(". ")
                        .append(course.getName())
                        .append(" - 成绩: ");

                if (course.getScore() >= 0) {
                    courseInfo.append(course.getScore());
                } else {
                    courseInfo.append("未录入");
                }
                courseInfo.append("\n");
            }
        }

        tvSelectedCourses.setText(courseInfo.toString());
    }

    // 移除 exportStudentProfile 方法
}
