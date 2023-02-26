package com.laodev.focus.models;

import com.laodev.focus.Utils.Constants;

public class Users {

    public String userId;
    public String userName;
    public String fullName;
    public String userAge;
    public String userCin;
    public String gender;
    public String userEmail;
    public String phoneNumber;
    public String userCovid;
    public String location;
    public String imgUrl;
    public String countryCode;
    public String district;
    public String province;
    public String latitude;
    public String longitude;

    public Users(){
        userId = "";
        userEmail = "";
        phoneNumber = "";
        imgUrl = "";
        userName = "";
        userCovid = "";
        location = "";
        fullName = "";
        userAge = "";
        userCin = "";
        gender = "";
        countryCode = "py";
    }

    public boolean isCheckProvinceDistrict(String mProvince, String mDistrict) {
        if(province.equals(mProvince) && district.equals(mDistrict) && userCovid.equals(Constants.COVID_INFECTED))
            return true;
        return false;
    }
}
