package com.example.studentmanager_system.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import com.example.studentmanager_system.R;

/**
 * 学生管理主页面
 */
public class AdminMangeStudentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mange_students); // 使用你提供的布局

        // 初始化视图和点击监听
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // 这里可以初始化其他需要的视图组件
        // 比如设置搜索框的监听器等
    }

    private void setupClickListeners() {
        // 返回按钮点击事件（如果需要）
        // ImageView ivBack = findViewById(R.id.iv_back);
        // if (ivBack != null) {
        //     ivBack.setOnClickListener(v -> finish());
        // }

        // 搜索按钮点击事件
        Button btnSearch = findViewById(R.id.btn_search);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                // 执行搜索逻辑
                performSearch();
            });
        }

        // 功能卡片点击事件
        setupCardClickListeners();

        // 底部导航栏点击事件
        setupBottomNavigation();
    }

    private void setupCardClickListeners() {
        // 总览学生信息卡片
        LinearLayout cardQueryStudent = findViewById(R.id.card_query_student);
        if (cardQueryStudent != null) {
            cardQueryStudent.setOnClickListener(v -> {
                // 跳转到学生信息总览页面
                Intent intent = new Intent(AdminMangeStudentActivity.this, studentinfoActivity.class);
                startActivity(intent);
            });
        }

        // 添加学生信息卡片
        LinearLayout cardAddStudent = findViewById(R.id.card_add_student);
        if (cardAddStudent != null) {
            cardAddStudent.setOnClickListener(v -> {
                // 跳转到添加学生信息页面
                // Intent intent = new Intent(StudentManagementActivity.this, AddStudentActivity.class);
                // startActivity(intent);

                // 暂时先跳转到现有的学生信息页面，你可以后续替换
                Intent intent = new Intent(AdminMangeStudentActivity.this, studentinfoActivity.class);
                startActivity(intent);
            });
        }

        // 导出学生名单卡片
        LinearLayout cardExportStudent = findViewById(R.id.card_export_student);
        if (cardExportStudent != null) {
            cardExportStudent.setOnClickListener(v -> {
                // 执行导出逻辑
                exportStudentList();
            });
        }

        // 批量操作卡片
        LinearLayout cardBatchOperation = findViewById(R.id.card_batch_operation);
        if (cardBatchOperation != null) {
            cardBatchOperation.setOnClickListener(v -> {
                // 跳转到批量操作页面或显示批量操作对话框
                showBatchOperationDialog();
            });
        }

        // 数据统计卡片
        LinearLayout cardDataStatistics = findViewById(R.id.card_data_statistics);
        if (cardDataStatistics != null) {
            cardDataStatistics.setOnClickListener(v -> {
                // 跳转到数据统计页面
                // Intent intent = new Intent(StudentManagementActivity.this, StatisticsActivity.class);
                // startActivity(intent);

                showStatistics();
            });
        }
    }

    private void setupBottomNavigation() {
        // 首页按钮
        LinearLayout navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(AdminMangeStudentActivity.this, adminActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }

        // 管理按钮（当前页面，不需要跳转，但可以刷新页面）
        LinearLayout navManage = findViewById(R.id.nav_manage);
        if (navManage != null) {
            navManage.setOnClickListener(v -> {
                // 刷新当前页面数据
                refreshData();
            });
        }

        // 我的按钮
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // 跳转到个人信息页面（如果已创建）
                // Intent intent = new Intent(StudentManagementActivity.this, ProfileActivity.class);
                // startActivity(intent);

                // 暂时显示提示或保持原样
            });
        }
    }

    /**
     * 执行搜索操作
     */
    private void performSearch() {
        // 获取搜索框内容
        // EditText etSearch = findViewById(R.id.et_search);
        // String searchText = etSearch.getText().toString().trim();

        // 执行搜索逻辑，可以跳转到搜索结果页面
        // Intent intent = new Intent(this, SearchResultActivity.class);
        // intent.putExtra("search_keyword", searchText);
        // startActivity(intent);

        // 暂时先跳转到学生信息页面
        Intent intent = new Intent(this, studentinfoActivity.class);
        startActivity(intent);
    }

    /**
     * 导出学生名单
     */
    private void exportStudentList() {
        // 实现导出逻辑
        // 可以显示导出选项对话框或直接执行导出

        // 示例：显示导出成功提示
        // Toast.makeText(this, "导出功能开发中", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示批量操作对话框
     */
    private void showBatchOperationDialog() {
        // 实现批量操作对话框
        // 可以显示导入、批量删除、批量更新等选项

        // 示例：显示提示
        // Toast.makeText(this, "批量操作功能开发中", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示统计信息
     */
    private void showStatistics() {
        // 刷新统计数据或显示详细统计页面
        // 可以在这里更新统计卡片的数据

        // 示例：显示提示
        // Toast.makeText(this, "数据统计功能开发中", Toast.LENGTH_SHORT).show();
    }

    /**
     * 刷新页面数据
     */
    private void refreshData() {
        // 重新加载统计数据等
        // 可以调用API重新获取数据并更新UI
    }
}