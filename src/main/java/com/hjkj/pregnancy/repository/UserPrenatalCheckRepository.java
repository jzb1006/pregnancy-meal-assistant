package com.hjkj.pregnancy.repository;

import com.hjkj.pregnancy.entity.UserPrenatalCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户产检状态数据访问接口
 * 
 * @author Zhibin Jiang
 */
@Repository
public interface UserPrenatalCheckRepository extends JpaRepository<UserPrenatalCheck, Long> {

    /**
     * 查询用户所有产检状态
     *
     * @param openId 用户唯一标识
     * @return 用户产检状态列表
     */
    List<UserPrenatalCheck> findByOpenId(String openId);

    /**
     * 查询用户指定产检项目的状态
     *
     * @param openId 用户唯一标识
     * @param templateCode 项目编码
     * @return 用户产检状态（可选）
     */
    Optional<UserPrenatalCheck> findByOpenIdAndTemplateCode(String openId, String templateCode);

    /**
     * 查询用户未完成的产检项目
     *
     * @param openId 用户唯一标识
     * @return 用户产检状态列表
     */
    List<UserPrenatalCheck> findByOpenIdAndIsDoneFalse(String openId);
}

