package com.example.studentmanager_system.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;

/**
 * 学生主界面
 */
public class studentActivity extends Activity {
    private myDatabaseHelper dbHelper;
    private String ID;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.student_layout);

        Button select = findViewById(R.id.student_activity_selectInfo);
        Button changePassword = findViewById(R.id.student_activity_changePassword);
        Button selectCourseBtn = findViewById(R.id.student_activity_select_course);
        // 新增：初始化已选课程按钮（绑定布局中的student_activity_selected_courses）
        // 新增：已选课程按钮声明
        Button selectedCoursesBtn = findViewById(R.id.student_activity_selected_courses);
        dbHelper = myDatabaseHelper.getInstance(this);
        intent = getIntent();
        //以AlertDialog的形式显示个人详细信息
        select.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(studentActivity.this);
            builder.setTitle("个人信息");
            ID = intent.getStringExtra("id");//获取传入的学号用于查询详细信息
            StringBuilder sb = new StringBuilder();

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("select * from student where id=?", new String[]{ID});
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                String number = cursor.getString(cursor.getColumnIndexOrThrow("number"));
                int mathScore = cursor.getInt(cursor.getColumnIndexOrThrow("mathScore"));
                int chineseScore = cursor.getInt(cursor.getColumnIndexOrThrow("chineseScore"));
                int englishScore = cursor.getInt(cursor.getColumnIndexOrThrow("englishScore"));
                int ranking = cursor.getInt(cursor.getColumnIndexOrThrow("ranking"));
                sb.append("姓名：").append(name).append("\n");
                sb.append("学号：").append(id).append("\n");
                sb.append("手机号：").append(number).append("\n");
                sb.append("密码：").append(password).append("\n");
                sb.append("数学成绩：").append(mathScore).append("\n");
                sb.append("语文成绩：").append(chineseScore).append("\n");
                sb.append("英语成绩：").append(englishScore).append("\n");
                int sum = mathScore + chineseScore + englishScore;//总成绩
                sb.append("总成绩：").append(sum).append("\n");
                sb.append("名次：").append(ranking).append("\n");
            }
            cursor.close();
            builder.setMessage(sb.toString());
            builder.create().show();
        });

        selectCourseBtn.setOnClickListener(v -> {
            // 传递当前登录学生ID
            ID = intent.getStringExtra("id");
            Log.d("StudentID", "传递的ID：" + ID); // 查看日志是否为null
            Intent intent = new Intent(studentActivity.this, CourseListActivity.class);
            intent.putExtra("studentID", ID); // 替换为实际获取学生ID的逻辑
            startActivity(intent);
        });

        // 新增：已选课程按钮跳转逻辑（与选课按钮逻辑结构一致，确保ID传递正确）
        selectedCoursesBtn.setOnClickListener(v -> {
            // 1. 获取当前学生ID（与查询信息、选课按钮使用相同的获取逻辑：从Intent的"id"字段）
            ID = intent.getStringExtra("id");
            Log.d("StudentID_Selected", "传递到已选课程的ID：" + ID); // 日志用于调试，确认ID非null

            // 2. 跳转至已选课程页面（SelectedCoursesActivity）
            Intent selectedIntent = new Intent(studentActivity.this, SelectedCoursesActivity.class);
            // 传递ID：参数名"studentId"需与SelectedCoursesActivity中接收的参数名一致（避免接收null）
            selectedIntent.putExtra("studentId", ID);
            startActivity(selectedIntent);
        });

        changePassword.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(studentActivity.this);
            LayoutInflater factory = LayoutInflater.from(studentActivity.this);
            final View view = factory.inflate(R.layout.change_password_layout, null);
            builder.setView(view);
            builder.setTitle("修改密码");
            builder.setNegativeButton("取消", null);

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                final EditText firstPassword = view.findViewById(R.id.student_change_password);
                final EditText secondPassword = view.findViewById(R.id.student_change_password_second_password);
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String first = firstPassword.getText().toString();
                    String second = secondPassword.getText().toString();
                    if (!TextUtils.isEmpty(first) && !TextUtils.isEmpty(second)) {
                        if (first.matches("[0-9]{6}") && second.matches("[0-9]{6}")) {
                            if (second.equals(first)) {
                                ID = intent.getStringExtra("id");//获取传入的学号用于修改密码
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                db.execSQL("update student set password=? where id=?", new String[]{second, ID});
                                Toast.makeText(studentActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(studentActivity.this, "两次密码不一致", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(studentActivity.this, "密码必须为6位数字", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(studentActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                    }
                }
            });


            builder.create().show();
        });


    }
}
