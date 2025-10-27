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

        TextView idTv = view.findViewById(R.id.teacher_item_id);
        TextView nameTv = view.findViewById(R.id.teacher_item_name);
        TextView subjectTv = view.findViewById(R.id.teacher_item_subject);

        assert teacher != null;
        idTv.setText(teacher.getId());
        nameTv.setText(teacher.getName());
        subjectTv.setText(teacher.getSubject());

        return view;
    }
}