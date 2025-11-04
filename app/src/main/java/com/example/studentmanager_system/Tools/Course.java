// app/src/main/java/com/example/studentmanager_system/Tools/Course.java
package com.example.studentmanager_system.Tools;

import java.io.Serializable;
import java.util.List;

public class Course implements Serializable {
    private String id;          // 课程ID
    private String name;        // 课程名称
    private String teacherId;   // 教师ID（关联教师表）
    private String teacherName; // 教师姓名（查询时关联获取）
    private List<String> teacherNames; // 多个教师姓名列表
    private float credit;       // 学分
    private int hours;          // 学时
    private String subject;     // 课程所属学科
    private String classTime;   // 上课时间
    private String classLocation; // 上课地点
    private float averageScore;   // 平均成绩
    private float score = -1;   // 学生成绩，默认为-1表示未知

    // getter和setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public List<String> getTeacherNames() { return teacherNames; }
    public void setTeacherNames(List<String> teacherNames) { this.teacherNames = teacherNames; }

    public float getCredit() { return credit; }
    public void setCredit(float credit) { this.credit = credit; }

    public int getHours() { return hours; }
    public void setHours(int hours) { this.hours = hours; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    // 新增字段的getter和setter
    public String getClassTime() { return classTime; }
    public void setClassTime(String classTime) { this.classTime = classTime; }

    public String getClassLocation() { return classLocation; }
    public void setClassLocation(String classLocation) { this.classLocation = classLocation; }

    public float getAverageScore() { return averageScore; }
    public void setAverageScore(float averageScore) { this.averageScore = averageScore; }

    // 成绩的getter和setter
    public float getScore() { return score; }
    public void setScore(float score) { this.score = score; }
}
