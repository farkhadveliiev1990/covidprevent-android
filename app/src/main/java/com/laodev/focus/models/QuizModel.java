package com.laodev.focus.models;

public class QuizModel {

    public String quizDate;
    public String answOne;
    public String answTwo;
    public String answThr;
    public String answFour;
    public String quizTime;
    public String status;
    public String isFirst;

    public QuizModel(){
        boolean answ = false;
        quizDate = "";
        answOne = String.valueOf(answ);
        answTwo = String.valueOf(answ);
        answThr = String.valueOf(answ);
        answFour = String.valueOf(answ);
        quizTime = "";
        status = "";
        isFirst = "no";
    }
}
