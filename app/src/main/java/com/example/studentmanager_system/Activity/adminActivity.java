package com.example.studentmanager_system.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.studentmanager_system.R;
import com.google.android.material.card.MaterialCardView;
import androidx.viewpager2.widget.ViewPager2;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.ImageView;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout.LayoutParams;
import com.example.studentmanager_system.Util.MapPagerAdapter;

// 新增导入
import android.view.LayoutInflater;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

/**
 * 管理员主界面 - 根页面
 */
public class adminActivity extends Activity {

    private long exit_time = 0; // 记录第一次按返回键的时间
    private String currentAdminName; // 当前登录的管理员用户名
    private com.example.studentmanager_system.Util.myDatabaseHelper dbHelper; // 数据库帮助类

    // 地图相关组件
    private ViewPager2 viewPagerMaps;
    private TextView tvMapCaption;
    private LinearLayout layoutIndicators;
    private MapPagerAdapter mapAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_layout);

        // 获取传递过来的管理员用户名
        Intent intent = getIntent();
        currentAdminName = intent.getStringExtra("current_admin_name");

        // 如果没有通过Intent获取到用户名，则尝试从SharedPreferences获取
        if (currentAdminName == null || currentAdminName.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("admin_prefs", MODE_PRIVATE);
            currentAdminName = prefs.getString("current_admin_name", "");
        }

        // 保存用户名到SharedPreferences以便后续使用
        SharedPreferences prefs = getSharedPreferences("admin_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("current_admin_name", currentAdminName);
        editor.apply();

        // 初始化数据库帮助类
        dbHelper = com.example.studentmanager_system.Util.myDatabaseHelper.getInstance(this);

        // 管理按钮点击事件
        LinearLayout navManage = findViewById(R.id.nav_manage);
        if (navManage != null) {
            navManage.setOnClickListener(v -> {
                Intent intentNav = new Intent(adminActivity.this, AdminManagementActivity.class);
                startActivity(intentNav);
                // 从首页进入管理页面，不结束首页
            });
        }

        // 我的按钮点击事件
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // 这里可以跳转到关于南京邮电大学的介绍页面
                Intent intentNav = new Intent(adminActivity.this, NjuptInfoActivity.class);
                startActivity(intentNav);
            });
        }

        // 添加学生管理和教师管理卡片的点击事件
        setupManagementCards();

        // 初始化地图组件
        initMapComponents();
    }

    /**
     * 设置管理卡片的点击事件
     */
    private void setupManagementCards() {
        // 学生管理卡片点击事件
        MaterialCardView cardStudentManage = findViewById(R.id.card_student_manage);
        if (cardStudentManage != null) {
            cardStudentManage.setOnClickListener(v -> {
                Intent intent = new Intent(adminActivity.this, AdminManageStudentActivity.class);
                startActivity(intent);
            });
        }

        // 教师管理卡片点击事件
        MaterialCardView cardTeacherManage = findViewById(R.id.card_teacher_manage);
        if (cardTeacherManage != null) {
            cardTeacherManage.setOnClickListener(v -> {
                Intent intent = new Intent(adminActivity.this, AdminManageTeacherActivity.class);
                startActivity(intent);
            });
        }

        // 课程管理卡片点击事件
        MaterialCardView browseCourseCard = findViewById(R.id.card_ranking);
        if (browseCourseCard != null) {
            browseCourseCard.setOnClickListener(v -> {
                // TODO: 实现浏览课程功能
                // 示例跳转到课程浏览页面
                Intent intent = new Intent(adminActivity.this, courseinfoActivity.class);
                startActivity(intent);
            });
        }

        // 数据管理卡片点击事件
        MaterialCardView cardDataManage = findViewById(R.id.card_data_manage);
        if (cardDataManage != null) {
            cardDataManage.setOnClickListener(v -> {
                Intent intent = new Intent(adminActivity.this, DataManagementActivity.class);
                startActivity(intent);
            });
        }

        // 修改密码卡片点击事件
        MaterialCardView cardSettings = findViewById(R.id.card_settings);
        if (cardSettings != null) {
            cardSettings.setOnClickListener(v -> {
                showChangePasswordDialog();
            });
        }
    }

    /**
     * 初始化地图组件
     */
    private void initMapComponents() {
        viewPagerMaps = findViewById(R.id.viewPager_maps);
        tvMapCaption = findViewById(R.id.tv_map_caption);
        layoutIndicators = findViewById(R.id.layout_indicators);

        if (viewPagerMaps != null && tvMapCaption != null && layoutIndicators != null) {
            mapAdapter = new MapPagerAdapter();
            viewPagerMaps.setAdapter(mapAdapter);

            // 设置页面改变监听器
            viewPagerMaps.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    tvMapCaption.setText(mapAdapter.getMapName(position));
                    setupIndicators(position);
                }
            });

            // 设置地图点击事件
            mapAdapter.setOnMapClickListener(this::showFullMap);

            // 初始化指示器
            setupIndicators(0);
        }
    }

    /**
     * 设置指示器
     */
    private void setupIndicators(int currentPosition) {
        layoutIndicators.removeAllViews();

        int[] mapImages = {R.drawable.xianlin, R.drawable.sanpailou, R.drawable.suojincun};

        for (int i = 0; i < mapImages.length; i++) {
            ImageView indicator = new ImageView(this);
            indicator.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
            ));
            indicator.setPadding(8, 0, 8, 0);

            if (i == currentPosition) {
                indicator.setImageResource(R.drawable.ic_indicator_active);
            } else {
                indicator.setImageResource(R.drawable.ic_indicator_inactive);
            }

            layoutIndicators.addView(indicator);
        }
    }

    /**
     * 显示全屏地图
     */
    private void showFullMap(int position) {
        Intent intent = new Intent(this, FullMapActivity.class);
        intent.putExtra("map_position", position);
        startActivity(intent);
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
        Intent intent = new Intent(this, admin_login_activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        // 注意：移除了 System.exit(0)，因为：
        // 1. 使用 FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK 已经足够清理任务栈
        // 2. System.exit(0) 会强制终止进程，可能影响用户体验
        // 3. Android 不推荐在正常流程中使用 System.exit(0)
    }

    /**
     * 显示修改密码对话框
     */
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.change_password_dialog, null);
        builder.setView(dialogView);

        EditText oldPasswordInput = dialogView.findViewById(R.id.old_password_input);
        EditText newPasswordInput = dialogView.findViewById(R.id.new_password_input);
        EditText confirmNewPasswordInput = dialogView.findViewById(R.id.confirm_new_password_input);

        builder.setTitle("修改密码")
                .setPositiveButton("确认", (dialog, which) -> {
                    String oldPassword = oldPasswordInput.getText().toString();
                    String newPassword = newPasswordInput.getText().toString();
                    String confirmNewPassword = confirmNewPasswordInput.getText().toString();

                    changePassword(oldPassword, newPassword, confirmNewPassword);
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * 修改密码逻辑
     */
    private void changePassword(String oldPassword, String newPassword, String confirmNewPassword) {
        // 验证输入不为空
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(this, "所有字段都不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT password FROM admin WHERE name=?", new String[]{currentAdminName});

        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"));

            // 验证旧密码
            if (!oldPassword.equals(storedPassword)) {
                Toast.makeText(this, "原密码错误", Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }

            // 验证新密码规则
            if (!newPassword.matches("[0-9]{6}")) {
                Toast.makeText(this, "密码必须为6位数字", Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }

            // 验证两次输入的新密码是否一致
            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }

            // 更新密码
            db.execSQL("UPDATE admin SET password=? WHERE name=?",
                    new String[]{newPassword, currentAdminName});
            Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "用户不存在", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
    }
}
