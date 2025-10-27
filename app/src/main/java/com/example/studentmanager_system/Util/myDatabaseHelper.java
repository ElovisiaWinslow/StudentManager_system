package com.example.studentmanager_system.Util;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.studentmanager_system.Tools.Course;

import java.util.ArrayList;
import java.util.List;

public class myDatabaseHelper extends SQLiteOpenHelper {

    private static myDatabaseHelper instance;
    // 数据库名称
    private static final String DB_NAME = "StudentManagement.db";
    // 数据库版本（从2升级到3，支持新表）
    private static final int DB_VERSION = 4;

    // 表名常量（便于后续引用）
    public static final String ADMIN_TABLE = "admin";
    public static final String STUDENT_TABLE = "student";
    public static final String TEACHER_TABLE = "teacher";       // 新增：教师表
    public static final String COURSE_TABLE = "course";         // 新增：课程表
    public static final String STUDENT_COURSE_TABLE = "student_course";  // 新增：学生选课表

    // 表结构SQL语句
    public static final String CREATE_ADMIN = "create table " + ADMIN_TABLE + " (" +
            "id integer primary key autoincrement, " +
            "name text not null, " +
            "password text not null)";

    public static final String CREATE_STUDENT = "create table " + STUDENT_TABLE + " (" +
            "id text primary key, " +
            "name text not null, " +
            "password text not null, " +
            "sex text not null, " +
            "number text, " +  // 这里保持你原有的"number"字段（推测是手机号）
            "mathScore integer, " +
            "chineseScore integer, " +
            "englishScore integer, " +
            "ranking integer)";  // 已有的排名字段

    // 新增：教师表结构（id为主键，包含姓名、密码、性别、电话、任教科目）
    public static final String CREATE_TEACHER = "create table " + TEACHER_TABLE + " (" +
            "id text primary key, " +
            "name text not null, " +
            "password text not null, " +
            "sex text, " +  // 性别（可空）
            "phone text, " +  // 电话（可空）
            "subject text)";  // 任教科目

    // 新增：课程表结构（包含外键关联教师）
    public static final String CREATE_COURSE = "create table " + COURSE_TABLE + " (" +
            "id text primary key, " +
            "name text not null, " +
            "teacher_id text, " +
            "subject text not null, " +
            "credit real, " +  // 学分
            "hours integer, " +  // 学时
            "foreign key(teacher_id) references " + TEACHER_TABLE + "(id))";  // 外键约束

    // 新增：学生选课表（关联学生和课程，记录成绩）
    public static final String CREATE_STUDENT_COURSE = "create table " + STUDENT_COURSE_TABLE + " (" +
            "id integer primary key autoincrement, " +
            "student_id text, " +  // 学生ID（关联学生表）
            "course_id text, " +  // 课程ID（关联课程表）
            "score real, " +  // 课程成绩
            "foreign key(student_id) references " + STUDENT_TABLE + "(id), " +
            "foreign key(course_id) references " + COURSE_TABLE + "(id), " +
            "unique(student_id, course_id))";  // 唯一约束：一个学生不能重复选同一门课

