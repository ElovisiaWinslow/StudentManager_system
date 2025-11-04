package com.example.studentmanager_system.Util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Tools.Student;  // 修改导入路径

import java.util.List;


/**
 * 自定义学生信息适配器（基本信息）
 */
public class StudentAdapter extends ArrayAdapter<Student> {
    private final int resourceId;

    public StudentAdapter(Context context, int resource, List<Student> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Student student = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.student_name = view.findViewById(R.id.student_name);
            viewHolder.student_id = view.findViewById(R.id.student_id);
            viewHolder.student_image = view.findViewById(R.id.student_image);
            viewHolder.student_image.setLayoutParams(new LinearLayout.LayoutParams(100, 100));

            view.setTag(viewHolder);

        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }


        if (student != null) {
            viewHolder.student_name.setText(student.getName());
        }
        if (student != null) {
            viewHolder.student_id.setText(student.getId());
        }
        String sex = null;
        if (student != null) {
            sex = student.getSex();
        }
        if (sex != null && sex.equals("男")) {
            viewHolder.student_image.setImageResource(R.drawable.man);
        }
        if (sex != null && sex.equals("女")) {
            viewHolder.student_image.setImageResource(R.drawable.woman);
        }


        return view;

    }

    static class ViewHolder {
        ImageView student_image;
        TextView student_name;
        TextView student_id;

    }

}
