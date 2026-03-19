package com.udari.evora.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private String cartItemId;
    private String productId;
    private String productTitle;
    private String productImage;
    private double unitPrice;
    private int quantity;
    private double totalPrice;
}