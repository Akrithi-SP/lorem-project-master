package com.robosoft.lorem.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class offerAllFields
{
//    private String offerId;
//    private float discount;
//    private int maxCashBack;
//    private String validUpto;
//    private int validPerMonth;
//    private String photo;
//    private String description;
//    private int brandId;
//    private boolean codEnabled;
//    private float superCashPercent;
//    private int maxSuperCash;

    offer  offer;

    private String addressDesc;

    private List<RestaurantAddress> restaurantAddress;


}
