package com.mall.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mall.dto.WechatPayNotifyDTO;
import com.mall.service.OrderService;
import com.mall.service.WechatPayService;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Base64;

@Service
public class WechatPayServiceImpl implements WechatPayService {
    private static final Logger log = LoggerFactory.getLogger(WechatPayServiceImpl.class);
    private static final String NATIVE_PAY_API = "https://api.mch.weixin.qq.com/v3/pay/transactions/native";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${wechat.pay.app-id}")
    private String appId;
    
    @Value("${wechat.pay.mch-id}")
    private String mchId;
    
    @Value("${wechat.pay.notify-url}")
    private String notifyUrl;
    
    @Value("${wechat.pay.api-v3-key}")
    private String apiV3Key;
    
    @Value("${wechat.pay.merchant-serial-number}")
    private String merchantSerialNumber;
    
    @Value("${wechat.pay.enabled:false}")  // 默认禁用
    private boolean enabled;
    
    private final CloseableHttpClient wechatPayClient;
    private final PrivateKey merchantPrivateKey;
    private Verifier verifier;
    private final OrderService orderService;
    
    public WechatPayServiceImpl(CloseableHttpClient wechatPayClient, 
                               PrivateKey merchantPrivateKey,
                               OrderService orderService) {
        this.wechatPayClient = wechatPayClient;
        this.merchantPrivateKey = merchantPrivateKey;
        this.orderService = orderService;
    }
    
    @PostConstruct
    public void init() throws Exception {
        if (!enabled) {
            log.warn("微信支付功能未启用");
            return;
        }
        
        log.info("初始化微信支付服务...");
        try {
            // 初始化证书管理器
            CertificatesManager certificatesManager = CertificatesManager.getInstance();
            // 创建签名器
            PrivateKeySigner signer = new PrivateKeySigner(merchantSerialNumber, merchantPrivateKey);
            // 使用 APIv3 密钥初始化
            certificatesManager.putMerchant(mchId, 
                new WechatPay2Credentials(mchId, signer),
                apiV3Key.getBytes(StandardCharsets.UTF_8));
            
            this.verifier = certificatesManager.getVerifier(mchId);
            log.info("微信支付服务初始化完成");
        } catch (Exception e) {
            log.error("初始化微信支付服务失败: {}", e.getMessage(), e);
            if (enabled) {
                throw new RuntimeException("初始化微信支付服务失败", e);
            } else {
                log.warn("由于微信支付功能未启用，忽略初始化失败");
            }
        }
    }
    
    @Override
    public String createOrder(String orderId, BigDecimal amount, String description) {
        if (!enabled) {
            throw new RuntimeException("微信支付功能未启用");
        }
        try {
            log.info("开始创建微信支付订单, 订单号: {}, 金额: {}, 描述: {}", orderId, amount, description);
            
            // 构建请求JSON
            ObjectNode requestJson = objectMapper.createObjectNode();
            requestJson.put("mchid", mchId);
            requestJson.put("appid", appId);
            requestJson.put("description", description);
            requestJson.put("out_trade_no", orderId);
            requestJson.put("notify_url", notifyUrl);
            
            // 构建金额信息
            ObjectNode amountJson = requestJson.putObject("amount");
            // 将BigDecimal金额转换为分为单位的整数
            amountJson.put("total", amount.multiply(new BigDecimal("100")).intValue());
            amountJson.put("currency", "CNY");
            
            // 创建POST请求
            HttpPost httpPost = new HttpPost(NATIVE_PAY_API);
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-Type", "application/json");
            
            StringEntity entity = new StringEntity(requestJson.toString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            
            // 发送请求
            try (CloseableHttpResponse response = wechatPayClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                log.debug("微信支付响应状态码: {}, 响应内容: {}", statusCode, responseBody);
                
                if (statusCode == 200) {
                    // 解析响应JSON
                    ObjectNode responseJson = (ObjectNode) objectMapper.readTree(responseBody);
                    String codeUrl = responseJson.get("code_url").asText();
                    log.info("创建微信支付订单成功, 支付二维码链接: {}", codeUrl);
                    return codeUrl;
                } else {
                    log.error("创建微信支付订单失败, 状态码: {}, 响应内容: {}", statusCode, responseBody);
                    throw new RuntimeException("创建微信支付订单失败: " + responseBody);
                }
            }
        } catch (Exception e) {
            log.error("创建微信支付订单失败", e);
            throw new RuntimeException("创建微信支付订单失败", e);
        }
    }
    
    @Override
    public boolean verifyNotify(String notifyData, String signature, String nonce, String timestamp, String serialNumber) {
        if (!enabled) {
            throw new RuntimeException("微信支付功能未启用");
        }
        try {
            log.info("开始验证微信支付回调通知");
            log.debug("回调通知数据: {}", notifyData);
            log.debug("签名: {}, 随机串: {}, 时间戳: {}, 证书序列号: {}", signature, nonce, timestamp, serialNumber);
            
            // 构建验签名串
            String message = timestamp + "\n" + nonce + "\n" + notifyData + "\n";
            
            // 验证签名
            boolean isSignatureValid = verifier.verify(serialNumber, message.getBytes(StandardCharsets.UTF_8), signature);
            
            if (isSignatureValid) {
                log.info("微信支付回调通知验签成功");
                // 解密回调通知数据
                ObjectNode notifyJson = (ObjectNode) objectMapper.readTree(notifyData);
                String resourceType = notifyJson.get("resource_type").asText();
                ObjectNode resource = (ObjectNode) notifyJson.get("resource");
                String ciphertext = resource.get("ciphertext").asText();
                String nonceCipher = resource.get("nonce").asText();
                String associatedData = resource.has("associated_data") ? 
                    resource.get("associated_data").asText() : "";
                
                // 解密数据
                String decryptedData = decryptResource(ciphertext, nonceCipher, associatedData);
                WechatPayNotifyDTO notifyResult = objectMapper.readValue(decryptedData, WechatPayNotifyDTO.class);
                
                // 处理支付结果
                if ("SUCCESS".equals(notifyResult.getTradeState())) {
                    // 支付成功，更新订单状态
                    orderService.paySuccess(notifyResult.getOutTradeNo(), 
                            notifyResult.getTransactionId(),
                            new BigDecimal(notifyResult.getAmount().getTotal()).divide(new BigDecimal("100")));
                    return true;
                } else {
                    log.warn("支付未成功，状态: {}, 描述: {}", 
                            notifyResult.getTradeState(), 
                            notifyResult.getTradeStateDesc());
                    return false;
                }
            } else {
                log.error("微信支付回调通知验签失败");
                return false;
            }
        } catch (Exception e) {
            log.error("验证微信支付回调通知失败", e);
            throw new RuntimeException("验证微信支付回调通知失败", e);
        }
    }
    
    /**
     * 解密回调通知数据
     */
    private String decryptResource(String ciphertext, String nonce, String associatedData) throws Exception {
        final int KEY_LENGTH = 32;
        final int TAG_LENGTH = 128;
        
        // 解密
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec key = new SecretKeySpec(apiV3Key.getBytes(StandardCharsets.UTF_8), "AES");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, nonce.getBytes(StandardCharsets.UTF_8));
        
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        if (associatedData != null && !associatedData.isEmpty()) {
            cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
        }
        
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
        return new String(decryptedData, StandardCharsets.UTF_8);
    }
} 