    // 私有构造函数（单例模式）
    private myDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // 单例模式获取实例
    public static myDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            // 使用应用上下文，避免内存泄漏
            instance = new myDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建所有表（新表和原有表）
        db.execSQL(CREATE_ADMIN);
        db.execSQL(CREATE_STUDENT);
        db.execSQL(CREATE_TEACHER);       // 新增
        db.execSQL(CREATE_COURSE);        // 新增
        db.execSQL(CREATE_STUDENT_COURSE); // 新增
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // 版本1→2：添加学生表的ranking字段（保持你原有的逻辑）
        if (oldVersion < 2) {
            db.execSQL("alter table " + STUDENT_TABLE + " add column ranking integer");
        }
        // 版本2→3：新增教师、课程、学生选课表（支持新功能）
        if (oldVersion < 3) {
            db.execSQL(CREATE_TEACHER);
            db.execSQL(CREATE_COURSE);
            db.execSQL(CREATE_STUDENT_COURSE);
        }
        if (oldVersion < 4) {
            db.execSQL("alter table " + COURSE_TABLE + " add column subject text not null default ''");
        }
    }

    // 新增：批量插入数据（用于导入功能）
    public void bulkInsert(String table, ContentValues[] values) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction(); // 开启事务，提升批量插入性能
        try {
            for (ContentValues value : values) {
                // 存在冲突时替换（如ID重复则更新）
                db.insertWithOnConflict(table, null, value, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful(); // 标记事务成功
        } finally {
            db.endTransaction(); // 结束事务（成功则提交，失败则回滚）
        }
    }

    // 检查学生是否已选该课程（利用表的unique约束）
    public boolean isCourseSelected(String studentId, String courseId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(STUDENT_COURSE_TABLE, null,
                "student_id=? AND course_id=?",
                new String[]{studentId, courseId},
                null, null, null);
        boolean isSelected = cursor.getCount() > 0;
        cursor.close();
        return isSelected;
    }


    // 获取学生已选课程详情
    @SuppressLint("Range")
    public List<Course> getSelectedCourses(String studentId) {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT c.*, t.name as teacher_name " +
                "FROM " + STUDENT_COURSE_TABLE + " sc " +
                "JOIN " + COURSE_TABLE + " c ON sc.course_id = c.id " +
                "LEFT JOIN " + TEACHER_TABLE + " t ON c.teacher_id = t.id " +
                "WHERE sc.student_id = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{studentId});
        while (cursor.moveToNext()) {
            Course course = new Course();
            course.setId(cursor.getString(cursor.getColumnIndex("id")));
            course.setName(cursor.getString(cursor.getColumnIndex("name")));
            course.setTeacherName(cursor.getString(cursor.getColumnIndex("teacher_name")));
            course.setCredit(cursor.getFloat(cursor.getColumnIndex("credit")));
            course.setHours(cursor.getInt(cursor.getColumnIndex("hours")));
            course.setSubject(cursor.getString(cursor.getColumnIndex("subject")));
            courses.add(course);
        }
        cursor.close();
        return courses;
    }

    // 获取学生已选课程ID列表
    public List<String> getSelectedCourseIds(String studentId) {
        List<String> courseIds = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select course_id from " + STUDENT_COURSE_TABLE + " where student_id=?",
                new String[]{studentId}
        );
        while (cursor.moveToNext()) {
            courseIds.add(cursor.getString(0));
        }
        cursor.close();
        return courseIds;
    }

    // 选课：插入选课记录
    public boolean selectCourse(String studentId, String courseId) {
        // 检查是否已选该课程
        List<String> selected = getSelectedCourseIds(studentId);
        if (selected.contains(courseId)) {
            return false; // 已选则返回失败
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("student_id", studentId);
        values.put("course_id", courseId);
        values.put("score", 0); // 初始成绩为0
        long rowId = db.insert(STUDENT_COURSE_TABLE, null, values);
        return rowId != -1;
    }

    // 退课：删除选课记录
    public boolean dropCourse(String studentId, String courseId) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsDeleted = db.delete(
                STUDENT_COURSE_TABLE,
                "student_id=? and course_id=?",
                new String[]{studentId, courseId}
        );
        return rowsDeleted > 0;
    }

    // 获取所有课程（补充方法）
    @SuppressLint("Range")
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(COURSE_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Course course = new Course();
            course.setId(cursor.getString(cursor.getColumnIndex("id")));
            course.setName(cursor.getString(cursor.getColumnIndex("name")));
            course.setTeacherId(cursor.getString(cursor.getColumnIndex("teacher_id")));
            course.setCredit(cursor.getFloat(cursor.getColumnIndex("credit")));
            course.setHours(cursor.getInt(cursor.getColumnIndex("hours")));
            courses.add(course);
        }
        cursor.close();
        return courses;
    }

}
