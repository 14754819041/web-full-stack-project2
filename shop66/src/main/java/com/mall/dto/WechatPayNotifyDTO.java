package com.mall.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WechatPayNotifyDTO {
    @JsonProperty("mchid")
    private String mchId;
    
    @JsonProperty("appid")
    private String appId;
    
    @JsonProperty("out_trade_no")
    private String outTradeNo;
    
    @JsonProperty("transaction_id")
    private String transactionId;
    
    @JsonProperty("trade_state")
    private String tradeState;
    
    @JsonProperty("trade_state_desc")
    private String tradeStateDesc;
    
    @JsonProperty("success_time")
    private String successTime;
    
    private Amount amount;
    
    @Data
    public static class Amount {
        private Integer total;
        private String currency;
        @JsonProperty("payer_total")
        private Integer payerTotal;
    }
} 