// app/src/main/java/com/example/studentmanager_system/Activity/ScheduleActivity.java
package com.example.studentmanager_system.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Tools.Course;
import com.example.studentmanager_system.Util.myDatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {
    private myDatabaseHelper dbHelper;
    private String studentId;
    private List<Course> selectedCourses;

    // 用于存储每个时间段的课程
    private final Map<String, List<Course>> scheduleMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        studentId = getIntent().getStringExtra("studentId");
        if (studentId == null) {
            Toast.makeText(this, "学生信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = myDatabaseHelper.getInstance(this);
        loadSelectedCourses();
        displaySchedule();
        setupBottomNavigation(); // 添加底部导航栏设置
    }

    private void loadSelectedCourses() {
        selectedCourses = dbHelper.getSelectedCourses(studentId);
        organizeSchedule();
    }

    private void organizeSchedule() {
        scheduleMap.clear();

        // 将课程按照时间段分组
        for (Course course : selectedCourses) {
            String classTime = course.getClassTime();
            if (classTime != null && !classTime.isEmpty()) {
                // 解析新的时间格式，例如："Mon1,2;Wed3,4"
                String[] timeSlots = classTime.split(";");
                for (String timeSlot : timeSlots) {
                    if (!scheduleMap.containsKey(timeSlot)) {
                        scheduleMap.put(timeSlot, new ArrayList<>());
                    }
                    scheduleMap.get(timeSlot).add(course);
                }
            }
        }
    }

    private void displaySchedule() {
        // 检查并显示课程冲突警告
        checkAndShowConflictWarning();

        // 遍历所有时间段并显示课程
        for (Map.Entry<String, List<Course>> entry : scheduleMap.entrySet()) {
            String timeSlot = entry.getKey();
            List<Course> courses = entry.getValue();

            // 获取对应的布局ID
            int layoutId = getLayoutId(timeSlot);
            if (layoutId == 0) continue;

            LinearLayout container = findViewById(layoutId);
            if (container == null) continue;

            // 清空原有内容
            container.removeAllViews();

            // 显示课程信息
            if (!courses.isEmpty()) {
                Course firstCourse = courses.get(0);

                // 设置背景色
                int color = getColorForCourse(firstCourse);
                container.setBackgroundColor(color);

                // 添加课程信息
                TextView courseName = new TextView(this);
                courseName.setText(firstCourse.getName());
                courseName.setTextColor(0xFFFFFFFF); // 白色文字
                courseName.setTextSize(12);
                courseName.setMaxLines(2);
                courseName.setEllipsize(android.text.TextUtils.TruncateAt.END);

                TextView courseLocation = new TextView(this);
                String location = firstCourse.getClassLocation();
                courseLocation.setText(location != null ? location : "地点待定");
                courseLocation.setTextColor(0xFFFFFFFF);
                courseLocation.setTextSize(10);

                container.addView(courseName);
                container.addView(courseLocation);

                // 如果有多门课程，添加省略号提示
                if (courses.size() > 1) {
                    TextView ellipsis = new TextView(this);
                    ellipsis.setText("...");
                    ellipsis.setTextColor(0xFFFFFFFF);
                    ellipsis.setTextSize(14);
                    container.addView(ellipsis);
                }

                // 设置点击事件
                final List<Course> finalCourses = new ArrayList<>(courses);
                container.setOnClickListener(v -> showCourseDetails(finalCourses));
            }
        }
    }

    private int getLayoutId(String timeSlot) {
        // 解析时间格式，例如："Mon1,2"
        if (timeSlot.length() < 4) return 0;

        String day = timeSlot.substring(0, 3); // "Mon", "Tue", etc.
        String periods = timeSlot.substring(3); // "1,2", "3,4", etc.

        // 将英文星期转换为布局ID
        String dayKey = "";
        switch (day) {
            case "Mon":
                dayKey = "monday";
                break;
            case "Tue":
                dayKey = "tuesday";
                break;
            case "Wed":
                dayKey = "wednesday";
                break;
            case "Thu":
                dayKey = "thursday";
                break;
            case "Fri":
                dayKey = "friday";
                break;
            default:
                return 0;
        }

        // 将节次转换为标识 (处理 "1,2" 格式)
        String periodKey = periods.replace(",", "_"); // "1_2"

        // 构造资源ID名称
        String resourceName = dayKey + "_" + periodKey;

        // 获取资源ID
        return getResources().getIdentifier(resourceName, "id", getPackageName());
    }

    private int getColorForCourse(Course course) {
        // 为不同课程生成不同的颜色
        int hash = course.getName().hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;

        // 调整亮度确保文字可读
        r = Math.min(200, Math.max(100, r));
        g = Math.min(200, Math.max(100, g));
        b = Math.min(200, Math.max(100, b));

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private void showCourseDetails(List<Course> courses) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("课程详情");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);

            if (i > 0) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 2));
                divider.setBackgroundColor(0xFFCCCCCC);
                layout.addView(divider);
            }

            TextView courseInfo = new TextView(this);
            StringBuilder info = new StringBuilder();
            info.append("课程名称: ").append(course.getName()).append("\n");
            info.append("授课教师: ").append(course.getTeacherName()).append("\n");
            info.append("上课时间: ").append(course.getClassTime()).append("\n");
            info.append("上课地点: ").append(course.getClassLocation() != null ?
                    course.getClassLocation() : "地点待定").append("\n");
            info.append("学分: ").append(course.getCredit()).append("\n");
            info.append("学时: ").append(course.getHours());

            courseInfo.setText(info.toString());
            courseInfo.setPadding(0, 16, 0, 16);
            layout.addView(courseInfo);
        }

        builder.setView(layout);
        builder.setPositiveButton("确定", null);
        builder.show();
    }

    /**
     * 检查是否存在课程时间冲突
     * @return true表示存在冲突，false表示无冲突
     */
    private boolean checkCourseConflicts() {
        // 检查scheduleMap中是否有时间段包含多于一门课程
        for (List<Course> courses : scheduleMap.values()) {
            if (courses.size() > 1) {
                return true; // 发现冲突
            }
        }
        return false; // 无冲突
    }

    /**
     * 检查并显示课程冲突警告
     */
    private void checkAndShowConflictWarning() {
        LinearLayout conflictWarningLayout = findViewById(R.id.conflict_warning_layout);
        if (checkCourseConflicts()) {
            conflictWarningLayout.setVisibility(View.VISIBLE);
        } else {
            conflictWarningLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 设置底部导航栏点击事件
     */
    private void setupBottomNavigation() {
        // 首页按钮
        LinearLayout navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // 跳转到学生主页
                Intent intent = new Intent(ScheduleActivity.this, studentActivity.class);
                intent.putExtra("id", studentId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 管理按钮
        LinearLayout navManage = findViewById(R.id.nav_manage);
        if (navManage != null) {
            navManage.setOnClickListener(v -> {
                // 跳转到学生管理页面
                Intent intent = new Intent(ScheduleActivity.this, StudentManagementActivity.class);
                intent.putExtra("studentId", studentId);
                startActivity(intent);
                finish();
            });
        }

        // 我的按钮
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // 跳转到学生个人资料页面
                Intent intent = new Intent(ScheduleActivity.this, StudentProfileActivity.class);
                intent.putExtra("studentId", studentId);
                startActivity(intent);
                finish();
            });
        }
    }
}
