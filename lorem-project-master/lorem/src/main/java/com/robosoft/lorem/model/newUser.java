package com.robosoft.lorem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class newUser
{
    private String emailId;
    private int emailOtp;
    private String otpExpireTime;
    private boolean otpVerified;


}
