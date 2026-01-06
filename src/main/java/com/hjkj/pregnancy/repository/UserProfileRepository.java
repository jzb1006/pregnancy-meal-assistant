package com.hjkj.pregnancy.repository;

import com.hjkj.pregnancy.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户档案数据访问层
 * 
 * @author Zhibin Jiang
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * 根据OpenID查询用户档案
     * 
     * @param openId 用户唯一标识
     * @return 用户档案
     */
    Optional<UserProfile> findByOpenId(String openId);

    /**
     * 检查OpenID是否存在
     * 
     * @param openId 用户唯一标识
     * @return 是否存在
     */
    boolean existsByOpenId(String openId);
}






