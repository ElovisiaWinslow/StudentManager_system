package com.example.studentmanager_system.Tools;

public class StudentCourse {
    private int id;
    private String studentId;
    private String courseId;
    private double score;

    // 构造函数
    public StudentCourse(int id, String studentId, String courseId, double score) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.score = score;
    }

    // Getter和Setter方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}
