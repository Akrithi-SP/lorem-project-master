package com.robosoft.lorem.model;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

/*
{
"cartId":"",
"userId":"",
"restaurantId":"",
"itemsIncart":[{
"dishId":"",
"dishCount":"",
"customizationInfo":"",
"addOnCount":"",
"itemCount":""
},],
"cookingInstruction":"",
"toPay":"",
"scheduleDate":"",
"scheduleTime":"",
}
 */

public class CartModel {


    private Integer cartId;
    private int userId;
    private int restaurantId;
    private List<ItemModel> itemsIncart;
    private String cookingInstruction;
    private double toPay;
    private Date scheduleDate;
    private Time scheduleTime;


    public Integer getCartId() {
        return cartId;
    }

    public void setCartId(Integer cartId) {
        this.cartId = cartId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<ItemModel> getItemsIncart() {
        return itemsIncart;
    }

    public void setItemsIncart(List<ItemModel> itemsIncart) {
        this.itemsIncart = itemsIncart;
    }

    public String getCookingInstruction() {
        return cookingInstruction;
    }

    public void setCookingInstruction(String cookingInstruction) {
        this.cookingInstruction = cookingInstruction;
    }

    public double getToPay() {
        return toPay;
    }

    public void setToPay(double toPay) {
        this.toPay = toPay;
    }

    public Date getScheduleDate() {
        return scheduleDate;
    }


    //format  yyyy-[m]m-[d]d
    public void setScheduleDate(String scheduleDate) {
        this.scheduleDate = Date.valueOf(scheduleDate);
    }

    public Time getScheduleTime() {
        return scheduleTime;
    }


    // format hh:mm:ss
    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = Time.valueOf(scheduleTime);
    }
}
