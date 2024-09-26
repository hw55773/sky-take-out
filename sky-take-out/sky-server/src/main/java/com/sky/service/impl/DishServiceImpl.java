package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * className:       DishServiceImpl
 * author:          wenhao2002
 * date:            2024/5/21 19:58
 */
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    DishMapper dishMapper;
    @Autowired
    DishFlavorMapper dishFlavorMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Autowired
    CategoryMapper categoryMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(DishDTO dishDTO) {
        //向菜品表中插入一条数据
        //数据拷贝
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //插入数据
        dishMapper.insertDish(dish);
        //获取插入后的主键id
        Long id = dish.getId();
        //向口味表中插入数据
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(id));
            dishFlavorMapper.insert(flavors);
        }

    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page= dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBatch(List<Long> ids) {
        //检查是否能够删除菜品--是否是起售状态
          //获取菜品中处于起售状态的菜品数
        int sum=dishMapper.coutStatusByids(ids);
        if (sum>0){
             throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        //检查是否能够删除菜品--是否关联套餐
        int num=setmealDishMapper.coutHaveDishByIds(ids);
        if (num>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品
        dishMapper.deleteBatch(ids);
        //删除根据菜品口味表中的口味
        dishFlavorMapper.deleteByDishId(ids);
    }

    @Override
    public DishVO getById(Long id) {
        //根据id获取菜品信息
        DishVO dishVO= dishMapper.getById(id);
        //获取分类id并获取对应分类名
        Long categoryId=dishMapper.getCategoryById(id);
       String name= categoryMapper.getNameById(categoryId);
        //根据id获取菜品对应口味信息
      List<DishFlavor> dishFlavors= dishFlavorMapper.getByDishId(id);

      dishVO.setFlavors(dishFlavors);
      dishVO.setCategoryName(name);

        return dishVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateDish(DishDTO dishDTO) {
        //拷贝到实体类,插入到dish表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.updateDish(dish);
        if (dishDTO.getFlavors()!=null&&dishDTO.getFlavors().size()>0) {
            //删除口味数据
            List<Long> ids=new ArrayList<>();
            ids.add(dishDTO.getId());
            dishFlavorMapper.deleteByDishId(ids);
            //重新插入数据
            dishFlavorMapper.insert(dishDTO.getFlavors());
        }
    }

    @Override
    public List<Dish> getCategoryById(Long categoryId) {
      List<Dish>  dish= dishMapper.getDishByCategoryById(categoryId);
        return dish;
    }
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
