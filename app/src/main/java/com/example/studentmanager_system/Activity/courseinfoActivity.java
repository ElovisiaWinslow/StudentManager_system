// app/src/main/java/com/example/studentmanager_system/Activity/courseinfoActivity.java
package com.example.studentmanager_system.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Tools.Course;
import com.example.studentmanager_system.Util.CourseAdapter;
import com.example.studentmanager_system.Util.myDatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 展示课程信息的activity
 */
public class courseinfoActivity extends AppCompatActivity {
    private final List<Course> courseList = new ArrayList<>();
    private myDatabaseHelper dbHelper;
    private CourseAdapter adapter;
    private Spinner gradeSpinner;
    private EditText etSearch; // 搜索输入框引用
    private final List<String> gradeList = new ArrayList<>();
    private String selectedGrade = "";
    private String searchKeyword = ""; // 添加搜索关键字字段

    // 批量操作相关变量
    private boolean isBatchMode = false;
    private final Set<Integer> selectedPositions = new HashSet<>();
    private LinearLayout batchOperationLayout;
    private Button btnBatchOperation;
    private TextView tvTitle;

    // 使用新的 Activity Result API 替代 startActivityForResult
    private final ActivityResultLauncher<Intent> updateCourseLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            initCourses();
                            adapter.notifyDataSetChanged();
                            // 当从更新课程活动返回时，刷新年级筛选器
                            if (searchKeyword.isEmpty()) {
                                refreshGradeSpinner();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.courseinfo_activity_layout);
        dbHelper = myDatabaseHelper.getInstance(this);

        // 检查是否有搜索关键字传入
        Intent intent = getIntent();
        if (intent.hasExtra("search_keyword")) {
            searchKeyword = intent.getStringExtra("search_keyword");
        }

        // 初始化视图组件
        initViews();

        // 初始化筛选控件
        gradeSpinner = findViewById(R.id.spinner_grade);
        etSearch = findViewById(R.id.et_search);
        // 搜索按钮引用
        Button btnSearch = findViewById(R.id.btn_search); // 初始化搜索按钮

        setupSpinners();

        // 为搜索按钮设置监听器
        btnSearch.setOnClickListener(v -> {
            searchKeyword = etSearch.getText().toString().trim();
            filterCourses();
        });

