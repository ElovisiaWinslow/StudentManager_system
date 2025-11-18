// app/src/main/java/com/example/studentmanager_system/Util/CourseAdapter.java
package com.example.studentmanager_system.Util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Tools.Course;

import java.util.List;
import java.util.Objects;

public class CourseAdapter extends BaseAdapter {
    private Context context;
    private List<Course> courses;
    private List<String> selectedCourseIds;
    private OnCourseSelectListener selectListener;
    private OnItemClickListener itemClickListener;
    private boolean showOnlyDrop = false; // 默认显示选课/退课按钮
    private boolean courseInfoMode = false; // 课程信息展示模式

    public CourseAdapter(Context context, List<Course> courses, List<String> selectedCourseIds) {
        this.context = context;
        this.courses = courses;
        this.selectedCourseIds = selectedCourseIds;
    }

    public void setShowOnlyDrop(boolean showOnlyDrop) {
        this.showOnlyDrop = showOnlyDrop;
    }

    public void setCourseInfoMode(boolean courseInfoMode) {
        this.courseInfoMode = courseInfoMode;
    }

    public interface OnCourseSelectListener {
        void onCourseSelect(String courseId, boolean isSelect);
    }

    public interface OnItemClickListener {
        void onItemClick(Course course);
    }

    public void setOnCourseSelectListener(OnCourseSelectListener listener) {
        this.selectListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @Override
    public int getCount() {
        return courses.size();
    }

    @Override
    public Object getItem(int position) {
        return courses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            // 根据模式选择不同的布局文件
            int layoutResource = courseInfoMode ? R.layout.course_show_item : R.layout.course_item;
            convertView = LayoutInflater.from(context).inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            // 查找通用控件
            holder.courseName = convertView.findViewById(R.id.tv_course_name);
            holder.courseSubject = convertView.findViewById(R.id.tv_course_subject);
            holder.courseInfo = convertView.findViewById(R.id.tv_course_info);

            // 根据不同布局查找特定控件
            if (courseInfoMode) {
                holder.courseTime = convertView.findViewById(R.id.tv_course_time);
                holder.courseLocation = convertView.findViewById(R.id.tv_course_location);
            } else {
                holder.btnSelect = convertView.findViewById(R.id.btn_select_course);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Course course = courses.get(position);

        // 设置通用信息
        holder.courseName.setText(course.getName());
        if (holder.courseSubject != null) {
            holder.courseSubject.setText("学科: " + course.getSubject());
        }

        // 根据模式设置不同信息
        if (courseInfoMode) {
            holder.courseInfo.setText(String.format("学分:%.1f 学时:%d", course.getCredit(), course.getHours()));
            if (holder.courseTime != null) {
                holder.courseTime.setText("时间: " + (course.getClassTime() != null ? course.getClassTime() : "未安排"));
            }
            if (holder.courseLocation != null) {
                holder.courseLocation.setText("地点: " + (course.getClassLocation() != null ? course.getClassLocation() : "未安排"));
            }
        } else {
            holder.courseInfo.setText(String.format("学分:%.1f 学时:%d", course.getCredit(), course.getHours()));

            // 只在非课程信息展示模式下处理按钮逻辑
            if (holder.btnSelect != null) {
                // 设置按钮状态
                String courseId = course.getId();
                boolean isSelected = selectedCourseIds.contains(courseId);

                if (showOnlyDrop && isSelected) {
                    // 只显示退课按钮模式（已选课程）
                    holder.btnSelect.setText("退课");
                    holder.btnSelect.setVisibility(View.VISIBLE);
                    holder.btnSelect.setOnClickListener(v -> {
                        if (selectListener != null) {
                            selectListener.onCourseSelect(courseId, false); // false表示退课
                        }
                    });
                } else if (!showOnlyDrop) {
                    // 正常模式（显示选课/退课按钮）
                    if (isSelected) {
                        holder.btnSelect.setText("已选");
                        holder.btnSelect.setEnabled(false);
                    } else if (isSameCourseSelected(course)) {
                        // 检查是否选择了同一课程的其他实例
                        holder.btnSelect.setText("已选其他教师");
                        holder.btnSelect.setEnabled(false);
                    } else {
                        holder.btnSelect.setText("选课");
                        holder.btnSelect.setEnabled(true);
                        holder.btnSelect.setOnClickListener(v -> {
                            if (selectListener != null) {
                                selectListener.onCourseSelect(courseId, true); // true表示选课
                            }
                        });
                    }
                    holder.btnSelect.setVisibility(View.VISIBLE);
                } else {
                    // 不显示按钮
                    holder.btnSelect.setVisibility(View.GONE);
                }
            }
        }

        // 设置整个课程项的点击事件 - 关键修改
        if (courseInfoMode) {
            // 课程信息模式下，不设置点击事件，让父级ListView处理
            convertView.setOnClickListener(null);
            convertView.setClickable(false);
        } else {
            // 非课程信息模式下，设置点击事件
            convertView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(course);
                }
            });
        }

        return convertView;
    }

    /**
     * 检查是否已经选择了同一课程名的其他课程实例
     * @param currentCourse 当前课程
     * @return 是否已选同一课程的其他实例
     */
    private boolean isSameCourseSelected(Course currentCourse) {
        // 遍历所有已选课程ID
        for (String selectedId : selectedCourseIds) {
            // 在所有课程中查找已选课程
            for (Course course : courses) {
                // 使用 Objects.equals() 避免 NullPointerException
                if (Objects.equals(course.getId(), selectedId) &&
                        Objects.equals(course.getName(), currentCourse.getName()) &&
                        !Objects.equals(course.getId(), currentCourse.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    static class ViewHolder {
        TextView courseName;
        TextView courseSubject;
        TextView courseInfo;
        TextView courseTime;
        TextView courseLocation;
        Button btnSelect;
    }
}
