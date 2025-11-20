package com.example.studentmanager_system.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Tools.Student;
import com.example.studentmanager_system.Util.StudentAdapter;
import com.example.studentmanager_system.Util.myDatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

/**
 * 展示学生信息的activity
 */
public class studentinfoActivity extends AppCompatActivity {
    private final List<Student> studentList = new ArrayList<>();
    private myDatabaseHelper dbHelper;
    private StudentAdapter adapter;
    private Spinner gradeSpinner, classSpinner;
    private final List<String> gradeList = new ArrayList<>();
    private final List<String> classList = new ArrayList<>();
    private String selectedGrade = "";
    private String selectedClass = "";
    private String searchKeyword = ""; // 添加搜索关键字字段

    // 批量操作相关变量
    private boolean isBatchMode = false;
    private final Set<Integer> selectedPositions = new HashSet<>();
    private LinearLayout batchOperationLayout;
    private Button btnBatchOperation;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.studentinfo_activity_layout);
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
        classSpinner = findViewById(R.id.spinner_class);

        setupSpinners();

        initStudent(); // 从数据库中检索学生信息
        adapter = new StudentAdapter(studentinfoActivity.this, R.layout.student_item, studentList);
        ListView listView = findViewById(R.id.recycler_view);
        listView.setAdapter(adapter);

        // listView点击事件
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (isBatchMode) {
                toggleSelection(position);
            } else {
                final Student student = studentList.get(position); // 捕获学生实例
                showOperationDialog(student, position);
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

        // 设置底部导航栏
        setupBottomNavigation();
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
            btnDeleteSelected.setOnClickListener(v -> deleteSelectedStudents());
        }
    }

    // 添加底部导航栏设置方法
    private void setupBottomNavigation() {
        // 首页按钮
        LinearLayout navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // 回到管理员主页
                Intent intent = new Intent(studentinfoActivity.this, adminActivity.class);
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
                Intent intent = new Intent(studentinfoActivity.this, NjuptInfoActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    // 设置筛选器
    @SuppressLint("SetTextI18n")
    private void setupSpinners() {
        // 只有在非搜索模式下才加载筛选数据
        if (searchKeyword.isEmpty()) {
            loadGradeAndClassData();

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
                    filterStudents();
                    updateClassSpinner(); // 更新班级下拉菜单
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedGrade = "";
                    filterStudents();
                    updateClassSpinner();
                }
            });

            // 班级筛选
            ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
            classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            classSpinner.setAdapter(classAdapter);

            classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        selectedClass = "";
                    } else {
                        selectedClass = classList.get(position);
                    }
                    filterStudents();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedClass = "";
                    filterStudents();
                }
            });
        } else {
            // 搜索模式下隐藏筛选器
            if (gradeSpinner != null) gradeSpinner.setVisibility(View.GONE);
            if (classSpinner != null) classSpinner.setVisibility(View.GONE);

            // 更新标题显示搜索关键字
            if (tvTitle != null) {
                tvTitle.setText("搜索结果: \"" + searchKeyword + "\"");
            }
        }
    }

    // 根据选中的年级更新班级下拉菜单
    private void updateClassSpinner() {
        classList.clear();
        classList.add("全部班级");

        if (!selectedGrade.isEmpty()) {
            // 查询指定年级的所有班级
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT DISTINCT class FROM " + myDatabaseHelper.STUDENT_TABLE +
                    " WHERE grade = ?", new String[]{selectedGrade});

            List<String> classes = new ArrayList<>();
            while (cursor.moveToNext()) {
                String clazz = cursor.getString(cursor.getColumnIndexOrThrow("class"));
                classes.add(clazz);
            }
            cursor.close();

            // 对班级进行排序
            Collections.sort(classes);
            classList.addAll(classes);
        } else {
            // 如果没有选择年级，则显示所有班级
            Set<String> allClasses = new HashSet<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT DISTINCT class FROM " + myDatabaseHelper.STUDENT_TABLE, null);

            while (cursor.moveToNext()) {
                String clazz = cursor.getString(cursor.getColumnIndexOrThrow("class"));
                allClasses.add(clazz);
            }
            cursor.close();

            List<String> sortedClasses = new ArrayList<>(allClasses);
            Collections.sort(sortedClasses);
            classList.addAll(sortedClasses);
        }

        // 更新班级下拉菜单适配器
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(classAdapter);
    }

    // 加载年级和班级数据
    private void loadGradeAndClassData() {
        gradeList.clear();
        classList.clear();

        gradeList.add("全部年级");
        classList.add("全部班级");

        Set<String> grades = new HashSet<>();
        Set<String> classes = new HashSet<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT grade, class FROM " + myDatabaseHelper.STUDENT_TABLE, null);

        while (cursor.moveToNext()) {
            String grade = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("grade")));
            String clazz = cursor.getString(cursor.getColumnIndexOrThrow("class"));

            grades.add(grade);
            classes.add(clazz);
        }
        cursor.close();

        // 对年级和班级进行排序
        List<String> sortedGrades = new ArrayList<>(grades);
        sortedGrades.sort((a, b) -> {
            try {
                return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
            } catch (NumberFormatException e) {
                return a.compareTo(b);
            }
        });

        List<String> sortedClasses = new ArrayList<>(classes);
        Collections.sort(sortedClasses);

        gradeList.addAll(sortedGrades);
        classList.addAll(sortedClasses);
    }

    // 筛选学生
    private void filterStudents() {
        studentList.clear();
        initStudent();
        adapter.notifyDataSetChanged();
        clearSelection(); // 筛选后清空选择
    }

    // 初始化学生信息
    private void initStudent() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        studentList.clear();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select DISTINCT * from ").append(myDatabaseHelper.STUDENT_TABLE);
        List<String> conditions = new ArrayList<>();
        List<String> args = new ArrayList<>();

        // 如果有搜索关键字，则添加搜索条件
        if (!searchKeyword.isEmpty()) {
            conditions.add("(id LIKE ? OR name LIKE ?)");
            args.add("%" + searchKeyword + "%");
            args.add("%" + searchKeyword + "%");
        } else {
            // 否则使用原有的筛选条件
            if (!selectedGrade.isEmpty()) {
                conditions.add("grade = ?");
                args.add(selectedGrade);
            }

            if (!selectedClass.isEmpty()) {
                conditions.add("class = ?");
                args.add(selectedClass);
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
            String number = cursor.getString(cursor.getColumnIndexOrThrow("number"));
            int grade = cursor.getInt(cursor.getColumnIndexOrThrow("grade"));
            String clazz = cursor.getString(cursor.getColumnIndexOrThrow("class"));
            double completedCredits = cursor.getDouble(cursor.getColumnIndexOrThrow("completedCredits"));

            Student student = new Student(id, name, password, sex, number, grade, clazz);
            student.setCompletedCredits(completedCredits);
            studentList.add(student);
        }
        cursor.close();
    }

    // 显示操作对话框
    private void showOperationDialog(Student student, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(studentinfoActivity.this);
        LayoutInflater factory = LayoutInflater.from(studentinfoActivity.this);
        final View textEntryView = factory.inflate(R.layout.student_info_layout, null); // 加载AlertDialog自定义布局
        builder.setView(textEntryView);
        builder.setTitle("请选择相关操作");

        Button selectInfo = textEntryView.findViewById(R.id.student_info_select); // 查看学生详细信息按钮
        selectInfo.setOnClickListener(v -> {
            // 再次弹出一个alertDialog，用于显示详细学生信息
            AlertDialog.Builder select_builder = new AlertDialog.Builder(studentinfoActivity.this);
            select_builder.setTitle("学生详细信息");
            String sb = "姓名：" + student.getName() + "\n" +
                    "学号：" + student.getId() + "\n" +
                    "性别：" + student.getSex() + "\n" +
                    "手机号：" + student.getNumber() + "\n" +
                    "年级：" + student.getGrade() + "\n" +
                    "班级：" + student.getClazz() + "\n" +
                    "已完成学分：" + student.getCompletedCredits() + "\n";
            select_builder.setMessage(sb);
            select_builder.create().show();
        });

        // 删除学生信息
        Button delete_info = textEntryView.findViewById(R.id.student_info_delete);
        delete_info.setOnClickListener(v -> {
            AlertDialog.Builder delete_builder = new AlertDialog.Builder(studentinfoActivity.this);
            delete_builder.setTitle("警告！！！！");
            delete_builder.setMessage("您将删除该学生信息，此操作不可逆，请谨慎操作！");

            delete_builder.setNegativeButton("取消", null);
            delete_builder.setPositiveButton("确定", (dialog, which) -> {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("delete from " + myDatabaseHelper.STUDENT_TABLE + " where id=?", new String[]{student.getId()});
                studentList.remove(position); // 移除
                adapter.notifyDataSetChanged(); // 刷新列表
                clearSelection(); // 清除选择
            });
            delete_builder.create().show();
        });

        // 修改学生信息,通过intent传递旧学生信息
        Button update_info = textEntryView.findViewById(R.id.student_info_update);
        update_info.setOnClickListener(v -> {
            // 跳转到添加学生信息的界面,通过intent传递数据
            Intent intent = new Intent(studentinfoActivity.this, add_studentinfoActivity.class);
            intent.putExtra("haveData", "true");
            intent.putExtra("name", student.getName());
            intent.putExtra("sex", student.getSex());
            intent.putExtra("id", student.getId());
            intent.putExtra("number", student.getNumber());
            intent.putExtra("password", student.getPassword());
            intent.putExtra("grade", student.getGrade());
            intent.putExtra("clazz", student.getClazz());
            intent.putExtra("completedCredits", student.getCompletedCredits());
            startActivity(intent);
        });

        builder.create().show();
    }

    // 返回时刷新列表（处理修改后的数据更新）
    @Override
    protected void onResume() {
        super.onResume();
        initStudent();
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
        for (int i = 0; i < studentList.size(); i++) {
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
                tvTitle.setText("学生信息 (" + selectedPositions.size() + "已选择)");
            } else if (!searchKeyword.isEmpty()) {
                tvTitle.setText("搜索结果: \"" + searchKeyword + "\"");
            } else {
                tvTitle.setText("学生信息");
            }
        }
    }

    // 删除选中的学生
    private void deleteSelectedStudents() {
        if (selectedPositions.isEmpty()) {
            Toast.makeText(this, "请先选择要删除的学生", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("警告")
                .setMessage("确定要删除选中的" + selectedPositions.size() + "位学生信息吗？此操作不可逆！")
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
            // 删除学生记录
            for (int position : positionsToDelete) {
                if (position >= 0 && position < studentList.size()) {
                    Student student = studentList.get(position);
                    db.execSQL("DELETE FROM " + myDatabaseHelper.STUDENT_TABLE + " WHERE id=?",
                            new String[]{student.getId()});
                }
            }

            db.setTransactionSuccessful(); // 标记事务成功

            // 更新UI
            for (int position : positionsToDelete) {
                if (position >= 0 && position < studentList.size()) {
                    studentList.remove(position);
                }
            }

            clearSelection();
            adapter.notifyDataSetChanged();

            Toast.makeText(this, "成功删除" + positionsToDelete.size() + "位学生", Toast.LENGTH_SHORT).show();

            // 退出批量模式
            exitBatchMode();
        } catch (Exception e) {
            Toast.makeText(this, "删除过程中发生错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.endTransaction(); // 结束事务
        }
    }
}
