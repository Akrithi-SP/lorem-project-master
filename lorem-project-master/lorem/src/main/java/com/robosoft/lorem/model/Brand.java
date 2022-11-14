package com.robosoft.lorem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Brand
{
    private int brandId;
    private String brandName;
    private String description;
    private MultipartFile logo;
    private MultipartFile profilePic;
    private String brandOrigin;
}
