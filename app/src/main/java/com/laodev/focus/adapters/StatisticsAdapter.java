package com.laodev.focus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.laodev.focus.R;
import com.laodev.focus.Utils.AppManager;
import com.laodev.focus.Utils.Constants;
import com.laodev.focus.models.QuizModel;
import com.laodev.focus.models.Users;

import java.util.List;

public class StatisticsAdapter extends BaseAdapter {

    private Context context;

    private List<QuizModel> mQuizs;
    private QuizsListAdapterCallback listener;

    public StatisticsAdapter(Context context, List<QuizModel> quizs, QuizsListAdapterCallback callback) {
        this.context = context;
        mQuizs = quizs;
        this.listener = callback;
    }

    @Override
    public int getCount() {
        return mQuizs.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View containView, ViewGroup parent) {

        containView = LayoutInflater.from(context).inflate(R.layout.item_statistics, null);

        QuizModel quizModel = mQuizs.get(position);

        LinearLayout lltDate = containView.findViewById(R.id.llt_item_statistics_date);
        TextView txtDate = containView.findViewById(R.id.txt_item_statistics_date);


        if(quizModel.isFirst.equals(Constants.isFirstTitle)){
            AppManager.gQuizDate = quizModel.quizDate;
            txtDate.setText(quizModel.quizDate);
            lltDate.setVisibility(View.VISIBLE);
        }else{
            lltDate.setVisibility(View.GONE);
        }

        ImageView imgCovidStatus = containView.findViewById(R.id.img_statistics_covid_status);
        ImageView imgMore = containView.findViewById(R.id.img_statistics_more);
        TextView txtCellTitle = containView.findViewById(R.id.txt_statistics_title);
        LinearLayout line = containView.findViewById(R.id.llt_line);

        String status = quizModel.status;
        if(status.equals(Constants.COVID_NORMAL)){
            imgCovidStatus.setImageResource(R.drawable.ic_covid_no);
        }else if(status.equals(Constants.COVID_SUSPECTED)){
            imgCovidStatus.setImageResource(R.drawable.ic_covid_medium);
        }else{
            imgCovidStatus.setImageResource(R.drawable.ic_covid_yes);
        }

        String title = context.getString(R.string.statistics_pre_title) + " " + quizModel.quizTime;
        txtCellTitle.setText(title);

        containView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickQuizCallback(quizModel);
            }
        });

        if(position == mQuizs.size()-1){
            line.setVisibility(View.GONE);
        }

        return containView;
    }

    public interface QuizsListAdapterCallback {
        void onClickQuizCallback(QuizModel quiz);
    }

}
