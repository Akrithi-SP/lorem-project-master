package com.robosoft.lorem.model;

import lombok.Data;

import java.sql.Date;

@Data
public class OpeningDetails {
    private Date dateOf;
    private String openingTime;
    private String closingTime;
    private String reason;
    private boolean opened;

}
