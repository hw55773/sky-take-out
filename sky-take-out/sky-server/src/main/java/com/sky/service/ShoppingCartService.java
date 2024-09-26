package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

/**
 * interfaceName:       ShoppingCartService
 * author:            wenhao2002
 * date:               2024/6/8 16:09
 */
public interface ShoppingCartService {
    void addCart(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> list();

    void delete();

    void sub(ShoppingCartDTO shoppingCartDTO);
}
