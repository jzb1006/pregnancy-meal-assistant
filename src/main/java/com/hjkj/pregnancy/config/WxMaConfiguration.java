package com.hjkj.pregnancy.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信小程序配置类
 * <p>
 * 配置微信小程序SDK，提供WxMaService用于调用微信小程序API。
 * 从application.yml读取AppID和AppSecret配置。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>初始化微信小程序配置</li>
 *   <li>创建WxMaService服务对象</li>
 *   <li>支持调用微信登录等API</li>
 * </ul>
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2026-01-06
 */
@Slf4j
@Configuration
public class WxMaConfiguration {

    /**
     * 微信小程序AppID
     */
    @Value("${wx.miniapp.appid}")
    private String appid;

    /**
     * 微信小程序AppSecret
     */
    @Value("${wx.miniapp.secret}")
    private String secret;

    /**
     * 创建微信小程序配置Bean
     *
     * @return WxMaConfig配置对象
     */
    @Bean
    public WxMaConfig wxMaConfig() {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(appid);
        config.setSecret(secret);
        log.info("微信小程序配置初始化完成, AppID: {}", appid);
        return config;
    }

    /**
     * 创建微信小程序服务Bean
     *
     * @param wxMaConfig 微信小程序配置
     * @return WxMaService服务对象
     */
    @Bean
    public WxMaService wxMaService(WxMaConfig wxMaConfig) {
        WxMaService service = new WxMaServiceImpl();
        service.setWxMaConfig(wxMaConfig);
        log.info("微信小程序服务初始化完成");
        return service;
    }
}


