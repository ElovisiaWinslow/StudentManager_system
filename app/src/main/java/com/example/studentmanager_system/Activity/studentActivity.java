// 修改后的 studentActivity.java
package com.example.studentmanager_system.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;
import com.google.android.material.card.MaterialCardView;
import androidx.viewpager2.widget.ViewPager2;
import android.widget.TextView;
import android.widget.ImageView;
import com.example.studentmanager_system.Util.MapPagerAdapter;

// 新增导入
import android.view.LayoutInflater;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

/**
 * 学生主界面
 */
public class studentActivity extends Activity {
    private myDatabaseHelper dbHelper;
    private String ID;
    private long exit_time = 0; // 记录第一次按返回键的时间

    private TextView tvMapCaption;
    private LinearLayout layoutIndicators;
    private MapPagerAdapter mapAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.student_layout);

        dbHelper = myDatabaseHelper.getInstance(this);
        Intent intent = getIntent();
        ID = intent.getStringExtra("id"); // 获取传入的学号

        // 初始化底部导航栏点击事件
        initBottomNavigation();

        // 初始化快捷功能卡片点击事件
        initQuickActionCards();

        // 初始化地图组件
        initMapComponents();
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
     * 初始化快捷功能卡片点击事件
     */
    private void initQuickActionCards() {
        // 课程表卡片点击事件
        MaterialCardView scheduleCard = findViewById(R.id.card_select_info);
        if (scheduleCard != null) {
            scheduleCard.setOnClickListener(v -> {
                Intent intent = new Intent(studentActivity.this, ScheduleActivity.class);
                intent.putExtra("studentId", ID);
                startActivity(intent);
            });
        }

        // 自主选课卡片点击事件
        MaterialCardView selectCourseCard = findViewById(R.id.card_select_course);
        if (selectCourseCard != null) {
            selectCourseCard.setOnClickListener(v -> {
                Intent intent = new Intent(studentActivity.this, CourseSelectionActivity.class);
                intent.putExtra("studentId", ID);
                startActivity(intent);
            });
        }

        // 已选课程卡片点击事件
        MaterialCardView selectedCoursesCard = findViewById(R.id.card_selected_courses);
        if (selectedCoursesCard != null) {
            selectedCoursesCard.setOnClickListener(v -> {
                Intent intent = new Intent(studentActivity.this, SelectedCoursesActivity.class);
                intent.putExtra("studentId", ID);
                startActivity(intent);
            });
        }

        // 修改密码卡片点击事件
        MaterialCardView changePasswordCard = findViewById(R.id.card_change_password);
        if (changePasswordCard != null) {
            changePasswordCard.setOnClickListener(v -> showChangePasswordDialog());
        }
    }

    /**
     * 初始化地图组件
     */
    private void initMapComponents() {
        // 地图相关组件
        ViewPager2 viewPagerMaps = findViewById(R.id.viewPager_maps);
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
            indicator.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
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
        Intent intent = new Intent(this, student_login_activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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
        Cursor cursor = db.rawQuery("SELECT password FROM student WHERE id=?", new String[]{ID});

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
            db.execSQL("UPDATE student SET password=? WHERE id=?",
                    new String[]{newPassword, ID});
            Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "用户不存在", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
    }
}
