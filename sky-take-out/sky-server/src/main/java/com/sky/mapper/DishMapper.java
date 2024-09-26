package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.anotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(OperationType.INSERT)
//切面编程插入公共字段
    void insertDish(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    int coutStatusByids(List<Long> ids);

    void deleteBatch(List<Long> ids);

    @Select("select * from dish where id=#{id}")
    DishVO getById(Long id);

    @Select("select category_id from dish where id=#{id}")
    Long getCategoryById(Long id);

    @AutoFill(OperationType.UPDATE)
    void updateDish(Dish dish);

    @Select("select * from dish where category_id=#{categoryId}")
    List<Dish> getDishByCategoryById(Long categoryId);

    @Select("select d.* from dish d left join setmeal_dish sd on d.id = sd.dish_id where setmeal_id=#{id}; ")
    List<Dish> getDishBySetmealId(Long id);

    List<Dish> list(Dish dish);
}
