package com.udari.evora.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product implements Serializable {

    private String productId;
    private String title;
    private String description;
    private double price;
    private String imageUrl;
    private int stockCount;
    private boolean status;

}