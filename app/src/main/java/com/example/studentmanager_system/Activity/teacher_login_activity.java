package com.example.studentmanager_system.Activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;

public class teacher_login_activity extends Activity {
    private EditText teacherIdInput;
    private EditText passwordInput;
    private myDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_login_layout);

        dbHelper = myDatabaseHelper.getInstance(this);
        teacherIdInput = findViewById(R.id.teacher_login_id_input);
        passwordInput = findViewById(R.id.teacher_login_password_input);
        Button loginButton = findViewById(R.id.teacher_login_button);

        loginButton.setOnClickListener(v -> login());
    }

    private void login() {
        String teacherId = teacherIdInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(teacherId) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "ID和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证教师信息
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select password from teacher where id=?", new String[]{teacherId});
        if (cursor.moveToNext()) {
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"));
            if (storedPassword.equals(password)) {
                // 登录成功，跳转到教师主界面并传递教师ID
                Intent intent = new Intent(this, teacherActivity.class);
                intent.putExtra("teacherId", teacherId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "教师ID不存在", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }
}