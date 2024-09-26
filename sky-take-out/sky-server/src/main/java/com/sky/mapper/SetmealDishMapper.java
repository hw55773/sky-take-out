package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * interfaceName:       SetmealDishMapper
 * author:            wenhao2002
 * date:               2024/5/22 22:01
 */
@Mapper
public interface SetmealDishMapper {
    int coutHaveDishByIds(List<Long> ids);

    void insertSetmealDish(List<SetmealDish> setmealDishes);

    @Select("select * from setmeal_dish where setmeal_id =#{id}")
    List<SetmealDish> getBySetmealId(Long id);

    @Delete("delete from setmeal_dish where setmeal_id=#{id}")
    void deleteBySetmealId(Long id);
}
