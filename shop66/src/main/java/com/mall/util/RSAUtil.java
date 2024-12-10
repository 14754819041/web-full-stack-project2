package com.mall.util;

import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

public class RSAUtil {
    private static final Logger log = LoggerFactory.getLogger(RSAUtil.class);

    public static PrivateKey loadPrivateKey(String privateKeyPEM) {
        try {
            log.debug("开始加载私钥...");
            log.debug("私钥内容: \n{}", privateKeyPEM);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(privateKeyPEM.getBytes(StandardCharsets.UTF_8));
            PrivateKey privateKey = PemUtil.loadPrivateKey(inputStream);
            log.debug("私钥加载成功: {}", privateKey.getAlgorithm());
            return privateKey;
        } catch (Exception e) {
            log.error("加载私钥失败: {}", e.getMessage(), e);
            throw new RuntimeException("加载私钥失败: " + e.getMessage(), e);
        }
    }
} 