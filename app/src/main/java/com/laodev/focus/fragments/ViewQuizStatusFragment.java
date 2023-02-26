package com.laodev.focus.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.laodev.focus.activities.MainActivity;
import com.laodev.focus.R;
import com.laodev.focus.Utils.Constants;
import com.laodev.focus.models.QuizModel;
import com.laodev.focus.uis.QuizUI;

public class ViewQuizStatusFragment extends Fragment {

    private MainActivity activity;

    private QuizModel quizModel;

    private QuizUI qui_temp_yes_one, qui_temp_no_one;
    private QuizUI qui_temp_yes_two, qui_temp_no_two;
    private QuizUI qui_temp_yes_thr, qui_temp_no_thr;
    private QuizUI qui_temp_yes_four, qui_temp_no_four;

    public ViewQuizStatusFragment(MainActivity context, QuizModel quiz) {
        activity = context;
        quizModel = quiz;
    }

    private void initWithEvent() {
        activity.setOnBackPressed(() -> activity.loadFragmentByIndex(1));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View frgView = inflater.inflate(R.layout.fragment_view_quiz_status, container, false);

        activity.toolBar.setTitle(activity.getString(R.string.toobar_title_view_my_quiz));

        initUIView(frgView);
        initData();
        initWithEvent();

        return frgView;
    }

    private void initUIView(View view) {
        qui_temp_yes_one = view.findViewById(R.id.qui_temp_yes_one);
        qui_temp_no_one = view.findViewById(R.id.qui_temp_no_one);

        qui_temp_yes_two = view.findViewById(R.id.qui_temp_yes_two);
        qui_temp_no_two = view.findViewById(R.id.qui_temp_no_two);

        qui_temp_yes_thr = view.findViewById(R.id.qui_temp_yes_thr);
        qui_temp_no_thr = view.findViewById(R.id.qui_temp_no_thr);

        qui_temp_yes_four = view.findViewById(R.id.qui_temp_yes_four);
        qui_temp_no_four = view.findViewById(R.id.qui_temp_no_four);

        TextView txtSubmittedDate = view.findViewById(R.id.txt_submitted_date);
        String submittedDate = quizModel.quizDate + " " + quizModel.quizTime;
        txtSubmittedDate.setText(submittedDate);

        ImageView imgStstus = view.findViewById(R.id.img_status);
        String status = quizModel.status;
        if(status.equals(Constants.COVID_NORMAL)){
            imgStstus.setImageResource(R.drawable.ic_covid_no);
        }else if(status.equals(Constants.COVID_SUSPECTED)){
            imgStstus.setImageResource(R.drawable.ic_covid_medium);
        }else{
            imgStstus.setImageResource(R.drawable.ic_covid_yes);
        }
    }

    private void initData() {
        qui_temp_yes_one.setSelected(Boolean.valueOf(quizModel.answOne));
        qui_temp_no_one.setSelected(!Boolean.valueOf(quizModel.answOne));

        qui_temp_yes_two.setSelected(Boolean.valueOf(quizModel.answTwo));
        qui_temp_no_two.setSelected(!Boolean.valueOf(quizModel.answTwo));

        qui_temp_yes_thr.setSelected(Boolean.valueOf(quizModel.answThr));
        qui_temp_no_thr.setSelected(!Boolean.valueOf(quizModel.answThr));

        qui_temp_yes_four.setSelected(Boolean.valueOf(quizModel.answFour));
        qui_temp_no_four.setSelected(!Boolean.valueOf(quizModel.answFour));
    }

}
