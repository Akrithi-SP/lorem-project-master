package com.robosoft.lorem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Menu {
    private int restaurantId;
    private int dishId;
    private float price;
    private boolean customizable;
    private String dishPhoto;
    private String foodType;
}
