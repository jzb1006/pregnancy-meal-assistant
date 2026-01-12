package com.hjkj.pregnancy.service.impl;

import com.hjkj.pregnancy.advisor.AiAdvisorContext;
import com.hjkj.pregnancy.entity.UserProfile;
import com.hjkj.pregnancy.exception.UserNotFoundException;
import com.hjkj.pregnancy.model.dto.FoodCheckResponse;
import com.hjkj.pregnancy.repository.UserProfileRepository;
import com.hjkj.pregnancy.service.ChatModelService;
import com.hjkj.pregnancy.service.FoodSafetyService;
import com.hjkj.pregnancy.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodSafetyServiceImpl implements FoodSafetyService {

        private final ChatModelService chatModelService;
        private final UserProfileRepository userProfileRepository;

        private static final String SAFETY_SYSTEM_PROMPT = """
                        # Role
                        你是一位拥有20年临床经验的产科营养专家和食品安全官。

                        # Objective
                        根据用户的查询和孕周，评估某种食物的安全性。

                        # Safety Standards (Strict)
                        1. 🔴 **禁止 (RED):** - 包含酒精。
                           - 生食或未熟透的肉/蛋/海鲜 (李斯特菌/沙门氏菌风险)。
                           - 高汞海鲜 (如旗鱼、鲨鱼、大眼金枪鱼)。
                           - 未经巴氏杀菌的奶酪/牛奶。
                           - 孕早期(1-12周)严禁食用活血化瘀或寒性极强的食物 (如大量螃蟹、甲鱼)。
                        2. 🟡 **慎吃 (YELLOW):** - 高糖、高油、高盐。
                           - 咖啡因 (每日限制 < 200mg)。
                           - 辛辣刺激 (易引起便秘/烧心)。
                           - 加工肉类 (亚硝酸盐)。
                        3. 🟢 **安全 (GREEN):** - 彻底煮熟的肉类/海鲜。
                           - 蔬菜水果 (需清洗干净)。
                           - 优质蛋白和全谷物。

                        # Output Format
                        请仅返回 JSON 格式，不要包含 Markdown 标记。结构如下：
                        {
                          "food_name": "标准食物名称",
                          "safety_level": "GREEN" 或 "YELLOW" 或 "RED",
                          "short_conclusion": "少于15字的简短结论，用于列表展示",
                          "risk_analysis": "详细分析风险点或营养价值 (50字左右)",
                          "suggested_amount": "具体的建议摄入量 (例如: 每周不超过2次 / 严禁食用)"
                        }
                        """;

        // 预埋的营养贴士库（兜底用，或者 Random 实现）
        private static final List<String> NUTRITION_TIPS = List.of(
                        "孕中期多吃含铁食物（红肉、动物肝脏），预防贫血。",
                        "少食多餐可以有效缓解孕期胃部不适。",
                        "每天喝足量的水，有助于羊水代谢循环。",
                        "深海鱼类含有丰富的DHA，有助于宝宝大脑发育。",
                        "补充叶酸不仅仅是备孕期，孕早期同样重要。",
                        "适当晒晒太阳，促进钙的吸收。",
                        "避免食用生冷、未煮熟的食物，预防细菌感染。");

        @Override
        public Flux<String> checkFoodSafety(String openId, String query) {
                // 1. 获取用户信息
                UserProfile user = userProfileRepository.findByOpenId(openId)
                                .orElseThrow(() -> new UserNotFoundException("用户不存在: " + openId));

                int week = DateUtil.calculatePregnancyWeek(user.getLastMenstrualPeriod());

                // 2. 构建 Prompt
                String userPromptText = """
                                用户查询：【%s】
                                当前孕周：孕 %d 周

                                请根据孕周特性（如孕早期更敏感、孕晚期需控糖）进行评估。
                                """.formatted(query, week);

                // 3. 配置格式转换器 (仅用于获取Prompt Schema，流式不直接转换对象)
                BeanOutputConverter<FoodCheckResponse> converter = new BeanOutputConverter<>(FoodCheckResponse.class);
                String format = converter.getFormat();

                String fullPrompt = SAFETY_SYSTEM_PROMPT + "\n\n" + userPromptText + "\n\n" + format;

                // 4. 创建 AI Advisor 上下文
                AiAdvisorContext context = AiAdvisorContext.of(openId, "food_safety_check", "CHECK");

                log.info("调用AI检查食品安全(流式): openId={}, query={}", openId, query);

                // 5. 使用 ChatModelService 流式调用
                return chatModelService.stream(fullPrompt, context)
                                .map(chatResponse -> {
                                        String content = chatResponse.getResult().getOutput().getText();
                                        return content != null ? content : "";
                                })
                                .doOnError(e -> log.error("食品安全流式检查失败", e));
        }

        @Override
        public String getNutritionTip(String openId) {
                // 简单实现：随机返回一条
                // 进阶实现：可以根据孕周返回 (Week based tips)
                return NUTRITION_TIPS.get(new Random().nextInt(NUTRITION_TIPS.size()));
        }
}
