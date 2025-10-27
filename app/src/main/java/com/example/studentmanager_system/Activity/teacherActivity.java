package com.example.studentmanager_system.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;

/**
 * 教师主界面
 */
public class teacherActivity extends Activity {
    private Button selectInfoBtn;      // 查看个人信息按钮
    private Button changePwdBtn;       // 修改密码按钮

    private Button queryStudentsBtn;

    private myDatabaseHelper dbHelper; // 数据库帮助类
    private String teacherId;          // 教师ID（从登录页传递）
    private Intent intent;             // 用于接收登录页传递的ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 去除标题栏
        setContentView(R.layout.teacher_layout);

        // 初始化控件
        initViews();

        // 初始化数据库
        dbHelper = myDatabaseHelper.getInstance(this);

        // 获取从登录页面传递的教师ID
        intent = getIntent();
        teacherId = intent.getStringExtra("teacherId");
    }

    // 初始化控件
    private void initViews() {
        selectInfoBtn = findViewById(R.id.teacher_activity_selectInfo);
        changePwdBtn = findViewById(R.id.teacher_activity_changePassword);
        queryStudentsBtn = findViewById(R.id.teacher_activity_queryStudents);

        // 查看个人信息点击事件
        selectInfoBtn.setOnClickListener(v -> showTeacherInfo());

        // 修改密码点击事件
        changePwdBtn.setOnClickListener(v -> showChangePwdDialog());

        queryStudentsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(teacherActivity.this, TeacherCourseStudentsActivity.class);
            intent.putExtra("teacherId", teacherId); // 传递当前教师ID
            startActivity(intent);
        });
    }

    // 显示教师个人信息
    private void showTeacherInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("教师个人信息");

        // 从数据库查询教师信息
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from teacher where id=?", new String[]{teacherId});

        StringBuilder info = new StringBuilder();
        if (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
            String sex = cursor.getString(cursor.getColumnIndexOrThrow("sex"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String subject = cursor.getString(cursor.getColumnIndexOrThrow("subject"));

            // 拼接信息字符串
            info.append("教师ID：").append(id).append("\n")
                    .append("姓名：").append(name).append("\n")
                    .append("性别：").append(TextUtils.isEmpty(sex) ? "未设置" : sex).append("\n")
                    .append("联系电话：").append(TextUtils.isEmpty(phone) ? "未设置" : phone).append("\n")
                    .append("任教科目：").append(TextUtils.isEmpty(subject) ? "未设置" : subject).append("\n")
                    .append("登录密码：").append(password).append("\n");
        } else {
            info.append("未查询到教师信息");
        }
        cursor.close();

        // 显示信息对话框
        builder.setMessage(info.toString());
        builder.setPositiveButton("确定", null);
        builder.create().show();
    }

    // 显示修改密码对话框
    private void showChangePwdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.change_password_layout, null); // 复用学生的密码修改布局
        builder.setView(view);
        builder.setTitle("修改密码");

        // 获取输入框控件
        EditText newPwdEt = view.findViewById(R.id.student_change_password);
        EditText confirmPwdEt = view.findViewById(R.id.student_change_password_second_password);

        // 取消按钮
        builder.setNegativeButton("取消", null);

        // 确定按钮
        builder.setPositiveButton("确定", (dialog, which) -> {
            String newPwd = newPwdEt.getText().toString().trim();
            String confirmPwd = confirmPwdEt.getText().toString().trim();

            // 验证输入
            if (TextUtils.isEmpty(newPwd) || TextUtils.isEmpty(confirmPwd)) {
                Toast.makeText(teacherActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPwd.matches("[0-9]{6}")) {
                Toast.makeText(teacherActivity.this, "密码必须为6位数字", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPwd.equals(confirmPwd)) {
                Toast.makeText(teacherActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            // 更新数据库中的密码
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("update teacher set password=? where id=?", new String[]{newPwd, teacherId});
            Toast.makeText(teacherActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
        });

        builder.create().show();
    }
}