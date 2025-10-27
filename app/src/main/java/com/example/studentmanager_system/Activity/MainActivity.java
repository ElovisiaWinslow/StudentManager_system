package com.example.studentmanager_system.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;

public class MainActivity extends Activity {
    private long exit_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 保留：确保布局延伸+消除黑边（兼容定制系统）
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_main);

        // 初始化控件和数据库
        LinearLayout adminContainer = findViewById(R.id.main_activity_admin_container);
        LinearLayout teacherContainer = findViewById(R.id.main_activity_teacher_container);
        LinearLayout studentContainer = findViewById(R.id.main_activity_student_container);
        myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(this);
        dbHelper.getWritableDatabase();

        // 点击事件
        adminContainer.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, admin_login_activity.class))
        );
        teacherContainer.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, teacher_login_activity.class))
        );
        studentContainer.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, student_login_activity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 保留：切回界面时重新隐藏系统栏（防止意外显示）
        hideSystemBars();
    }

    private void hideSystemBars() {
        View decorView = getWindow().getDecorView();
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(
                getWindow(), decorView
        );
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        controller.hide(WindowInsetsCompat.Type.systemBars());
    }

    // 双击返回退出逻辑
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exit_time > 2000) {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                exit_time = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}