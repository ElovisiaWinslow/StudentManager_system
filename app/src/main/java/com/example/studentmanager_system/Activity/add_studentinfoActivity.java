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


/**
 * 添加学生信息的界面,修改学生信息的界面
 */
public class add_studentinfoActivity extends Activity {

    private EditText name;
    private EditText sex;
    private EditText id;
    private EditText number;
    private EditText password;
    // 新增年级和班级字段
    private EditText grade;
    private EditText class_;

    private String oldID;//用于防治修改信息时将ID也修改了，而原始的有该ID的学生信息还保存在数据库中

    private myDatabaseHelper dbHelper;
    Intent oldData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_student_info);

        name = findViewById(R.id.add_student_layout_name);
        sex = findViewById(R.id.add_student_layout_sex);
        id = findViewById(R.id.add_student_layout_id);
        number = findViewById(R.id.add_student_layout_number);
        password = findViewById(R.id.add_student_layout_password);
        // 初始化新增控件
        grade = findViewById(R.id.add_student_layout_grade);
        class_ = findViewById(R.id.add_student_layout_class);

        dbHelper = myDatabaseHelper.getInstance(this);

        oldData = getIntent();
        if (Objects.equals(oldData.getStringExtra("haveData"), "true")) {
            initInfo();//恢复旧数据
        }

        //确定按钮
        Button sure = findViewById(R.id.add_student_layout_sure);
        //将数据插入数据库
        sure.setOnClickListener(v -> {
            //sex不能为空否则程序崩溃，因为在StudentAdapter中有一个ImageView要设置图片
            //我这里要求id,name,sex都不能为空
            String id_ = id.getText().toString();
            String name_ = name.getText().toString();
            String sex_ = sex.getText().toString();
            String password_ = password.getText().toString();
            String number_ = number.getText().toString();
            // 获取新增字段的值
            String grade_ = grade.getText().toString();
            String class__ = class_.getText().toString();

            if (!TextUtils.isEmpty(id_) && !TextUtils.isEmpty(name_) && !TextUtils.isEmpty(sex_)) {

                if (sex_.matches("[女|男]")) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.beginTransaction();//开启事务

                    // 如果是修改模式，先删除旧数据
                    if (Objects.equals(oldData.getStringExtra("haveData"), "true")) {
                        db.execSQL("delete from student where id=?", new String[]{oldID});//删除旧数据
                    }

                    //判断学号是否重复
                    Cursor cursor = db.rawQuery("select * from student where id=?", new String[]{id_});
                    if (cursor.moveToNext()) {
                        // 如果是修改且学号未改变，允许更新
                        if (Objects.equals(oldData.getStringExtra("haveData"), "true") && id_.equals(oldID)) {
                            cursor.close();
                            // 在插入语句中添加年级和班级字段
                            db.execSQL("insert into student(id,name,sex,password,number,grade,class) values(?,?,?,?,?,?,?)",
                                    new String[]{id_, name_, sex_, password_, number_, grade_, class__});
                            db.setTransactionSuccessful();//事务执行成功
                            db.endTransaction();//结束事务

                            Toast.makeText(add_studentinfoActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                            finish(); // 返回学生管理页面
                        } else {
                            Toast.makeText(add_studentinfoActivity.this, "已有学生使用该学号,请重新输入", Toast.LENGTH_SHORT).show();
                            cursor.close();
                            db.endTransaction();
                        }
                    } else {
                        cursor.close();
                        // 在插入语句中添加年级和班级字段
                        db.execSQL("insert into student(id,name,sex,password,number,grade,class) values(?,?,?,?,?,?,?)",
                                new String[]{id_, name_, sex_, password_, number_, grade_, class__});
                        db.setTransactionSuccessful();//事务执行成功
                        db.endTransaction();//结束事务

                        Toast.makeText(add_studentinfoActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                        finish(); // 返回学生管理页面
                    }
                } else {
                    Toast.makeText(add_studentinfoActivity.this, "请输入正确的性别信息", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(add_studentinfoActivity.this, "姓名，学号，性别均不能为空", Toast.LENGTH_SHORT).show();
            }

        });

    }

    //恢复旧数据
    private void initInfo() {
        String oldName = oldData.getStringExtra("name");
        name.setText(oldName);
        String oldSex = oldData.getStringExtra("sex");
        sex.setText(oldSex);
        String oldId = oldData.getStringExtra("id");
        oldID = oldId;
        id.setText(oldId);
        String oldNumber = oldData.getStringExtra("number");
        number.setText(oldNumber);
        String oldPassword = oldData.getStringExtra("password");
        password.setText(oldPassword);
        // 恢复新增字段的值
        int oldGrade = oldData.getIntExtra("grade", -1);
        if (oldGrade != -1) grade.setText(String.valueOf(oldGrade));
        // 修复字段名不匹配问题：将"class"改为"clazz"
        String oldClass = oldData.getStringExtra("clazz");
        if (oldClass != null) class_.setText(oldClass);
    }
}
