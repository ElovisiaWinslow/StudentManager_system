package com.example.studentmanager_system.Tools;

public class Student {
    private String id;         // 学号
    private String name;       // 姓名
    private String password;   // 密码
    private String sex;        // 性别（注意字段名是sex，不是gender）
    private String number;     // 电话（注意字段名是number，不是phone）
    private int mathScore;     // 数学成绩
    private int chineseScore;  // 语文成绩
    private int englishScore;  // 英语成绩
    private int ranking;       // 排名

    // 构造函数（与CSVUtil中调用的参数匹配）
    public Student(String id, String name, String password, String sex, String number,
                   int mathScore, int chineseScore, int englishScore) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.sex = sex;
        this.number = number;
        this.mathScore = mathScore;
        this.chineseScore = chineseScore;
        this.englishScore = englishScore;
    }

    // Getter方法（与CSVUtil中调用的方法匹配）
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getSex() { return sex; }  // 注意方法名是getSex()
    public String getNumber() { return number; }  // 注意方法名是getNumber()
    public int getMathScore() { return mathScore; }
    public int getChineseScore() { return chineseScore; }
    public int getEnglishScore() { return englishScore; }
    public int getRanking() { return ranking; }

    // Setter方法（可选，根据需要添加）
    public void setRanking(int ranking) { this.ranking = ranking; }
}

