// Activity/StudentStatisticsActivity.java
package com.example.studentmanager_system.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Tools.Student;
import com.example.studentmanager_system.Util.ExcelUtil;
import com.example.studentmanager_system.Util.WarningStudentAdapter;
import com.example.studentmanager_system.Util.myDatabaseHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StudentStatisticsActivity extends AppCompatActivity {
    private myDatabaseHelper dbHelper;
    private TextView tvTotalStudents, tvTotalClasses, tvFreshman, tvSophomore, tvJunior, tvSenior, tvWarningCount;
    private PieChart pieChart;
    private List<Student> warningStudents;

    // 使用新的 Activity Result API 替代 startActivityForResult
    private ActivityResultLauncher<Intent> exportLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_statistics);

        dbHelper = myDatabaseHelper.getInstance(this);
        warningStudents = new ArrayList<>();

        // 注册 ActivityResultLauncher
        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            boolean success = exportWarningStudentsToExcel(uri);
                            if (success) {
                                Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        initViews();
        loadData();
        setupChart();
        setupClickListeners();
    }

    private void initViews() {
        tvTotalStudents = findViewById(R.id.tv_total_students);
        tvTotalClasses = findViewById(R.id.tv_total_classes);
        tvFreshman = findViewById(R.id.tv_freshman);
        tvSophomore = findViewById(R.id.tv_sophomore);
        tvJunior = findViewById(R.id.tv_junior);
        tvSenior = findViewById(R.id.tv_senior);
        tvWarningCount = findViewById(R.id.tv_warning_count);
        pieChart = findViewById(R.id.pie_chart);
    }

    private int getCurrentAcademicYear() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH); // 0-based, so September is 8

        // 如果当前月份小于9月(即还没到新学年)，则属于上一学年
        if (currentMonth < 8) { // 8 represents September
            return currentYear - 1;
        } else {
            return currentYear;
        }
    }

    private void loadData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int currentAcademicYear = getCurrentAcademicYear(); // 例如: 2025

        // 学生总数
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + myDatabaseHelper.STUDENT_TABLE, null);
        if (cursor.moveToFirst()) {
            tvTotalStudents.setText(String.valueOf(cursor.getInt(0)));
        }
        cursor.close();

        // 班级总数
        cursor = db.rawQuery("SELECT COUNT(DISTINCT class) FROM " + myDatabaseHelper.STUDENT_TABLE, null);
        if (cursor.moveToFirst()) {
            tvTotalClasses.setText(String.valueOf(cursor.getInt(0)));
        }
        cursor.close();

        // 各年级学生数
        loadGradeData(db, currentAcademicYear);

        // 学分预警学生数（当前学年的大四学生中已完成学分<100）
        int seniorGrade = currentAcademicYear - 3; // 例如: 2025-3=2022
        cursor = db.rawQuery("SELECT * FROM " + myDatabaseHelper.STUDENT_TABLE +
                        " WHERE grade = ? AND completedCredits < 100",
                new String[]{String.valueOf(seniorGrade)});

        int warningCount = cursor.getCount();
        tvWarningCount.setText(String.valueOf(warningCount));

        // 保存预警学生数据
        warningStudents.clear();
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
            warningStudents.add(student);
        }
        cursor.close();
    }

    private void loadGradeData(SQLiteDatabase db, int currentAcademicYear) {
        // 大一学生数 (当前学年入学)
        // 例如: 2025
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + myDatabaseHelper.STUDENT_TABLE +
                " WHERE grade = ?", new String[]{String.valueOf(currentAcademicYear)});
        if (cursor.moveToFirst()) {
            tvFreshman.setText(String.valueOf(cursor.getInt(0)));
        }
        cursor.close();

        // 大二学生数 (去年入学)
        int sophomoreGrade = currentAcademicYear - 1; // 例如: 2024
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + myDatabaseHelper.STUDENT_TABLE +
                " WHERE grade = ?", new String[]{String.valueOf(sophomoreGrade)});
        if (cursor.moveToFirst()) {
            tvSophomore.setText(String.valueOf(cursor.getInt(0)));
        }
        cursor.close();

        // 大三学生数 (前年入学)
        int juniorGrade = currentAcademicYear - 2; // 例如: 2023
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + myDatabaseHelper.STUDENT_TABLE +
                " WHERE grade = ?", new String[]{String.valueOf(juniorGrade)});
        if (cursor.moveToFirst()) {
            tvJunior.setText(String.valueOf(cursor.getInt(0)));
        }
        cursor.close();

        // 大四学生数 (三年前入学)
        int seniorGrade = currentAcademicYear - 3; // 例如: 2022
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + myDatabaseHelper.STUDENT_TABLE +
                " WHERE grade = ?", new String[]{String.valueOf(seniorGrade)});
        if (cursor.moveToFirst()) {
            tvSenior.setText(String.valueOf(cursor.getInt(0)));
        }
        cursor.close();
    }

    private void setupChart() {
        // 获取当前学年并计算大四年级
        int currentAcademicYear = getCurrentAcademicYear();
        int seniorGrade = currentAcademicYear - 3; // 大四学生年级

        // 查询大四学生总数
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor totalSeniorCursor = db.rawQuery("SELECT COUNT(*) FROM " + myDatabaseHelper.STUDENT_TABLE +
                " WHERE grade = ?", new String[]{String.valueOf(seniorGrade)});
        int totalSeniorStudents = 0;
        if (totalSeniorCursor.moveToFirst()) {
            totalSeniorStudents = totalSeniorCursor.getInt(0);
        }
        totalSeniorCursor.close();

        // 预警学生数已经在 tvWarningCount 中显示
        int warningStudentsCount = Integer.parseInt(tvWarningCount.getText().toString());

        // 计算非预警的大四学生数
        int normalSeniorStudents = totalSeniorStudents - warningStudentsCount;

        // 如果没有大四学生，则不显示图表
        if (totalSeniorStudents == 0) {
            pieChart.clear();
            pieChart.setNoDataText("暂无大四学生数据");
            pieChart.invalidate();
            return;
        }

        // 设置饼状图数据
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(normalSeniorStudents, "正常大四学生"));
        entries.add(new PieEntry(warningStudentsCount, "预警大四学生"));

        PieDataSet dataSet = new PieDataSet(entries, "大四学生学分状态");
        dataSet.setColors(Color.rgb(102, 196, 102), Color.rgb(255, 87, 34));
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);

        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setUsePercentValues(true);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        pieChart.invalidate(); // 刷新图表
    }


    private void setupClickListeners() {
        tvWarningCount.setOnClickListener(v -> showWarningStudentsDialog());

        findViewById(R.id.btn_export_warning).setOnClickListener(v -> exportWarningStudents());
    }

    private void showWarningStudentsDialog() {
        if (warningStudents.isEmpty()) {
            Toast.makeText(this, "暂无预警学生", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_warning_students, null);
        builder.setView(dialogView);

        ListView listView = dialogView.findViewById(R.id.lv_warning_students);
        WarningStudentAdapter adapter = new WarningStudentAdapter(this, R.layout.item_warning_student, warningStudents);
        listView.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void exportWarningStudents() {
        if (warningStudents.isEmpty()) {
            Toast.makeText(this, "暂无预警学生可导出", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_TITLE, "学分预警学生名单.xlsx");

        // 使用新的方式启动
        exportLauncher.launch(intent);
    }

    // 注意：已移除已弃用的 onActivityResult 方法

    private boolean exportWarningStudentsToExcel(Uri uri) {
        return ExcelUtil.exportWarningStudents(this, uri, warningStudents);
    }
}