        // 监听软键盘上的完成按钮
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchKeyword = etSearch.getText().toString().trim();
                filterCourses();
                return true;
            }
            return false;
        });

        initCourses(); // 从数据库中检索课程信息
        adapter = new CourseAdapter(courseinfoActivity.this, courseList, new ArrayList<>());
        adapter.setCourseInfoMode(true); // 设置为课程信息展示模式
        ListView listView = findViewById(R.id.recycler_view);
        listView.setAdapter(adapter);

        // listView点击事件
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (isBatchMode) {
                toggleSelection(position);
            } else {
                final Course course = courseList.get(position); // 捕获课程实例
                showOperationDialog(course, position);
            }
        });

        // 添加长按事件进入批量操作模式
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (!isBatchMode) {
                enterBatchMode();
            }
            toggleSelection(position);
            return true;
        });

        // 如果是从其他地方带搜索词跳转过来的，设置到输入框
        if (!searchKeyword.isEmpty()) {
            etSearch.setText(searchKeyword);
        }

        // 设置底部导航栏点击事件
        setupBottomNavigation();
    }

    // 添加底部导航栏设置方法
    private void setupBottomNavigation() {
        // 首页按钮
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            // 回到管理员主页
            Intent intent = new Intent(courseinfoActivity.this, adminActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // 管理按钮
        findViewById(R.id.nav_manage).setOnClickListener(v -> {
            // 回到管理页面
            Intent intent = new Intent(courseinfoActivity.this, AdminManagementActivity.class);
            startActivity(intent);
            finish();
        });

        // 我的按钮
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            // 跳转到NJUPT信息页面
            Intent intent = new Intent(courseinfoActivity.this, NjuptInfoActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // 初始化视图组件
    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        btnBatchOperation = findViewById(R.id.btn_batch_operation);

        // 初始化批量操作控件
        batchOperationLayout = findViewById(R.id.batch_operation_layout);
        if (batchOperationLayout != null) {
            Button btnSelectAll = findViewById(R.id.btn_select_all);
            Button btnDeselectAll = findViewById(R.id.btn_deselect_all);
            Button btnDeleteSelected = findViewById(R.id.btn_delete_selected);

            // 设置批量操作按钮监听器
            btnBatchOperation.setOnClickListener(v -> toggleBatchMode());
            btnSelectAll.setOnClickListener(v -> selectAll());
            btnDeselectAll.setOnClickListener(v -> deselectAll());
            btnDeleteSelected.setOnClickListener(v -> deleteSelectedCourses());
        }
    }

    // 设置筛选器
    @SuppressLint("SetTextI18n")
    private void setupSpinners() {
        // 只有在非搜索模式下才加载筛选数据
        if (searchKeyword.isEmpty()) {
            loadGradeData();

            // 年级筛选
            ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gradeList);
            gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            gradeSpinner.setAdapter(gradeAdapter);

            gradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        selectedGrade = "";
                    } else {
                        selectedGrade = gradeList.get(position);
                    }
                    filterCourses();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedGrade = "";
                    filterCourses();
                }
            });
        } else {
            // 搜索模式下隐藏筛选器
            if (gradeSpinner != null) gradeSpinner.setVisibility(View.GONE);

            // 更新标题显示搜索关键字
            if (tvTitle != null) {
                tvTitle.setText("搜索结果: \"" + searchKeyword + "\"");
            }
        }
    }

    // 加载年级数据
    private void loadGradeData() {
        gradeList.clear();
        gradeList.add("全部年级");

        Set<String> grades = new HashSet<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT grade FROM " + myDatabaseHelper.COURSE_TABLE, null);

        while (cursor.moveToNext()) {
            String grade = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("grade")));
            grades.add(grade);
        }
        cursor.close();

        // 对年级进行排序
        List<String> sortedGrades = new ArrayList<>(grades);
        sortedGrades.sort((a, b) -> {
            try {
                return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
            } catch (NumberFormatException e) {
                return a.compareTo(b);
            }
        });

        // 添加"年级"后缀
        sortedGrades.replaceAll(s -> s + "年级");

        gradeList.addAll(sortedGrades);
    }

    // 筛选课程
    private void filterCourses() {
        courseList.clear();
        initCourses();
        adapter.notifyDataSetChanged();
        clearSelection(); // 筛选后清空选择
    }

    // 初始化课程信息 - 修改后的版本
    private void initCourses() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        courseList.clear();

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT DISTINCT * FROM ").append(myDatabaseHelper.COURSE_TABLE);
        List<String> conditions = new ArrayList<>();
        List<String> args = new ArrayList<>();

        // 如果有搜索关键字，则添加搜索条件
        // 修改：支持按课程ID、课程名称或教师ID搜索
        if (!searchKeyword.isEmpty()) {
            conditions.add("(id LIKE ? OR name LIKE ? OR teacher_id LIKE ?)");
            args.add("%" + searchKeyword + "%");
            args.add("%" + searchKeyword + "%");
            args.add("%" + searchKeyword + "%");
        } else {
            // 否则使用原有的筛选条件
            if (!selectedGrade.isEmpty()) {
                conditions.add("grade = ?");
                // 去掉"年级"后缀，只保留数字进行查询
                String gradeNumber = selectedGrade.replace("年级", "");
                args.add(gradeNumber);
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

        // 修改排序逻辑：优先按教师ID排序，其次按课程名称排序
        queryBuilder.append(" ORDER BY teacher_id, name");

        Cursor cursor = db.rawQuery(queryBuilder.toString(),
                args.isEmpty() ? null : args.toArray(new String[0]));

        // 使用Map来存储相同名称的课程
        Map<String, Course> courseMap = new HashMap<>();
        Map<String, List<String>> courseTeacherIdsMap = new HashMap<>();

        while (cursor.moveToNext()) {
            String courseName = cursor.getString(cursor.getColumnIndexOrThrow("name"));

            if (!courseMap.containsKey(courseName)) {
                Course course = new Course();
                course.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
                course.setName(courseName);
                course.setSubject(cursor.getString(cursor.getColumnIndexOrThrow("subject")));
                course.setCredit(cursor.getFloat(cursor.getColumnIndexOrThrow("credit")));
                course.setHours(cursor.getInt(cursor.getColumnIndexOrThrow("hours")));
                course.setClassTime(cursor.getString(cursor.getColumnIndexOrThrow("class_time")));
                course.setClassLocation(cursor.getString(cursor.getColumnIndexOrThrow("class_location")));
                course.setAverageScore(cursor.getFloat(cursor.getColumnIndexOrThrow("average_score")));
                course.setGrade(cursor.getInt(cursor.getColumnIndexOrThrow("grade")));

                courseMap.put(courseName, course);
                courseTeacherIdsMap.put(courseName, new ArrayList<>());
            }

            // 收集教师ID
            String teacherId = cursor.getString(cursor.getColumnIndexOrThrow("teacher_id"));
            Objects.requireNonNull(courseTeacherIdsMap.get(courseName)).add(teacherId);
        }
        cursor.close();

        // 将合并后的课程添加到列表中
        courseList.addAll(courseMap.values());
    }

    // 显示操作对话框
    private void showOperationDialog(Course course, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(courseinfoActivity.this);
        LayoutInflater factory = LayoutInflater.from(courseinfoActivity.this);
        final View textEntryView = factory.inflate(R.layout.course_info_layout, null); // 加载AlertDialog自定义布局
        builder.setView(textEntryView);
        builder.setTitle("请选择相关操作");

        Button selectInfo = textEntryView.findViewById(R.id.course_info_select); // 查看课程详细信息按钮
        selectInfo.setOnClickListener(v -> {
            // 显示详细课程信息，包括所有教师
            String detailedInfo = getCourseDetailsByName(course.getName());
            AlertDialog.Builder select_builder = new AlertDialog.Builder(courseinfoActivity.this);
            select_builder.setTitle("课程详细信息");
            select_builder.setMessage(detailedInfo);
            select_builder.create().show();
        });

        // 修改课程信息
        Button update_info = textEntryView.findViewById(R.id.course_info_update);
        update_info.setOnClickListener(v -> {
            // 跳转到修改课程信息的界面,通过intent传递课程名称
            Intent intent = new Intent(courseinfoActivity.this, updateCourseActivity.class);
            intent.putExtra("course_name", course.getName());
            updateCourseLauncher.launch(intent); // 使用新的 Activity Result API
        });

        // 删除课程信息
        Button delete_info = textEntryView.findViewById(R.id.course_info_delete);
        delete_info.setOnClickListener(v -> {
            AlertDialog.Builder delete_builder = new AlertDialog.Builder(courseinfoActivity.this);
            delete_builder.setTitle("警告！！！！");
            delete_builder.setMessage("您将删除该课程信息，此操作不可逆，请谨慎操作！");

            delete_builder.setNegativeButton("取消", null);
            delete_builder.setPositiveButton("确定", (dialog, which) -> {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                try {
                    db.beginTransaction(); // 开启事务

                    String courseName = course.getName();

                    // 1. 先查询将要删除的课程涉及哪些教师
                    Cursor teacherCursor = db.query(
                            myDatabaseHelper.COURSE_TABLE,
                            new String[]{"DISTINCT teacher_id"},
                            "name = ?",
                            new String[]{courseName},
                            null, null, null
                    );

                    List<String> affectedTeacherIds = new ArrayList<>();
                    while (teacherCursor.moveToNext()) {
                        affectedTeacherIds.add(teacherCursor.getString(teacherCursor.getColumnIndexOrThrow("teacher_id")));
                    }
                    teacherCursor.close();

                    // 2. 删除所有同名课程
                    db.execSQL("DELETE FROM " + myDatabaseHelper.COURSE_TABLE + " WHERE name=?", new String[]{courseName});

                    // 3. 更新涉及的教师的course字段
                    updateTeacherCoursesForTeachers(db, affectedTeacherIds);

                    // 4. 更新UI
                    courseList.remove(position);
                    adapter.notifyDataSetChanged();
                    clearSelection();

                    db.setTransactionSuccessful(); // 提交事务
                    Toast.makeText(courseinfoActivity.this, "课程及关联教师信息已更新", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("CourseInfoActivity", "删除课程失败", e);
                    Toast.makeText(courseinfoActivity.this, "删除课程失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    db.endTransaction(); // 结束事务
                }
            });
            delete_builder.create().show();
        });

        builder.create().show();
    }

    // 根据课程名称获取详细信息（包括所有教师）
    private String getCourseDetailsByName(String courseName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                myDatabaseHelper.COURSE_TABLE,
                null,
                "name = ?",
                new String[]{courseName},
                null, null,
                "teacher_id"
        );

        StringBuilder sb = new StringBuilder();
        sb.append("课程名称：").append(courseName).append("\n");

        if (cursor.moveToFirst()) {
            // 获取通用信息（从第一条记录）
            sb.append("学科：").append(cursor.getString(cursor.getColumnIndexOrThrow("subject"))).append("\n");
            sb.append("学分：").append(cursor.getFloat(cursor.getColumnIndexOrThrow("credit"))).append("\n");
            sb.append("学时：").append(cursor.getInt(cursor.getColumnIndexOrThrow("hours"))).append("\n");
            sb.append("年级：").append(cursor.getInt(cursor.getColumnIndexOrThrow("grade"))).append("\n\n");

            // 收集所有教师信息
            sb.append("授课教师列表：\n");
            do {
                String teacherId = cursor.getString(cursor.getColumnIndexOrThrow("teacher_id"));
                String classTime = cursor.getString(cursor.getColumnIndexOrThrow("class_time"));
                String classLocation = cursor.getString(cursor.getColumnIndexOrThrow("class_location"));

                String teacherName = getTeacherNameById(teacherId);

                sb.append("- 教师：").append(teacherName != null ? teacherName : "未知教师")
                        .append(" (ID: ").append(teacherId).append(")\n");

                if (classTime != null && !classTime.isEmpty()) {
                    sb.append("  时间：").append(classTime).append("\n");
                }
                if (classLocation != null && !classLocation.isEmpty()) {
                    sb.append("  地点：").append(classLocation).append("\n");
                }
                sb.append("\n");
            } while (cursor.moveToNext());
        } else {
            sb.append("未找到相关课程信息。\n");
        }
        cursor.close();

        return sb.toString();
    }

    // 根据教师ID获取教师姓名
    private String getTeacherNameById(String teacherId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                myDatabaseHelper.TEACHER_TABLE,
                new String[]{"name"},
                "id = ?",
                new String[]{teacherId},
                null, null, null
        );

        String teacherName = null;
        if (cursor.moveToFirst()) {
            teacherName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        }
        cursor.close();
        return teacherName;
    }

    // 返回时刷新列表（处理修改后的数据更新）
    @Override
    protected void onResume() {
        super.onResume();
        initCourses();
        adapter.notifyDataSetChanged();
        // 当恢复活动时也刷新年级筛选器
        if (searchKeyword.isEmpty()) {
            refreshGradeSpinner();
        }
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
        if (batchOperationLayout != null) {
            batchOperationLayout.setVisibility(View.VISIBLE);
        }
        if (btnBatchOperation != null) {
            btnBatchOperation.setText("取消批量");
        }
        updateBatchModeUI();
    }

    // 退出批量操作模式
    private void exitBatchMode() {
        isBatchMode = false;
        clearSelection();
        if (batchOperationLayout != null) {
            batchOperationLayout.setVisibility(View.GONE);
        }
        if (btnBatchOperation != null) {
            btnBatchOperation.setText("批量操作");
        }
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
        for (int i = 0; i < courseList.size(); i++) {
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
                tvTitle.setText("课程信息 (" + selectedPositions.size() + "已选择)");
            } else if (!searchKeyword.isEmpty()) {
                tvTitle.setText("搜索结果: \"" + searchKeyword + "\"");
            } else {
                tvTitle.setText("课程信息");
            }
        }
    }

    // 删除选中的课程
    private void deleteSelectedCourses() {
        if (selectedPositions.isEmpty()) {
            Toast.makeText(this, "请先选择要删除的课程", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("警告")
                .setMessage("确定要删除选中的" + selectedPositions.size() + "门课程信息吗？此操作不可逆！")
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
        db.beginTransaction(); // 开启事务，确保操作原子性
        try {
            // 收集所有将要删除的课程名称
            Set<String> courseNamesToDelete = new HashSet<>();
            for (int position : positionsToDelete) {
                if (position >= 0 && position < courseList.size()) {
                    Course course = courseList.get(position);
                    courseNamesToDelete.add(course.getName());
                }
            }

            // 收集所有将要受到影响的教师ID
            Set<String> affectedTeacherIds = new HashSet<>();
            for (String courseName : courseNamesToDelete) {
                Cursor teacherCursor = db.query(
                        myDatabaseHelper.COURSE_TABLE,
                        new String[]{"DISTINCT teacher_id"},
                        "name = ?",
                        new String[]{courseName},
                        null, null, null
                );

                while (teacherCursor.moveToNext()) {
                    affectedTeacherIds.add(teacherCursor.getString(teacherCursor.getColumnIndexOrThrow("teacher_id")));
                }
                teacherCursor.close();
            }

            // 删除课程记录（删除所有同名课程）
            for (String courseName : courseNamesToDelete) {
                db.execSQL("DELETE FROM " + myDatabaseHelper.COURSE_TABLE + " WHERE name=?",
                        new String[]{courseName});
            }

            // 更新受影响的教师的course字段
            updateTeacherCoursesForTeachers(db, new ArrayList<>(affectedTeacherIds));

            db.setTransactionSuccessful(); // 标记事务成功

            // 更新UI
            for (int position : positionsToDelete) {
                if (position >= 0 && position < courseList.size()) {
                    courseList.remove(position);
                }
            }

            clearSelection();
            adapter.notifyDataSetChanged();

            Toast.makeText(this, "成功删除" + positionsToDelete.size() + "门课程及相关教师信息", Toast.LENGTH_SHORT).show();

            // 退出批量模式
            exitBatchMode();
        } catch (Exception e) {
            Toast.makeText(this, "删除过程中发生错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("CourseInfoActivity", "批量删除课程失败", e);
        } finally {
            db.endTransaction(); // 结束事务
        }
    }

    // 刷新年级筛选器
    private void refreshGradeSpinner() {
        String previousSelectedGrade = selectedGrade; // 保存当前选择的年级

        loadGradeData(); // 重新加载年级数据

        // 更新年级筛选器适配器
        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gradeList);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner.setAdapter(gradeAdapter);

        // 恢复之前的选择（如果还存在）
        if (!previousSelectedGrade.isEmpty()) {
            int position = gradeList.indexOf(previousSelectedGrade);
            if (position >= 0) {
                gradeSpinner.setSelection(position);
            } else {
                // 如果之前选择的年级不存在了，重置为"全部年级"
                gradeSpinner.setSelection(0);
                selectedGrade = "";
            }
        }

        // 重新设置监听器以确保正常工作
        gradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedGrade = "";
                } else {
                    selectedGrade = gradeList.get(position);
                }
                filterCourses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGrade = "";
                filterCourses();
            }
        });
    }

    /**
     * 更新指定教师列表的course字段
     * @param db 数据库实例
     * @param teacherIds 需要更新的教师ID列表
     */
    private void updateTeacherCoursesForTeachers(SQLiteDatabase db, List<String> teacherIds) {
        for (String teacherId : teacherIds) {
            // 获取该教师教授的所有课程
            StringBuilder courses = new StringBuilder();
            Cursor courseCursor = db.query(
                    myDatabaseHelper.COURSE_TABLE,
                    new String[]{"DISTINCT name"}, // 使用DISTINCT避免重复
                    "teacher_id=?",
                    new String[]{teacherId},
                    null, null, "name"
            );

            if (courseCursor.moveToFirst()) {
                do {
                    if (courses.length() > 0) {
                        courses.append(", ");
                    }
                    courses.append(courseCursor.getString(courseCursor.getColumnIndexOrThrow("name")));
                } while (courseCursor.moveToNext());
            }
            courseCursor.close();

            // 更新教师表中的course字段
            ContentValues cv = new ContentValues();
            cv.put("course", courses.toString());
            db.update(
                    myDatabaseHelper.TEACHER_TABLE,
                    cv,
                    "id=?",
                    new String[]{teacherId}
            );
        }
    }
}
