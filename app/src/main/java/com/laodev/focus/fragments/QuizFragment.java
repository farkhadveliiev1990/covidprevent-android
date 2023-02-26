package com.laodev.focus.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.laodev.focus.activities.MainActivity;
import com.laodev.focus.R;
import com.laodev.focus.Utils.AppManager;
import com.laodev.focus.Utils.Constants;
import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.models.NotiModel;
import com.laodev.focus.models.QuizModel;
import com.laodev.focus.uis.QuizUI;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuizFragment extends Fragment {

    private MainActivity activity;

    private QuizModel quizModel = new QuizModel();

    private ImageView imgCovidImage;

    private QuizUI qui_temp_yes_one, qui_temp_no_one;
    private QuizUI qui_temp_yes_two, qui_temp_no_two;
    private QuizUI qui_temp_yes_thr, qui_temp_no_thr;
    private QuizUI qui_temp_yes_four, qui_temp_no_four;

    private LinearLayout lltSave, lltCancel;

    public QuizFragment(MainActivity context) {
        activity = context;
    }

    private void initEvents() {
        qui_temp_yes_one.setQuizUICallback((title, isSelected) -> {
            qui_temp_no_one.setSelected(!isSelected);
            quizModel.answOne = String.valueOf(isSelected);
            setCOVIDImage(checkCOVIDStatus());
        });
        qui_temp_no_one.setQuizUICallback((title, isSelected) -> {
            qui_temp_yes_one.setSelected(!isSelected);
            quizModel.answOne = String.valueOf(!isSelected);
            setCOVIDImage(checkCOVIDStatus());
        });

        qui_temp_yes_two.setQuizUICallback((title, isSelected) -> {
            qui_temp_no_two.setSelected(!isSelected);
            quizModel.answTwo = String.valueOf(isSelected);
            setCOVIDImage(checkCOVIDStatus());
        });
        qui_temp_no_two.setQuizUICallback((title, isSelected) -> {
            qui_temp_yes_two.setSelected(!isSelected);
            quizModel.answTwo = String.valueOf(!isSelected);
            setCOVIDImage(checkCOVIDStatus());
        });

        qui_temp_yes_thr.setQuizUICallback((title, isSelected) -> {
            qui_temp_no_thr.setSelected(!isSelected);
            quizModel.answThr = String.valueOf(isSelected);
            setCOVIDImage(checkCOVIDStatus());
        });
        qui_temp_no_thr.setQuizUICallback((title, isSelected) -> {
            qui_temp_yes_thr.setSelected(!isSelected);
            quizModel.answThr = String.valueOf(!isSelected);
            setCOVIDImage(checkCOVIDStatus());
        });

        qui_temp_yes_four.setQuizUICallback((title, isSelected) -> {
            qui_temp_no_four.setSelected(!isSelected);
            quizModel.answFour = String.valueOf(isSelected);
            setCOVIDImage(checkCOVIDStatus());
        });
        qui_temp_no_four.setQuizUICallback((title, isSelected) -> {
            qui_temp_yes_four.setSelected(!isSelected);
            quizModel.answFour = String.valueOf(!isSelected);
            setCOVIDImage(checkCOVIDStatus());
        });

        lltSave.setOnClickListener(v -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            quizModel.quizTime = sdf.format(new Date());

            SimpleDateFormat tbName = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            quizModel.quizDate = tbName.format(new Date());
            quizModel.status = checkCOVIDStatus();

            ProgressDialog dialog = ProgressDialog.show(activity, "", getString(R.string.alert_connect_server));
            dialog.show();
            if (quizModel.status.equals(Constants.COVID_INFECTED)) {
                FireManager.getFriendTokenList(AppManager.gFriendIDList, new FireManager.GetFriendsListener(){
                    @Override
                    public void onSuccess(List<String> tokens) {
                        String title = activity.getString(R.string.app_name);
                        String body = " Usted ha estado en contacto con una persona que puede estar contagiado por Covid-19, comuníquese a los números indicados para reportar su estado";
                        for(int i=0; i<tokens.size(); i++){
                            FireManager.onSendFBPushnotification(tokens.get(i), title, body);
                        }
                    }

                    @Override
                    public void onFailed() {
                        Toast.makeText(activity, activity.getString(R.string.quiz_save_error), Toast.LENGTH_SHORT).show();
                    }
                });

                for (String userid: AppManager.gFriendIDList) {
                    NotiModel model = new NotiModel();
                    model.friendid = AppManager.gUserInfo.userId;
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    model.datetime = simpleDateFormat.format(new Date());
                    FireManager.onSaveNotiToFB(userid, model);
                }
            }

            FireManager.getQuizListByUserIdAndDate(AppManager.gUserInfo.userId, new FireManager.GetQuizsListener() {
                @Override
                public void onSuccess(List<QuizModel> quizs) {
                    if(checkIsFirst(quizModel.quizDate, quizs)){
                        quizModel.isFirst = Constants.isFirstTitle;
                    }else{
                        quizModel.isFirst = Constants.isNotFirstTitle;
                    }
                    quizs.add(quizModel);

                    Collections.sort(quizs, (o1, o2) -> o1.quizDate.compareTo(o2.quizDate));

                    FireManager.registerQuizToDatabase(AppManager.gUserInfo.userId, quizs, new FireManager.RegisterQuizListener() {
                        @Override
                        public void onSuccess(String result) {
                            Toast.makeText(activity, activity.getString(R.string.quiz_save_success), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(activity, activity.getString(R.string.quiz_save_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailed() {
                    Toast.makeText(activity, activity.getString(R.string.quiz_save_error), Toast.LENGTH_SHORT).show();
                }
            });

            AppManager.gUserInfo.userCovid = quizModel.status;
            FireManager.updateProfile(AppManager.gUserInfo, new FireManager.UpdateProfileListener() {
                @Override
                public void onSuccess(String userID) {
                    dialog.dismiss();
                    Toast.makeText(activity, activity.getString(R.string.success_update_profile), Toast.LENGTH_SHORT).show();
                    activity.loadFragmentByIndex(1);
                }

                @Override
                public void onFailure() {
                    dialog.dismiss();
                    Toast.makeText(activity, activity.getString(R.string.faile_update_profile), Toast.LENGTH_SHORT).show();
                }
            });
        });

        lltCancel.setOnClickListener(v -> {
            activity.loadFragmentByIndex(1);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View frgView = inflater.inflate(R.layout.fragment_quiz, container, false);

        activity.toolBar.setTitle(getString(R.string.new_quiz));

        initUIView(frgView);
        initEvents();

        return frgView;
    }

    private void initUIView(View view) {
        imgCovidImage = view.findViewById(R.id.img_quiz_covid_image);
        imgCovidImage.setImageResource(R.drawable.ic_covid_no);

        qui_temp_yes_one = view.findViewById(R.id.qui_temp_yes_one);
        qui_temp_no_one = view.findViewById(R.id.qui_temp_no_one);

        qui_temp_yes_two = view.findViewById(R.id.qui_temp_yes_two);
        qui_temp_no_two = view.findViewById(R.id.qui_temp_no_two);

        qui_temp_yes_thr = view.findViewById(R.id.qui_temp_yes_thr);
        qui_temp_no_thr = view.findViewById(R.id.qui_temp_no_thr);

        qui_temp_yes_four = view.findViewById(R.id.qui_temp_yes_four);
        qui_temp_no_four = view.findViewById(R.id.qui_temp_no_four);

        lltSave = view.findViewById(R.id.llt_quiz_save);
        lltCancel = view.findViewById(R.id.llt_quiz_cancel);

        TextView txtSubmittedDate = view.findViewById(R.id.txt_perform_date);
        SimpleDateFormat tbName = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        txtSubmittedDate.setText(tbName.format(new Date()));
    }

    private String checkCOVIDStatus(){
        int statusCnt = 0;
        String strStatus = "";
        if(quizModel.answOne.equals("true"))statusCnt++;
        if(quizModel.answTwo.equals("true"))statusCnt++;
        if(quizModel.answThr.equals("true"))statusCnt++;
        if(quizModel.answFour.equals("true"))statusCnt++;

        if(statusCnt == 0) strStatus = Constants.COVID_NORMAL;
        if(statusCnt >= 1 && statusCnt < 4) strStatus = Constants.COVID_SUSPECTED;
//        if(statusCnt == 4) strStatus = Constants.COVID_INFECTED;
        if(statusCnt == 4) strStatus = Constants.COVID_SUSPECTED;

        return strStatus;
    }

    private void setCOVIDImage(String status){
        if (status.equals(Constants.COVID_NORMAL)) {
            imgCovidImage.setImageResource(R.drawable.ic_covid_no);
        } else if(status.equals(Constants.COVID_SUSPECTED)){
            imgCovidImage.setImageResource(R.drawable.ic_covid_medium);
        } else {
            imgCovidImage.setImageResource(R.drawable.ic_covid_yes);
        }
    }

    private boolean checkIsFirst(String currDate, List<QuizModel> quizss){
        if (quizss.size() == 0) {
            return true;
        } else {
            for(QuizModel quiz: quizss){
                if(quiz.quizDate.equals(currDate)){
                    return false;
                }
            }
        }
        return true;
    }

}
