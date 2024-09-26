package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrderMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * className:       OrderController
 * author:          wenhao2002
 * date:            2024/6/10 14:50
 */
@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "管理端接口")
public class OrderController {

    @Autowired
    OrderService orderService;
    @Autowired
    OrderMapper orderMapper;

    /**
     * 订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    @ApiOperation("订单查询")
    @GetMapping("/conditionSearch")
    public Result<PageResult> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {

        log.info("订单查询 {}",ordersPageQueryDTO);
        PageResult pageResult = orderService.pageOrderlistHistory(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 统计各个订单状态
     * @return
     */
    @ApiOperation("统计各个订单状态")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statisticStatus(){

        log.info("统计各个订单状态");
        OrderStatisticsVO orderStatisticsVO = orderService.statisticStatus();

        return Result.success(orderStatisticsVO);
    }

    /**
     * 查询订单详情
     * @return
     */
    @ApiOperation("查询订单详情")
    @GetMapping("/details/{id}")
    public Result<OrderVO> orderDetail(@PathVariable Long id){

        OrderVO orderVO = orderService.getOrderDetailById(id);

        return Result.success(orderVO);
    }

    /**
     * 接单
     * @param orders
     * @return
     */
    @ApiOperation("接单")
    @PutMapping("/confirm")
    public Result acceptOrder(@RequestBody Orders orders){

        log.info("接单");
        orders.setStatus(Orders.CONFIRMED);
        orderService.updateStatus(orders);

        return Result.success();
    }
    /**
     * 拒单
     * @param orders
     * @return
     */
    @ApiOperation("拒单")
    @PutMapping("/rejection")
    public Result rejection(@RequestBody Orders orders){

        log.info("拒单");
        orders.setStatus(Orders.CANCELLED);
        orderService.updateStatus(orders);

        return Result.success();
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @ApiOperation("取消订单")
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("取消订单");
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 派送订单
     * @param id
     * @return
     */
    @ApiOperation("派送订单")
    @PutMapping("/delivery/{id}")
    public Result deliverOrder(@PathVariable Long id){
        Orders orders1 = orderMapper.getById(id);
        if (orders1==null|| Objects.equals(orders1.getStatus(), Orders.CONFIRMED)){
                throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        log.info("派送订单 {}",id);
        Orders orders = Orders.builder().id(id).status(Orders.DELIVERY_IN_PROGRESS).build();
        orderService.updateStatus(orders);
        return Result.success();
    }

    /**
     * 完成订单
     * @param id
     * @return
     */
    @ApiOperation("完成订单")
    @PutMapping("/complete/{id}")
    public Result completeOrder(@PathVariable Long id){
        Orders orders1 = orderMapper.getById(id);
        if (orders1==null|| Objects.equals(orders1.getStatus(), Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        log.info("完成订单 {}",id);

        Orders orders = Orders.builder().id(id).status(Orders.COMPLETED).build();
        orderService.updateStatus(orders);
        return Result.success();
    }

}
