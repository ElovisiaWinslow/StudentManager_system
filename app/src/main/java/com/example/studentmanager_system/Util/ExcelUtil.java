package com.example.studentmanager_system.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.studentmanager_system.Tools.Student;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {
    private static final String TAG = "ExcelUtil";
    // 测试模式开关（保留原功能，用于定位问题）
    private static final boolean TEST_MODE = false;

    // -------------------------- 导出Excel核心方法 --------------------------
    // 导出学生数据（保留测试模式）
    // 修改导出学生数据方法中的列名和表头
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
                new String[]{"id", "name", "password", "sex", "number", "completedCredits", "GPA", "grade", "class"},
                new String[]{"学号", "姓名", "密码", "性别", "电话", "已完成学分", "GPA", "年级", "班级"},
                "学生数据表"
        );
    }

    // 导出教师数据
    public static boolean exportTeachers(Context context, Uri uri) {
        return exportExcelData(
                context,
                uri,
                myDatabaseHelper.TEACHER_TABLE,
                new String[]{"id", "name", "password", "sex", "phone", "college", "department", "course"},
                new String[]{"教师ID", "姓名", "密码", "性别", "电话", "所在学院", "所在系", "教授课程"},
                "教师数据表"
        );
    }

    // 导出课程数据（已修改：包含所有课程字段，包括subject）
    public static boolean exportCourses(Context context, Uri uri) {
        return exportExcelData(
                context,
                uri,
                myDatabaseHelper.COURSE_TABLE,
                new String[]{"id", "name", "teacher_id", "subject", "credit", "hours", "class_time", "class_location", "average_score", "grade"},
                new String[]{"课程ID", "课程名称", "教师ID", "科目", "学分", "学时", "上课时间", "上课地点", "平均成绩", "年级"},
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
                    if (dbColumns[i].equals("GPA") || dbColumns[i].equals("credit") ||
                            dbColumns[i].equals("hours") || dbColumns[i].equals("completedCredits") ||
                            dbColumns[i].equals("grade")) { // 添加对grade字段的处理
                        try {
                            if (cellValue != null && !cellValue.isEmpty()) {
                                if (dbColumns[i].equals("credit") || dbColumns[i].equals("completedCredits") || dbColumns[i].equals("GPA")) {
                                    cell.setCellValue(Double.parseDouble(cellValue)); // 浮点数类型
                                } else {
                                    cell.setCellValue(Integer.parseInt(cellValue)); // 整数类型
                                }
                            } else {
                                cell.setCellValue(""); // 空值处理
                            }
                        } catch (NumberFormatException e) {
                            cell.setCellValue(cellValue != null ? cellValue : ""); // 转换失败则保留文本
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

    // -------------------------- 导入功能（使用Excel格式） --------------------------
    // 导入学生数据（使用Excel格式）
    public static List<String> importStudents(Context context, Uri uri) {
        List<String> errors = new ArrayList<>();
        List<Student> students = new ArrayList<>();

        InputStream inputStream = null;
        Workbook workbook = null;
        SQLiteDatabase db = null;

        try {
            // 打开输入流并创建工作簿
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                errors.add("无法打开文件输入流");
                return errors;
            }
            workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表

            // 检查表头 - 根据新格式验证
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                errors.add("Excel文件格式错误，未找到表头");
                return errors;
            }

            // 验证表头格式是否正确
            String[] expectedHeaders = {"学号", "姓名", "密码", "性别", "电话", "年级", "班级", "已完成学分", "GPA"};
            for (int i = 0; i < expectedHeaders.length; i++) {
                if (!expectedHeaders[i].equals(getCellStringValue(headerRow.getCell(i)))) {
                    errors.add("Excel文件表头格式错误，第" + (i+1) + "列应为'" + expectedHeaders[i] + "'");
                    return errors;
                }
            }

            // 获取数据库实例
            myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(context);
            db = dbHelper.getWritableDatabase(); // 使用可写数据库
            db.beginTransaction(); // 开启事务

            // 遍历数据行（从第二行开始）
            int rowCount = sheet.getPhysicalNumberOfRows();
            for (int r = 1; r < rowCount; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                try {
                    // 根据表头位置读取数据
                    String id = getCellStringValue(row.getCell(0)); // 学号
                    String name = getCellStringValue(row.getCell(1));     // 姓名
                    String password = getCellStringValue(row.getCell(2)); // 密码
                    String gender = getCellStringValue(row.getCell(3));   // 性别
                    String phone = getCellStringValue(row.getCell(4));    // 电话

                    // 读取年级
                    int grade = 0;
                    try {
                        Cell gradeCell = row.getCell(5); // 年级在第6列
                        if (gradeCell != null) {
                            if (gradeCell.getCellType() == CellType.NUMERIC) {
                                grade = (int) gradeCell.getNumericCellValue();
                            } else {
                                grade = Integer.parseInt(getCellStringValue(gradeCell));
                            }
                        }
                    } catch (NumberFormatException e) {
                        // 保持默认值0
                    }

                    // 读取班级
                    String clazz = getCellStringValue(row.getCell(6)); // 班级在第7列

                    // 读取已完成学分
                    float completedCredits = 0;
                    try {
                        Cell creditsCell = row.getCell(7); // 已完成学分在第8列
                        if (creditsCell != null) {
                            if (creditsCell.getCellType() == CellType.NUMERIC) {
                                completedCredits = (float) creditsCell.getNumericCellValue();
                            } else {
                                completedCredits = Float.parseFloat(getCellStringValue(creditsCell));
                            }
                        }
                    } catch (NumberFormatException e) {
                        // 保持默认值0
                    }

                    // 读取 GPA
                    float gpa = 0;
                    try {
                        Cell gpaCell = row.getCell(8); // GPA 在第9列
                        if (gpaCell != null) {
                            if (gpaCell.getCellType() == CellType.NUMERIC) {
                                gpa = (float) gpaCell.getNumericCellValue();
                            } else {
                                gpa = Float.parseFloat(getCellStringValue(gpaCell));
                            }
                        }
                    } catch (NumberFormatException e) {
                        // 保持默认值0
                    }

                    // 创建学生对象
                    Student student = new Student(id, name, password, gender, phone, grade, clazz);
                    student.setCompletedCredits(completedCredits);
                    student.setGPA(gpa); // 设置 GPA 值
                    students.add(student);

                    // 直接插入数据库（使用REPLACE策略）
                    ContentValues cv = new ContentValues();
                    cv.put("id", id);
                    cv.put("name", name);
                    cv.put("password", password);
                    cv.put("sex", gender);
                    cv.put("number", phone);
                    cv.put("grade", grade);
                    cv.put("class", clazz);
                    cv.put("completedCredits", completedCredits);
                    cv.put("GPA", gpa);

                    long result = db.insertWithOnConflict(
                            myDatabaseHelper.STUDENT_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_REPLACE
                    );

                    if (result == -1) {
                        errors.add("第" + (r + 1) + "行：插入数据库失败");
                        Log.e(TAG, "插入学生失败: " + id);
                    } else {
                        Log.d(TAG, "成功插入/更新学生: " + id + ", 姓名: " + name);
                    }

                } catch (Exception e) {
                    errors.add("第" + (r + 1) + "行：" + e.getMessage());
                    Log.e(TAG, "处理第" + (r + 1) + "行时出错", e);
                }
            }

            db.setTransactionSuccessful(); // 标记事务成功
            Log.d(TAG, "学生导入事务提交成功，共处理 " + students.size() + " 条记录");

        } catch (Exception e) {
            errors.add("导入失败：" + e.getMessage());
            Log.e(TAG, "导入学生数据异常", e);
        } finally {
            // 关闭资源
            if (db != null) {
                db.endTransaction();
                db.close();
            }
            if (workbook != null) {
                try { workbook.close(); } catch (IOException e) { Log.e(TAG, "关闭Workbook失败", e); }
            }
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException e) { Log.e(TAG, "关闭InputStream失败", e); }
            }
        }

        return errors;
    }

    // 导入教师数据（使用Excel格式）
    public static List<String> importTeachers(Context context, Uri uri) {
        List<String> errors = new ArrayList<>();

        InputStream inputStream = null;
        Workbook workbook = null;
        SQLiteDatabase db = null;

        try {
            // 打开输入流并创建工作簿
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                errors.add("无法打开文件输入流");
                return errors;
            }
            workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表

            // 检查表头
            Row headerRow = sheet.getRow(0);
            if (headerRow == null || !getCellStringValue(headerRow.getCell(0)).contains("教师ID")) {
                errors.add("Excel文件格式错误，未找到正确的表头");
                return errors;
            }

            // 获取数据库实例
            myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(context);
            db = dbHelper.getWritableDatabase(); // 使用可写数据库
            db.beginTransaction(); // 开启事务

            // 遍历数据行（从第二行开始）
            int rowCount = sheet.getPhysicalNumberOfRows();
            for (int r = 1; r < rowCount; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                try {
                    // 读取教师信息
                    String id = getCellStringValue(row.getCell(0));
                    String name = getCellStringValue(row.getCell(1));
                    String password = getCellStringValue(row.getCell(2));
                    String sex = getCellStringValue(row.getCell(3));
                    String phone = getCellStringValue(row.getCell(4));
                    String college = getCellStringValue(row.getCell(5));      // 新增字段
                    String department = getCellStringValue(row.getCell(6));   // 新增字段
                    String course = getCellStringValue(row.getCell(7));       // 更新字段名

                    // 插入数据库（使用REPLACE策略）
                    ContentValues cv = new ContentValues();
                    cv.put("id", id);
                    cv.put("name", name);
                    cv.put("password", password);
                    cv.put("sex", sex);
                    cv.put("phone", phone);
                    cv.put("college", college);       // 新增字段
                    cv.put("department", department); // 新增字段
                    cv.put("course", course);         // 更新字段名

                    long result = db.insertWithOnConflict(
                            myDatabaseHelper.TEACHER_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_REPLACE
                    );

                    if (result == -1) {
                        errors.add("第" + (r + 1) + "行：插入数据库失败");
                        Log.e(TAG, "插入教师失败: " + id);
                    } else {
                        Log.d(TAG, "成功插入/更新教师: " + id + ", 姓名: " + name);
                    }

                    // 移除了原有的自动创建课程的逻辑

                } catch (Exception e) {
                    errors.add("第" + (r + 1) + "行：" + e.getMessage());
                    Log.e(TAG, "处理第" + (r + 1) + "行时出错", e);
                }
            }

            db.setTransactionSuccessful(); // 标记事务成功
            Log.d(TAG, "教师导入事务提交成功");

        } catch (Exception e) {
            errors.add("导入失败：" + e.getMessage());
            Log.e(TAG, "导入教师数据异常", e);
        } finally {
            // 关闭资源
            if (db != null) {
                db.endTransaction();
                db.close();
            }
            if (workbook != null) {
                try { workbook.close(); } catch (IOException e) { Log.e(TAG, "关闭Workbook失败", e); }
            }
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException e) { Log.e(TAG, "关闭InputStream失败", e); }
            }
        }

        return errors;
    }


    // 导入课程数据（已修改：处理所有课程字段，包括subject）
    public static List<String> importCourses(Context context, Uri uri) {
        List<String> errors = new ArrayList<>();

        InputStream inputStream = null;
        Workbook workbook = null;
        SQLiteDatabase db = null;

        try {
            // 打开输入流并创建工作簿
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                errors.add("无法打开文件输入流");
                return errors;
            }
            workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表

            // 检查表头
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                errors.add("Excel文件格式错误，未找到表头");
                return errors;
            }

            // 打印表头信息用于调试
            StringBuilder headerInfo = new StringBuilder("表头信息: ");
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                headerInfo.append(getCellStringValue(headerRow.getCell(i))).append(" | ");
            }
            Log.d(TAG, headerInfo.toString());

            // 获取数据库实例
            myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(context);
            db = dbHelper.getWritableDatabase(); // 使用可写数据库
            db.beginTransaction(); // 开启事务

            // 遍历数据行（从第二行开始）
            int rowCount = sheet.getPhysicalNumberOfRows();
            Log.d(TAG, "Excel文件总行数: " + rowCount);

            int successCount = 0;
            for (int r = 1; r < rowCount; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    Log.w(TAG, "第" + (r + 1) + "行为空，跳过");
                    continue;
                }

                try {
                    // 读取课程信息
                    String id = getCellStringValue(row.getCell(0));
                    String name = getCellStringValue(row.getCell(1));
                    String teacherId = getCellStringValue(row.getCell(2));
                    String subject = getCellStringValue(row.getCell(3)); // 新增subject字段处理

                    // 处理数值类型字段
                    double credit = 0;
                    int hours = 0;
                    double averageScore = 0;
                    int grade = 1; // 默认年级为1

                    try {
                        Cell creditCell = row.getCell(4);
                        if (creditCell != null) {
                            if (creditCell.getCellType() == CellType.NUMERIC) {
                                credit = creditCell.getNumericCellValue();
                            } else {
                                credit = Double.parseDouble(getCellStringValue(creditCell));
                            }
                        }
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "学分格式错误，课程ID: " + id + ", 使用默认值0");
                    }

                    try {
                        Cell hoursCell = row.getCell(5);
                        if (hoursCell != null) {
                            if (hoursCell.getCellType() == CellType.NUMERIC) {
                                hours = (int) hoursCell.getNumericCellValue();
                            } else {
                                hours = Integer.parseInt(getCellStringValue(hoursCell));
                            }
                        }
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "学时格式错误，课程ID: " + id + ", 使用默认值0");
                    }

                    // 处理新增字段（上课时间、上课地点、平均成绩）
                    String classTime = getCellStringValue(row.getCell(6));
                    String classLocation = getCellStringValue(row.getCell(7));

                    try {
                        Cell averageScoreCell = row.getCell(8);
                        if (averageScoreCell != null) {
                            if (averageScoreCell.getCellType() == CellType.NUMERIC) {
                                averageScore = averageScoreCell.getNumericCellValue();
                            } else {
                                averageScore = Double.parseDouble(getCellStringValue(averageScoreCell));
                            }
                        }
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "平均成绩格式错误，课程ID: " + id + ", 使用默认值0");
                    }

                    // 处理年级字段
                    try {
                        Cell gradeCell = row.getCell(9);
                        if (gradeCell != null) {
                            if (gradeCell.getCellType() == CellType.NUMERIC) {
                                grade = (int) gradeCell.getNumericCellValue();
                            } else {
                                grade = Integer.parseInt(getCellStringValue(gradeCell));
                            }
                        }
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "年级格式错误，课程ID: " + id + ", 使用默认值1");
                    }

                    // 验证必要字段
                    if (TextUtils.isEmpty(id)) {
                        errors.add("第" + (r + 1) + "行：课程ID不能为空");
                        continue;
                    }

                    if (TextUtils.isEmpty(name)) {
                        errors.add("第" + (r + 1) + "行：课程名称不能为空");
                        continue;
                    }

                    // 插入数据库（使用REPLACE策略实现更新或插入）
                    ContentValues cv = new ContentValues();
                    cv.put("id", id);
                    cv.put("name", name);
                    cv.put("teacher_id", teacherId);
                    cv.put("subject", subject); // 使用从Excel读取的subject值
                    cv.put("credit", credit);
                    cv.put("hours", hours);
                    cv.put("class_time", classTime);
                    cv.put("class_location", classLocation);
                    cv.put("grade", grade);
                    cv.put("average_score", averageScore);

                    long result = db.insertWithOnConflict(
                            myDatabaseHelper.COURSE_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_REPLACE
                    );

                    if (result == -1) {
                        errors.add("第" + (r + 1) + "行：插入数据库失败");
                        Log.e(TAG, "插入课程失败: " + id);
                    } else {
                        successCount++;
                        Log.d(TAG, "成功插入/更新课程: " + id + ", 名称: " + name +
                                ", 教师ID: " + teacherId + ", 学分: " + credit +
                                ", 学时: " + hours + ", 年级: " + grade);
                    }

                } catch (Exception e) {
                    errors.add("第" + (r + 1) + "行：" + e.getMessage());
                    Log.e(TAG, "处理第" + (r + 1) + "行时出错", e);
                }
            }

            db.setTransactionSuccessful(); // 标记事务成功
            Log.d(TAG, "课程导入事务提交成功，共成功处理 " + successCount + " 条记录");

            // 新增：更新教师表中的教授课程字段
            updateTeacherCourses(db);

        } catch (Exception e) {
            errors.add("导入失败：" + e.getMessage());
            Log.e(TAG, "导入课程数据异常", e);
        } finally {
            // 关闭资源
            if (db != null) {
                db.endTransaction();
                db.close();
            }
            if (workbook != null) {
                try { workbook.close(); } catch (IOException e) { Log.e(TAG, "关闭Workbook失败", e); }
            }
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException e) { Log.e(TAG, "关闭InputStream失败", e); }
            }
        }

        return errors;
    }

    /**
     * 更新教师表中的教授课程字段
     * @param db 数据库实例
     */
    private static void updateTeacherCourses(SQLiteDatabase db) {
        // 查询所有教师
        Cursor teacherCursor = db.query(
                myDatabaseHelper.TEACHER_TABLE,
                new String[]{"id"},
                null, null, null, null, null
        );

        if (teacherCursor.moveToFirst()) {
            do {
                String teacherId = teacherCursor.getString(teacherCursor.getColumnIndexOrThrow("id"));

                // 获取该教师教授的所有课程
                StringBuilder courses = new StringBuilder();
                Cursor courseCursor = db.query(
                        myDatabaseHelper.COURSE_TABLE,
                        new String[]{"name"},
                        "teacher_id=?",
                        new String[]{teacherId},
                        null, null, null
                );

                if (courseCursor.moveToFirst()) {
                    do {
                        if (courses.length() > 0) {
                            courses.append(", ");
                        }
                        courses.append(courseCursor.getString(courseCursor.getColumnIndexOrThrow("name")));
                    } while (courseCursor.moveToNext());
                } else {
                    courses.append(""); // 如果没有课程，设置为空字符串
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
            } while (teacherCursor.moveToNext());
        }
        teacherCursor.close();

        Log.d(TAG, "教师教授课程字段更新完成");
    }

    // 辅助方法：安全获取单元格字符串值
    private static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    return String.valueOf((int) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    // 导出预警学生名单
    public static boolean exportWarningStudents(Context context, Uri uri, List<Student> warningStudents) {
        Workbook workbook = null;
        OutputStream outputStream = null;

        try {
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("学分预警学生名单");

            // 设置列宽
            sheet.setColumnWidth(0, 20 * 256);
            sheet.setColumnWidth(1, 20 * 256);
            sheet.setColumnWidth(2, 20 * 256);
            sheet.setColumnWidth(3, 20 * 256);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("学号");
            headerRow.createCell(1).setCellValue("姓名");
            headerRow.createCell(2).setCellValue("班级");
            headerRow.createCell(3).setCellValue("已完成学分");

            // 填充数据
            for (int i = 0; i < warningStudents.size(); i++) {
                Student student = warningStudents.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(student.getId());
                row.createCell(1).setCellValue(student.getName());
                row.createCell(2).setCellValue(student.getClazz());
                row.createCell(3).setCellValue(student.getCompletedCredits());
            }

            // 写入文件
            outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream == null) {
                return false;
            }
            workbook.write(outputStream);
            outputStream.flush();
            return true;

        } catch (Exception e) {
            Log.e(TAG, "导出预警学生失败: " + e.getMessage(), e);
            Toast.makeText(context, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        } finally {
            if (workbook != null) {
                try { workbook.close(); } catch (IOException e) { Log.e(TAG, "关闭Workbook失败", e); }
            }
            if (outputStream != null) {
                try { outputStream.close(); } catch (IOException e) { Log.e(TAG, "关闭OutputStream失败", e); }
            }
        }
    }
}
