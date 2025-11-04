// app/src/main/java/com/example/studentmanager_system/Activity/CourseDetailActivity.java
package com.example.studentmanager_system.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Tools.Course;
import com.example.studentmanager_system.Util.myDatabaseHelper;
import java.util.List;

public class CourseDetailActivity extends AppCompatActivity {
    private Course course;
    private String studentId;
    private myDatabaseHelper dbHelper;
    private Button btnSelectCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // 获取传递的数据
        course = getCourseFromIntent();
        studentId = getIntent().getStringExtra("studentId");

        if (course == null || studentId == null) {
            Toast.makeText(this, "数据错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = myDatabaseHelper.getInstance(this);

        // 注册返回监听器
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        initViews();
        updateSelectButtonState();
    }

    @SuppressWarnings("deprecation")
    private Course getCourseFromIntent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return getIntent().getSerializableExtra("course", Course.class);
        } else {
            return (Course) getIntent().getSerializableExtra("course");
        }
    }

    private void initViews() {
        // 设置课程基本信息
        TextView tvCourseName = findViewById(R.id.tv_course_name);
        TextView tvCourseSubject = findViewById(R.id.tv_course_subject);
        TextView tvCourseInfo = findViewById(R.id.tv_course_info);
        TextView tvCourseTime = findViewById(R.id.tv_course_time);
        TextView tvCourseLocation = findViewById(R.id.tv_course_location);

        tvCourseName.setText(course.getName());
        tvCourseSubject.setText("学科: " + course.getSubject());
        tvCourseInfo.setText("学分: " + course.getCredit() + " | 学时: " + course.getHours());
        tvCourseTime.setText("时间: " + (course.getClassTime() != null ?
                course.getClassTime() : "待定"));
        tvCourseLocation.setText("地点: " + (course.getClassLocation() != null ?
                course.getClassLocation() : "待定"));

        // 设置教师列表
        ListView listTeachers = findViewById(R.id.list_teachers);
        List<String> teacherNames = course.getTeacherNames();
        if (teacherNames != null && !teacherNames.isEmpty()) {
            ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(
                    this, R.layout.teacher_item, R.id.tv_teacher_name, teacherNames);
            listTeachers.setAdapter(teacherAdapter);
        } else {
            // 如果没有教师列表，显示默认教师姓名
            String[] defaultTeacher = {course.getTeacherName() != null ?
                    course.getTeacherName() : "未知教师"};
            ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(
                    this, R.layout.teacher_item, R.id.tv_teacher_name, defaultTeacher);
            listTeachers.setAdapter(teacherAdapter);
        }

        // 设置选课按钮
        btnSelectCourse = findViewById(R.id.btn_select_course);
        btnSelectCourse.setOnClickListener(v -> handleCourseSelection());
    }

    private void updateSelectButtonState() {
        boolean isSelected = dbHelper.isCourseSelected(studentId, course.getId());
        if (isSelected) {
            btnSelectCourse.setText("退课");
            btnSelectCourse.setBackgroundColor(
                    androidx.core.content.ContextCompat.getColor(this, android.R.color.holo_red_light));
        } else {
            btnSelectCourse.setText("选课");
            btnSelectCourse.setBackgroundColor(
                    androidx.core.content.ContextCompat.getColor(this, android.R.color.holo_blue_light));
        }
    }

    private void handleCourseSelection() {
        boolean isSelected = dbHelper.isCourseSelected(studentId, course.getId());
        boolean success;

        if (isSelected) {
            // 退课
            success = dbHelper.dropCourse(studentId, course.getId());
            Toast.makeText(this, success ? "退课成功" : "退课失败", Toast.LENGTH_SHORT).show();
        } else {
            // 选课
            success = dbHelper.selectCourse(studentId, course.getId());
            Toast.makeText(this, success ? "选课成功" : "选课失败，可能已选该课程", Toast.LENGTH_SHORT).show();
        }

        if (success) {
            updateSelectButtonState();
        }
    }
}
