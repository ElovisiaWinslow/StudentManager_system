package com.example.studentmanager_system.Activity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.CourseAdapter;
import com.example.studentmanager_system.Util.myDatabaseHelper;
import com.example.studentmanager_system.Tools.Course;
import java.util.List;

public class SelectedCoursesActivity extends AppCompatActivity {
    private ListView selectedCourseListView;
    private myDatabaseHelper dbHelper;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_courses); // 可复用course_list布局

        studentId = getIntent().getStringExtra("studentId");
        if (studentId == null) {
            Toast.makeText(this, "学生信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = myDatabaseHelper.getInstance(this);
        selectedCourseListView = findViewById(R.id.selected_course_list);
        loadSelectedCourses();
    }

    // 加载已选课程
    private void loadSelectedCourses() {
        List<Course> selectedCourses = dbHelper.getSelectedCourses(studentId);
        if (selectedCourses.isEmpty()) {
            Toast.makeText(this, "尚未选择任何课程", Toast.LENGTH_SHORT).show();
            return;
        }

        // 已选课程列表中只显示退课按钮
        CourseAdapter adapter = new CourseAdapter(this, selectedCourses,
                dbHelper.getSelectedCourseIds(studentId), studentId);
        adapter.setShowOnlyDrop(true); // 自定义适配器方法，只显示退课按钮
        adapter.setOnCourseSelectListener((courseId, isSelect) -> {
            // 已选课程列表中只有退课操作（isSelect恒为false）
            boolean success = dbHelper.dropCourse(studentId, courseId);
            Toast.makeText(this, success ? "退课成功" : "退课失败", Toast.LENGTH_SHORT).show();
            loadSelectedCourses();
        });

        selectedCourseListView.setAdapter(adapter);
    }
}