package com.mall.config;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.ByteArrayInputStream;

@Configuration
public class WechatPayConfig {
    private static final Logger log = LoggerFactory.getLogger(WechatPayConfig.class);
    
    @Value("${wechat.pay.app-id}")
    private String appId;
    
    @Value("${wechat.pay.mch-id}")
    private String mchId;
    
    @Value("${wechat.pay.merchant-serial-number}")
    private String merchantSerialNumber;
    
    @Value("${wechat.pay.merchant-private-key-path}")
    private Resource merchantPrivateKeyPath;
    
    @Value("${wechat.pay.api-v3-key}")
    private String apiV3Key;
    
    @Bean
    public PrivateKey merchantPrivateKey() throws Exception {
        log.info("开始初始化商户私钥...");
        log.debug("加载私钥文件: {}", merchantPrivateKeyPath.getFilename());
        return PemUtil.loadPrivateKey(merchantPrivateKeyPath.getInputStream());
    }
    
    @Bean
    public CloseableHttpClient wechatPayClient(PrivateKey merchantPrivateKey)throws Exception {
        log.info("开始初始化微信支付客户端...");
        
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                .withMerchant(mchId, merchantSerialNumber, merchantPrivateKey)
                .withValidator((response) -> true);  // 暂时不验证应答签名
        
        return builder.build();
    }
}