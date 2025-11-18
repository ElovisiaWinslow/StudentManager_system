// app/src/main/java/com/example/studentmanager_system/Activity/CourseSelectionActivity.java
package com.example.studentmanager_system.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.CourseAdapter;
import com.example.studentmanager_system.Util.myDatabaseHelper;
import com.example.studentmanager_system.Tools.Course;
import java.util.List;

public class CourseSelectionActivity extends AppCompatActivity {
    private ListView courseListView;
    private myDatabaseHelper dbHelper;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_selection);

        studentId = getIntent().getStringExtra("studentId");
        if (studentId == null) {
            Toast.makeText(this, "学生信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 注册新的返回监听器替代已弃用的onBackPressed()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 回到管理页面，结束当前选课页面
                Intent intent = new Intent(CourseSelectionActivity.this, StudentManagementActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        dbHelper = myDatabaseHelper.getInstance(this);
        courseListView = findViewById(R.id.course_list);

        // 初始化底部导航栏点击事件
        initBottomNavigation();

        loadAllCourses();
    }

    private void initBottomNavigation() {
        // 首页按钮点击事件
        LinearLayout navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // 直接回首页，结束当前页面
                Intent intent = new Intent(CourseSelectionActivity.this, studentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 管理按钮点击事件
        LinearLayout navManage = findViewById(R.id.nav_manage);
        if (navManage != null) {
            navManage.setOnClickListener(v -> {
                // 回到管理页面，结束当前页面
                Intent intent = new Intent(CourseSelectionActivity.this, StudentManagementActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 我的按钮（当前页面，不需要处理）
    }

    private void loadAllCourses() {
        List<Course> allCourses = dbHelper.getAllCourses();
        if (allCourses.isEmpty()) {
            Toast.makeText(this, "暂无课程信息", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedCourseIds = dbHelper.getSelectedCourseIds(studentId);

        CourseAdapter adapter = new CourseAdapter(this, allCourses, selectedCourseIds);
        adapter.setShowOnlyDrop(false); // 显示选课/退课按钮

        // 修改选课按钮点击逻辑，改为跳转到课程详情页面
        adapter.setOnCourseSelectListener((courseId, isSelect) -> {
            // 添加参数检查
            if (studentId == null) {
                Toast.makeText(this, "课程或学生信息缺失", Toast.LENGTH_SHORT).show();
                return;
            }

            // 查找对应的课程对象
            Course selectedCourse = null;
            for (Course course : allCourses) {
                if (course.getId().equals(courseId) || ("AGGREGATED_" + course.getName()).equals(courseId)) {
                    selectedCourse = course;
                    break;
                }
            }

            if (selectedCourse != null) {
                // 跳转到课程详情页面
                Intent intent = new Intent(CourseSelectionActivity.this, CourseDetailActivity.class);
                intent.putExtra("course", selectedCourse);
                intent.putExtra("studentId", studentId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "未找到课程信息", Toast.LENGTH_SHORT).show();
            }
        });

        // 设置课程项点击监听器
        adapter.setOnItemClickListener(course -> {
            // 跳转到课程详情页面
            Intent intent = new Intent(CourseSelectionActivity.this, CourseDetailActivity.class);
            intent.putExtra("course", course);
            intent.putExtra("studentId", studentId);
            startActivity(intent);
        });

        courseListView.setAdapter(adapter);
    }
}
