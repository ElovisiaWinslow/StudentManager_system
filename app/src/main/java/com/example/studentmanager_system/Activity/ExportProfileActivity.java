// app/src/main/java/com/example/studentmanager_system/Activity/ExportProfileActivity.java
package com.example.studentmanager_system.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class ExportProfileActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 1002;
    private String studentId;

    // 使用新的 Activity Result API 替代 startActivityForResult
    private ActivityResultLauncher<Intent> exportFileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_profile);

        studentId = getIntent().getStringExtra("studentId");
        if (studentId == null) {
            Toast.makeText(this, "学生信息错误", Toast.LENGTH_SHORT).show();
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
        intent.putExtra(Intent.EXTRA_TITLE, "学生_" + studentId + "_个人信息.xlsx");
        exportFileLauncher.launch(intent); // 使用新的 launcher 启动
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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

            // 创建学生信息工作表
            Sheet studentSheet = workbook.createSheet("个人信息");
            createStudentInfoSheet(studentSheet, dbHelper);

            // 创建已选课程工作表
            Sheet coursesSheet = workbook.createSheet("已选课程");
            createSelectedCoursesSheet(coursesSheet, dbHelper);

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

    @SuppressLint("Range")
    private void createStudentInfoSheet(Sheet sheet, myDatabaseHelper dbHelper) {
        // 表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学号", "姓名", "性别", "电话", "年级", "班级", "排名", "已完成学分"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 学生信息
        var db = dbHelper.getReadableDatabase();
        var cursor = db.query(
                myDatabaseHelper.STUDENT_TABLE,
                new String[]{"id", "name", "sex", "number", "grade", "class", "ranking", "completedCredits"},
                "id=?",
                new String[]{studentId},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            Row dataRow = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = dataRow.createCell(i);
                cell.setCellValue(cursor.getString(i));
            }
        }
        cursor.close();
    }

    @SuppressLint("Range")
    private void createSelectedCoursesSheet(Sheet sheet, myDatabaseHelper dbHelper) {
        // 表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"课程ID", "课程名称", "授课教师", "学分", "学时", "上课时间", "上课地点", "成绩"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 已选课程信息
        var courses = dbHelper.getSelectedCourses(studentId);
        for (int i = 0; i < courses.size(); i++) {
            Row dataRow = sheet.createRow(i + 1);
            var course = courses.get(i);

            dataRow.createCell(0).setCellValue(course.getId());
            dataRow.createCell(1).setCellValue(course.getName());
            dataRow.createCell(2).setCellValue(course.getTeacherName() != null ? course.getTeacherName() : "");
            dataRow.createCell(3).setCellValue(course.getCredit());
            dataRow.createCell(4).setCellValue(course.getHours());
            dataRow.createCell(5).setCellValue(course.getClassTime() != null ? course.getClassTime() : "");
            dataRow.createCell(6).setCellValue(course.getClassLocation() != null ? course.getClassLocation() : "");

            if (course.getScore() >= 0) {
                dataRow.createCell(7).setCellValue(course.getScore());
            } else {
                dataRow.createCell(7).setCellValue("未录入");
            }
        }
    }
}
