// app/src/main/java/com/example/studentmanager_system/Activity/StudentProfileActivity.java
package com.example.studentmanager_system.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        tvStudentId = findViewById(R.id.tv_student_id);
        tvStudentName = findViewById(R.id.tv_student_name);
        tvStudentSex = findViewById(R.id.tv_student_sex);
        tvStudentNumber = findViewById(R.id.tv_student_number);
        tvStudentGrade = findViewById(R.id.tv_student_grade);
        tvStudentClass = findViewById(R.id.tv_student_class);
        tvStudentRanking = findViewById(R.id.tv_student_ranking);
        tvCompletedCredits = findViewById(R.id.tv_completed_credits);
        tvSelectedCourses = findViewById(R.id.tv_selected_courses);
        Button btnExportProfile = findViewById(R.id.btn_export_profile);

        btnExportProfile.setOnClickListener(v -> exportStudentProfile());
    }

    @SuppressLint("SetTextI18n")
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
            tvStudentSex.setText("性别: " + cursor.getString(2));
            tvStudentNumber.setText("电话: " + cursor.getString(3));
            tvStudentGrade.setText("年级: " + cursor.getString(4));
            tvStudentClass.setText("班级: " + cursor.getString(5));
            tvStudentRanking.setText("GPA: " + cursor.getString(6));
            tvCompletedCredits.setText("已完成学分: " + cursor.getString(7));
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

    private void exportStudentProfile() {
        // 导出学生个人信息和成绩
        Intent intent = new Intent(this, ExportProfileActivity.class);
        intent.putExtra("studentId", studentId);
        startActivity(intent);
    }
}
