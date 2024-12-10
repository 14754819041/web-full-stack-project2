package com.mall.controller;

import com.alipay.easysdk.factory.Factory;
import com.mall.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/alipay")
@Slf4j
public class AlipayController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        try {
            // 获取支付宝POST过来的数据
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            log.info("收到支付宝回调：params={}", params);

            // 验证签名
            if (Factory.Payment.Common().verifyNotify(params)) {
                // 验证通知数据的正确性
                String tradeStatus = params.get("trade_status");
                String outTradeNo = params.get("out_trade_no"); // 商户订单号，即支付流水号
                String tradeNo = params.get("trade_no");     // 支付宝交易号
                String totalAmount = params.get("total_amount");

                // 支付成功
                if ("TRADE_SUCCESS".equals(tradeStatus)) {
                    paymentService.handleCallback(
                        outTradeNo,
                        tradeNo,
                        new BigDecimal(totalAmount)
                    );
                    log.info("支付宝支付成功：outTradeNo={}, tradeNo={}, amount={}", 
                        outTradeNo, tradeNo, totalAmount);
                    return "success";
                }
            } else {
                log.error("支付宝回调验签失败");
            }
        } catch (Exception e) {
            log.error("处理支付宝回调异常", e);
        }
        return "fail";
    }

    @GetMapping("/return")
    public String returnUrl(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            log.info("支付宝同步回调：{}", params);

            // 验证签名
            if (!Factory.Payment.Common().verifyNotify(params)) {
                log.error("支付宝同步回调签名验证失败");
                return "支付失败";
            }

            return "支付成功";
        } catch (Exception e) {
            log.error("处理支付宝同步回调失败：", e);
            return "支付异常";
        }
    }
} 