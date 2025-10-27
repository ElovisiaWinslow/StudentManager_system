package com.example.studentmanager_system.Tools;

public class Course {
    private String id;          // 课程ID
    private String name;        // 课程名称
    private String teacherId;   // 教师ID（关联教师表）
    private String teacherName; // 教师姓名（查询时关联获取）
    private float credit;       // 学分
    private int hours;          // 学时
    private String subject;     // 课程所属学科

    // getter和setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public float getCredit() { return credit; }
    public void setCredit(float credit) { this.credit = credit; }

    public int getHours() { return hours; }
    public void setHours(int hours) { this.hours = hours; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
}