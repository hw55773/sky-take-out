package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * className:       OrderTask
 * author:          wenhao2002
 * date:            2024/6/10 21:25
 */
@Slf4j
@Component
public class OrderTask {

    @Autowired
    OrderMapper orderMapper;


    /**
     * 超时订单处理
     */
    @Scheduled(cron = "0 * * * * ?")
    public void timeOutOrder() {
        log.info("超时订单定时任务");
        //查询出超时订单
        //select * from orders where status=? and order_time <?
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.selectOutTimeLT(Orders.PENDING_PAYMENT, time);
        //修改超时订单状态，并说明原因
        if (ordersList!=null&&ordersList.size()>0) {
            ordersList.forEach(
                    orders -> {
                        orders.setStatus(Orders.CANCELLED);
                        orders.setCancelReason("订单超时");
                        orders.setCancelTime(LocalDateTime.now());
                        orderMapper.update(orders);
                    }
            );
        }

    }
    //@Scheduled(cron = "0/5 * * * * ?")
    @Scheduled(cron = "0 0 1 * * ?")
    public void deliveryOrder(){
        log.info("处理未收货");
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        //查询出未收货订单
        List<Orders> ordersList = orderMapper.selectOutTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        //为其设置更新状态
        if (ordersList!=null&&ordersList.size()>0) {
            ordersList.forEach(
                    orders -> {
                        orders.setStatus(Orders.COMPLETED);
                        orderMapper.update(orders);
                    }
            );
        }

    }

}
