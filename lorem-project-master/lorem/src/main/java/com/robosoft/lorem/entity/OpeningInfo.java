package com.robosoft.lorem.entity;

import lombok.AllArgsConstructor;

import java.sql.Date;


@AllArgsConstructor
public class OpeningInfo {

    private int restaurantId;
    private Date dateOf;
    private String openingTime;
    private String closingTime;
    private String reason;
    private boolean opened;

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Date getDateOf() {
        return dateOf;
    }

    public void setDateOf(String dateOf) {
        this.dateOf = Date.valueOf(dateOf);
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }
}
