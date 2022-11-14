package com.robosoft.lorem.response;

import lombok.Data;

import java.util.List;

@Data
public class OrderDetails
{
    private int orderId;
    private List<DishInfo> dishInfoList;
    private int userId;
    private float amountPaid;
    private float totalAmount;
    private int restaurantId;
    private float fee;
    private float discount;
    private String paymentMode;
    private String deliveryAddress;
    private String scheduleDate;
    private String scheduleTime;
}
