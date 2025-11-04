package com.example.studentmanager_system.Util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.studentmanager_system.R;
import com.example.studentmanager_system.Tools.Student;
import java.util.List;

public class WarningStudentAdapter extends ArrayAdapter<Student> {
    private int resourceId;

    public WarningStudentAdapter(Context context, int textViewResourceId, List<Student> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Student student = getItem(position);
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.studentName = view.findViewById(R.id.tv_student_name);
            viewHolder.studentId = view.findViewById(R.id.tv_student_id);
            viewHolder.completedCredits = view.findViewById(R.id.tv_completed_credits);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        if (student != null) {
            viewHolder.studentName.setText(student.getName());
            viewHolder.studentId.setText(student.getId());
            viewHolder.completedCredits.setText(String.valueOf(student.getCompletedCredits()));
        }

        return view;
    }

    class ViewHolder {
        TextView studentName;
        TextView studentId;
        TextView completedCredits;
    }
}
