package com.example.studentmanager_system.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.database.sqlite.SQLiteDatabase;
import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.ExcelUtil;
import com.example.studentmanager_system.Util.myDatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DataManagementActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_STORAGE = 1001;
    private String importType;
    private static final String TAG = "DataManagement";
    private String currentExportDataType;

    // 定义2个Launcher：分别处理「导出文件结果」和「选择文件导入结果」
    private ActivityResultLauncher<Intent> exportFileLauncher;
    private ActivityResultLauncher<Intent> importFileLauncher;

    private static final String SAMPLE_DATA_FOLDER = "sample_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_management);

        // 注册新的返回监听器替代已弃用的onBackPressed()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 直接销毁当前页面，返回上一个页面
                finish();
            }
        });

        checkDatabaseAvailability();
        initActivityResultLaunchers();
        initButtons();

        // 设置底部导航栏点击事件
        setupBottomNavigation();
    }

    // 添加底部导航栏设置方法
    private void setupBottomNavigation() {
        // 首页按钮
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            // 回到管理员主页
            Intent intent = new Intent(DataManagementActivity.this, adminActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // 管理按钮
        findViewById(R.id.nav_manage).setOnClickListener(v -> {
            // 回到管理页面
            Intent intent = new Intent(DataManagementActivity.this, AdminManagementActivity.class);
            startActivity(intent);
            finish();
        });

        // 我的按钮
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            // 跳转到NJUPT信息页面
            Intent intent = new Intent(DataManagementActivity.this, NjuptInfoActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // 初始化Activity Result Launcher
    private void initActivityResultLaunchers() {
        // 处理「导出文件」结果（改为Excel导出）
        exportFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        Log.d(TAG, "导出操作取消或失败");
                        return;
                    }
                    Uri saveUri = result.getData().getData();
                    Log.d(TAG, "导出Excel文件URI: " + saveUri + ", 类型: " + currentExportDataType);

                    boolean success = false;
                    String msg = "";
                    // 替换为Excel工具类的导出方法
                    switch (currentExportDataType) {
                        case "students":
                            success = ExcelUtil.exportStudents(this, saveUri);
                            msg = "学生数据导出" + (success ? "成功" : "失败");
                            break;
                        case "teachers":
                            success = ExcelUtil.exportTeachers(this, saveUri);
                            msg = "教师数据导出" + (success ? "成功" : "失败");
                            break;
                        case "courses":
                            success = ExcelUtil.exportCourses(this, saveUri);
                            msg = "课程数据导出" + (success ? "成功" : "失败");
                            break;
                    }
                    if (success) {
                        int count = getTableDataCount(
                                currentExportDataType.equals("students") ? myDatabaseHelper.STUDENT_TABLE :
                                        currentExportDataType.equals("teachers") ? myDatabaseHelper.TEACHER_TABLE :
                                                myDatabaseHelper.COURSE_TABLE
                        );
                        msg += "（共" + count + "条数据）";
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        );

        // 修改导入功能为Excel格式
        importFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        Log.d(TAG, "导入操作取消或失败");
                        return;
                    }
                    Uri selectUri = result.getData().getData();
                    Log.d(TAG, "导入文件URI: " + selectUri + ", 类型: " + importType);

                    if (selectUri != null) {
                        Log.d(TAG, "选中文件MIME类型: " + getContentResolver().getType(selectUri));
                        int originalFlags = result.getData().getFlags();
                        int takeFlags = originalFlags
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        takeFlags |= Intent.FLAG_GRANT_READ_URI_PERMISSION;

                        try {
                            getContentResolver().takePersistableUriPermission(selectUri, takeFlags);
                            Log.d(TAG, "权限持久化成功");
                        } catch (SecurityException e) {
                            Log.e(TAG, "权限持久化失败", e);
                            Toast.makeText(this, "权限持久化失败，尝试继续导入...", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e(TAG, "处理文件异常", e);
                            Toast.makeText(this, "处理文件时出错", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 修改文件类型检查为Excel格式
                        String fileMimeType = getContentResolver().getType(selectUri);
                        if (fileMimeType == null ||
                                (!fileMimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") &&
                                        !fileMimeType.equals("application/vnd.ms-excel"))) {
                            Toast.makeText(this, "请选择.xlsx或.xls格式的Excel文件", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<String> errors;
                        switch (importType) {
                            case "students":
                                errors = ExcelUtil.importStudents(this, selectUri);
                                break;
                            case "teachers":
                                errors = ExcelUtil.importTeachers(this, selectUri);
                                break;
                            case "courses":
                                errors = ExcelUtil.importCourses(this, selectUri);
                                break;
                            default:
                                Toast.makeText(this, "未知的导入类型", Toast.LENGTH_SHORT).show();
                                return;
                        }

                        if (errors.isEmpty()) {
                            Toast.makeText(this, "数据导入成功", Toast.LENGTH_SHORT).show();
                        } else {
                            StringBuilder errorMsg = new StringBuilder("导入失败：\n");
                            for (String error : errors) {
                                errorMsg.append(error).append("\n");
                            }
                            Toast.makeText(this, errorMsg.toString(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "无法获取文件路径", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void checkDatabaseAvailability() {
        try {
            SQLiteDatabase db = myDatabaseHelper.getInstance(this).getReadableDatabase();
            if (db == null || !db.isOpen()) {
                Toast.makeText(this, "数据库初始化失败！", Toast.LENGTH_LONG).show();
                Log.e(TAG, "数据库实例为空或未打开");
            } else {
                Log.d(TAG, "数据库初始化成功");
                db.close();
            }
        } catch (Exception e) {
            Toast.makeText(this, "数据库访问异常：" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "数据库检查失败", e);
        }
    }

    private void initButtons() {
        // 导出学生数据（Excel）
        findViewById(R.id.export_students).setOnClickListener(v -> {
            try {
                int count = getTableDataCount(myDatabaseHelper.STUDENT_TABLE);
                if (count == 0) {
                    Toast.makeText(this, "学生表中没有数据，导出文件将为空", Toast.LENGTH_SHORT).show();
                }
                currentExportDataType = "students";
                startSaveFileFlow("students");
            } catch (Exception e) {
                Toast.makeText(this, "导出按钮点击异常：" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "导出按钮点击崩溃", e);
            }
        });

        // 导出教师数据（Excel）
        findViewById(R.id.export_teachers).setOnClickListener(v -> {
            try {
                int count = getTableDataCount(myDatabaseHelper.TEACHER_TABLE);
                if (count == 0) {
                    Toast.makeText(this, "教师表中没有数据，导出文件将为空", Toast.LENGTH_SHORT).show();
                }
                currentExportDataType = "teachers";
                startSaveFileFlow("teachers");
            } catch (Exception e) {
                Toast.makeText(this, "导出按钮点击异常：" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "导出按钮点击崩溃", e);
            }
        });

        // 导出课程数据（Excel）
        findViewById(R.id.export_courses).setOnClickListener(v -> {
            try {
                int count = getTableDataCount(myDatabaseHelper.COURSE_TABLE);
                if (count == 0) {
                    Toast.makeText(this, "课程表中没有数据，导出文件将为空", Toast.LENGTH_SHORT).show();
                }
                currentExportDataType = "courses";
                startSaveFileFlow("courses");
            } catch (Exception e) {
                Toast.makeText(this, "导出按钮点击异常：" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "导出按钮点击崩溃", e);
            }
        });

        // 修改导入按钮为Excel格式
        findViewById(R.id.import_students).setOnClickListener(v -> {
            importType = "students";
            startSelectFileFlow();
        });
        findViewById(R.id.import_teachers).setOnClickListener(v -> {
            importType = "teachers";
            startSelectFileFlow();
        });
        findViewById(R.id.import_courses).setOnClickListener(v -> {
            importType = "courses";
            startSelectFileFlow();
        });

        // 添加一键导入示例数据按钮
        findViewById(R.id.import_sample_data).setOnClickListener(v -> {
            importSampleData();
        });
    }

    private int getTableDataCount(String tableName) {
        try {
            myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(this);
            if (dbHelper == null) {
                Log.e(TAG, "数据库帮助类实例为null");
                Toast.makeText(this, "数据库访问失败", Toast.LENGTH_SHORT).show();
                return 0;
            }

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            if (db == null || !db.isOpen()) {
                Log.e(TAG, "无法获取可读数据库");
                Toast.makeText(this, "数据库访问失败", Toast.LENGTH_SHORT).show();
                return 0;
            }

            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
            db.close();
            return count;
        } catch (Exception e) {
            Log.e(TAG, "查询数据量时崩溃：" + e.getMessage(), e);
            Toast.makeText(this, "查询数据失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

    private void startSaveFileFlow(String dataType) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "请授予存储权限", Toast.LENGTH_SHORT).show();
                    requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_STORAGE);
                    return;
                }
            }
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            // 修正MIME类型为标准Excel格式
            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 文件名后缀改为.xlsx
            String defaultName = dataType + "_" + System.currentTimeMillis() + ".xlsx";
            intent.putExtra(Intent.EXTRA_TITLE, defaultName);
            intent.putExtra("EXPORT_DATA_TYPE", dataType);
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                    Uri.parse("content://com.android.providers.downloads.documents/tree/downloads"));

            exportFileLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "创建Excel文件选择器失败：" + e.getMessage(), e);
            Toast.makeText(this, "无法打开文件保存界面", Toast.LENGTH_SHORT).show();
        }
    }

    private void startSelectFileFlow() {
        // 修改导入为Excel格式
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // 支持Excel文件格式
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // .xlsx
                "application/vnd.ms-excel"  // .xls
        });
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_TITLE, "选择Excel文件");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        try {
            Intent chooserIntent = Intent.createChooser(intent, "选择Excel文件（.xlsx或.xls格式）");
            importFileLauncher.launch(chooserIntent);
        } catch (Exception e) {
            Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "文件管理器不支持", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限已授予，可以导出数据", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "未授予存储权限，无法导出数据", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "用户拒绝了存储权限");
            }
        }
    }

    // 添加一键导入示例数据的方法
    private void importSampleData() {
        new Thread(() -> {
            List<String> allErrors = new ArrayList<>();

            try {
                // 导入学生数据
                List<String> studentErrors = importSampleFile("students.xlsx", "students");
                allErrors.addAll(studentErrors);

                // 导入教师数据
                List<String> teacherErrors = importSampleFile("teachers.xlsx", "teachers");
                allErrors.addAll(teacherErrors);

                // 导入课程数据
                List<String> courseErrors = importSampleFile("courses.xlsx", "courses");
                allErrors.addAll(courseErrors);

                runOnUiThread(() -> {
                    if (allErrors.isEmpty()) {
                        Toast.makeText(DataManagementActivity.this, "示例数据导入成功！", Toast.LENGTH_LONG).show();
                    } else {
                        StringBuilder errorMsg = new StringBuilder("部分数据导入失败：\n");
                        for (String error : allErrors) {
                            errorMsg.append(error).append("\n");
                        }
                        Toast.makeText(DataManagementActivity.this, errorMsg.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(DataManagementActivity.this, "导入示例数据时发生错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // 添加从assets读取并导入示例文件的方法
    private List<String> importSampleFile(String fileName, String type) {
        try {
            // 创建临时文件
            File tempFile = new File(getCacheDir(), fileName);

            // 从assets复制文件到临时文件
            copyAssetsToFile(fileName, tempFile);

            // 使用现有的导入方法
            Uri tempUri = Uri.fromFile(tempFile);
            List<String> errors;

            switch (type) {
                case "students":
                    errors = ExcelUtil.importStudents(this, tempUri);
                    break;
                case "teachers":
                    errors = ExcelUtil.importTeachers(this, tempUri);
                    break;
                case "courses":
                    errors = ExcelUtil.importCourses(this, tempUri);
                    break;
                default:
                    errors = new ArrayList<>();
                    errors.add("未知的导入类型: " + type);
            }

            // 删除临时文件
            tempFile.delete();

            return errors;
        } catch (Exception e) {
            List<String> errors = new ArrayList<>();
            errors.add(fileName + "导入失败: " + e.getMessage());
            return errors;
        }
    }

    // 复用你已有的copyAssetsToFile方法，稍作修改
    private void copyAssetsToFile(String assetFileName, File destinationFile) throws IOException {
        AssetManager assetManager = getAssets();
        try (InputStream inputStream = assetManager.open(SAMPLE_DATA_FOLDER + "/" + assetFileName);
             FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }
}
