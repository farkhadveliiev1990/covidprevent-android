package com.laodev.focus.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.laodev.focus.R;
import com.laodev.focus.Utils.AppManager;
import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.activities.MainActivity;
import com.laodev.focus.adapters.StatisticsAdapter;
import com.laodev.focus.models.QuizModel;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment implements StatisticsAdapter.QuizsListAdapterCallback {

    private MainActivity activity;

    private List<QuizModel> mQuizs = new ArrayList<>();
    private StatisticsAdapter mStatisticsAdapter;

    private TextView txtNoQuiz;
    private ImageView addQuiz;

    public StatisticsFragment(MainActivity context) {
        activity = context;
    }

    private void initEvents(){
        addQuiz.setOnClickListener(v -> {
            activity.gotoQuizFragment();
        });
        activity.setOnBackPressed(() -> activity.exitDialog());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View frgView = inflater.inflate(R.layout.fragment_statistics, container, false);

        activity.toolBar.setTitle(getString(R.string.bottom_quiz));

        initUIView(frgView);
        initEvents();

        return frgView;
    }

    private void initUIView(View mainView) {
        ListView lstView = mainView.findViewById(R.id.lst_statistics_list);
        txtNoQuiz = mainView.findViewById(R.id.txt_statistics_no_quiz);
        txtNoQuiz.setVisibility(View.GONE);

        mStatisticsAdapter = new StatisticsAdapter(activity, mQuizs, this);
        lstView.setAdapter(mStatisticsAdapter);

        addQuiz = mainView.findViewById(R.id.img_friend_add_quiz);

        initData();
    }

    private void initData() {
        ProgressDialog dialog = ProgressDialog.show(activity, "", getString(R.string.alert_connect_server));
        dialog.show();
        FireManager.getQuizListByUserIdAndDate(AppManager.gUserInfo.userId, new FireManager.GetQuizsListener() {
            @Override
            public void onSuccess(List<QuizModel> quizs) {
                dialog.dismiss();
                mQuizs.clear();
                mQuizs.addAll(quizs);
                mStatisticsAdapter.notifyDataSetChanged();

                if(quizs.size() == 0){
                    txtNoQuiz.setVisibility(View.VISIBLE);
                }else{
                    txtNoQuiz.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailed() {
                dialog.dismiss();
                Toast.makeText(activity, activity.getString(R.string.quiz_save_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClickQuizCallback(QuizModel quiz) {
        activity.gotoQuizStatusFragment(quiz);
    }

}
