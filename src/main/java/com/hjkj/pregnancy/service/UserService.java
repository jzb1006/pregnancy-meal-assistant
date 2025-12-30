package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.model.dto.UserProfileRequest;
import com.hjkj.pregnancy.model.vo.UserStatusVO;

/**
 * 用户服务接口
 * 
 * @author Zhibin Jiang
 */
public interface UserService {

    /**
     * 初始化或更新用户档案
     * 
     * @param request 用户档案请求
     * @return 用户状态信息
     */
    UserStatusVO saveOrUpdateProfile(UserProfileRequest request);

    /**
     * 获取用户当前状态
     * 
     * @param openId 用户唯一标识
     * @return 用户状态信息
     */
    UserStatusVO getUserStatus(String openId);
}


