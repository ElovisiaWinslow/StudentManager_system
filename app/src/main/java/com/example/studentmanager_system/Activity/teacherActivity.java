package com.example.studentmanager_system.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;

/**
 * 教师主界面
 */
public class teacherActivity extends Activity {

    private myDatabaseHelper dbHelper; // 数据库帮助类
    private String teacherId;          // 教师ID（从登录页传递）
    private long exit_time = 0; // 记录第一次按返回键的时间

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
        // 用于接收登录页传递的ID
        Intent intent = getIntent();
        teacherId = intent.getStringExtra("teacherId");
    }

    // 初始化控件
    private void initViews() {
// 底部导航栏 - 管理按钮
        LinearLayout navManage = findViewById(R.id.nav_manage);
        if (navManage != null) {
            navManage.setOnClickListener(v -> {
                // 跳转到教师管理页面
                Intent intent = new Intent(teacherActivity.this, TeacherManagementActivity.class);
                intent.putExtra("teacherId", teacherId); // 传递教师ID到管理页面
                startActivity(intent);
            });
        }


        // 底部导航栏 - 首页按钮
        LinearLayout navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // 首页即当前页面，可不做处理或刷新页面
            });
        }

        // 底部导航栏 - 我的按钮
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // TODO: 跳转到教师个人中心页面
            });
        }
    }

    /**
     * 双击返回退出逻辑
     * 在首页按下返回：提示"再按一次退出"，第二次按返回退出到登录界面
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            // 计算时间差
            long currentTime = System.currentTimeMillis();

            if (currentTime - exit_time > 2000) {
                // 第一次按下返回键，或者超过2秒再次按下
                Toast.makeText(this, "再按一次退出到登录界面", Toast.LENGTH_SHORT).show();
                exit_time = currentTime;
            } else {
                // 2秒内第二次按下返回键，退出到登录界面
                exitToLogin();
            }
            return true; // 消费掉返回键事件，不执行默认行为
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出到登录界面
     */
    private void exitToLogin() {
        Intent intent = new Intent(this, teacher_login_activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
