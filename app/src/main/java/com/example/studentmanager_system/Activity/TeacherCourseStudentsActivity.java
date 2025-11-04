package com.example.studentmanager_system.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;
import com.example.studentmanager_system.Tools.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TeacherCourseStudentsActivity.java
public class TeacherCourseStudentsActivity extends AppCompatActivity {
    private myDatabaseHelper dbHelper;
    private ListView courseListView;
    private List<Map<String, String>> courseList;
    private String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_course_list_layout);

        // 获取教师ID
        teacherId = getIntent().getStringExtra("teacherId");

        dbHelper = new myDatabaseHelper(this);
        courseList = new ArrayList<>();

        courseListView = findViewById(R.id.course_list_view);

        // 加载教师所授课程
        loadTeacherCourses();

        // 课程列表点击事件：展示该课程的学生
        courseListView.setOnItemClickListener((parent, view, position, id) -> {
            String courseId = courseList.get(position).get("courseId");
            String courseName = courseList.get(position).get("courseName");
            showCourseStudents(courseId, courseName);
        });
    }

    // 从数据库加载教师所授课程
    private void loadTeacherCourses() {
        courseList.clear();
        // 查询教师教授的所有课程
        var db = dbHelper.getReadableDatabase();
        var cursor = db.rawQuery(
                "SELECT id, name FROM " + myDatabaseHelper.COURSE_TABLE + " WHERE teacher_id=?",
                new String[]{teacherId}
        );

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "未查询到您教授的课程", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        // 封装课程数据
        while (cursor.moveToNext()) {
            String courseId = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            String courseName = cursor.getString(cursor.getColumnIndexOrThrow("name"));

            Map<String, String> course = new HashMap<>();
            course.put("courseId", courseId);
            course.put("courseName", courseName);
            courseList.add(course);
        }
        cursor.close();

        // 绑定适配器
        SimpleAdapter courseAdapter = new SimpleAdapter(
                this,
                courseList,
                R.layout.course_item_layout,
                new String[]{"courseName", "courseId"},
                new int[]{R.id.tv_course_name, R.id.tv_course_id}
        );
        courseListView.setAdapter(courseAdapter);
    }

    // 显示某门课程的选课学生
    private void showCourseStudents(String courseId, String courseName) {
        // 查询选了这门课的学生列表 - 使用正确的 student_course 表
        var db = dbHelper.getReadableDatabase();
        var cursor = db.rawQuery(
                "SELECT sc.student_id, s.name, s.sex, s.number " +
                        "FROM " + myDatabaseHelper.STUDENT_COURSE_TABLE + " sc " +
                        "JOIN " + myDatabaseHelper.STUDENT_TABLE + " s ON sc.student_id = s.id " +
                        "WHERE sc.course_id=?",
                new String[]{courseId}
        );

        // 根据学生ID查询学生详情
        List<Student> students = new ArrayList<>();
        while (cursor.moveToNext()) {
            students.add(new Student(
                    cursor.getString(0), // student_id
                    cursor.getString(1), // name
                    "", // password（无需展示）
                    cursor.getString(2), // sex
                    cursor.getString(3)  // number
            ));
        }
        cursor.close();

        if (students.isEmpty()) {
            Toast.makeText(this, courseName + "暂无学生选课", Toast.LENGTH_SHORT).show();
            return;
        }

        // 用对话框展示学生名单
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(courseName + "的学生名单 (" + students.size() + "人)");

        // 构建学生信息字符串
        StringBuilder studentInfo = new StringBuilder();
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            studentInfo.append(i + 1)
                    .append(". ")
                    .append(s.getName())
                    .append(" (")
                    .append(s.getNumber())
                    .append(")")
                    .append("\n");
        }

        builder.setMessage(studentInfo.toString());
        builder.setPositiveButton("确定", null);
        builder.show();
    }

}
