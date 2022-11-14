package com.robosoft.lorem.controller;

import com.robosoft.lorem.model.User;
import com.robosoft.lorem.model.newUser;
import com.robosoft.lorem.service.DAOService;
import com.robosoft.lorem.service.EmailService;
import com.robosoft.lorem.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.Random;

@RestController
@RequestMapping("/users")
public class TwoFactorServiceController
{
    @Autowired
    EmailService emailService;
    @Autowired
    SmsService smsService;
    @Autowired
    DAOService daoService;

    @PutMapping("/emails2fa")
    public ResponseEntity<Object> send2faCodeInEmail(@RequestBody newUser newuser) throws MessagingException {
        //2fa code generation
        Integer tfaCode =Integer.valueOf(new Random().nextInt(9999)+1000);

        boolean email_check=daoService.update2FAProperties(newuser.getEmailId(),tfaCode);

        if(email_check)
        {
            try
            {
                boolean emailSent=emailService.sendEmail(newuser.getEmailId(),tfaCode);
                if(emailSent)
                {
                    return new ResponseEntity<>("OTP sent to your email",HttpStatus.OK);
                }
            }
            catch (Exception e)
            {
                return  new ResponseEntity<>("OTP could not be sent",HttpStatus.FORBIDDEN);
            }
        }
        return  new ResponseEntity<>("Account already exists",HttpStatus.FORBIDDEN);
    }

    @PutMapping("/verifyEMail2fa")
    public ResponseEntity<Object> verifyEmail(@RequestBody newUser newUser)
    {
        //front-end will provide the email id
        boolean isValid=daoService.checkEmailCode(newUser.getEmailId(),newUser.getEmailOtp());
        if(isValid)
        {
            return  new ResponseEntity<>("OTP verified",HttpStatus.OK);
        }
        return  new ResponseEntity<>("Invalid OTP",HttpStatus.FORBIDDEN);
    }


    @PutMapping("/verifyMobile2fa")
    public ResponseEntity<Object> verifySms(@RequestParam("userId") int userId,@RequestParam("mobileNo") String mobileNo,@RequestParam("otpNumber") int otpNumber)
    {
        boolean isValid=daoService.checkSmsCode(userId,mobileNo,otpNumber);
        if(isValid)
        {
            return  new ResponseEntity<>("OTP verified",HttpStatus.OK);
        }
        return  new ResponseEntity<>("Invalid OTP",HttpStatus.FORBIDDEN);
    }


    @PutMapping("/forgotPassword/email")
    public ResponseEntity<String> sendEmail2FaToResetPassword(@RequestBody newUser newuser) throws MessagingException {
        Integer tfaCode =Integer.valueOf(new Random().nextInt(9999)+1000);

        boolean result=daoService.forgotPasswordEmail(newuser.getEmailId(), tfaCode);

        if(result)
        {
            try
            {
                boolean emailSent=emailService.sendEmail(newuser.getEmailId(),tfaCode);
                if(emailSent)
                {
                    return new ResponseEntity<>("OTP sent to your email",HttpStatus.OK);
                }
            }
            catch (Exception e)
            {
                return  new ResponseEntity<>("OTP could not be sent to your mail",HttpStatus.FORBIDDEN);
            }
        }
        //if no account found
        return  new ResponseEntity<>("No account with this email",HttpStatus.FORBIDDEN);
    }


    // since separate fields are required for updating passwords w.r.t email and number
    @PutMapping("/reset/email")
    public ResponseEntity<Object> resetPassword(@RequestBody User user)
    {
        //front end will check for otp verification and redirect to password resetting
        boolean resetPassword=daoService.resetPassword(user);
        if(resetPassword)
        {
            return  new ResponseEntity<>("Password reset successful",HttpStatus.OK);
        }
        return  new ResponseEntity<>("Could not reset password",HttpStatus.FORBIDDEN);
    }


    @PutMapping("/mobiles2fa")
    public ResponseEntity<String> resetPasswordThroughSms(@RequestBody User user) throws MessagingException {
        Integer tfaCode =Integer.valueOf(new Random().nextInt(9999)+1000);

        boolean result=daoService.forgotPasswordSms(user.getMobileNo(), tfaCode);

        if(result)
        {
            try
            {
                boolean smsSent=smsService.sendSms(user.getMobileNo(),tfaCode);
                if(smsSent)
                {
                    return new ResponseEntity<>("OTP sent to your mobile",HttpStatus.OK);
                }
            }
            catch (Exception e)
            {
                return  new ResponseEntity<>("OTP could not be sent to your number",HttpStatus.FORBIDDEN);
            }
        }
        //if no mobile number found
        return  new ResponseEntity<>("mobile number is not verified",HttpStatus.FORBIDDEN);
    }

    // ** note -changes
    @PutMapping("/reset/sms")
    public ResponseEntity<Object> resetPasswordEnabledThroughSms(@RequestBody User user)
    {
        //front end will check for otp verification and redirect to password resetting
        boolean resetPassword=daoService.resetPassword(user);
        if(resetPassword)
        {
            return  new ResponseEntity<>("Password reset successful",HttpStatus.OK);
        }
        return  new ResponseEntity<>("Could not reset password",HttpStatus.FORBIDDEN);
    }

    //extra api
    //@RequestMapping(value="/users/mobileNumber",method=RequestMethod.PUT)
    @PutMapping("/mobileNumber")
    public ResponseEntity<Object> updateMobileNumber(@RequestBody User user)
    {
        boolean updated=daoService.updatingNumber(user);
        if(updated)
        {
            return  new ResponseEntity<>("updated successfully",HttpStatus.OK);
        }
        return  new ResponseEntity<>("you Cannot update the same number",HttpStatus.FORBIDDEN);
    }
    //call the above 'verify' api to verify new number
}
