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

public class TeacherCourseStudentsActivity extends AppCompatActivity {
    private ListView courseListView;
    private myDatabaseHelper dbHelper;
    private String teacherId;
    private List<Map<String, String>> courseList = new ArrayList<>(); // 存储课程信息

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_course_students_layout);

        teacherId = getIntent().getStringExtra("teacherId");
        dbHelper = myDatabaseHelper.getInstance(this);
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
                "select id, name from " + myDatabaseHelper.COURSE_TABLE + " where teacher_id=?",
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

        // 显示课程列表
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                courseList,
                android.R.layout.simple_list_item_1,
                new String[]{"courseName"}, // 显示课程名称
                new int[]{android.R.id.text1}
        );
        courseListView.setAdapter(adapter);
    }

    // 展示某课程的学生名单
    private void showCourseStudents(String courseId, String courseName) {
        // 查询选了该课程的学生ID
        var db = dbHelper.getReadableDatabase();
        var cursor = db.rawQuery(
                "select student_id from " + myDatabaseHelper.STUDENT_COURSE_TABLE + " where course_id=?",
                new String[]{courseId}
        );

        List<String> studentIds = new ArrayList<>();
        while (cursor.moveToNext()) {
            studentIds.add(cursor.getString(cursor.getColumnIndexOrThrow("student_id")));
        }
        cursor.close();

        if (studentIds.isEmpty()) {
            Toast.makeText(this, courseName + "暂无学生选课", Toast.LENGTH_SHORT).show();
            return;
        }

        // 根据学生ID查询学生详情
        List<Student> students = new ArrayList<>();
        for (String studentId : studentIds) {
            var studentCursor = db.rawQuery(
                    "select id, name, sex, number from " + myDatabaseHelper.STUDENT_TABLE + " where id=?",
                    new String[]{studentId}
            );
            if (studentCursor.moveToNext()) {
                students.add(new Student(
                        studentCursor.getString(0), // id
                        studentCursor.getString(1), // name
                        "", // password（无需展示）
                        studentCursor.getString(2), // sex
                        studentCursor.getString(3), // number
                        0, 0, 0 // 成绩（无需展示）
                ));
            }
            studentCursor.close();
        }

        // 用对话框展示学生名单
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(courseName + "的学生名单");

        // 构建学生信息字符串
        StringBuilder studentInfo = new StringBuilder();
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            studentInfo.append(i + 1)
                    .append(". 学号：").append(s.getId())
                    .append("，姓名：").append(s.getName())
                    .append("，性别：").append(s.getSex())
                    .append("，电话：").append(s.getNumber())
                    .append("\n");
        }

        builder.setMessage(studentInfo.toString());
        builder.setPositiveButton("确定", null);
        builder.show();
    }
}