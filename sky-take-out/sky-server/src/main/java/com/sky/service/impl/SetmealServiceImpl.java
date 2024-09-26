package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * className:       SetmealServiceImpl
 * author:          wenhao2002
 * date:            2024/5/25 18:14
 */
@Slf4j
@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    SetmealMapper setmealMapper;

    @Autowired
    DishMapper dishMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Autowired
    CategoryMapper categoryMapper;

    @Transactional(rollbackFor = Exception.class)
    public void insertSetmeal(SetmealDTO setmealDTO) {
        //先对套餐表进行插入
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insertSetmeal(setmeal);
        //插入到套餐菜品表
        log.info("套餐菜品信息  {}", setmealDTO.getSetmealDishes());
        setmealDishMapper.insertSetmealDish(setmealDTO.getSetmealDishes());

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        List<SetmealVO> setmealList = setmealMapper.selectQuery(setmealPageQueryDTO);
        log.info("分页展示:  {}", setmealList);
        setmealList.forEach(
                setmealVO -> {
                    log.info("{}", setmealVO);
                    setmealVO.setCategoryName(categoryMapper.getNameById(setmealVO.getCategoryId()));
                }
        );
        Page<SetmealVO> page = (Page<SetmealVO>) setmealList;
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void delete(List<Long> ids) {
        //查询是否有处于起售状态的套餐
        ids.forEach(
                id -> {
                    //根据id获取套餐
                    Setmeal setmeal = setmealMapper.getById(id);
                    if (StatusConstant.ENABLE.equals(setmeal.getStatus())) {
                        throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
                    }
                }
        );
        setmealMapper.delete(ids);

    }

    @Override
    public SetmealVO getById(Long id) {
        //根据id查询套餐
        Setmeal setmeal = setmealMapper.getById(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        //根据套餐id获取套餐-菜品表信息
        List<SetmealDish> setmealDishes= setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }


    @Transactional(rollbackFor = Exception.class)
    public void update(SetmealDTO setmealDTO) {
        //对象拷贝
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //更新套餐表
        setmealMapper.update(setmeal);
        //根据id删除对应的套餐菜品表信息
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        //插入更新后的菜品信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(
                setmealDish -> setmealDish.setSetmealId(setmealDTO.getId())
        );
        setmealDishMapper.insertSetmealDish(setmealDishes);
    }

    @Override
    public void setStatus(Integer status, Long id) {
        //查看是否有停售的菜品
        if(status==StatusConstant.ENABLE){
            List<Dish> dishList=dishMapper.getDishBySetmealId(id);
            dishList.forEach(
                    dish -> {
                        if (dish.getStatus()==StatusConstant.DISABLE){
                            throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                        }
                    }
            );
        }
        //更新状态
        setmealMapper.update(Setmeal.builder()
                .id(id)
                .status(status)
                .build());
    }
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
