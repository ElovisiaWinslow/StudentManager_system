// app/src/main/java/com/example/studentmanager_system/Activity/updateCourseActivity.java
package com.example.studentmanager_system.Activity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Tools.Course;
import com.example.studentmanager_system.Util.myDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class updateCourseActivity extends AppCompatActivity {
    private String courseName;
    private String courseSubject;
    private myDatabaseHelper dbHelper;
    private List<Course> courseList;
    private EditText etCourseName, etCourseSubject, etCourseCredit, etCourseHours, etCourseGrade;
    private LinearLayout teacherCoursesContainer;

    // 用于保存教师课程信息项的视图引用
    private List<TeacherCourseViewHolder> teacherCourseViewHolders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_update_layout);

        dbHelper = myDatabaseHelper.getInstance(this);
        courseList = new ArrayList<>();
        teacherCourseViewHolders = new ArrayList<>();

        // 获取传递的课程名称
        courseName = getIntent().getStringExtra("course_name");
        if (TextUtils.isEmpty(courseName)) {
            Toast.makeText(this, "课程信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadCourseData();
        setupViews();
    }

    private void initViews() {
        etCourseName = findViewById(R.id.et_course_name);
        etCourseSubject = findViewById(R.id.et_course_subject);
        etCourseCredit = findViewById(R.id.et_course_credit);
        etCourseHours = findViewById(R.id.et_course_hours);
        etCourseGrade = findViewById(R.id.et_course_grade); // 添加年级输入框引用
        teacherCoursesContainer = findViewById(R.id.teacher_courses_container);

        Button btnSaveChanges = findViewById(R.id.btn_save_changes);
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void loadCourseData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                myDatabaseHelper.COURSE_TABLE,
                null,
                "name = ?",
                new String[]{courseName},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            do {
                Course course = new Course();
                course.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
                course.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                course.setTeacherId(cursor.getString(cursor.getColumnIndexOrThrow("teacher_id")));
                course.setSubject(cursor.getString(cursor.getColumnIndexOrThrow("subject")));
                course.setCredit(cursor.getFloat(cursor.getColumnIndexOrThrow("credit")));
                course.setHours(cursor.getInt(cursor.getColumnIndexOrThrow("hours")));
                course.setClassTime(cursor.getString(cursor.getColumnIndexOrThrow("class_time")));
                course.setClassLocation(cursor.getString(cursor.getColumnIndexOrThrow("class_location")));
                course.setAverageScore(cursor.getFloat(cursor.getColumnIndexOrThrow("average_score")));
                course.setGrade(cursor.getInt(cursor.getColumnIndexOrThrow("grade")));

                courseList.add(course);

                // 获取第一个课程的信息作为基本信息
                if (courseList.size() == 1) {
                    courseSubject = course.getSubject();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    @SuppressLint("SetTextI18n")
    private void setupViews() {
        if (courseList.isEmpty()) {
            Toast.makeText(this, "未找到课程信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 设置课程基本信息
        etCourseName.setText(courseName);
        etCourseSubject.setText(courseSubject);

        // 设置第一个课程的学分和学时作为默认值
        Course firstCourse = courseList.get(0);
        etCourseCredit.setText(String.valueOf(firstCourse.getCredit()));
        etCourseHours.setText(String.valueOf(firstCourse.getHours()));
        etCourseGrade.setText(String.valueOf(firstCourse.getGrade())); // 设置年级默认值

        // 为每个教师课程创建视图项
        teacherCoursesContainer.removeAllViews();
        teacherCourseViewHolders.clear();

        for (Course course : courseList) {
            // 获取教师姓名
            String teacherName = getTeacherNameById(course.getTeacherId());

            // 创建教师课程项视图
            View teacherCourseView = LayoutInflater.from(this)
                    .inflate(R.layout.teacher_course_item, teacherCoursesContainer, false);

            TeacherCourseViewHolder holder = new TeacherCourseViewHolder(teacherCourseView);
            holder.tvTeacherName.setText(teacherName != null ? teacherName : "未知教师");
            holder.tvTeacherId.setText("教师ID: " + course.getTeacherId());
            holder.etClassTime.setText(course.getClassTime() != null ? course.getClassTime() : "");
            holder.etClassLocation.setText(course.getClassLocation() != null ? course.getClassLocation() : "");

            // 保存ViewHolder引用
            holder.courseId = course.getId();
            teacherCourseViewHolders.add(holder);

            teacherCoursesContainer.addView(teacherCourseView);
        }
    }

    private String getTeacherNameById(String teacherId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                myDatabaseHelper.TEACHER_TABLE,
                new String[]{"name"},
                "id = ?",
                new String[]{teacherId},
                null, null, null
        );

        String teacherName = null;
        if (cursor.moveToFirst()) {
            teacherName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        }
        cursor.close();
        return teacherName;
    }

    private void saveChanges() {
        // 验证输入
        String subject = etCourseSubject.getText().toString().trim();
        String creditStr = etCourseCredit.getText().toString().trim();
        String hoursStr = etCourseHours.getText().toString().trim();
        String gradeStr = etCourseGrade.getText().toString().trim(); // 获取年级输入

        if (TextUtils.isEmpty(subject)) {
            Toast.makeText(this, "请输入课程学科", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(creditStr)) {
            Toast.makeText(this, "请输入学分", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(hoursStr)) {
            Toast.makeText(this, "请输入学时", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(gradeStr)) {
            Toast.makeText(this, "请输入年级", Toast.LENGTH_SHORT).show();
            return;
        }

        float credit;
        int hours;
        int grade; // 添加年级变量
        try {
            credit = Float.parseFloat(creditStr);
            hours = Integer.parseInt(hoursStr);
            grade = Integer.parseInt(gradeStr); // 解析年级
        } catch (NumberFormatException e) {
            Toast.makeText(this, "学分、学时或年级格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 更新所有同名课程的公共信息（学科、学分、学时、年级）
            for (Course course : courseList) {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put("subject", subject);
                values.put("credit", credit);
                values.put("hours", hours);
                values.put("grade", grade); // 添加年级更新

                db.update(
                        myDatabaseHelper.COURSE_TABLE,
                        values,
                        "id = ?",
                        new String[]{course.getId()}
                );
            }

            // 更新每个教师课程的独立信息（上课时间、地点）
            for (TeacherCourseViewHolder holder : teacherCourseViewHolders) {
                String classTime = holder.etClassTime.getText().toString().trim();
                String classLocation = holder.etClassLocation.getText().toString().trim();

                android.content.ContentValues values = new android.content.ContentValues();
                values.put("class_time", classTime.isEmpty() ? null : classTime);
                values.put("class_location", classLocation.isEmpty() ? null : classLocation);

                db.update(
                        myDatabaseHelper.COURSE_TABLE,
                        values,
                        "id = ?",
                        new String[]{holder.courseId}
                );
            }

            db.setTransactionSuccessful();
            Toast.makeText(this, "课程信息更新成功", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "更新失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
    }

    // 教师课程项的ViewHolder
    private static class TeacherCourseViewHolder {
        TextView tvTeacherName;
        TextView tvTeacherId;
        EditText etClassTime;
        EditText etClassLocation;
        String courseId;

        TeacherCourseViewHolder(View view) {
            tvTeacherName = view.findViewById(R.id.tv_teacher_name);
            tvTeacherId = view.findViewById(R.id.tv_teacher_id);
            etClassTime = view.findViewById(R.id.et_class_time);
            etClassLocation = view.findViewById(R.id.et_class_location);
        }
    }
}
