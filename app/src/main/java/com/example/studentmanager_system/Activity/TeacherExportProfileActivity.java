// app/src/main/java/com/example/studentmanager_system/Activity/TeacherExportProfileActivity.java
package com.example.studentmanager_system.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.myDatabaseHelper;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;

public class TeacherExportProfileActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 1002;
    private String teacherId;

    // 使用新的 Activity Result API 替代 startActivityForResult
    private ActivityResultLauncher<Intent> exportFileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_export_profile);

        teacherId = getIntent().getStringExtra("teacherId");
        if (teacherId == null) {
            Toast.makeText(this, "教师信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 注册 Activity Result Launcher
        exportFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                Uri uri = data.getData();
                                if (uri != null) {
                                    exportProfileToExcel(uri);
                                }
                            }
                        }
                    }
                });

        Button btnExport = findViewById(R.id.btn_export);
        btnExport.setOnClickListener(v -> checkPermissionAndExport());
    }

    private void checkPermissionAndExport() {
        // 对于 Android 10 (API 29) 及以上版本，使用分区存储不需要 WRITE_EXTERNAL_STORAGE 权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            createExportFile();
            return;
        }

        // Android 10 以下版本需要检查存储权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 检查是否需要显示权限解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // 显示权限解释对话框
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("需要存储权限")
                        .setMessage("导出个人信息需要存储权限来保存Excel文件到您的设备上。请授予权限以继续。")
                        .setPositiveButton("授予权限", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_PERMISSION);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            } else {
                // 直接请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION);
            }
        } else {
            createExportFile();
        }
    }

    private void createExportFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_TITLE, "教师_" + teacherId + "_个人信息.xlsx");
        exportFileLauncher.launch(intent); // 使用新的 launcher 启动
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createExportFile();
            } else {
                // 用户拒绝了权限，检查是否选中了"不再询问"
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // 用户选择了"不再询问"，引导用户到设置页面手动开启权限
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("权限被拒绝")
                            .setMessage("存储权限被拒绝且不再询问。请到应用设置中手动开启存储权限以导出文件。")
                            .setPositiveButton("去设置", (dialog, which) -> {
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .setNegativeButton("取消", null)
                            .show();
                } else {
                    // 用户只是拒绝了权限
                    Toast.makeText(this, "需要存储权限才能导出文件", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @SuppressLint("Range")
    private void exportProfileToExcel(Uri uri) {
        myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(this);
        XSSFWorkbook workbook = null;
        OutputStream outputStream = null;

        try {
            workbook = new XSSFWorkbook();

            // 创建教师信息工作表
            Sheet teacherSheet = workbook.createSheet("个人信息");
            createTeacherInfoSheet(teacherSheet, dbHelper);

            // 创建授课信息工作表
            Sheet coursesSheet = workbook.createSheet("授课信息");
            createTeachingCoursesSheet(coursesSheet, dbHelper);

            // 为每门课程创建学生名单工作表
            createCourseStudentSheets(workbook, dbHelper);

            // 写入文件
            outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                workbook.write(outputStream);
                outputStream.flush();
                Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取教师教授的所有课程
     */
    @SuppressLint("Range")
    private String getTeacherCourses(myDatabaseHelper dbHelper, String teacherId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                myDatabaseHelper.COURSE_TABLE,
                new String[]{"name"},
                "teacher_id=?",
                new String[]{teacherId},
                null,
                null,
                null
        );

        StringBuilder courses = new StringBuilder();
        if (cursor.moveToFirst()) {
            do {
                if (courses.length() > 0) {
                    courses.append(", ");
                }
                courses.append(cursor.getString(cursor.getColumnIndex("name")));
            } while (cursor.moveToNext());
        } else {
            courses.append("未设置");
        }

        cursor.close();
        return courses.toString();
    }

    @SuppressLint({"Range", "DefaultLocale"})
    private void createTeacherInfoSheet(Sheet sheet, myDatabaseHelper dbHelper) {
        // 表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"工号", "姓名", "性别", "电话", "所在学院", "所在系", "教授课程"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 教师信息
        var db = dbHelper.getReadableDatabase();
        var cursor = db.query(
                myDatabaseHelper.TEACHER_TABLE,
                new String[]{"id", "name", "sex", "phone", "college", "department"},
                "id=?",
                new String[]{teacherId},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            Row dataRow = sheet.createRow(1);
            // 工号
            dataRow.createCell(0).setCellValue(cursor.getString(cursor.getColumnIndex("id")));
            // 姓名
            dataRow.createCell(1).setCellValue(cursor.getString(cursor.getColumnIndex("name")));
            // 性别
            dataRow.createCell(2).setCellValue(cursor.getString(cursor.getColumnIndex("sex")) != null ?
                    cursor.getString(cursor.getColumnIndex("sex")) : "未设置");
            // 电话
            dataRow.createCell(3).setCellValue(cursor.getString(cursor.getColumnIndex("phone")) != null ?
                    cursor.getString(cursor.getColumnIndex("phone")) : "未设置");
            // 所在学院
            dataRow.createCell(4).setCellValue(cursor.getString(cursor.getColumnIndex("college")) != null ?
                    cursor.getString(cursor.getColumnIndex("college")) : "未设置");
            // 所在系
            dataRow.createCell(5).setCellValue(cursor.getString(cursor.getColumnIndex("department")) != null ?
                    cursor.getString(cursor.getColumnIndex("department")) : "未设置");
            // 教授课程（从course表获取完整信息）
            dataRow.createCell(6).setCellValue(getTeacherCourses(dbHelper, teacherId));
        }
        cursor.close();
    }

    @SuppressLint("Range")
    private void createTeachingCoursesSheet(Sheet sheet, myDatabaseHelper dbHelper) {
        // 表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"课程ID", "课程名称", "学分", "学时", "上课时间", "上课地点", "年级", "平均成绩"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 授课信息
        var db = dbHelper.getReadableDatabase();
        var cursor = db.query(
                myDatabaseHelper.COURSE_TABLE,
                new String[]{"id", "name", "credit", "hours", "class_time", "class_location", "grade", "average_score"},
                "teacher_id=?",
                new String[]{teacherId},
                null, null, null
        );

        int rowNum = 1;
        while (cursor.moveToNext()) {
            Row dataRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = dataRow.createCell(i);
                if (i == 7) { // 平均成绩字段需要格式化
                    double avgScore = cursor.getDouble(cursor.getColumnIndex("average_score"));
                    if (avgScore > 0) {
                        cell.setCellValue(String.format("%.2f", avgScore));
                    } else {
                        cell.setCellValue("暂无");
                    }
                } else {
                    cell.setCellValue(cursor.getString(cursor.getColumnIndex(headers[i].equals("课程ID") ? "id" :
                            headers[i].equals("课程名称") ? "name" :
                                    headers[i].equals("学分") ? "credit" :
                                            headers[i].equals("学时") ? "hours" :
                                                    headers[i].equals("上课时间") ? "class_time" :
                                                            headers[i].equals("上课地点") ? "class_location" :
                                                                    headers[i].equals("年级") ? "grade" : "")));
                }
            }
        }
        cursor.close();
    }

    /**
     * 为教师的每门课程创建学生名单工作表
     */
    @SuppressLint("Range")
    private void createCourseStudentSheets(XSSFWorkbook workbook, myDatabaseHelper dbHelper) {
        // 获取教师的所有课程
        var db = dbHelper.getReadableDatabase();
        var courseCursor = db.query(
                myDatabaseHelper.COURSE_TABLE,
                new String[]{"id", "name"},
                "teacher_id=?",
                new String[]{teacherId},
                null, null, null
        );

        while (courseCursor.moveToNext()) {
            String courseId = courseCursor.getString(courseCursor.getColumnIndex("id"));
            String courseName = courseCursor.getString(courseCursor.getColumnIndex("name"));

            // 创建工作表，使用课程名称命名
            // Excel工作表名称限制为31个字符，超过的部分截断
            String sheetName = courseName.length() > 31 ? courseName.substring(0, 28) + "..." : courseName;
            Sheet sheet = workbook.createSheet(sheetName);

            // 表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"学号", "姓名", "性别", "电话", "年级", "班级", "成绩"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 查询选修该课程的学生信息
            String sql = "SELECT s.id, s.name, s.sex, s.number, s.grade, s.class, sc.score " +
                    "FROM " + myDatabaseHelper.STUDENT_COURSE_TABLE + " sc " +
                    "JOIN " + myDatabaseHelper.STUDENT_TABLE + " s ON sc.student_id = s.id " +
                    "WHERE sc.course_id = ?";

            var studentCursor = db.rawQuery(sql, new String[]{courseId});

            int rowNum = 1;
            while (studentCursor.moveToNext()) {
                Row dataRow = sheet.createRow(rowNum++);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = dataRow.createCell(i);
                    if (i == 6) { // 成绩列
                        double score = studentCursor.getDouble(studentCursor.getColumnIndex("score"));
                        if (score >= 0) {
                            cell.setCellValue(score);
                        } else {
                            cell.setCellValue("未录入");
                        }
                    } else {
                        cell.setCellValue(studentCursor.getString(studentCursor.getColumnIndex(
                                headers[i].equals("学号") ? "id" :
                                        headers[i].equals("姓名") ? "name" :
                                                headers[i].equals("性别") ? "sex" :
                                                        headers[i].equals("电话") ? "number" :
                                                                headers[i].equals("年级") ? "grade" :
                                                                        headers[i].equals("班级") ? "class" : "")));
                    }
                }
            }
            studentCursor.close();
        }
        courseCursor.close();
    }
}
