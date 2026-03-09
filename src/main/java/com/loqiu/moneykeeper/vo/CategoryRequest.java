package com.loqiu.moneykeeper.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryRequest {
    private String name;
    private String icon;
    private String color;
    private String type;
}