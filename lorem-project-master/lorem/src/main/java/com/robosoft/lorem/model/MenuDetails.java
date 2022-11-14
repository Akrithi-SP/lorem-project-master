package com.robosoft.lorem.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MenuDetails {

    private int restaurantId;
    private String dishName;
    private float price;
    private boolean customizable;
    private String description;
    private String dishPhoto;
    private boolean veg;

}
