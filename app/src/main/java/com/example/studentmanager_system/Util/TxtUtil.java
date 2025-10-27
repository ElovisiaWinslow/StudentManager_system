package com.example.studentmanager_system.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.studentmanager_system.Tools.Student;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;  // 新增这行

public class TxtUtil {
    private static final String TAG = "TxtUtil";
    private static final String SEPARATOR = "\t";  // 制表符分隔
    // 新增：测试模式开关（用于定位问题）
    private static final boolean TEST_MODE = false;

    // 导出学生数据（添加测试模式）
    public static boolean exportStudents(Context context, Uri uri) {
        if (TEST_MODE) {
            // 测试模式：直接写入固定内容，跳过数据库
            return testExport(context, uri, "学生数据测试");
        }
        return exportData(context, uri, myDatabaseHelper.STUDENT_TABLE,
                new String[]{"id", "name", "password", "sex", "number", "mathScore", "chineseScore", "englishScore", "ranking"},
                "学号\t\t姓名\t密码\t性别\t电话\t\t数学\t语文\t英语\t排名");
    }
    // 导出教师数据
    public static boolean exportTeachers(Context context, Uri uri) {
        return exportData(context, uri, myDatabaseHelper.TEACHER_TABLE,
                new String[]{"id", "name", "password", "sex", "phone", "subject"},
                "教师ID\t姓名\t密码\t性别\t电话\t任教科目");
    }

