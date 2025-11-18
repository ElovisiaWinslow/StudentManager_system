// FullMapActivity.java
package com.example.studentmanager_system.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Util.ZoomLayout;

public class FullMapActivity extends Activity {

    private ImageView fullMapImage;
    private TextView mapTitle;
    private ZoomLayout zoomLayout;
    private int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_map);

        initViews();
        handleIntentData();
    }

    private void initViews() {
        fullMapImage = findViewById(R.id.full_map_image);
        mapTitle = findViewById(R.id.full_map_title);
        zoomLayout = findViewById(R.id.zoom_layout);

        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 初始时图片自适应屏幕
        fullMapImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    private void handleIntentData() {
        currentPosition = getIntent().getIntExtra("map_position", 0);

        // 根据位置设置对应的地图和标题
        switch (currentPosition) {
            case 0:
                fullMapImage.setImageResource(R.drawable.xianlin);
                mapTitle.setText("南京邮电大学仙林校区");
                break;
            case 1:
                fullMapImage.setImageResource(R.drawable.sanpailou);
                mapTitle.setText("南京邮电大学三牌楼校区");
                break;
            case 2:
                fullMapImage.setImageResource(R.drawable.suojincun);
                mapTitle.setText("南京邮电大学锁金村校区");
                break;
            default:
                fullMapImage.setImageResource(R.drawable.xianlin);
                mapTitle.setText("南京邮电大学仙林校区");
                break;
        }
    }

    // 添加重置缩放的方法（可选，可以在需要时调用）
    public void resetZoom() {
        if (zoomLayout != null) {
            zoomLayout.resetZoom();
        }
    }
}