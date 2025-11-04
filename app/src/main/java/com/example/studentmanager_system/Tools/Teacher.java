package com.example.studentmanager_system.Tools;

public class Teacher {
    private String id;
    private String name;
    private String password;
    private String gender;
    private String phone;
    private String course;  // 修改属性名从 subject 为 course
    private String college;     // 新增字段：所在学院
    private String department;  // 新增字段：所在系

    // 构造函数（更新以包含新字段）
    public Teacher(String id, String name, String password, String gender, String phone, String course, String college, String department) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.gender = gender;
        this.phone = phone;
        this.course = course;
        this.college = college;     // 初始化新增字段
        this.department = department; // 初始化新增字段
    }

    // Getter和Setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // 修改方法名从 getSubject 为 getCourse
    public String getCourse() { return course; }
    // 修改方法名从 setSubject 为 setCourse
    public void setCourse(String course) { this.course = course; }

    // 新增字段的Getter和Setter方法
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
