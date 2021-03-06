package com.tanp.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.tanp.mmall.common.Const;
import com.tanp.mmall.common.ResponseCode;
import com.tanp.mmall.common.ServerResponse;
import com.tanp.mmall.pojo.User;
import com.tanp.mmall.service.IOrderService;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author CodeBricklayer
 * @date 2019/11/5 11:54
 * @description TODO
 */
@Controller
@RequestMapping("/order")
public class OrderController {

  private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

  @Autowired
  private IOrderService iOrderService;


  //创建订单接口
  @RequestMapping("/create.do")
  @ResponseBody
  public ServerResponse create(HttpSession session, Integer shippingId) {
    User user = (User) session.getAttribute(Const.CURRENT_USER);
    if (user == null) {
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
    }
    return iOrderService.createOrder(user.getId(),shippingId);
  }


  //取消订单接口
  @RequestMapping("/cancel.do")
  @ResponseBody
  public ServerResponse cancel(HttpSession session, Long orderNo) {
    User user = (User) session.getAttribute(Const.CURRENT_USER);
    if (user == null) {
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
    }
    return iOrderService.cancel(user.getId(),orderNo);
  }

  //获取购物车接口 (已经选中打算结算的购物车中商品的信息接口)
  @RequestMapping("/getOrderCartProduct.do")
  @ResponseBody
  public ServerResponse getOrderCartProduct(HttpSession session) {
    User user = (User) session.getAttribute(Const.CURRENT_USER);
    if (user == null) {
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
    }
    return iOrderService.getOrderCartProduct(user.getId());
  }


  ////查询订单详情的接口
  @RequestMapping("/detail.do")
  @ResponseBody
  public ServerResponse detail(HttpSession session, Long orderNo) {
    User user = (User) session.getAttribute(Const.CURRENT_USER);
    if (user == null) {
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
    }
    return iOrderService.getOrderDetail(user.getId(),orderNo);
  }



  //个人中心查看订单列表接口
  @RequestMapping("/list.do")
  @ResponseBody
  public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize) {
    User user = (User) session.getAttribute(Const.CURRENT_USER);
    if (user == null) {
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
    }
    return iOrderService.getOrderList(user.getId(),pageNum,pageSize);
  }


  @RequestMapping("/pay.do")
  @ResponseBody
  public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request) {
    User user = (User) session.getAttribute(Const.CURRENT_USER);
    if (user == null) {
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
          ResponseCode.NEED_LOGIN.getDesc());
    }
    //这是一个文件目录，用户存放支付宝中生成的二维码存放目录，对应的目录路径是tomcat 服务器目录
    String path = request.getSession().getServletContext().getRealPath("upload");

    return iOrderService.pay(orderNo, user.getId(), path);
  }

  @RequestMapping("/alipayCallback.do")
  @ResponseBody
  public Object alipayCallback(HttpServletRequest request) {
    Map<String, String> params = Maps.newHashMap();
    Map requestParams = request.getParameterMap();
    for (Iterator iterator = requestParams.keySet().iterator(); iterator.hasNext(); ) {
      String name = (String) iterator.next();
      String[] values = (String[]) requestParams.get(name);
      String valueStr = "";
      for (int i = 0; i < values.length; i++) {
        valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
      }
      params.put(name, valueStr);
    }

    logger
        .info("支付宝回调,sign:{},trade_status:{},参数:{}", params.get("sign"), params.get("trade_status"),
            params.toString());

    //!!!验证回调的正确性，是不是支付宝发了，而且还要避免重复通知
    params.remove("sign_type");
    try {
      boolean alipayRSACheckedV2 = AlipaySignature
          .rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
      if (!alipayRSACheckedV2) {
        return ServerResponse.createByErrorMessage("非法请求，验证不通过");
      }
    } catch (AlipayApiException e) {
      logger.info("支付宝回调异常:{}", e);
    }

    //验证各种数据
    ServerResponse serverResponse = iOrderService.aliCallBack(params);
    if (serverResponse.isSuccess()) {
      return Const.AlipayCallback.RESPONSE_SUCCESS;
    }
    return Const.AlipayCallback.RESPONSE_FAILED;
  }

  @RequestMapping("/queryOrderPayStatus.do")
  @ResponseBody
  public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo) {
    User user = (User) session.getAttribute(Const.CURRENT_USER);
    if (user == null) {
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
          ResponseCode.NEED_LOGIN.getDesc());
    }
    ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
    if (serverResponse.isSuccess()) {
      return ServerResponse.createBySuccess(true);
    }
    return ServerResponse.createBySuccess(false);
  }
}
