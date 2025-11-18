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

        // 获取姓名和工号的 TextView
        TextView nameTv = view.findViewById(R.id.tv_teacher_name);
        TextView idTv = view.findViewById(R.id.tv_teacher_id);

        assert teacher != null;
        // 分别设置姓名和工号到对应的 TextView
        nameTv.setText(teacher.getName());
        idTv.setText(teacher.getId());

        return view;
    }
}
