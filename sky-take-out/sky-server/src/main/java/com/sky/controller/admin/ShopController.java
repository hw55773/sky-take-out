package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * className:       ShopController
 * author:          wenhao2002
 * date:            2024/5/30 16:53
 */
@Api(tags = "店铺相关接口")
@RestController("adminShopController")
@Slf4j
@RequestMapping("/admin/shop")
public class ShopController {

    public final static String KEY="SHOP_STATUS";

    @Autowired
    RedisTemplate redisTemplate;


    /**
     * 设置店铺状态
     * @param status
     * @return
     */
    @ApiOperation("设置店铺状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺状态为 {}",status==1 ? "营业中" :"打烊中");
        redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }

    /**
     * 获取店铺状态
     * @return
     */
    @ApiOperation("获取店铺状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
   Integer status= (Integer) redisTemplate.opsForValue().get(KEY);
            log.info("获取到的店铺状态为 {}",status==1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
