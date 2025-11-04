// app/src/main/java/com/example/studentmanager_system/Activity/SelectedCoursesActivity.java
package com.example.studentmanager_system.Activity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;
import com.example.studentmanager_system.Util.CourseAdapter;
import com.example.studentmanager_system.Tools.Course;

import java.util.List;

public class SelectedCoursesActivity extends AppCompatActivity {
    private ListView courseListView;
    private myDatabaseHelper dbHelper;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_courses);

        studentId = getIntent().getStringExtra("studentId");
        if (studentId == null) {
            Toast.makeText(this, "学生信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = myDatabaseHelper.getInstance(this);
        courseListView = findViewById(R.id.selected_course_list);

        loadSelectedCourses();
    }

    private void loadSelectedCourses() {
        // 获取学生已选课程
        List<Course> selectedCourses = dbHelper.getSelectedCourses(studentId);

        if (selectedCourses.isEmpty()) {
            Toast.makeText(this, "您还没有选择任何课程", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取已选课程ID列表（用于适配器）
        List<String> selectedCourseIds = dbHelper.getSelectedCourseIds(studentId);

        // 创建适配器，设置为只显示退课按钮模式
        CourseAdapter adapter = new CourseAdapter(this, selectedCourses, selectedCourseIds);
        adapter.setShowOnlyDrop(true); // 设置为只显示退课按钮模式

        // 设置退课监听器
        adapter.setOnCourseSelectListener((courseId, isSelect) -> {
            if (!isSelect) { // 退课操作
                boolean success = dbHelper.dropCourse(studentId, courseId);
                Toast.makeText(this, success ? "退课成功" : "退课失败", Toast.LENGTH_SHORT).show();

                if (success) {
                    // 刷新列表
                    loadSelectedCourses();
                }
            }
        });

        // 设置课程项点击监听器（查看课程详情）
        adapter.setOnItemClickListener(course -> {
            // 跳转到课程详情页面
            android.content.Intent intent = new android.content.Intent(SelectedCoursesActivity.this, CourseDetailActivity.class);
            intent.putExtra("course", course);
            intent.putExtra("studentId", studentId);
            startActivity(intent);
        });

        courseListView.setAdapter(adapter);
    }
}
