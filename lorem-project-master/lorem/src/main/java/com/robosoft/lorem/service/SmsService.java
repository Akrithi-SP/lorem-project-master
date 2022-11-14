package com.robosoft.lorem.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class SmsService
{
    private static final String ACCOUNT_SID="ACb5ec8bb4f4130842f8710ec148c91e9c";
    private static final String AUTH_ID="1cc37665375e61da1c55789f14083016";
    static
    {
        Twilio.init(ACCOUNT_SID,AUTH_ID);
    }
    public boolean sendSms(String mobileNumber, int tfaCode)
    {
        Message.creator(new PhoneNumber(mobileNumber), new PhoneNumber("+14245883990"),
                "Your Two Factor Authentication code is:"+tfaCode).create();
        return true;
    }
}
