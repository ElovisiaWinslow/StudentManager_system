package com.example.studentmanager_system.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.CourseAdapter;
import com.example.studentmanager_system.Util.myDatabaseHelper;
import com.example.studentmanager_system.Tools.Course;
import java.util.List;

public class CourseListActivity extends AppCompatActivity {
    private ListView courseListView;
    private myDatabaseHelper dbHelper;
    private String studentId;
    private CourseAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        // 获取当前学生ID
        studentId = getIntent().getStringExtra("studentID");
        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "学生信息获取失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = myDatabaseHelper.getInstance(this);
        courseListView = findViewById(R.id.lv_courses);
        loadCourses();
    }

    // 加载所有可选课程及已选状态
    private void loadCourses() {
        // 获取所有课程
        List<Course> allCourses = dbHelper.getAllCourses();
        // 获取已选课程ID列表
        List<String> selectedCourseIds = dbHelper.getSelectedCourseIds(studentId);

        // 设置适配器
        adapter = new CourseAdapter(this, allCourses, selectedCourseIds, studentId);
        // 选课状态变化监听（可选课/已选课切换）
        adapter.setOnCourseSelectListener((courseId, isSelect) -> {
            if (isSelect) {
                // 选课：插入student_course表
                boolean success = dbHelper.selectCourse(studentId, courseId);
                Toast.makeText(this, success ? "选课成功" : "选课失败", Toast.LENGTH_SHORT).show();
            } else {
                // 退课：删除student_course表记录
                boolean success = dbHelper.dropCourse(studentId, courseId);
                Toast.makeText(this, success ? "退课成功" : "退课失败", Toast.LENGTH_SHORT).show();
            }
            // 刷新列表
            loadCourses();
        });

        courseListView.setAdapter(adapter);
    }
}