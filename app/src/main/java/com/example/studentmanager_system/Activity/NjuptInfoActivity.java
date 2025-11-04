// NjuptInfoActivity.java
package com.example.studentmanager_system.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studentmanager_system.R;
import com.google.android.material.card.MaterialCardView;

public class NjuptInfoActivity extends Activity {

    private TextView tvTitle;
    private ImageView ivNjuptPhoto;
    private MaterialCardView cardIntroduction, cardHistory, cardDisciplines, cardCulture;
    private TextView tvContent;
    private MaterialCardView cardContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_njupt_info);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        // 设置标题与首页一致
        if (tvTitle != null) {
            tvTitle.setText("NJUPT 管理系统");
        }

        cardIntroduction = findViewById(R.id.card_introduction);
        cardHistory = findViewById(R.id.card_history);
        cardDisciplines = findViewById(R.id.card_disciplines);
        cardCulture = findViewById(R.id.card_culture);
        cardContent = findViewById(R.id.card_content);
        tvContent = findViewById(R.id.tv_content);

        // 底部导航栏按钮
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navManage = findViewById(R.id.nav_manage);
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // 跳转到首页
                finish();
            });
        }

        if (navManage != null) {
            navManage.setOnClickListener(v -> {
                // 跳转到管理页面
                Intent intent = new Intent(NjuptInfoActivity.this, AdminManagementActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // 当前已在"我的"页面
            });
        }
    }

    private void setupClickListeners() {
        if (cardIntroduction != null) {
            cardIntroduction.setOnClickListener(v -> showContent("introduction"));
        }

        if (cardHistory != null) {
            cardHistory.setOnClickListener(v -> showContent("history"));
        }

        if (cardDisciplines != null) {
            cardDisciplines.setOnClickListener(v -> showContent("disciplines"));
        }

        if (cardCulture != null) {
            cardCulture.setOnClickListener(v -> showContent("culture"));
        }
    }

    private void showContent(String type) {
        if (cardContent == null || tvContent == null) return;

        cardContent.setVisibility(android.view.View.VISIBLE);

        switch (type) {
            case "introduction":
                tvContent.setText("南京邮电大学（Nanjing University of Posts and Telecommunications），简称南邮，是首批国家\"双一流\"世界一流学科建设高校，是工业和信息化部、国家邮政局与江苏省共建高校，是江苏高水平大学建设高校。学校以电子信息、通信工程等专业著称，被誉为\"华夏IT英才的摇篮\"。");
                break;
            case "history":
                tvContent.setText("学校始建于1949年，前身是被誉为\"华夏IT英才的摇篮\"的南京邮电学校。1958年扩建为本科院校，定名为南京邮电学院；2000年由原南京邮电学院的直属单位——江苏省邮电学校并入组建新的南京邮电学院；2005年更名为南京邮电大学。");
                break;
            case "disciplines":
                tvContent.setText("学校构建了以工科为主体，以信息学科为特色，涵盖工、理、经、管、文、教、艺等多个学科相互交融的学科体系。拥有博士后科研流动站3个，一级学科博士学位授权点8个，一级学科硕士学位授权点20个。在第四轮学科评估中，电子科学与技术为A-，信息与通信工程为B+。");
                break;
            case "culture":
                tvContent.setText("校训：厚德、弘毅、求是、笃行\n\n办学特色：崇尚实践、面向基层、服务社会\n\n校园精神：信达天下、自强不息\n\n学校形成了以物联网、移动通信、大数据、人工智能等为主要特色的教学科研体系，为国家培养了大批优秀的通信和信息技术人才。");
                break;
            default:
                tvContent.setText("暂无内容");
                break;
        }
    }
}
