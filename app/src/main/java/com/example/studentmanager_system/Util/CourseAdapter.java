package com.example.studentmanager_system.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Tools.Course;

import java.util.List;

public class CourseAdapter extends BaseAdapter {
    private Context context;
    private List<Course> courseList;
    private List<String> selectedCourseIds;
    private String studentId;
    private myDatabaseHelper dbHelper;
    // 定义选课状态监听器
    public interface OnCourseSelectListener {
        void onSelect(String courseId, boolean isSelect); // true为选课，false为退课
    }

    private OnCourseSelectListener selectListener;
    private boolean showOnlyDrop = false; // 是否只显示退课按钮

    public void setOnCourseSelectListener(OnCourseSelectListener listener) {
        this.selectListener = listener;
    }

    public void setShowOnlyDrop(boolean showOnlyDrop) {
        this.showOnlyDrop = showOnlyDrop;
    }

    // 构造方法
    public CourseAdapter(Context context, List<Course> courseList,
                         List<String> selectedCourseIds, String studentId) {
        this.context = context;
        this.courseList = courseList;
        this.selectedCourseIds = selectedCourseIds;
        this.studentId = studentId;
        // 使用单例模式获取数据库实例（避免内存泄漏）
        this.dbHelper = myDatabaseHelper.getInstance(context);
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
            holder.btnSelectCourse = convertView.findViewById(R.id.btn_select_course);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Course course = courseList.get(position);
        holder.tvCourseName.setText(course.getName());
        holder.tvCourseSubject.setText("学科: " + course.getSubject());
        holder.tvCourseTeacher.setText("授课教师: " + (course.getTeacherName() != null ?
                course.getTeacherName() : "未知"));
        holder.tvCourseInfo.setText("学分: " + course.getCredit() + " | 学时: " + course.getHours());

        // 关键修改：先判断是否为“仅显示退课”模式（已选课程列表）
        boolean isSelected = selectedCourseIds.contains(course.getId());
        if (showOnlyDrop) {
            // 已选课程列表：强制显示“退课”按钮，且按钮可点击
            holder.btnSelectCourse.setText("退课");
            holder.btnSelectCourse.setEnabled(true);
            holder.btnSelectCourse.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_light)); // 红色区分退课
        } else {
            // 普通选课列表：按原逻辑显示“选课”/“已选”
            if (isSelected) {
                holder.btnSelectCourse.setText("已选");
                holder.btnSelectCourse.setEnabled(false);
                holder.btnSelectCourse.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            } else {
                holder.btnSelectCourse.setText("选课");
                holder.btnSelectCourse.setEnabled(true);
                holder.btnSelectCourse.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
            }
        }

        // 关键修改：统一使用ViewHolder的按钮，删除重复的findViewById
        holder.btnSelectCourse.setOnClickListener(v -> {
            if (selectListener != null) {
                // 逻辑判断：showOnlyDrop=true时为退课（isSelect=false），否则为选课（isSelect=!isSelected）
                boolean isSelectOperation = !showOnlyDrop && !isSelected;
                selectListener.onSelect(course.getId(), isSelectOperation);
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
        Button btnSelectCourse;
    }
}
