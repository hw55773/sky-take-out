package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * className:       OrderServiceImpl
 * author:          wenhao2002
 * date:            2024/6/8 23:09
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    ShoppingCartMapper shoppingCartMapper;
    @Autowired
    AddressBookMapper addressBookMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    WeChatPayUtil weChatPayUtil;
    WebSocketServer webSocketServer;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //检查抛出业务异常
        //判断地址是否为空
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //判断购物车是否为空
        //根据用户Id查询用户购物车数据
        Long userId = BaseContext.getCurrentId();
//        ShoppingCart shoppingCart = new ShoppingCart();
//        shoppingCart.setUserId(userId);
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.select(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单插入一条数据
        //构造一个订单对象
        Orders orders = new Orders();
        //属性拷贝
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        //补充属性
        orders.setNumber(String.valueOf(System.currentTimeMillis()) + userId);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        //插入数据
        orderMapper.insert(orders);
        //向订单明细插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
//        shoppingCartList.forEach(
//                shoppingCart1 -> {
//                    OrderDetail orderDetail=new OrderDetail();
//                    BeanUtils.copyProperties(shoppingCart1,orderDetail);
//                    orderDetail.setOrderId(orders.getId());
//                    orderDetailList.add(orderDetail);
//                }
//        );
        for (ShoppingCart shoppingCart1 : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart1, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        //插入数据
        orderDetailMapper.insertBatch(orderDetailList);

        //清空购物车
        shoppingCartMapper.delete(userId);
        //返回封装数据VO

        return OrderSubmitVO.builder()
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .id(orders.getId())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        //连接websocket提示来了一单
        Map<String,Object> map = new HashMap<>();
        map.put("type",1);
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号"+outTradeNo);
        webSocketServer.sendToAllClient(JSONObject.toJSONString(map));
        orderMapper.update(orders);
    }

    @Override
    public PageResult pageOrderlistHistory(OrdersPageQueryDTO ordersPageQueryDTO) {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> ordersList = orderMapper.select(ordersPageQueryDTO);
        //查询订单明细根据订单id并封装到OrderVo中
        List<OrderVO> orderVOList = new ArrayList<>();
        if (ordersList!=null&&ordersList.getTotal()>0) {
            for (Orders order: ordersList) {
                Long orderId = order.getId();
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order,orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(ordersList.getTotal(),orderVOList);
    }

    @Override
    public OrderVO getOrderDetailById(Long id) {

       Orders order= orderMapper.getById(id);
       List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(order.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    @Override
    public void cancelOrder(Long id) {
        Orders orders = Orders.builder().status(Orders.CANCELLED).id(id)
                .build();
        log.info("状态： {}",orders.getStatus());
        orderMapper.update(orders);
    }

    @Override
    public void reOrder(Long id) {
        //根据订单id查询出要再来一单的id
        Long userId = BaseContext.getCurrentId();
        //根据订单id查询订单明细表
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> shoppingCartList=new ArrayList<>();
        //将订单明细表中的数据插入到购物车中
        orderDetails.forEach(
                orderDetail -> {
                    ShoppingCart shoppingCart=new ShoppingCart();
                    BeanUtils.copyProperties(orderDetail,shoppingCart);
                    shoppingCart.setUserId(userId);
                    shoppingCart.setCreateTime(LocalDateTime.now());
                    shoppingCartList.add(shoppingCart);
                }
        );
        //批量插入到购物车中
        shoppingCartMapper.insertBacth(shoppingCartList);

    }

    @Override
    public OrderStatisticsVO statisticStatus() {

        OrderStatisticsVO orderStatisticsVO=new OrderStatisticsVO();
        Integer toBeConfirmed=orderMapper.statisticStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed=orderMapper.statisticStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.statisticStatus(Orders.DELIVERY_IN_PROGRESS);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;
    }

    @Override
    public void updateStatus(Orders orders) {
        orderMapper.update(orders);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {

        Orders orders=new Orders();
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orderMapper.update(orders);

    }

    @Override
    public void reminder(Long id) {
        //判断订单是否存在
        Orders orders = orderMapper.getById(id);
        if (orders==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Map<String,Object> map =new HashMap<>();
        map.put("type",2);
        map.put("orderId",orders.getId());
        map.put("content","订单号"+orders.getNumber());

        webSocketServer.sendToAllClient(JSON.toJSONString(map));

    }

}
