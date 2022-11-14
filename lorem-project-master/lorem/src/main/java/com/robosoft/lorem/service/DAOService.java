package com.robosoft.lorem.service;

import com.robosoft.lorem.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DAOService
{
    private static final String EMAIL_VERIFICATION="INSERT INTO newUser(emailId,emailOtp,OtpExpireTime) VALUES(?,?,?)";
    private static final String UPDATE_OTP="UPDATE newUser set emailOtp=?,OtpExpireTime=? where emailId=?";
    private static final String UPDATE_EMAIL_VERIFIED="UPDATE newUser set otpVerified=true where emailId=?";
    private static final String PHONE_NUMBER_VERIFICATION="INSERT INTO mobileOtp(OtpNumber,mobileNo,OtpExpireTime) VALUES(?,?,?)";
    private static final String UPDATE_NUMBER_VERIFIED="UPDATE mobileOtp set otpVerified=true where mobileNo=?";
    private static final String CHECK_EMAIL="select emailId from user where emailId=?";
    private static final String RESET_PASSWORD="UPDATE user set password=? where emailId=?";
    private static final String CHECK_MOBILE="select mobileNo from user where mobileNo=?";
    private static final String RESET_PASSWORD_BY_SMS="UPDATE user set password=? where mobileNo=?";
    private static final String UPDATE_MOBILE_OTP_FORGOT_PASSWORD="UPDATE mobileOtp set otpNumber=?,OtpExpireTime=? where mobileNo=?";
    private static final String GET_MOBILE_NO_BY_USERID="select mobileNo from user where userId=?";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    SmsService smsService;

    public boolean update2FAProperties(String emailId, int tfaCode)
    {
        try
        {
            String email= jdbcTemplate.queryForObject("select emailId from newUser where emailId=?",new Object[]{emailId},String.class);
            if(email!=null)
            {
                try
                {
                    boolean check_verified_or_not=jdbcTemplate.queryForObject("select otpVerified from newUser where email=?",new Object[]{emailId},Boolean.class);
                    return false;
                }
                catch (Exception e)
                {
                    // email is present but if not verified
                    jdbcTemplate.update(UPDATE_OTP,tfaCode,(System.currentTimeMillis()/1000)+120,email);
                    return true;
                }
            }
        }

        //allow creating an account only if email is not registered
        catch (DataAccessException e)
        {
            jdbcTemplate.update(EMAIL_VERIFICATION,emailId,tfaCode,(System.currentTimeMillis()/1000)+120);
            return true;
        }
        return false;
    }

    //verifying email otp
    public boolean checkEmailCode(String id,int code)
    {
        boolean isValid= jdbcTemplate.queryForObject("select count(*) from newUser where emailOtp=? and emailId=? and OtpExpireTime>=?",new Object[]{code,id,System.currentTimeMillis()/1000},Integer.class)>0;

        if(isValid )
        {
            boolean otpVerified=jdbcTemplate.queryForObject("select otpVerified from newUser where emailId=?",new Object[]{id},boolean.class);
            if(!otpVerified)
            {
                jdbcTemplate.update(UPDATE_EMAIL_VERIFIED,id);
            }
        }
        return isValid;
    }


    //-- verifying mobile otp
    public boolean checkSmsCode(int userId,String mobileNum,int code)
    {
        boolean isValid= jdbcTemplate.queryForObject("select count(*) from mobileOtp where otpNumber=? and mobileNo=? and OtpExpireTime>=?",new Object[]{code,mobileNum,System.currentTimeMillis()/1000},Integer.class)>0;

        if(isValid )
        {
            boolean otpVerified=jdbcTemplate.queryForObject("select otpVerified from mobileOtp where mobileNo=?",new Object[]{mobileNum},boolean.class);

            if(!otpVerified)
            {
                jdbcTemplate.update(UPDATE_NUMBER_VERIFIED,mobileNum);
                jdbcTemplate.update("update user set mobileNo=? where userId=?",mobileNum,userId);
            }
        }
        return isValid;
    }

    public boolean  forgotPasswordEmail(String emailId, int tfaCode)
    {
        try
        {
            //email id from user
            String email=jdbcTemplate.queryForObject(CHECK_EMAIL,new Object[]{emailId},String.class);

            if(email!=null)
            {
                jdbcTemplate.update(UPDATE_OTP,tfaCode,(System.currentTimeMillis()/1000)+120,emailId);
                return true;
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return false;
    }


    public boolean resetPassword(User user)
    {
        if(user.getEmailId()!=null)
        {
            jdbcTemplate.update(RESET_PASSWORD,user.getPassword(),user.getEmailId());
            return true;
        }
        else if(user.getMobileNo()!=null)
        {
            jdbcTemplate.update(RESET_PASSWORD_BY_SMS,user.getPassword(),user.getMobileNo());
            return true;
        }
        return false;
    }

//----------------
    public boolean  forgotPasswordSms(String mobileNumber, int tfaCode)
    {
        String mobile=jdbcTemplate.queryForObject(CHECK_MOBILE,new Object[]{mobileNumber},String.class);
        if(mobile!=null)
        {
            jdbcTemplate.update(UPDATE_MOBILE_OTP_FORGOT_PASSWORD,tfaCode,(System.currentTimeMillis()/1000)+120,mobileNumber);
            return true;
        }
        return false; //if no mobile no is found, verify through email.
    }

    // extra
    public boolean updatingNumber(User user)
    {
        //old mobile number
        String phone = jdbcTemplate.queryForObject(GET_MOBILE_NO_BY_USERID,new Object[]{user.getUserId()},String.class);

        if(phone.equals(user.getMobileNo()))
        {
            return false;
        }

        //insert mobile number to mobileOtp table
        jdbcTemplate.update("insert into mobileOtp(mobileNo) values(?)",user.getMobileNo());

        //update user mobile no
        jdbcTemplate.update("update user set mobileNo=? where userId=?",user.getMobileNo(),user.getUserId());

        //delete old number from mobileOtp
        jdbcTemplate.update("delete from mobileOtp where mobileNo=?",phone);

        return true;
    }
}