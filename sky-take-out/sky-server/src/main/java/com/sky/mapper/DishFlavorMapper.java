package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * interfaceName:       DishFlavorMapper
 * author:            wenhao2002
 * date:               2024/5/21 20:17
 */
@Mapper
public interface DishFlavorMapper {

    void insert(List<DishFlavor> flavors);

    void deleteByDishId(List<Long> ids);

    @Select("select * from dish_flavor where dish_id=#{id}")
    List<DishFlavor> getByDishId(Long id);
}
