package com.robosoft.lorem.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User
{
    private int userId;
    private String firstName;
    private String lastName;
    private String emailId;
    private String mobileNo;
    private String profilePic;
    private int creditScore;
    private String role;
    private String password;

    //transient has 2 types ,checkout
//    @Transient                   // front end will send the referral code(userId) with the referral link
                                    //after new user clicks on link he will be redirected to reg page
    private int referralCode;   // front end will fetch the referral code from the link & store it
            // once the email is verified code will be sent over the registration link along with user details

}
