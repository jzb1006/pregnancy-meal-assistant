package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.model.PageResult;
import com.hjkj.pregnancy.model.dto.HistorySearchRequest;
import com.hjkj.pregnancy.model.vo.MealVO;

import java.util.List;

/**
 * 浏览历史服务接口
 * 
 * @author Zhibin Jiang
 */
public interface HistoryService {

    /**
     * 记录浏览历史
     * 
     * @param userId   用户ID
     * @param recipeId 食谱ID
     */
    void recordHistory(Long userId, Long recipeId);

    /**
     * 获取用户最近浏览的食谱ID列表
     * 
     * @param userId 用户ID
     * @param limit  数量限制
     * @return 食谱ID列表
     */
    List<Long> getRecentRecipeIds(Long userId, int limit);

    /**
     * 获取用户浏览历史 (分页)
     * 
     * @param openId 用户唯一标识
     * @param page   页码 (1开始)
     * @param size   每页大小
     * @return 分页浏览历史
     */
    PageResult<MealVO> getUserHistory(String openId, int page, int size);

    /**
     * 获取用户最近浏览的菜品名称列表
     * 
     * @param userId 用户ID
     * @param limit  数量限制
     * @return 菜品名称列表
     */
    List<String> getRecentDishNames(Long userId, int limit);

    /**
     * 根据食谱ID获取菜单详情
     * <p>
     * 查询指定用户的某个历史记录详情，包含完整的菜单信息和用户反馈状态。
     * 如果用户未浏览过该菜单，将抛出业务异常。
     * </p>
     *
     * @param openId   用户唯一标识
     * @param recipeId 食谱ID
     * @return 菜单详情VO
     * @throws com.hjkj.pregnancy.exception.BusinessException 当用户不存在或未浏览过该菜单时
     */
    MealVO getMealDetail(String openId, Long recipeId);

    /**
     * 搜索用户浏览历史（支持多条件）
     * <p>
     * 支持以下搜索条件的任意组合：
     * <ul>
     *   <li>菜单名称模糊搜索</li>
     *   <li>用户反馈筛选（喜欢/不喜欢/吃腻了）</li>
     *   <li>餐次类型筛选（早餐/午餐/晚餐）</li>
     * </ul>
     * 未指定的搜索条件将被忽略，返回结果按浏览时间倒序排列。
     * </p>
     *
     * @param openId  用户唯一标识
     * @param request 搜索请求参数
     * @return 分页搜索结果
     * @throws com.hjkj.pregnancy.exception.BusinessException 当用户不存在时
     */
    PageResult<MealVO> searchUserHistory(String openId, HistorySearchRequest request);
}
