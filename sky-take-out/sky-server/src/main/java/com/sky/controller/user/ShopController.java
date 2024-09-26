package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * className:       ShopController
 * author:          wenhao2002
 * date:            2024/5/30 17:13
 */
@RestController("userShopController")
@RequestMapping("/user/shop/")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {

    public final static String KEY="SHOP_STATUS";

    @Autowired
    RedisTemplate redisTemplate;

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
