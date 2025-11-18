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
 * 学生主界面
 */
public class studentActivity extends Activity {
    private myDatabaseHelper dbHelper;
    private String ID;
    private Intent intent;
    private long exit_time = 0; // 记录第一次按返回键的时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.student_layout);

        dbHelper = myDatabaseHelper.getInstance(this);
        intent = getIntent();
        ID = intent.getStringExtra("id"); // 获取传入的学号

        // 初始化底部导航栏点击事件
        initBottomNavigation();
    }

    /**
     * 初始化底部导航栏点击事件
     */
    private void initBottomNavigation() {
        // 管理按钮点击事件
        LinearLayout navManage = findViewById(R.id.nav_manage);
        if (navManage != null) {
            navManage.setOnClickListener(v -> {
                Intent intent = new Intent(studentActivity.this, StudentManagementActivity.class);
                startActivity(intent);
                // 从首页进入管理页面，不结束首页
            });
        }

        // 我的按钮点击事件
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(studentActivity.this, StudentProfileActivity.class);
                intent.putExtra("studentId", ID); // 使用已有的学生ID
                startActivity(intent);
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
        Intent intent = new Intent(this, student_login_activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
