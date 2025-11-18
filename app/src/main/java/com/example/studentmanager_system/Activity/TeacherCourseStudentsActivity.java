// app/src/main/java/com/example/studentmanager_system/Activity/TeacherCourseStudentsActivity.java
package com.example.studentmanager_system.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;
import com.example.studentmanager_system.Tools.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TeacherCourseStudentsActivity.java
public class TeacherCourseStudentsActivity extends AppCompatActivity {
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

        dbHelper = new myDatabaseHelper(this);
        courseList = new ArrayList<>();

        courseListView = findViewById(R.id.course_list_view);
        // 修改标题显示
        setTitle("课程学生管理");

        // 加载教师所授课程
        loadTeacherCourses();

        // 设置底部导航栏
        setupBottomNavigation();

        // 课程列表点击事件：展示该课程的学生
        courseListView.setOnItemClickListener((parent, view, position, id) -> {
            String courseId = courseList.get(position).get("courseId");
            String courseName = courseList.get(position).get("courseName");
            showCourseStudents(courseId, courseName);
        });
    }

    // 设置底部导航栏点击事件
    private void setupBottomNavigation() {
        // 底部导航栏 - 首页按钮
        LinearLayout navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // 回到教师主页
                Intent intent = new Intent(TeacherCourseStudentsActivity.this, teacherActivity.class);
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
                Intent intent = new Intent(TeacherCourseStudentsActivity.this, TeacherManagementActivity.class);
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
            this.inflater = LayoutInflater.from(TeacherCourseStudentsActivity.this);
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

    // 显示某门课程的选课学生
    private void showCourseStudents(String courseId, String courseName) {
        // 查询选了这门课的学生列表 - 使用正确的 student_course 表
        var db = dbHelper.getReadableDatabase();
        var cursor = db.rawQuery(
                "SELECT sc.student_id, s.name, s.sex, s.number, s.grade, s.class " +
                        "FROM " + myDatabaseHelper.STUDENT_COURSE_TABLE + " sc " +
                        "JOIN " + myDatabaseHelper.STUDENT_TABLE + " s ON sc.student_id = s.id " +
                        "WHERE sc.course_id=?",
                new String[]{courseId}
        );

        // 根据学生ID查询学生详情
        List<Student> students = new ArrayList<>();
        while (cursor.moveToNext()) {
            Student student = new Student(
                    cursor.getString(0), // student_id
                    cursor.getString(1), // name
                    "", // password（无需展示）
                    cursor.getString(2), // sex
                    cursor.getString(3)  // number
            );
            // 设置年级和班级信息
            student.setGrade(cursor.getInt(4));
            student.setClazz(cursor.getString(5));
            students.add(student);
        }
        cursor.close();

        if (students.isEmpty()) {
            Toast.makeText(this, courseName + "暂无学生选课", Toast.LENGTH_SHORT).show();
            return;
        }

        // 用对话框展示学生名单（可点击）
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(courseName + "的学生名单 (" + students.size() + "人)");

        // 创建学生列表适配器
        StudentListAdapter adapter = new StudentListAdapter(students);

        ListView listView = new ListView(this);
        listView.setAdapter(adapter);

        builder.setView(listView);
        AlertDialog dialog = builder.create();

        // 设置学生列表项点击事件
        listView.setOnItemClickListener((parent, view, position, id) -> {
            dialog.dismiss();
            showStudentDetails(students.get(position));
        });

        dialog.show();
    }

    // 学生列表适配器
    private class StudentListAdapter extends BaseAdapter {
        private final List<Student> students;
        private final LayoutInflater inflater;

        public StudentListAdapter(List<Student> students) {
            this.students = students;
            this.inflater = LayoutInflater.from(TeacherCourseStudentsActivity.this);
        }

        @Override
        public int getCount() {
            return students.size();
        }

        @Override
        public Object getItem(int position) {
            return students.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
                holder = new ViewHolder();
                holder.text1 = convertView.findViewById(android.R.id.text1);
                holder.text2 = convertView.findViewById(android.R.id.text2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Student student = students.get(position);
            holder.text1.setText(student.getName());
            holder.text2.setText("学号: " + student.getId());

            return convertView;
        }

        class ViewHolder {
            TextView text1;
            TextView text2;
        }
    }

    // 显示学生详细信息
    private void showStudentDetails(Student student) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("学生详细信息");

        // 构建学生详细信息字符串
        String details = "姓名: " + student.getName() + "\n" +
                "学号: " + student.getId() + "\n" +
                "性别: " + student.getSex() + "\n" +
                "电话: " + (student.getNumber() != null ? student.getNumber() : "未填写") + "\n" +
                "年级: " + student.getGrade() + "\n" +
                "班级: " + (student.getClazz() != null ? student.getClazz() : "未分配") + "\n" +
                "已完成学分: " + student.getCompletedCredits() + "\n" +
                "GPA: " + student.getGPA() + "\n";

        builder.setMessage(details);
        builder.setPositiveButton("确定", null);
        builder.show();
    }
}
