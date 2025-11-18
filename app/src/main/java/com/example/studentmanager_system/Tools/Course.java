// app/src/main/java/com/example/studentmanager_system/Tools/Course.java
package com.example.studentmanager_system.Tools;

import java.io.Serializable;
import java.util.List;

public class Course implements Serializable {
    private String id;
    private String name;
    private String teacherName;
    private String teacherId;
    private float credit;
    private int hours;
    private String subject;
    private String classTime;       // 上课时间
    private String classLocation;   // 上课地点
    private float averageScore;     // 平均成绩
    private int grade;              // 年级
    private float score;            // 学生成绩
    private String selectedTeacherId; // 学生选择的教师ID

    // 用于存储同名课程的不同实例
    private List<Course> courseInstances;

    // Getter和Setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public float getCredit() { return credit; }
    public void setCredit(float credit) { this.credit = credit; }

    public int getHours() { return hours; }
    public void setHours(int hours) { this.hours = hours; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getClassTime() { return classTime; }
    public void setClassTime(String classTime) { this.classTime = classTime; }

    public String getClassLocation() { return classLocation; }
    public void setClassLocation(String classLocation) { this.classLocation = classLocation; }

    public float getAverageScore() { return averageScore; }
    public void setAverageScore(float averageScore) { this.averageScore = averageScore; }

    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }

    public float getScore() { return score; }
    public void setScore(float score) { this.score = score; }

    public String getSelectedTeacherId() { return selectedTeacherId; }
    public void setSelectedTeacherId(String selectedTeacherId) { this.selectedTeacherId = selectedTeacherId; }

    public List<Course> getCourseInstances() { return courseInstances; }
    public void setCourseInstances(List<Course> courseInstances) { this.courseInstances = courseInstances; }
}
