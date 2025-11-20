package com.example.studentmanager_system.Tools;

import com.example.studentmanager_system.Util.myDatabaseHelper;
import android.content.Context;
import java.util.List;

public class Student {
    private String id;         // 学号
    private String name;       // 姓名
    private String password;   // 密码
    private String sex;        // 性别
    private String number;     // 电话
    private int ranking;       // 排名
    private double completedCredits; // 已修学分
    private int grade;         // 年级
    private String clazz;      // 班级
    private double gpa;        // GPA

    // 构造函数（基础版本）
    public Student() {
    }

    // 构造函数（与CSVUtil中调用的参数匹配，但去掉成绩字段）
    public Student(String id, String name, String password, String sex, String number) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.sex = sex;
        this.number = number;
    }

    // 新增构造函数，包含年级和班级
    public Student(String id, String name, String password, String sex, String number, int grade, String clazz) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.sex = sex;
        this.number = number;
        this.grade = grade;
        this.clazz = clazz;
    }

    // Getter方法
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getSex() { return sex; }
    public String getNumber() { return number; }
    public int getRanking() { return ranking; }
    public double getCompletedCredits() { return completedCredits; }
    public int getGrade() { return grade; }          // 添加getter
    public String getClazz() { return clazz; }       // 添加getter
    public double getGPA() { return gpa; }           // 添加GPA的getter

    // Setter方法
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
    public void setSex(String sex) { this.sex = sex; }
    public void setNumber(String number) { this.number = number; }
    public void setRanking(int ranking) { this.ranking = ranking; }
    public void setCompletedCredits(double completedCredits) { this.completedCredits = completedCredits; }
    public void setGrade(int grade) { this.grade = grade; }        // 添加setter
    public void setClazz(String clazz) { this.clazz = clazz; }     // 添加setter
    public void setGPA(double gpa) { this.gpa = gpa; }             // 添加GPA的setter

    /**
     * 获取学生已选课程列表
     * @param context 应用上下文
     * @return 课程列表
     */
    public List<Course> getSelectedCourses(Context context) {
        myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(context);
        return dbHelper.getSelectedCourses(this.id);
    }

    /**
     * 选课
     * @param context 应用上下文
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 是否选课成功
     */
    public boolean selectCourse(Context context, String courseId, String teacherId) {
        myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(context);
        return dbHelper.selectCourse(this.id, courseId, teacherId);
    }

    /**
     * 退课
     * @param context 应用上下文
     * @param courseId 课程ID
     * @return 是否退课成功
     */
    public boolean dropCourse(Context context, String courseId) {
        myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(context);
        return dbHelper.dropCourse(this.id, courseId);
    }

    /**
     * 检查是否已选指定课程
     * @param context 应用上下文
     * @param courseId 课程ID
     * @return 是否已选该课程
     */
    public boolean isCourseSelected(Context context, String courseId) {
        myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(context);
        return dbHelper.isCourseSelected(this.id, courseId);
    }

    /**
     * 计算学生GPA
     * GPA = Σ(课程学分 × 课程绩点) / Σ(课程学分)
     * 课程绩点 = 成绩 × 0.05 (按每1分对应0.05绩点规则)
     * @param context 应用上下文
     * @return 计算得到的GPA值
     */
    public double calculateGPA(Context context) {
        List<Course> selectedCourses = getSelectedCourses(context);
        double totalCreditPoints = 0.0;
        double totalCredits = 0.0;

        for (Course course : selectedCourses) {
            double score = course.getScore();
            // 只计算有效成绩（排除未录入的成绩，即score >= 0）
            if (score >= 0) {
                double gradePoint = score * 0.05; // 每1分对应0.05绩点
                totalCreditPoints += course.getCredit() * gradePoint;
                totalCredits += course.getCredit();
            }
        }

        if (totalCredits == 0) {
            return 0.0;
        }

        double calculatedGPA = totalCreditPoints / totalCredits;
        // 更新学生GPA
        setGPA(calculatedGPA);
        return calculatedGPA;
    }

    /**
     * 根据成绩更新已完成学分
     * @param context 应用上下文
     * @return 更新后的已完成学分
     */
    public double updateCompletedCredits(Context context) {
        myDatabaseHelper dbHelper = myDatabaseHelper.getInstance(context);
        double completedCredits = 0.0;
        List<Course> selectedCourses = getSelectedCourses(context);

        for (Course course : selectedCourses) {
            // 成绩大于等于60分视为获得该课程学分
            if (course.getScore() >= 60) {
                completedCredits += course.getCredit();
            }
        }

        // 更新学生已完成学分
        setCompletedCredits(completedCredits);
        // 同步更新数据库
        dbHelper.updateStudentCredits(this.id, completedCredits);
        return completedCredits;
    }
}
