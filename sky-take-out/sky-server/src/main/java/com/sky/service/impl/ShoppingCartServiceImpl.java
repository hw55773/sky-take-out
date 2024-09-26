package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * className:       ShoppingCartServiceImpl
 * author:          wenhao2002
 * date:            2024/6/8 16:09
 */
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    ShoppingCartMapper shoppingCartMapper;
    @Autowired
    DishMapper dishMapper;
    @Autowired
    SetmealMapper setmealMapper;

    @Override
    public void addCart(ShoppingCartDTO shoppingCartDTO) {
        //根据传进来的数据查看数据库中是否有数据
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list= shoppingCartMapper.select(shoppingCart);
        //如果有，将购物车表的number字段加一
        if (list!=null&&list.size()==1){
            ShoppingCart shoppingCart1 = list.get(0);
            shoppingCart1.setNumber(shoppingCart1.getNumber()+1);
            shoppingCartMapper.update(shoppingCart1);
        }else
        //如果没有插入数据
        {
            Long dishId = shoppingCart.getDishId();
            if (dishId !=null){
                DishVO dishVO = dishMapper.getById(dishId);
                shoppingCart.setName(dishVO.getName());
                shoppingCart.setAmount(dishVO.getPrice());
                shoppingCart.setImage(dishVO.getImage());
            }else {
                Setmeal setmeal = setmealMapper.getById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
       return shoppingCartMapper.select(shoppingCart);
    }

    @Override
    public void delete() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.delete(userId);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {

        //查询出要减少的购物项
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> cartList = shoppingCartMapper.select(shoppingCart);
        //根据购物项中的number字段判断：number<=1,删除该购物项；number>1,number-1
        if (cartList!=null&&cartList.size()>0) {
            ShoppingCart cart = cartList.get(0);
            Integer number =cart.getNumber();
            if(number>1){
                cart.setNumber(number-1);
                shoppingCartMapper.update(cart);
            }else {
                shoppingCartMapper.deleteById(cart.getId());
            }
        }
    }
}
