package com.example.studentmanager_system.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;

// 添加必要的导入
import androidx.viewpager2.widget.ViewPager2;

import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import com.example.studentmanager_system.Util.MapPagerAdapter;

// 新增导入
import android.view.LayoutInflater;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import com.google.android.material.card.MaterialCardView;

/**
 * 教师主界面
 */
public class teacherActivity extends Activity {

    private myDatabaseHelper dbHelper; // 数据库帮助类
    private String teacherId;          // 教师ID（从登录页传递）
    private long exit_time = 0; // 记录第一次按返回键的时间

    private TextView tvMapCaption;
    private LinearLayout layoutIndicators;
    private MapPagerAdapter mapAdapter;

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
                // 跳转到教师个人中心页面
                Intent intent = new Intent(teacherActivity.this, TeacherProfileActivity.class);
                intent.putExtra("teacherId", teacherId); // 传递教师ID到个人资料页面
                startActivity(intent);
            });
        }

        // 授课表卡片点击事件
        MaterialCardView cardSchedule = findViewById(R.id.card_select_info);
        if (cardSchedule != null) {
            cardSchedule.setOnClickListener(v -> {
                Intent intent = new Intent(teacherActivity.this, TeacherScheduleActivity.class);
                intent.putExtra("teacherId", teacherId);
                startActivity(intent);
            });
        }

        // 成绩管理卡片点击事件
        MaterialCardView cardGradeManagement = findViewById(R.id.card_change_password);
        if (cardGradeManagement != null) {
            cardGradeManagement.setOnClickListener(v -> {
                Intent intent = new Intent(teacherActivity.this, TeacherGradeManagementActivity.class);
                intent.putExtra("teacherId", teacherId);
                startActivity(intent);
            });
        }

        // 课程管理卡片点击事件
        MaterialCardView cardCourseManagement = findViewById(R.id.card_course_management);
        if (cardCourseManagement != null) {
            cardCourseManagement.setOnClickListener(v -> {
                Intent intent = new Intent(teacherActivity.this, TeacherCourseStudentsActivity.class);
                intent.putExtra("teacherId", teacherId);
                startActivity(intent);
            });
        }

        // 修改密码卡片点击事件
        MaterialCardView cardChangePassword = findViewById(R.id.card_query_students);
        if (cardChangePassword != null) {
            cardChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        // 初始化地图组件
        initMapComponents();
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
        Intent intent = new Intent(this, teacher_login_activity.class);
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
        Cursor cursor = db.rawQuery("SELECT password FROM teacher WHERE id=?", new String[]{teacherId});

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
            db.execSQL("UPDATE teacher SET password=? WHERE id=?",
                    new String[]{newPassword, teacherId});
            Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "用户不存在", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
    }
}
