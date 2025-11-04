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

import java.util.Objects;

public class add_teacherinfoActivity extends Activity {

    private EditText idEt, nameEt, passwordEt, sexEt, phoneEt, courseEt;
    private EditText collegeEt, departmentEt; // 新增字段控件
    private Button sureBtn;
    private myDatabaseHelper dbHelper;
    private String oldTeacherId; // 用于修改时保存原始ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_teacher_info);

        // 初始化控件
        initViews();

        dbHelper = myDatabaseHelper.getInstance(this);

        // 接收修改数据（如果是修改操作）
        Intent intent = getIntent();
        if (Objects.equals(intent.getStringExtra("haveData"), "true")) {
            initOldData(intent);
        }

        // 确定按钮点击事件
        sureBtn.setOnClickListener(v -> saveTeacherInfo());
    }

    private void initViews() {
        idEt = findViewById(R.id.add_teacher_layout_id);
        nameEt = findViewById(R.id.add_teacher_layout_name);
        passwordEt = findViewById(R.id.add_teacher_layout_password);
        sexEt = findViewById(R.id.add_teacher_layout_sex);
        phoneEt = findViewById(R.id.add_teacher_layout_phone);
        courseEt = findViewById(R.id.add_teacher_layout_course);
        // 初始化新增字段控件
        collegeEt = findViewById(R.id.add_teacher_layout_college);
        departmentEt = findViewById(R.id.add_teacher_layout_department);
        sureBtn = findViewById(R.id.add_teacher_layout_sure);
    }

    // 初始化旧数据（修改时使用）
    private void initOldData(Intent intent) {
        oldTeacherId = intent.getStringExtra("id");
        idEt.setText(oldTeacherId);
        nameEt.setText(intent.getStringExtra("name"));
        passwordEt.setText(intent.getStringExtra("password"));
        sexEt.setText(intent.getStringExtra("sex"));
        phoneEt.setText(intent.getStringExtra("phone"));
        courseEt.setText(intent.getStringExtra("course"));
        // 初始化新增字段的旧数据
        collegeEt.setText(intent.getStringExtra("college"));
        departmentEt.setText(intent.getStringExtra("department"));
    }

    // 保存教师信息到数据库
    private void saveTeacherInfo() {
        String id = idEt.getText().toString().trim();
        String name = nameEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();
        String sex = sexEt.getText().toString().trim();
        String phone = phoneEt.getText().toString().trim();
        String course = courseEt.getText().toString().trim();
        // 获取新增字段的值
        String college = collegeEt.getText().toString().trim();
        String department = departmentEt.getText().toString().trim();

        // 数据验证
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(name) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "教师ID、姓名、密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.matches("[0-9]{6}")) {
            Toast.makeText(this, "密码必须为6位数字", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(sex) && !sex.matches("[男女]")) {
            Toast.makeText(this, "性别请输入'男'或'女'", Toast.LENGTH_SHORT).show();
            return;
        }

        // 数据库操作
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 如果是修改操作，先删除旧数据
            if (Objects.equals(getIntent().getStringExtra("haveData"), "true")) {
                db.execSQL("delete from " + myDatabaseHelper.TEACHER_TABLE + " where id=?", new String[]{oldTeacherId});
            }

            // 检查ID是否已存在
            Cursor cursor = db.rawQuery("select * from " + myDatabaseHelper.TEACHER_TABLE + " where id=?", new String[]{id});
            if (cursor.moveToNext()) {
                Toast.makeText(this, "该教师ID已存在", Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }
            cursor.close();

            // 插入新数据（更新SQL语句以包含新字段）
            db.execSQL("insert into " + myDatabaseHelper.TEACHER_TABLE +
                            "(id, name, password, sex, phone, college, department, course) values(?,?,?,?,?,?,?,?)",
                    new String[]{id, name, password, sex, phone, college, department, course});

            // 4. 新增：同步添加课程到课程表（若任教课程不为空）
            if (!TextUtils.isEmpty(course)) {
                // 生成课程ID（可自定义规则，例如"教师ID_课程"）
                String courseId = id + "_" + course.replace(" ", "");
                // 课程名称默认使用任教课程名称
                // 课程其他字段（学分、学时可设默认值，或后续扩展为输入项）
                double credit = 3.0; // 默认学分
                int hours = 48;      // 默认学时
                String classTime = ""; // 默认上课时间
                String classLocation = ""; // 默认上课地点

                // 检查课程是否已存在（避免重复添加）
                Cursor courseCursor = db.rawQuery(
                        "select id from " + myDatabaseHelper.COURSE_TABLE + " where id=?",
                        new String[]{courseId}
                );
                if (!courseCursor.moveToNext()) {
                    // 插入新课程
                    db.execSQL(
                            "insert into " + myDatabaseHelper.COURSE_TABLE +
                                    "(id, name, teacher_id, subject, credit, hours, class_time, class_location, average_score) values(?,?,?,?,?,?,?,?,?)",
                            new String[]{
                                    courseId,
                                    course,
                                    id,
                                    course,
                                    String.valueOf(credit),
                                    String.valueOf(hours),
                                    classTime,
                                    classLocation,
                                    "0.0"
                            }
                    );
                }
                courseCursor.close();
            }

            db.setTransactionSuccessful();
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            finish(); // 返回管理员界面
        } catch (Exception e) {
            Toast.makeText(this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
    }
}
