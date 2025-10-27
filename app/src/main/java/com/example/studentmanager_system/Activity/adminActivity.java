package com.example.studentmanager_system.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.studentmanager_system.R;

/**
 * 管理员主界面
 */
public class adminActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_layout);

        //初始化相关控件
        //查询学生信息按钮
        Button select = findViewById(R.id.admin_activity_select);
        //添加学生信息按钮
        Button add = findViewById(R.id.admin_activity_add);
        Button addTeacherBtn = findViewById(R.id.admin_activity_add_teacher);
        Button teacherInfoBtn = findViewById(R.id.admin_teacher_info);
        //查看总成绩排名按钮
        Button order = findViewById(R.id.admin_activity_order);
        //强制下线
        TextView forceOffline = findViewById(R.id.admin_activity_forceOffline);
        // 新增：初始化数据管理按钮
        // 新增：数据管理按钮
        Button dataManageBtn = findViewById(R.id.data_manage_btn);

        /*
          查询学生信息
         */
        select.setOnClickListener(view -> {
            Intent intent = new Intent(adminActivity.this, studentinfoActivity.class);
            startActivity(intent);
        });

        /*
          添加学生信息
         */
        add.setOnClickListener(view -> {
            Intent intent = new Intent(adminActivity.this, add_studentinfoActivity.class);
            intent.putExtra("haveData","false");
            startActivity(intent);
        });

        addTeacherBtn.setOnClickListener(v -> {
            Intent intent = new Intent(adminActivity.this, add_teacherinfoActivity.class);
            intent.putExtra("haveData", "false"); // 标记为新增
            startActivity(intent);
        });

        teacherInfoBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(adminActivity.this, teacherinfoActivity.class);
                    startActivity(intent);
        });

        /*
          查询学生总成绩
         */
        order.setOnClickListener(v -> {
            Intent intent=new Intent(adminActivity.this,student_total_score.class);
            startActivity(intent);
        });

        /*
          强制下线
         */
        forceOffline.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction("com.example.OfflineBroadcast");
            sendBroadcast(intent);
        });

        // 新增：数据导入导出管理按钮点击事件
        dataManageBtn.setOnClickListener(v -> {
            // 跳转到数据管理界面
            Intent intent = new Intent(adminActivity.this, DataManagementActivity.class);
            startActivity(intent);
        });

    }
}
