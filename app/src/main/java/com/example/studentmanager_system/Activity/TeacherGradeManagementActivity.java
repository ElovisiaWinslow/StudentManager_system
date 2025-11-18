// app/src/main/java/com/example/studentmanager_system/Activity/TeacherGradeManagementActivity.java
package com.example.studentmanager_system.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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

        // 设置底部导航栏
        setupBottomNavigation();

        // 课程列表点击事件：展示该课程的学生成绩
        courseListView.setOnItemClickListener((parent, view, position, id) -> {
            String courseId = courseList.get(position).get("courseId");
            String courseName = courseList.get(position).get("courseName");
            showCourseStudentsWithGrades(courseId, courseName);
        });
    }

    // 设置底部导航栏点击事件
    private void setupBottomNavigation() {
        // 底部导航栏 - 首页按钮
        LinearLayout navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // 回到教师主页
                Intent intent = new Intent(TeacherGradeManagementActivity.this, teacherActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 底部导航栏 - 管理按钮
        LinearLayout navManage = findViewById(R.id.nav_manage);
        if (navManage != null) {
            navManage.setOnClickListener(v -> {
                // 进入管理页面
                Intent intent = new Intent(TeacherGradeManagementActivity.this, TeacherManagementActivity.class);
                intent.putExtra("teacherId", teacherId);
                startActivity(intent);
            });
        }
    }

    // 从数据库加载教师所授课程
    private void loadTeacherCourses() {
        courseList.clear();
        // 查询教师教授的所有课程及其详细信息
        var db = dbHelper.getReadableDatabase();
        var cursor = db.rawQuery(
                "SELECT id, name, credit, hours, class_time, class_location, average_score FROM " +
                        myDatabaseHelper.COURSE_TABLE + " WHERE teacher_id=?",
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
            @SuppressLint("Range") String credit = String.valueOf(cursor.getFloat(cursor.getColumnIndex("credit")));
            @SuppressLint("Range") String hours = String.valueOf(cursor.getInt(cursor.getColumnIndex("hours")));
            @SuppressLint("Range") String classTime = cursor.getString(cursor.getColumnIndex("class_time"));
            @SuppressLint("Range") String classLocation = cursor.getString(cursor.getColumnIndex("class_location"));
            @SuppressLint({"Range", "DefaultLocale"}) String averageScore = String.format("%.2f", cursor.getFloat(cursor.getColumnIndex("average_score")));

            Map<String, String> course = new HashMap<>();
            course.put("courseId", courseId);
            course.put("courseName", courseName);
            course.put("credit", credit);
            course.put("hours", hours);
            course.put("classTime", classTime != null ? classTime : "未安排");
            course.put("classLocation", classLocation != null ? classLocation : "未安排");
            course.put("averageScore", averageScore);
            courseList.add(course);
        }
        cursor.close();

        // 绑定自定义适配器
        CourseDetailAdapter courseAdapter = new CourseDetailAdapter();
        courseListView.setAdapter(courseAdapter);
    }

    // 自定义适配器，用于展示课程详细信息
    private class CourseDetailAdapter extends BaseAdapter {
        private final LayoutInflater inflater;

        public CourseDetailAdapter() {
            this.inflater = LayoutInflater.from(TeacherGradeManagementActivity.this);
        }

        @Override
        public int getCount() {
            return courseList.size();
        }

        @Override
        public Object getItem(int position) {
            return courseList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.course_detail_item, parent, false);
                holder = new ViewHolder();
                holder.tvCourseName = convertView.findViewById(R.id.tv_course_name);
                holder.tvCourseCredit = convertView.findViewById(R.id.tv_course_credit);
                holder.tvCourseHours = convertView.findViewById(R.id.tv_course_hours);
                holder.tvCourseTime = convertView.findViewById(R.id.tv_course_time);
                holder.tvCourseLocation = convertView.findViewById(R.id.tv_course_location);
                holder.tvCourseAverageScore = convertView.findViewById(R.id.tv_course_average_score);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Map<String, String> course = courseList.get(position);

            holder.tvCourseName.setText(course.get("courseName"));
            holder.tvCourseCredit.setText(course.get("credit"));
            holder.tvCourseHours.setText(course.get("hours"));
            holder.tvCourseTime.setText(course.get("classTime"));
            holder.tvCourseLocation.setText(course.get("classLocation"));
            holder.tvCourseAverageScore.setText(course.get("averageScore"));

            return convertView;
        }

        class ViewHolder {
            TextView tvCourseName;
            TextView tvCourseCredit;
            TextView tvCourseHours;
            TextView tvCourseTime;
            TextView tvCourseLocation;
            TextView tvCourseAverageScore;
        }
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
                    " (" + student.get("studentId") + ")" +
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

        // 如果成绩更新成功，同时更新课程的平均成绩并刷新界面
        if (rowsAffected > 0) {
            dbHelper.updateCourseAverageScore(courseId);
            // 刷新界面显示最新的平均成绩
            refreshCourseAverageScore(courseId);
        }

        return rowsAffected > 0;
    }

    // 刷新特定课程的平均成绩显示
    @SuppressLint("DefaultLocale")
    private void refreshCourseAverageScore(String courseId) {
        // 查询最新的平均成绩
        var db = dbHelper.getReadableDatabase();
        var cursor = db.rawQuery(
                "SELECT average_score FROM " + myDatabaseHelper.COURSE_TABLE + " WHERE id=?",
                new String[]{courseId}
        );

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") float newAverageScore = cursor.getFloat(cursor.getColumnIndex("average_score"));

            // 在courseList中找到对应课程并更新平均成绩
            for (Map<String, String> course : courseList) {
                if (courseId.equals(course.get("courseId"))) {
                    course.put("averageScore", String.format("%.2f", newAverageScore));
                    break;
                }
            }

            // 通知适配器数据已更改
            ((BaseAdapter) courseListView.getAdapter()).notifyDataSetChanged();
        }
        cursor.close();
    }
}
