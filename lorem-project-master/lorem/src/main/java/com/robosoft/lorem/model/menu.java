package com.robosoft.lorem.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class menu
{
    private String  dishPhoto;

    public menu(String dishPhoto)
    {
        this.dishPhoto=dishPhoto;
    }
}