    // 新增：测试导出方法（纯写入固定内容，无数据库操作）
    private static boolean testExport(Context context, Uri uri, String title) {
        OutputStreamWriter writer = null;
        OutputStream outputStream = null;
        try {
            Log.d(TAG, "【测试模式】开始写入固定内容，URI: " + uri);
            outputStream = context.getContentResolver().openOutputStream(uri, "wt");
            if (outputStream == null) {
                Log.e(TAG, "【测试模式】输出流为null");
                return false;
            }
            writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

            // 写入固定测试数据
            writer.write(title + "表头\n");
            writer.write("测试ID1\t测试名称1\t测试内容1\n");
            writer.write("测试ID2\t测试名称2\t测试内容2\n");
            writer.flush();
            outputStream.flush();
            Log.d(TAG, "【测试模式】固定内容写入完成");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "【测试模式】写入失败: " + e.getMessage(), e);
            return false;
        } finally {
            if (writer != null) try { writer.close(); } catch (Exception e) {}
            if (outputStream != null) try { outputStream.close(); } catch (Exception e) {}
        }
    }
    // 导出课程数据
    public static boolean exportCourses(Context context, Uri uri) {
        return exportData(context, uri, myDatabaseHelper.COURSE_TABLE,
                new String[]{"id", "name", "teacher_id", "credit", "hours"},
                "课程ID\t课程名称\t教师ID\t学分\t学时");
    }
    // 通用导出逻辑（强化数据库检查）
    private static boolean exportData(Context context, Uri uri, String table,
                                      String[] columns, String header) {
        try {
            // 1. 先检查数据库帮助类是否可用
            myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(context);
            if (dbHelper == null) {
                Log.e(TAG, "数据库帮助类实例为null！");
                Toast.makeText(context, "数据库初始化失败", Toast.LENGTH_SHORT).show();
                return false;
            }

            // 2. 获取数据库连接（带异常捕获）
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            if (db == null || !db.isOpen()) {
                Log.e(TAG, "无法打开数据库！");
                Toast.makeText(context, "无法访问数据库", Toast.LENGTH_SHORT).show();
                return false;
            }

            Cursor cursor = null;
            OutputStreamWriter writer = null;
            OutputStream outputStream = null;
            boolean isSuccess = false;

            try {
                // 3. 打开输出流（同之前逻辑）
                Log.d(TAG, "尝试打开输出流，URI: " + uri.toString());
                outputStream = context.getContentResolver().openOutputStream(uri, "wt");
                if (outputStream == null) {
                    Log.e(TAG, "输出流为null！");
                    return false;
                }
                writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

                // 4. 写入表头
                writer.write(header + "\n");
                writer.flush();
                outputStream.flush();
                Log.d(TAG, "表头已写入");

                // 5. 查询数据
                cursor = db.query(table, columns, null, null, null, null, null);
                int dataCount = cursor.getCount();
                Log.d(TAG, "查询到" + dataCount + "条数据");
                if (dataCount == 0) {
                    return true;
                }

                // 6. 写入数据
                int writeCount = 0;
                while (cursor.moveToNext()) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 0; i < columns.length; i++) {
                        if (i > 0) row.append(SEPARATOR);
                        String value = cursor.getString(i);
                        row.append(value != null ? value : "");
                    }
                    row.append("\n");
                    writer.write(row.toString());
                    writeCount++;
                }
                writer.flush();
                outputStream.flush();
                isSuccess = writeCount > 0;
                Log.d(TAG, "写入完成，共" + writeCount + "条");

            } finally {
                // 关闭资源
                if (cursor != null) cursor.close();
                if (writer != null) try { writer.close(); } catch (Exception e) {}
                if (outputStream != null) try { outputStream.close(); } catch (Exception e) {}
                db.close(); // 必须关闭数据库
            }

            return isSuccess;

        } catch (Exception e) {
            Log.e(TAG, "导出数据时发生致命异常: " + e.getMessage(), e);
            Toast.makeText(context, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    // 导入学生数据（补充批量插入逻辑）
    public static List<String> importStudents(Context context, Uri uri) {
        List<String> errors = new ArrayList<>();
        List<Student> students = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        context.getContentResolver().openInputStream(uri),
                        StandardCharsets.UTF_8))) {

            String line;
            int lineNum = 0;
            String header = reader.readLine(); // 读取表头并验证
            if (header == null || !header.contains("学号")) {
                errors.add("文件格式错误，未找到正确的表头");
                return errors;
            }

            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(SEPARATOR);
                if (parts.length < 5) {
                    errors.add("第" + lineNum + "行：字段不足（至少需要5个字段）");
                    continue;
                }

                try {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String password = parts[2].trim();
                    String gender = parts[3].trim();
                    String phone = parts.length > 4 ? parts[4].trim() : "";

                    int math = parts.length > 5 && !parts[5].isEmpty()
                            ? Integer.parseInt(parts[5]) : 0;
                    int chinese = parts.length > 6 && !parts[6].isEmpty()
                            ? Integer.parseInt(parts[6]) : 0;
                    int english = parts.length > 7 && !parts[7].isEmpty()
                            ? Integer.parseInt(parts[7]) : 0;

                    students.add(new Student(id, name, password, gender, phone, math, chinese, english));
                } catch (Exception e) {
                    errors.add("第" + lineNum + "行：" + e.getMessage());
                }
            }

            // 关键修复：批量插入数据到数据库
            if (!students.isEmpty()) {
                myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(context);
                ContentValues[] values = new ContentValues[students.size()];
                for (int i = 0; i < students.size(); i++) {
                    Student s = students.get(i);
                    ContentValues cv = new ContentValues();
                    cv.put("id", s.getId());
                    cv.put("name", s.getName());
                    cv.put("password", s.getPassword());
                    cv.put("sex", s.getSex());
                    cv.put("number", s.getNumber());
                    cv.put("mathScore", s.getMathScore());
                    cv.put("chineseScore", s.getChineseScore());
                    cv.put("englishScore", s.getEnglishScore());
                    values[i] = cv;
                }
                // 调用批量插入方法（myDatabaseHelper中已实现）
                dbHelper.bulkInsert(myDatabaseHelper.STUDENT_TABLE, values);
            }

        } catch (Exception e) {
            errors.add("导入失败：" + e.getMessage());
            Log.e(TAG, "导入学生数据异常", e);
        }

        return errors;
    }

    // 导入教师数据（保持不变）
    public static List<String> importTeachers(Context context, Uri uri) {
        // 原有代码不变...
        List<String> errors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        context.getContentResolver().openInputStream(uri),
                        StandardCharsets.UTF_8))) {

            String line;
            int lineNum = 0;
            String header = reader.readLine();
            if (header == null || !header.contains("教师ID")) {
                errors.add("文件格式错误，未找到正确的表头");
                return errors;
            }

            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(SEPARATOR);
                if (parts.length < 6) {
                    errors.add("第" + lineNum + "行：字段不足（至少需要6个字段）");
                    continue;
                }

                try {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String password = parts[2].trim();
                    String sex = parts[3].trim();
                    String phone = parts[4].trim();
                    String subject = parts[5].trim();

                    ContentValues cv = new ContentValues();
                    cv.put("id", id);
                    cv.put("name", name);
                    cv.put("password", password);
                    cv.put("sex", sex);
                    cv.put("phone", phone);
                    cv.put("subject", subject);

                    myDatabaseHelper.getInstance(context).getWritableDatabase()
                            .insert(myDatabaseHelper.TEACHER_TABLE, null, cv);
                } catch (Exception e) {
                    errors.add("第" + lineNum + "行：" + e.getMessage());
                }
            }

        } catch (IOException e) {
            errors.add("教师数据导入失败: " + e.getMessage());
        }
        return errors;
    }

    // 导入课程数据（保持不变）
    public static List<String> importCourses(Context context, Uri uri) {
        // 原有代码不变...
        List<String> errors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        context.getContentResolver().openInputStream(uri),
                        StandardCharsets.UTF_8))) {

            String line;
            int lineNum = 0;
            String header = reader.readLine();
            if (header == null || !header.contains("课程ID")) {
                errors.add("文件格式错误，未找到正确的表头");
                return errors;
            }

            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(SEPARATOR);
                if (parts.length < 5) {
                    errors.add("第" + lineNum + "行：字段不足（至少需要5个字段）");
                    continue;
                }

                try {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String teacherId = parts[2].trim();
                    int credit = Integer.parseInt(parts[3].trim());
                    int hours = Integer.parseInt(parts[4].trim());

                    ContentValues cv = new ContentValues();
                    cv.put("id", id);
                    cv.put("name", name);
                    cv.put("teacher_id", teacherId);
                    cv.put("credit", credit);
                    cv.put("hours", hours);

                    myDatabaseHelper.getInstance(context).getWritableDatabase()
                            .insert(myDatabaseHelper.COURSE_TABLE, null, cv);
                } catch (Exception e) {
                    errors.add("第" + lineNum + "行：" + e.getMessage());
                }
            }

        } catch (IOException e) {
            errors.add("课程数据导入失败: " + e.getMessage());
        }
        return errors;
    }
}
