package com.example.studentmanager_system.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.studentmanager_system.Tools.Student;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {
    private static final String TAG = "ExcelUtil";
    // 测试模式开关（保留原功能，用于定位问题）
    private static final boolean TEST_MODE = false;


    // -------------------------- 导出Excel核心方法 --------------------------
    // 导出学生数据（保留测试模式）
    public static boolean exportStudents(Context context, Uri uri) {
        if (TEST_MODE) {
            // 测试模式：生成固定Excel测试数据，跳过数据库
            return testExportExcel(context, uri);
        }
        // 真实模式：指定学生表列名、Excel表头、工作表名
        return exportExcelData(
                context,
                uri,
                myDatabaseHelper.STUDENT_TABLE,
                new String[]{"id", "name", "password", "sex", "number", "mathScore", "chineseScore", "englishScore", "ranking"},
                new String[]{"学号", "姓名", "密码", "性别", "电话", "数学成绩", "语文成绩", "英语成绩", "排名"},
                "学生数据表"
        );
    }

    // 导出教师数据
    public static boolean exportTeachers(Context context, Uri uri) {
        return exportExcelData(
                context,
                uri,
                myDatabaseHelper.TEACHER_TABLE,
                new String[]{"id", "name", "password", "sex", "phone", "subject"},
                new String[]{"教师ID", "姓名", "密码", "性别", "电话", "任教科目"},
                "教师数据表"
        );
    }

    // 导出课程数据
    public static boolean exportCourses(Context context, Uri uri) {
        return exportExcelData(
                context,
                uri,
                myDatabaseHelper.COURSE_TABLE,
                new String[]{"id", "name", "teacher_id", "credit", "hours"},
                new String[]{"课程ID", "课程名称", "教师ID", "学分", "学时"},
                "课程数据表"
        );
    }


    // -------------------------- 通用Excel导出逻辑（核心） --------------------------
    /**
     * 通用Excel导出方法
     * @param context 上下文
     * @param uri 保存Excel的目标URI
     * @param table 数据库表名
     * @param dbColumns 数据库查询列名数组
     * @param excelHeaders Excel表头数组（与dbColumns一一对应）
     * @param sheetName Excel工作表名称
     * @return 导出成功返回true，失败返回false
     */
    private static boolean exportExcelData(Context context, Uri uri, String table,
                                           String[] dbColumns, String[] excelHeaders, String sheetName) {
        Workbook workbook = null;
        OutputStream outputStream = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            // 1. 数据库有效性检查（保留原逻辑，确保数据库可用）
            myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(context);
            if (dbHelper == null) {
                Log.e(TAG, "数据库帮助类实例为null！");
                Toast.makeText(context, "数据库初始化失败", Toast.LENGTH_SHORT).show();
                return false;
            }
            db = dbHelper.getReadableDatabase();
            if (db == null || !db.isOpen()) {
                Log.e(TAG, "无法打开数据库！");
                Toast.makeText(context, "无法访问数据库", Toast.LENGTH_SHORT).show();
                return false;
            }

            // 2. 创建Excel工作簿和工作表
            workbook = new XSSFWorkbook(); // 生成xlsx格式（兼容新版Excel）
            Sheet sheet = workbook.createSheet(sheetName);
            // 设置列宽自适应（优化显示效果）
            for (int i = 0; i < excelHeaders.length; i++) {
                sheet.setColumnWidth(i, 20 * 256); // 20个字符宽度
            }

            // 3. 写入Excel表头（第一行）
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < excelHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(excelHeaders[i]); // 填充表头文字
            }
            Log.d(TAG, "Excel表头已写入：" + sheetName);

            // 4. 查询数据库数据
            cursor = db.query(table, dbColumns, null, null, null, null, null);
            int dataCount = cursor.getCount();
            Log.d(TAG, "查询到" + dataCount + "条" + sheetName + "数据");
            if (dataCount == 0) {
                return true; // 无数据仍算成功（生成空Excel）
            }

            // 5. 循环写入数据到Excel（从第二行开始，行号=1）
            int rowIndex = 1;
            while (cursor.moveToNext()) {
                Row dataRow = sheet.createRow(rowIndex++);
                // 按列填充数据（与表头、数据库列一一对应）
                for (int i = 0; i < dbColumns.length; i++) {
                    Cell cell = dataRow.createCell(i);
                    String cellValue = cursor.getString(i); // 从数据库获取值

                    // 特殊处理数字类型（成绩、学分、学时等），避免Excel中显示为文本
                    if (dbColumns[i].contains("Score") || dbColumns[i].equals("credit") || dbColumns[i].equals("hours") || dbColumns[i].equals("ranking")) {
                        try {
                            cell.setCellValue(Integer.parseInt(cellValue)); // 转为数字类型
                        } catch (NumberFormatException e) {
                            cell.setCellValue(cellValue); // 转换失败则保留文本
                        }
                    } else {
                        cell.setCellValue(cellValue != null ? cellValue : ""); // 文本类型直接填充
                    }
                }
            }
            Log.d(TAG, "数据写入完成，共" + (rowIndex - 1) + "条");

            // 6. 将Excel写入目标URI（通过ContentResolver获取输出流）
            outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream == null) {
                Log.e(TAG, "Excel输出流为null！");
                return false;
            }
            workbook.write(outputStream);
            outputStream.flush();
            return true;

        } catch (Exception e) {
            Log.e(TAG, "导出Excel失败: " + e.getMessage(), e);
            Toast.makeText(context, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;

        } finally {
            // 7. 关闭所有资源（避免内存泄漏）
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
            if (workbook != null) {
                try { workbook.close(); } catch (IOException e) { Log.e(TAG, "关闭Workbook失败", e); }
            }
            if (outputStream != null) {
                try { outputStream.close(); } catch (IOException e) { Log.e(TAG, "关闭OutputStream失败", e); }
            }
        }
    }


    // -------------------------- 测试模式（保留原功能） --------------------------
    /**
     * 测试模式：生成固定Excel数据，跳过数据库操作
     */
    private static boolean testExportExcel(Context context, Uri uri) {
        Workbook workbook = null;
        OutputStream outputStream = null;

        try {
            Log.d(TAG, "【测试模式】开始生成Excel，URI: " + uri);
            // 1. 创建测试用Excel
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("学生数据测试");
            sheet.setColumnWidth(0, 15 * 256);
            sheet.setColumnWidth(1, 15 * 256);
            sheet.setColumnWidth(2, 20 * 256);

            // 2. 写入测试表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("测试ID");
            headerRow.createCell(1).setCellValue("测试名称");
            headerRow.createCell(2).setCellValue("测试内容");

            // 3. 写入测试数据
            Row dataRow1 = sheet.createRow(1);
            dataRow1.createCell(0).setCellValue("TEST001");
            dataRow1.createCell(1).setCellValue("测试数据1");
            dataRow1.createCell(2).setCellValue("Excel测试模式正常");

            Row dataRow2 = sheet.createRow(2);
            dataRow2.createCell(0).setCellValue("TEST002");
            dataRow2.createCell(1).setCellValue("测试数据2");
            dataRow2.createCell(2).setCellValue("无数据库操作，仅生成文件");

            // 4. 写入到URI
            outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream == null) {
                Log.e(TAG, "【测试模式】输出流为null");
                return false;
            }
            workbook.write(outputStream);
            outputStream.flush();
            Log.d(TAG, "【测试模式】Excel生成完成");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "【测试模式】生成Excel失败: " + e.getMessage(), e);
            return false;

        } finally {
            if (workbook != null) {
                try { workbook.close(); } catch (IOException e) { Log.e(TAG, "【测试模式】关闭Workbook失败", e); }
            }
            if (outputStream != null) {
                try { outputStream.close(); } catch (IOException e) { Log.e(TAG, "【测试模式】关闭OutputStream失败", e); }
            }
        }
    }


    // -------------------------- 导入功能（保持原有TXT逻辑，无需修改） --------------------------
    // 导入学生数据（原TXT导入逻辑不变）
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

                String[] parts = line.split("\t"); // 仍用制表符分隔（原TXT格式）
                if (parts.length < 5) {
                    errors.add("第" + lineNum + "行：字段不足（至少需要5个字段）");
                    continue;
                }

                try {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String password = parts[2].trim();
                    String gender = parts[3].trim();
                    String phone = parts[4].trim();

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

            // 批量插入数据库（原逻辑不变）
            if (!students.isEmpty()) {
                myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(context);
                ContentValues[] values = new ContentValues[students.size()];
                for (int i = 0; i < students.size(); i++) {
                    ContentValues cv = getContentValues(students, i);
                    values[i] = cv;
                }
                dbHelper.bulkInsert(myDatabaseHelper.STUDENT_TABLE, values);
            }

        } catch (Exception e) {
            errors.add("导入失败：" + e.getMessage());
            Log.e(TAG, "导入学生数据异常", e);
        }

        return errors;
    }

    @NonNull
    private static ContentValues getContentValues(List<Student> students, int i) {
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
        return cv;
    }

    // 导入教师数据（原TXT逻辑不变）
    public static List<String> importTeachers(Context context, Uri uri) {
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

                String[] parts = line.split("\t");
                if (parts.length < 6) {
                    errors.add("第" + lineNum + "行：字段不足（至少需要6个字段）");
                    continue;
                }

                try {
                    ContentValues cv = getContentValues(parts);

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

    @NonNull
    private static ContentValues getContentValues(String[] parts) {
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
        return cv;
    }

    // 导入课程数据（原TXT逻辑不变）
    public static List<String> importCourses(Context context, Uri uri) {
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

                String[] parts = line.split("\t");
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