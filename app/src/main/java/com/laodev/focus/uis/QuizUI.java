package com.laodev.focus.uis;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.laodev.focus.R;

public class QuizUI extends LinearLayout {

    private String title;
    private boolean isSelected;

    private LinearLayout llt_quiz;
    private TextView lbl_title;
    private Switch sch_status;
    private boolean isClickable;

    private QuizUICallback callback;

    private OnClickListener clickView = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!isClickable){
                return;
            }
            isSelected = !isSelected;
            onEventSelected();
            callback.onClickQuizItem(title, isSelected);
        }
    };
    private CompoundButton.OnCheckedChangeListener changeSch = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (callback == null) {
                return;
            }
            isSelected = isChecked;
            onEventSelected();
            callback.onClickQuizItem(title, isSelected);
        }
    };

    public QuizUI(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.QuizUI);
        this.isSelected = arr.getBoolean(R.styleable.QuizUI_isSelected, false);
        this.title = arr.getString(R.styleable.QuizUI_title);
        this.isClickable = arr.getBoolean(R.styleable.QuizUI_clickable, true);
        arr.recycle();

        setOrientation(LinearLayout.HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.item_quiz, this, true);

        initUIView();
    }

    private void initUIView() {
        llt_quiz = findViewById(R.id.llt_quiz);
        llt_quiz.setOnClickListener(clickView);

        lbl_title = findViewById(R.id.lbl_quiz_title);
        lbl_title.setText(title);

        sch_status = findViewById(R.id.sch_quiz_status);
        sch_status.setEnabled(isClickable);
        sch_status.setOnCheckedChangeListener(changeSch);

        onEventSelected();
    }

    private void onEventSelected() {
        if (isSelected) {
            llt_quiz.setBackgroundColor(getResources().getColor(R.color.colorquizNo));
            lbl_title.setTextColor(getResources().getColor(R.color.colorquizBorder));
            sch_status.setChecked(true);
        } else {
            llt_quiz.setBackgroundColor(getResources().getColor(R.color.colorquizYes));
            lbl_title.setTextColor(getResources().getColor(R.color.colorTextBlack));
            sch_status.setChecked(false);
        }
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        onEventSelected();
    }

    public void setQuizUICallback(QuizUICallback callback) {
        this.callback = callback;
    }

    public interface QuizUICallback {
        void onClickQuizItem(String title, boolean isSelected);
    }

}
