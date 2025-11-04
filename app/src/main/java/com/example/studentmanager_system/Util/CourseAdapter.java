// app/src/main/java/com/example/studentmanager_system/Util/CourseAdapter.java
package com.example.studentmanager_system.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Tools.Course;
import java.util.List;

public class CourseAdapter extends BaseAdapter {
    private final Context context;
    private final List<Course> courseList;
    private final List<String> selectedCourseIds;

    // 定义选课状态监听器
    public interface OnCourseSelectListener {
        void onSelect(String courseId, boolean isSelect); // true为选课，false为退课
    }

    // 定义课程项点击监听器
    public interface OnItemClickListener {
        void onItemClick(Course course);
    }

    private OnCourseSelectListener selectListener;
    private OnItemClickListener itemClickListener;
    private boolean showOnlyDrop = false; // 是否只显示退课按钮

    public void setOnCourseSelectListener(OnCourseSelectListener listener) {
        this.selectListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setShowOnlyDrop(boolean showOnlyDrop) {
        this.showOnlyDrop = showOnlyDrop;
    }

    // 构造方法
    public CourseAdapter(Context context, List<Course> courseList,
                         List<String> selectedCourseIds) {
        this.context = context;
        this.courseList = courseList;
        this.selectedCourseIds = selectedCourseIds;
    }

    @Override
    public int getCount() {
        return courseList.size();
    }

    @Override
    public Object getItem(int position) {
        return courseList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.course_item, parent, false);
            holder = new ViewHolder();
            holder.tvCourseName = convertView.findViewById(R.id.tv_course_name);
            holder.tvCourseSubject = convertView.findViewById(R.id.tv_course_subject);
            holder.tvCourseTeacher = convertView.findViewById(R.id.tv_course_teacher);
            holder.tvCourseInfo = convertView.findViewById(R.id.tv_course_info);
            holder.tvCourseTime = convertView.findViewById(R.id.tv_course_time);
            holder.tvCourseLocation = convertView.findViewById(R.id.tv_course_location);
            holder.tvCourseScore = convertView.findViewById(R.id.tv_course_score); // 添加成绩显示 TextView
            holder.btnSelectCourse = convertView.findViewById(R.id.btn_select_course);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Course course = courseList.get(position);
        holder.tvCourseName.setText(course.getName());
        holder.tvCourseSubject.setText("学科: " + course.getSubject());

        // 显示教师信息
        String teacherInfo;
        if (course.getTeacherNames() != null && !course.getTeacherNames().isEmpty()) {
            teacherInfo = "授课教师: " + TextUtils.join(", ", course.getTeacherNames());
        } else if (course.getTeacherName() != null) {
            teacherInfo = "授课教师: " + course.getTeacherName();
        } else {
            teacherInfo = "授课教师: 未知";
        }
        holder.tvCourseTeacher.setText(teacherInfo);

        holder.tvCourseInfo.setText("学分: " + course.getCredit() + " | 学时: " + course.getHours());
        holder.tvCourseTime.setText("时间: " + (course.getClassTime() != null ?
                course.getClassTime() : "待定"));
        holder.tvCourseLocation.setText("地点: " + (course.getClassLocation() != null ?
                course.getClassLocation() : "待定"));

        // 显示成绩信息
        if (course.getScore() >= 0) {
            holder.tvCourseScore.setText("成绩: " + course.getScore());
        } else {
            holder.tvCourseScore.setText("成绩: 未知");
        }

        // 关键修改：先判断是否为"仅显示退课"模式（已选课程列表）
        boolean isSelected = selectedCourseIds.contains(course.getId());
        if (showOnlyDrop) {
            // 已选课程列表：强制显示"退课"按钮，且按钮可点击
            holder.btnSelectCourse.setText("退课");
            holder.btnSelectCourse.setEnabled(true);
            holder.btnSelectCourse.setBackgroundColor(
                    androidx.core.content.ContextCompat.getColor(context, android.R.color.holo_red_light)); // 红色区分退课
        } else {
            // 普通选课列表：按原逻辑显示"选课"/"已选"
            if (isSelected) {
                holder.btnSelectCourse.setText("已选");
                holder.btnSelectCourse.setEnabled(false);
                holder.btnSelectCourse.setBackgroundColor(
                        androidx.core.content.ContextCompat.getColor(context, android.R.color.darker_gray));
            } else {
                holder.btnSelectCourse.setText("选课");
                holder.btnSelectCourse.setEnabled(true);
                holder.btnSelectCourse.setBackgroundColor(
                        androidx.core.content.ContextCompat.getColor(context, android.R.color.holo_blue_light));
            }
        }

        holder.btnSelectCourse.setOnClickListener(v -> {
            if (selectListener != null) {
                // 逻辑判断：showOnlyDrop=true时为退课（isSelect=false），否则为选课（isSelect=!isSelected）
                boolean isSelectOperation = !showOnlyDrop && !isSelected;
                selectListener.onSelect(course.getId(), isSelectOperation);
            }
        });

        // 设置整个课程项的点击事件
        convertView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(course);
            }
        });

        return convertView;
    }

    // 视图持有者
    static class ViewHolder {
        TextView tvCourseName;
        TextView tvCourseSubject;
        TextView tvCourseTeacher;
        TextView tvCourseInfo;
        TextView tvCourseTime;
        TextView tvCourseLocation;
        TextView tvCourseScore; // 添加成绩显示 TextView
        Button btnSelectCourse;
    }
}
