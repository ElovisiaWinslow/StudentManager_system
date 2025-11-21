package com.example.studentmanager_system.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;

/**
 * 管理员登录界面
 */
public class admin_login_activity extends Activity {

    private EditText name;//用户名
    private EditText password;//用户密码
    private myDatabaseHelper dbHelper;



    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_login_layout);

        //初始化相关控件
        dbHelper = myDatabaseHelper.getInstance(this);


        name = findViewById(R.id.admin_login_activity_name_input);
        password = findViewById(R.id.admin_login_activity_password_input);
        //登录按钮
        Button login = findViewById(R.id.admin_login_activity_login);
        //注册
        TextView register = findViewById(R.id.admin_login_activity_register);
        //忘记密码
        TextView forgetNum = findViewById(R.id.admin_login_activity_forgetNum);

        //跳转到登录过的管理员界面
        login.setOnClickListener(view -> {
            String nameInfo = name.getText().toString();
            String passwordInfo = password.getText().toString();
            //从数据库中获取密码并判断是否相同
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("select password from admin where name=?", new String[]{nameInfo});
            String pi = null;
            if (cursor.moveToNext()){
                pi = cursor.getString(cursor.getColumnIndexOrThrow("password"));//获取密码
            }

            //如果密码正确跳转到登录后的界面
            if (passwordInfo.equals(pi)){
                Intent intent =  new Intent(admin_login_activity.this,adminActivity.class);
                intent.putExtra("current_admin_name", nameInfo); // Pass admin name to next activity
                startActivity(intent);
                cursor.close();
            }else{
                Toast.makeText(admin_login_activity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
            }
        });

        //自定义AlertDialog用于注册
        register.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(admin_login_activity.this);
            LayoutInflater factory = LayoutInflater.from(admin_login_activity.this);
            final View textEntryView = factory.inflate(R.layout.register,null);
            builder.setTitle("用户注册");
            builder.setView(textEntryView);

            final EditText code = textEntryView.findViewById(R.id.admin_register_info);
            final EditText name = textEntryView.findViewById(R.id.admin_register_name);
            final EditText firstPassword = textEntryView.findViewById(R.id.admin_register_first_password);
            final EditText secondPassword = textEntryView.findViewById(R.id.admin_register_second_password);

            builder.setNegativeButton("取消", (dialogInterface, i) -> {

            });

            builder.setPositiveButton("确定", (dialogInterface, i) -> {
                String codeInfo = code.getText().toString();
                //注册码88888
                if (codeInfo.equals("88888")){
                    String nameInfo = name.getText().toString();
                    String firstPasswordInfo = firstPassword.getText().toString();
                    String secondPasswordInfo = secondPassword.getText().toString();
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    // 检查用户名是否为空且不超过20个字符
                    if (nameInfo.isEmpty()) {
                        Toast.makeText(admin_login_activity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (nameInfo.length() > 20) {
                        Toast.makeText(admin_login_activity.this, "用户名不能超过20个字符", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 检测密码长度是否为6-24位
                    if (firstPasswordInfo.length() >= 6 && firstPasswordInfo.length() <= 24){
                        //两次密码相同
                        if (firstPasswordInfo.equals(secondPasswordInfo)){
                            @SuppressLint("Recycle") Cursor cursor = db.rawQuery("select name from admin where name=? ", new String[]{nameInfo});
                            //用户是否存在
                            if (cursor.moveToNext()){
                                Toast.makeText(admin_login_activity.this,"该用户已经存在",Toast.LENGTH_SHORT).show();
                            }else{
                                try {
                                    db.execSQL("insert into admin(name,password)values(?,?)", new String[]{nameInfo, firstPasswordInfo});
                                    Toast.makeText(admin_login_activity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                } catch (android.database.sqlite.SQLiteConstraintException e) {
                                    Toast.makeText(admin_login_activity.this, "用户名或密码不符合要求", Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(admin_login_activity.this, "注册失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else{
                            Toast.makeText(admin_login_activity.this, "两次密码不相同", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(admin_login_activity.this, "密码长度必须为6-24位", Toast.LENGTH_SHORT).show();
                    }
                } else{
                    Toast.makeText(admin_login_activity.this, "注册码错误", Toast.LENGTH_SHORT).show();
                }
            });


            builder.create().show();
        });


        /*
          忘记密码
         */
        forgetNum.setOnClickListener(view -> Toast.makeText(admin_login_activity.this, "此功能暂不支持", Toast.LENGTH_SHORT).show());
    }
}
