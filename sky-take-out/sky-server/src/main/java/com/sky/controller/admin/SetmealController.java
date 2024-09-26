package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * className:       SetmealController
 * author:          wenhao2002
 * date:            2024/5/25 18:06
 */
@Slf4j
@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    SetmealService setmealService;


    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO){
    log.info("新增套餐:  {}",setmealDTO);
    setmealService.insertSetmeal(setmealDTO);

        return Result.success();
    }
    @GetMapping("/page")
    @ApiOperation("分页查询套餐")
    public Result<PageResult> setmealQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询套餐   {}",setmealPageQueryDTO);
        PageResult pageResult=setmealService.page(setmealPageQueryDTO);

        return Result.success(pageResult);
    }
    @ApiOperation("批量删除套餐")
    @DeleteMapping
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result deleteStemeal(@RequestParam List<Long> ids){
        log.info("批量删除套餐: {}",ids);
        setmealService.delete(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id ){
        log.info("根据id查询套餐   {}",id);
      SetmealVO setmealVO = setmealService.getById(id);
        return  Result.success(setmealVO);
    }
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){

        log.info("修改套餐  {}",setmealDTO);
        setmealService.update(setmealDTO);


        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("修改套餐起售停售状态")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result saleONOrOff(@PathVariable Integer status,Long id){
    log.info("修改套餐起售停售状态:  {},{}",status,id);
    setmealService.setStatus(status,id);
        return Result.success();
    }
}
