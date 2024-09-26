package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * className:       DishController
 * author:          wenhao2002
 * date:            2024/5/21 19:48
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品接口")
public class DishController {
    @Autowired
    DishService dishService;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("新增菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}",dishDTO);
        dishService.save(dishDTO);
        //构造key
        String key="dish_"+dishDTO.getCategoryId();
        //删除缓存数据
        redisCatch(key);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品的分页查询")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO){

        log.info("菜品分页查询,{}",dishPageQueryDTO);
        PageResult pageResult=dishService.pageQuery(dishPageQueryDTO);

        return Result.success(pageResult);
    }
    @DeleteMapping
    @ApiOperation("批量删除")
    public Result deleteBatch(@RequestParam List<Long> ids){
        //删除缓存数据
        redisCatch("dish_*");
        log.info("批量删除菜品，{}",ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }



    /**
     * 根据id查询菜品详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品详情")
    public Result<DishVO> getById(@PathVariable Long id){
    log.info("根据id查询菜品详情:  {}",id);

    DishVO dishVO=dishService.getById(id);
        return Result.success(dishVO);
    }

    /**
     * \修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDTO){

        redisCatch("dish_*");
        log.info("修改菜品:  {}",dishDTO);
        dishService.updateDish(dishDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> getCategoryById(Long categoryId){
            log.info("根据分类id查询菜品   {}",categoryId);
          List<Dish> dish = dishService.getCategoryById(categoryId);

        return Result.success(dish);
    }
    @PostMapping("/status/{status}")
    @ApiOperation("修改菜品停售起售")
    public Result alterStatus(@PathVariable Integer status,Long id){
        redisCatch("dish_*");
        log.info("修改菜品停售起售  {}  {}",status,id);
        DishDTO dishDTO=new DishDTO();
        dishDTO.setStatus(status);
        dishDTO.setId(id);
        dishService.updateDish(dishDTO);

        return  Result.success();
    }
    private void redisCatch(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
