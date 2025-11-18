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
                Intent intent = new Intent(NjuptInfoActivity.this, adminActivity.class);
                startActivity(intent);
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
                tvContent.setText("南京邮电大学是一所红色基因厚重、信息特色鲜明的国家“双一流”建设高校和江苏高水平大学高峰计划A类建设高校。学校前身是1942年诞生于山东抗日根据地的“战邮干训班”，是新中国第一所邮电学校，被誉为“华夏IT英才的摇篮”。学校现有仙林、三牌楼、锁金村三个校区，拥有7个博士后流动站、10个一级学科博士学位授权点。形成了以工学为主体，电子信息为特色，多学科交融的高水平大学架构。办学83年来，已为国家培养了29万余名优秀人才，在信息通信领域享有崇高声誉。");
                break;
            case "history":
                tvContent.setText("南京邮电大学拥有光辉的红色办学历史。学校始于1942年我党我军在山东抗日根据地创办的“战邮干训班”，是早期系统培养通信人才的摇篮。1958年经国务院批准改建为本科高校“南京邮电学院”，成为原邮电部直属重点高校。2005年更名为南京邮电大学。2017年入选国家首批“双一流”建设高校，2022年再次入选第二轮“双一流”。学校历经战火洗礼，随军南下，从为抗战服务的干部训练班，茁壮成长为如今以信息科技为特色的高水平大学，谱写了“因邮电而生，随通信而长，由信息而强”的壮丽篇章。");
                break;
            case "disciplines":
                tvContent.setText("南京邮电大学已构建起“信息材料、信息器件、信息系统、信息网络、信息应用”五位一体的大信息学科体系，建设成果丰硕。学校拥有6个学科进入ESI全球排名前1%，其中计算机科学、工程学进入前1‰。拥有27个国家级一流本科专业建设点，14个通过国家工程教育认证的专业。学校科研平台实力雄厚，拥有2个全国重点实验室，作为主要协同单位入选国家“2011协同创新中心”2个。这些数据标志着南邮的学科建设实现了量与质的同步跃升，核心竞争力不断增强。");
                break;
            case "culture":
                tvContent.setText("南京邮电大学校园文化倡导“信达天下 自强不息”的精神，育人成果显著。学校拥有国家级实验教学示范中心、虚拟仿真实验教学中心等7个国家级实践教学平台。“十四五”以来，学生在教育部榜单内双创竞赛中获得国家级及以上奖项1000余项，在全国“双一流”高校大学生竞赛榜中位列第27名。同时，学校与全球90多所一流大学开展合作，在校留学生超1000人。厚重的红色战邮历史与活跃的现代创新氛围交织，共同塑造了南邮学子兼具家国情怀与国际视野的独特气质。");
                break;
            default:
                tvContent.setText("暂无内容");
                break;
        }
    }
}
