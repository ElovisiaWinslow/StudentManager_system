// app/src/main/java/com/example/studentmanager_system/Activity/CourseDetailActivity.java
package com.example.studentmanager_system.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private RadioGroup teacherRadioGroup;
    private List<Course> courseInstances;

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

        tvCourseName.setText(course.getName());
        tvCourseSubject.setText("学科: " + course.getSubject());
        tvCourseInfo.setText("学分: " + course.getCredit() + " | 学时: " + course.getHours());

        // 获取课程详细信息（包括所有教师）
        courseInstances = dbHelper.getCourseDetailsByName(course.getName());

        // 设置教师选择列表
        teacherRadioGroup = findViewById(R.id.teacher_radio_group);
        teacherRadioGroup.removeAllViews();

        for (int i = 0; i < courseInstances.size(); i++) {
            Course instance = courseInstances.get(i);
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(View.generateViewId());
            radioButton.setText(String.format("%s - 时间:%s 地点:%s",
                    instance.getTeacherName(),
                    instance.getClassTime() != null ? instance.getClassTime() : "待定",
                    instance.getClassLocation() != null ? instance.getClassLocation() : "待定"));
            radioButton.setTag(instance.getId()); // 用tag存储课程ID
            teacherRadioGroup.addView(radioButton);

            // 默认选中第一个
            if (i == 0) {
                radioButton.setChecked(true);
            }
        }

        // 设置选课按钮
        btnSelectCourse = findViewById(R.id.btn_select_course);
        btnSelectCourse.setOnClickListener(v -> handleCourseSelection());
    }

    private void updateSelectButtonState() {
        // 检查是否已经选择了同一门课程的任何实例
        if (dbHelper.isCourseNameSelected(studentId, course.getName())) {
            btnSelectCourse.setText("已选课");
            btnSelectCourse.setEnabled(false);
        } else {
            btnSelectCourse.setText("选课");
            btnSelectCourse.setEnabled(true);
        }
    }

    private void handleCourseSelection() {
        // 再次检查是否已经选择了同一门课程的任何实例
        if (dbHelper.isCourseNameSelected(studentId, course.getName())) {
            Toast.makeText(this, "您已选择该课程，无法再次选择", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取选中的教师
        int selectedRadioButtonId = teacherRadioGroup.getCheckedRadioButtonId();
        if (selectedRadioButtonId == -1) {
            Toast.makeText(this, "请选择授课教师", Toast.LENGTH_SHORT).show();
            return;
        }

        View selectedRadioButton = teacherRadioGroup.findViewById(selectedRadioButtonId);
        String selectedCourseId = (String) selectedRadioButton.getTag();

        // 选课
        boolean success = dbHelper.selectCourse(studentId, selectedCourseId,
                getTeacherIdByCourseId(selectedCourseId));
        Toast.makeText(this, success ? "选课成功" : "选课失败，可能已选该课程", Toast.LENGTH_SHORT).show();

        if (success) {
            updateSelectButtonState();
        }
    }

    private String getTeacherIdByCourseId(String courseId) {
        for (Course instance : courseInstances) {
            if (instance.getId().equals(courseId)) {
                return instance.getTeacherId();
            }
        }
        return null;
    }
}
