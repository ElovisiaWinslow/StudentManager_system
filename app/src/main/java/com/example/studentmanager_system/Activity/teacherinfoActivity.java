package com.example.studentmanager_system.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Tools.Teacher;
import com.example.studentmanager_system.Util.TeacherAdapter;
import com.example.studentmanager_system.Util.myDatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class teacherinfoActivity extends AppCompatActivity {
    private final List<Teacher> teacherList = new ArrayList<>();
    private myDatabaseHelper dbHelper;
    private TeacherAdapter adapter;
    private Spinner collegeSpinner, departmentSpinner;
    private final List<String> collegeList = new ArrayList<>();
    private final List<String> departmentList = new ArrayList<>();
    private String selectedCollege = "";
    private String selectedDepartment = "";
    private String searchKeyword = ""; // 添加搜索关键字字段

    // 批量操作相关变量
    private final Set<Integer> selectedPositions = new HashSet<>();
    private boolean isBatchMode = false;
    private LinearLayout batchOperationLayout;
    private Button btnBatchOperation;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacherinfo_activity_layout);
        dbHelper = myDatabaseHelper.getInstance(this);

        // 检查是否有搜索关键字传入
        Intent intent = getIntent();
        if (intent.hasExtra("search_keyword")) {
            searchKeyword = intent.getStringExtra("search_keyword");
        }

        // 初始化视图组件
        initViews();

        // 初始化筛选控件
        initFilterControls();

        setupSpinners();

        initTeachers(); // 从数据库加载教师信息
        adapter = new TeacherAdapter(this, R.layout.teacher_item, teacherList);
        ListView listView = findViewById(R.id.teacher_list_view);
        listView.setAdapter(adapter);

        // 列表项点击事件
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (isBatchMode) {
                toggleSelection(position);
            } else {
                Teacher teacher = teacherList.get(position);
                showOperationDialog(teacher, position);
            }
        });

        // 列表项长按事件（进入批量选择模式）
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (!isBatchMode) {
                enterBatchMode();
            }
            toggleSelection(position);
            return true;
        });

        // 设置底部导航栏点击事件
        setupBottomNavigation();
    }

    // 添加底部导航栏设置方法
    private void setupBottomNavigation() {
        // 首页按钮
        LinearLayout navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // 回到管理员主页
                Intent intent = new Intent(teacherinfoActivity.this, adminActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 管理按钮
        LinearLayout navManage = findViewById(R.id.nav_manage);
        if (navManage != null) {
            navManage.setOnClickListener(v -> {
                finish();
            });
        }

        // 我的按钮
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // 跳转到NJUPT信息页面
                Intent intent = new Intent(teacherinfoActivity.this, NjuptInfoActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    // 初始化视图组件
    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);

        // 初始化批量操作控件
        batchOperationLayout = findViewById(R.id.batch_operation_layout);
        Button btnSelectAll = findViewById(R.id.btn_select_all);
        Button btnDeselectAll = findViewById(R.id.btn_deselect_all);
        Button btnDeleteSelected = findViewById(R.id.btn_delete_selected);
        btnBatchOperation = findViewById(R.id.btn_batch_operation);

        // 设置批量操作按钮监听器
        btnSelectAll.setOnClickListener(v -> selectAll());
        btnDeselectAll.setOnClickListener(v -> deselectAll());
        btnDeleteSelected.setOnClickListener(v -> deleteSelectedTeachers());
        btnBatchOperation.setOnClickListener(v -> toggleBatchMode());
    }

    // 初始化筛选控件
    private void initFilterControls() {
        LinearLayout filterLayout = findViewById(R.id.filter_layout);
        if (filterLayout != null) {
            filterLayout.setVisibility(View.VISIBLE);
            collegeSpinner = findViewById(R.id.spinner_college);
            departmentSpinner = findViewById(R.id.spinner_department);
        }
    }

    // 设置筛选器
    @SuppressLint("SetTextI18n")
    private void setupSpinners() {
        // 只有在非搜索模式下才加载筛选数据
        if (searchKeyword.isEmpty()) {
            if (collegeSpinner == null || departmentSpinner == null) return;

            loadCollegeAndDepartmentData();

            // 学院筛选
            ArrayAdapter<String> collegeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, collegeList);
            collegeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            collegeSpinner.setAdapter(collegeAdapter);

            collegeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        selectedCollege = "";
                    } else {
                        selectedCollege = collegeList.get(position); // 改为使用实际值而非编号
                    }
                    filterTeachers();
                    updateDepartmentSpinner(); // 更新系下拉菜单
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedCollege = "";
                    filterTeachers();
                    updateDepartmentSpinner();
                }
            });

            // 系筛选
            ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentList);
            departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            departmentSpinner.setAdapter(departmentAdapter);

            departmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        selectedDepartment = "";
                    } else {
                        selectedDepartment = departmentList.get(position); // 改为使用实际值而非编号
                    }
                    filterTeachers();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedDepartment = "";
                    filterTeachers();
                }
            });
        } else {
            // 搜索模式下隐藏筛选器
            LinearLayout filterLayout = findViewById(R.id.filter_layout);
            if (filterLayout != null) {
                filterLayout.setVisibility(View.GONE);
            }

            // 更新标题显示搜索关键字
            if (tvTitle != null) {
                tvTitle.setText("搜索结果: \"" + searchKeyword + "\"");
            }
        }
    }

    // 根据选中的学院更新系下拉菜单
    private void updateDepartmentSpinner() {
        departmentList.clear();
        departmentList.add("全部系");

        if (!selectedCollege.isEmpty() && !selectedCollege.equals("全部学院")) {
            // 查询指定学院的所有系别
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT DISTINCT department FROM " + myDatabaseHelper.TEACHER_TABLE +
                            " WHERE college = ? AND department IS NOT NULL AND department != ''",
                    new String[]{selectedCollege});

            Set<String> departments = new HashSet<>();
            while (cursor.moveToNext()) {
                String department = cursor.getString(cursor.getColumnIndexOrThrow("department"));
                departments.add(department);
            }
            cursor.close();

            // 转换为列表并排序
            List<String> sortedDepartments = new ArrayList<>(departments);
            Collections.sort(sortedDepartments);
            departmentList.addAll(sortedDepartments);
        } else {
            // 如果没有选择学院，则显示所有系别
            Set<String> allDepartments = new HashSet<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT DISTINCT department FROM " + myDatabaseHelper.TEACHER_TABLE +
                            " WHERE department IS NOT NULL AND department != ''", null);

            while (cursor.moveToNext()) {
                String department = cursor.getString(cursor.getColumnIndexOrThrow("department"));
                allDepartments.add(department);
            }
            cursor.close();

            List<String> sortedDepartments = new ArrayList<>(allDepartments);
            Collections.sort(sortedDepartments);
            departmentList.addAll(sortedDepartments);
        }

        // 更新系下拉菜单适配器
        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentList);
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentSpinner.setAdapter(departmentAdapter);
    }

    // 加载学院和系数据
    private void loadCollegeAndDepartmentData() {
        collegeList.clear();
        departmentList.clear();

        collegeList.add("全部学院");
        departmentList.add("全部系");

        // 从数据库中查询实际存在的学院和系
        Set<String> colleges = new HashSet<>();
        Set<String> departments = new HashSet<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 查询所有非空的学院名称
        Cursor collegeCursor = db.rawQuery(
                "SELECT DISTINCT college FROM " + myDatabaseHelper.TEACHER_TABLE +
                        " WHERE college IS NOT NULL AND college != ''", null);

        while (collegeCursor.moveToNext()) {
            String college = collegeCursor.getString(collegeCursor.getColumnIndexOrThrow("college"));
            colleges.add(college);
        }
        collegeCursor.close();

        // 查询所有非空的系别名称
        Cursor departmentCursor = db.rawQuery(
                "SELECT DISTINCT department FROM " + myDatabaseHelper.TEACHER_TABLE +
                        " WHERE department IS NOT NULL AND department != ''", null);

        while (departmentCursor.moveToNext()) {
            String department = departmentCursor.getString(departmentCursor.getColumnIndexOrThrow("department"));
            departments.add(department);
        }
        departmentCursor.close();

        // 转换为列表并排序
        List<String> sortedColleges = new ArrayList<>(colleges);
        Collections.sort(sortedColleges);

        List<String> sortedDepartments = new ArrayList<>(departments);
        Collections.sort(sortedDepartments);

        collegeList.addAll(sortedColleges);
        departmentList.addAll(sortedDepartments);
    }

    // 筛选教师
    private void filterTeachers() {
        teacherList.clear();
        initTeachers();
        adapter.notifyDataSetChanged();
        clearSelection(); // 筛选后清空选择
    }

    // 从数据库加载所有教师信息（更新查询语句以包含新字段）
    private void initTeachers() {
        teacherList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select DISTINCT * from ").append(myDatabaseHelper.TEACHER_TABLE); // 使用 DISTINCT 避免重复

        List<String> conditions = new ArrayList<>();
        List<String> args = new ArrayList<>();

        // 如果有搜索关键字，则添加搜索条件
        if (!searchKeyword.isEmpty()) {
            conditions.add("(id LIKE ? OR name LIKE ?)");
            args.add("%" + searchKeyword + "%");
            args.add("%" + searchKeyword + "%");
        } else {
            // 否则使用原有的筛选条件
            if (!selectedCollege.isEmpty() && !selectedCollege.equals("全部学院")) {
                conditions.add("college = ?");
                args.add(selectedCollege);
            }

            if (!selectedDepartment.isEmpty() && !selectedDepartment.equals("全部系")) {
                conditions.add("department = ?");
                args.add(selectedDepartment);
            }
        }

        if (!conditions.isEmpty()) {
            queryBuilder.append(" WHERE ");
            for (int i = 0; i < conditions.size(); i++) {
                if (i > 0) {
                    queryBuilder.append(" AND ");
                }
                queryBuilder.append(conditions.get(i));
            }
        }

        queryBuilder.append(" order by id");

        Cursor cursor = db.rawQuery(queryBuilder.toString(),
                args.isEmpty() ? null : args.toArray(new String[0]));

        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
            String sex = cursor.getString(cursor.getColumnIndexOrThrow("sex"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            // 修改字段名从 "subject" 为 "course" 以匹配数据库定义
            String course = cursor.getString(cursor.getColumnIndexOrThrow("course"));
            // 获取新增字段的值
            String college = cursor.getString(cursor.getColumnIndexOrThrow("college"));
            String department = cursor.getString(cursor.getColumnIndexOrThrow("department"));

            // 更新构造函数调用以包含新字段
            teacherList.add(new Teacher(id, name, password, sex, phone, course, college, department));
        }
        cursor.close();
    }

    // 显示操作对话框（查看/删除/修改）
    private void showOperationDialog(Teacher teacher, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.teacher_info_layout, null); // 复用学生信息操作布局
        builder.setView(view);
        builder.setTitle("请选择操作");

        // 查看详细信息
        Button selectBtn = view.findViewById(R.id.teacher_info_select);
        selectBtn.setOnClickListener(v -> showTeacherDetail(teacher));

        // 删除教师信息
        Button deleteBtn = view.findViewById(R.id.teacher_info_delete);
        deleteBtn.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("警告")
                .setMessage("确定要删除该教师信息吗？此操作将同时删除该教师的所有课程及学生选课记录，不可逆！")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    try {
                        db.beginTransaction(); // 开启事务，确保级联操作原子性（要么全成功，要么全失败）

                        // 1. 原逻辑：删除教师记录（使用表常量TEACHER_TABLE）
                        db.execSQL("delete from " + myDatabaseHelper.TEACHER_TABLE + " where id=?",
                                new String[]{teacher.getId()});

                        // 2. 新增：查询该教师关联的所有课程ID（通过COURSE_TABLE的teacher_id字段）
                        Cursor courseCursor = db.query(
                                myDatabaseHelper.COURSE_TABLE, // 课程表名（使用常量）
                                new String[]{"id"}, // 仅查询课程ID，减少数据传输
                                "teacher_id = ?", // 条件：教师ID匹配
                                new String[]{teacher.getId()}, // 传入当前删除的教师ID
                                null, null, null
                        );

                        // 3. 新增：收集课程ID，并删除每个课程的学生选课记录
                        List<String> courseIds = new ArrayList<>();
                        while (courseCursor.moveToNext()) {
                            String courseId = courseCursor.getString(courseCursor.getColumnIndexOrThrow("id"));
                            courseIds.add(courseId);

                            // 3.1 删除该课程的所有学生选课记录（避免残留选课数据）
                            db.delete(
                                    myDatabaseHelper.STUDENT_COURSE_TABLE, // 学生选课表名（使用常量）
                                    "course_id = ?", // 条件：课程ID匹配
                                    new String[]{courseId}
                            );
                        }
                        courseCursor.close(); // 关闭游标，释放资源

                        // 4. 新增：删除该教师的所有课程（从课程表中移除）
                        for (String courseId : courseIds) {
                            db.delete(
                                    myDatabaseHelper.COURSE_TABLE, // 课程表名（使用常量）
                                    "id = ?", // 条件：课程ID匹配
                                    new String[]{courseId}
                            );
                        }

                        db.setTransactionSuccessful(); // 标记事务成功（所有操作完成后提交）

                        // 5. 原逻辑：更新教师列表
                        teacherList.remove(position);
                        adapter.notifyDataSetChanged();

                        // 重新加载筛选数据
                        loadCollegeAndDepartmentData();
                        updateDepartmentSpinner();

                        Toast.makeText(teacherinfoActivity.this, "教师及关联课程、选课记录已删除", Toast.LENGTH_SHORT).show();
                    } finally {
                        db.endTransaction(); // 结束事务（成功则提交，失败则回滚）
                    }
                })
                .show());

        // 修改教师信息（跳转到添加页面并传递旧数据）
        Button updateBtn = view.findViewById(R.id.teacher_info_update);
        updateBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, add_teacherinfoActivity.class);
            intent.putExtra("haveData", "true");
            intent.putExtra("id", teacher.getId());
            intent.putExtra("name", teacher.getName());
            intent.putExtra("password", teacher.getPassword());
            intent.putExtra("sex", teacher.getGender());
            intent.putExtra("phone", teacher.getPhone());
            // 修改传递的字段名从 "subject" 为 "course"
            intent.putExtra("course", teacher.getCourse());
            // 添加新增字段的数据传递
            intent.putExtra("college", teacher.getCollege());
            intent.putExtra("department", teacher.getDepartment());
            startActivity(intent);
        });

        builder.create().show();
    }

    // 显示教师详细信息（更新显示内容以包含新字段）
    private void showTeacherDetail(Teacher teacher) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("教师详细信息");
        String sb = "教师ID：" + teacher.getId() + "\n" +
                "姓名：" + teacher.getName() + "\n" +
                "性别：" + teacher.getGender() + "\n" +
                "联系电话：" + teacher.getPhone() + "\n" +
                "所在学院：" + teacher.getCollege() + "\n" +  // 新增字段显示
                "所在系：" + teacher.getDepartment() + "\n" +  // 新增字段显示
                "任教科目：" + teacher.getCourse() + "\n"; // 修改显示字段名从 "subject" 为 "course"
        builder.setMessage(sb);
        builder.setPositiveButton("确定", null);
        builder.show();
    }

    // 返回时刷新列表（处理修改后的数据更新）
    @Override
    protected void onResume() {
        super.onResume();
        initTeachers();
        adapter.notifyDataSetChanged();
    }

    // ================== 批量操作相关方法 ==================

    // 切换批量操作模式
    private void toggleBatchMode() {
        if (isBatchMode) {
            exitBatchMode();
        } else {
            enterBatchMode();
        }
    }

    // 进入批量操作模式
    private void enterBatchMode() {
        isBatchMode = true;
        clearSelection();
        batchOperationLayout.setVisibility(View.VISIBLE);
        btnBatchOperation.setText("取消批量");
        updateBatchModeUI();
    }

    // 退出批量操作模式
    private void exitBatchMode() {
        isBatchMode = false;
        clearSelection();
        batchOperationLayout.setVisibility(View.GONE);
        btnBatchOperation.setText("批量操作");
        updateBatchModeUI();
    }

    // 清空选择
    private void clearSelection() {
        selectedPositions.clear();
        adapter.notifyDataSetChanged();
        updateBatchModeUI();
    }

    // 切换选择状态
    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        adapter.notifyDataSetChanged();
        updateBatchModeUI();
    }

    // 全选
    private void selectAll() {
        selectedPositions.clear();
        for (int i = 0; i < teacherList.size(); i++) {
            selectedPositions.add(i);
        }
        adapter.notifyDataSetChanged();
        updateBatchModeUI();
    }

    // 取消全选
    private void deselectAll() {
        clearSelection();
    }

    // 更新批量模式UI
    @SuppressLint("SetTextI18n")
    private void updateBatchModeUI() {
        if (tvTitle != null) {
            if (isBatchMode) {
                tvTitle.setText("教师信息 (" + selectedPositions.size() + "已选择)");
            } else if (!searchKeyword.isEmpty()) {
                tvTitle.setText("搜索结果: \"" + searchKeyword + "\"");
            } else {
                tvTitle.setText("教师信息");
            }
        }
    }

    // 删除选中的教师
    private void deleteSelectedTeachers() {
        if (selectedPositions.isEmpty()) {
            Toast.makeText(this, "请先选择要删除的教师", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("警告")
                .setMessage("确定要删除选中的" + selectedPositions.size() + "位教师信息吗？此操作将同时删除这些教师的所有课程及学生选课记录，不可逆！")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> performBatchDelete())
                .show();
    }

    // 执行批量删除操作
    private void performBatchDelete() {
        if (selectedPositions.isEmpty()) return;

        // 将选中位置按降序排列，避免删除时索引变化
        List<Integer> positionsToDelete = new ArrayList<>(selectedPositions);
        positionsToDelete.sort(Collections.reverseOrder());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction(); // 开启事务，确保级联操作原子性

            // 删除教师记录及相关数据
            for (int position : positionsToDelete) {
                if (position >= 0 && position < teacherList.size()) {
                    Teacher teacher = teacherList.get(position);
                    String teacherId = teacher.getId();

                    // 1. 删除教师记录
                    db.execSQL("DELETE FROM " + myDatabaseHelper.TEACHER_TABLE + " WHERE id=?",
                            new String[]{teacherId});

                    // 2. 查询该教师关联的所有课程ID
                    Cursor courseCursor = db.query(
                            myDatabaseHelper.COURSE_TABLE,
                            new String[]{"id"},
                            "teacher_id = ?",
                            new String[]{teacherId},
                            null, null, null
                    );

                    // 3. 收集课程ID，并删除每个课程的学生选课记录
                    List<String> courseIds = new ArrayList<>();
                    while (courseCursor.moveToNext()) {
                        String courseId = courseCursor.getString(courseCursor.getColumnIndexOrThrow("id"));
                        courseIds.add(courseId);

                        // 删除该课程的所有学生选课记录
                        db.delete(
                                myDatabaseHelper.STUDENT_COURSE_TABLE,
                                "course_id = ?",
                                new String[]{courseId}
                        );
                    }
                    courseCursor.close();

                    // 4. 删除该教师的所有课程
                    for (String courseId : courseIds) {
                        db.delete(
                                myDatabaseHelper.COURSE_TABLE,
                                "id = ?",
                                new String[]{courseId}
                        );
                    }
                }
            }

            db.setTransactionSuccessful(); // 标记事务成功

            // 更新UI
            for (int position : positionsToDelete) {
                if (position >= 0 && position < teacherList.size()) {
                    teacherList.remove(position);
                }
            }

            clearSelection();
            adapter.notifyDataSetChanged();

            // 重新加载筛选数据
            loadCollegeAndDepartmentData();
            updateDepartmentSpinner();

            Toast.makeText(this, "成功删除" + positionsToDelete.size() + "位教师及关联数据", Toast.LENGTH_SHORT).show();

            // 退出批量模式
            exitBatchMode();
        } catch (Exception e) {
            Toast.makeText(this, "删除过程中发生错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.endTransaction(); // 结束事务
        }
    }
}
