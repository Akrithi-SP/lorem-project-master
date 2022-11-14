package com.robosoft.lorem.model;

import lombok.Data;

import java.util.List;

@Data
public class NearByBrandsSearchResult {

    private Integer resultsCount;
    private List<BrandSearchModel> nearByBrands;
}
