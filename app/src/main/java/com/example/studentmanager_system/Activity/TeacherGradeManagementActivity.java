// app/src/main/java/com/example/studentmanager_system/Activity/TeacherGradeManagementActivity.java
package com.example.studentmanager_system.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherGradeManagementActivity extends AppCompatActivity {
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

        dbHelper = myDatabaseHelper.getInstance(this);
        courseList = new ArrayList<>();

        courseListView = findViewById(R.id.course_list_view);
        // 修改标题显示
        setTitle("成绩管理");

        // 加载教师所授课程
        loadTeacherCourses();

        // 课程列表点击事件：展示该课程的学生成绩
        courseListView.setOnItemClickListener((parent, view, position, id) -> {
            String courseId = courseList.get(position).get("courseId");
            String courseName = courseList.get(position).get("courseName");
            showCourseStudentsWithGrades(courseId, courseName);
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
            @SuppressLint("Range") String courseId = cursor.getString(cursor.getColumnIndex("id"));
            @SuppressLint("Range") String courseName = cursor.getString(cursor.getColumnIndex("name"));

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

    // 显示某门课程的选课学生及成绩
    private void showCourseStudentsWithGrades(String courseId, String courseName) {
        // 查询选了这门课的学生列表及成绩
        var db = dbHelper.getReadableDatabase();
        var cursor = db.rawQuery(
                "SELECT sc.student_id, s.name, s.number, sc.score " +
                        "FROM " + myDatabaseHelper.STUDENT_COURSE_TABLE + " sc " +
                        "JOIN " + myDatabaseHelper.STUDENT_TABLE + " s ON sc.student_id = s.id " +
                        "WHERE sc.course_id=?",
                new String[]{courseId}
        );

        // 封装学生数据
        List<Map<String, String>> studentList = new ArrayList<>();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String studentId = cursor.getString(cursor.getColumnIndex("student_id"));
            @SuppressLint("Range") String studentName = cursor.getString(cursor.getColumnIndex("name"));
            @SuppressLint("Range") String studentNumber = cursor.getString(cursor.getColumnIndex("number"));
            @SuppressLint("Range") String score = cursor.getString(cursor.getColumnIndex("score"));

            Map<String, String> student = new HashMap<>();
            student.put("studentId", studentId);
            student.put("studentName", studentName);
            student.put("studentNumber", studentNumber);
            student.put("score", score);
            studentList.add(student);
        }
        cursor.close();

        if (studentList.isEmpty()) {
            Toast.makeText(this, courseName + "暂无学生选课", Toast.LENGTH_SHORT).show();
            return;
        }

        // 用对话框展示学生名单及成绩
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(courseName + "的学生成绩 (" + studentList.size() + "人)");

        // 构建学生信息数组
        String[] studentInfoArray = new String[studentList.size()];
        for (int i = 0; i < studentList.size(); i++) {
            Map<String, String> student = studentList.get(i);
            studentInfoArray[i] = (i + 1) + ". " + student.get("studentName") +
                    " (" + student.get("studentNumber") + ")" +
                    " - 成绩: " + student.get("score");
        }

        builder.setItems(studentInfoArray, (dialog, which) -> {
            // 点击学生项，弹出成绩修改对话框
            Map<String, String> selectedStudent = studentList.get(which);
            showScoreEditDialog(courseId, selectedStudent);
        });

        builder.setPositiveButton("确定", null);
        builder.show();
    }

    // 显示成绩修改对话框
    private void showScoreEditDialog(String courseId, Map<String, String> student) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改成绩 - " + student.get("studentName"));

        // 创建输入框
        final EditText input = new EditText(this);
        input.setText(student.get("score"));
        input.setHint("请输入成绩");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String scoreStr = input.getText().toString().trim();
            if (scoreStr.isEmpty()) {
                Toast.makeText(this, "成绩不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float score = Float.parseFloat(scoreStr);
                if (score < 0 || score > 100) {
                    Toast.makeText(this, "成绩应在0-100之间", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 更新成绩
                boolean success = updateStudentScore(courseId, student.get("studentId"), score);
                if (success) {
                    Toast.makeText(this, "成绩更新成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "成绩更新失败", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    // 更新学生成绩
// 修改 TeacherGradeManagementActivity.java 中的 updateStudentScore 方法
// 更新学生成绩并更新课程平均成绩
    private boolean updateStudentScore(String courseId, String studentId, float score) {
        var db = dbHelper.getWritableDatabase();
        var values = new android.content.ContentValues();
        values.put("score", score);

        int rowsAffected = db.update(
                myDatabaseHelper.STUDENT_COURSE_TABLE,
                values,
                "student_id=? AND course_id=?",
                new String[]{studentId, courseId}
        );

        // 如果成绩更新成功，同时更新课程的平均成绩
        if (rowsAffected > 0) {
            dbHelper.updateCourseAverageScore(courseId);
        }

        return rowsAffected > 0;
    }

}
