// app/src/main/java/com/example/studentmanager_system/Activity/TeacherProfileActivity.java
package com.example.studentmanager_system.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;
import com.example.studentmanager_system.Tools.Teacher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 教师个人资料界面
 */
public class TeacherProfileActivity extends Activity {

    private myDatabaseHelper dbHelper;
    private String teacherId;
    private Teacher currentTeacher;

    // UI组件
    private ImageView btnBack;
    private TextView tvTeacherName;
    private TextView tvTeacherId;
    private TextView tvTeacherGender;
    private TextView tvTeacherPhone;
    private TextView tvTeacherCollege;
    private TextView tvTeacherDepartment;
    private TextView tvTeacherCourse;
    private TextView tvSystemVersion;
    private TextView tvUpdateTime;

    // 底部导航栏组件
    private LinearLayout navHome;
    private LinearLayout navManage;
    private LinearLayout navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_teacher_profile);

        // 初始化数据库帮助类
        dbHelper = myDatabaseHelper.getInstance(this);

        // 获取从教师主页传递的教师ID
        Intent intent = getIntent();
        teacherId = intent.getStringExtra("teacherId");

        // 初始化视图
        initViews();

        // 加载教师信息
        loadTeacherInfo();

        // 设置事件监听器
        setListeners();
    }

    /**
     * 初始化视图组件
     */
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvTeacherName = findViewById(R.id.tv_teacher_name);
        tvTeacherId = findViewById(R.id.tv_teacher_id);
        tvTeacherGender = findViewById(R.id.tv_teacher_gender);
        tvTeacherPhone = findViewById(R.id.tv_teacher_phone);
        tvTeacherCollege = findViewById(R.id.tv_teacher_college);
        tvTeacherDepartment = findViewById(R.id.tv_teacher_department);
        tvTeacherCourse = findViewById(R.id.tv_teacher_course);
        tvSystemVersion = findViewById(R.id.tv_system_version);
        tvUpdateTime = findViewById(R.id.tv_update_time);

        // 初始化底部导航栏
        navHome = findViewById(R.id.nav_home);
        navManage = findViewById(R.id.nav_manage);
        navProfile = findViewById(R.id.nav_profile);

        // 设置系统信息
        setSystemInfo();
    }

    /**
     * 设置系统信息
     */
    @SuppressLint("SetTextI18n")
    private void setSystemInfo() {
        // 获取应用版本号
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvSystemVersion.setText("v" + packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            tvSystemVersion.setText("未知版本");
        }

        // 设置更新时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        tvUpdateTime.setText(currentDate);
    }

    /**
     * 加载教师信息
     */
    private void loadTeacherInfo() {
        if (teacherId == null || teacherId.isEmpty()) {
            Toast.makeText(this, "无法获取教师信息", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                myDatabaseHelper.TEACHER_TABLE,
                null,
                "id=?",
                new String[]{teacherId},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
            String gender = cursor.getString(cursor.getColumnIndexOrThrow("sex"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String course = cursor.getString(cursor.getColumnIndexOrThrow("course"));
            String college = cursor.getString(cursor.getColumnIndexOrThrow("college"));
            String department = cursor.getString(cursor.getColumnIndexOrThrow("department"));

            currentTeacher = new Teacher(id, name, password, gender, phone, course, college, department);

            // 更新UI
            updateUI();
        } else {
            Toast.makeText(this, "未找到教师信息", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
    }

    /**
     * 更新界面显示
     */
    @SuppressLint("SetTextI18n")
    private void updateUI() {
        if (currentTeacher != null) {
            tvTeacherName.setText(currentTeacher.getName());
            tvTeacherId.setText("工号: " + currentTeacher.getId());
            tvTeacherGender.setText(currentTeacher.getGender() != null ? currentTeacher.getGender() : "未设置");
            tvTeacherPhone.setText(currentTeacher.getPhone() != null ? currentTeacher.getPhone() : "未设置");
            tvTeacherCollege.setText(currentTeacher.getCollege() != null ? currentTeacher.getCollege() : "未设置");
            tvTeacherDepartment.setText(currentTeacher.getDepartment() != null ? currentTeacher.getDepartment() : "未设置");

            // 显示所有课程（直接使用Teacher对象中的course字段）
            tvTeacherCourse.setText(currentTeacher.getCourse() != null ? currentTeacher.getCourse() : "未设置");
        }
    }

    /**
     * 设置事件监听器
     */
    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());

        // 设置底部导航栏点击事件
        navHome.setOnClickListener(v -> {
            // 返回教师主页
            Intent intent = new Intent(TeacherProfileActivity.this, teacherActivity.class);
            intent.putExtra("teacherId", teacherId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        navManage.setOnClickListener(v -> {
            // 跳转到教师管理页面
            Intent intent = new Intent(TeacherProfileActivity.this, TeacherManagementActivity.class);
            intent.putExtra("teacherId", teacherId);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            // 当前已在个人资料页面，无需跳转
        });
    }
}
