package com.example.studentmanager_system.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Tools.Teacher;

import java.util.List;

public class TeacherAdapter extends ArrayAdapter<Teacher> {
    private final int resourceId;

    public TeacherAdapter(Context context, int textViewResourceId, List<Teacher> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Teacher teacher = getItem(position);
        @SuppressLint("ViewHolder") View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);

        // 修改为使用 teacher_item.xml 中实际存在的 ID
        TextView nameTv = view.findViewById(R.id.tv_teacher_name);

        assert teacher != null;
        // 由于布局中只有一个 TextView，我们只显示教师姓名
        nameTv.setText(teacher.getName() + " (" + teacher.getId() + ")");

        return view;
    }
}
