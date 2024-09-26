package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * interfaceName:       ShoppingCartMapper
 * author:            wenhao2002
 * date:               2024/6/8 16:24
 */
@Mapper
public interface ShoppingCartMapper {

    /**
     * 条件查询
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> select(ShoppingCart shoppingCart);

    /**
     * 更新数量
     * @param shoppingCart1
     */
    @Update("update shopping_cart set number=#{number} where id=#{id}")
    void update(ShoppingCart shoppingCart1);

    /**
     * 新增购物车
     * @param shoppingCart
     */
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户id删除
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id=#{userId}")
    void delete(Long userId);

    @Delete("delete from shopping_cart where id=#{id}")
    void deleteById(Long id);

    void insertBacth(List<ShoppingCart> shoppingCartList);
}
