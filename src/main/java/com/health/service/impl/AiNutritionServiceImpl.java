package com.health.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.common.PageResult;
import com.health.entity.AiAnalysisLog;
import com.health.entity.FoodRecord;
import com.health.entity.User;
import com.health.mapper.AiAnalysisLogMapper;
import com.health.mapper.FoodRecordMapper;
import com.health.mapper.UserMapper;
import com.health.service.AiNutritionService;
import com.health.vo.AiAnalyzeDietVO;
import com.health.vo.AiAnalyzeManualVO;
import com.health.vo.NutritionSummaryVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AI 营养分析服务实现类
 *
 * 配置 DEEPSEEK_API_KEY 环境变量后自动使用 DeepSeek API，
 * 未配置时使用内置 Mock 数据作为降级方案。
 * 使用 deepseek-chat 模型，避免 reasoner 模型的 thinking 模式问题。
 */
@Service
public class AiNutritionServiceImpl implements AiNutritionService {

    /** 模拟食物营养数据库 */
    private static final Map<String, MockNutrition> MOCK_DATA = Map.of(
            "鸡胸肉", new MockNutrition(165.0, 31.0, 0.0, 3.6,
                    "鸡胸肉是优质高蛋白低脂肪食物，适合减脂期食用。",
                    List.of(
                            Map.of("name", "胆固醇", "val", "85", "unit", "mg"),
                            Map.of("name", "钠", "val", "74", "unit", "mg"),
                            Map.of("name", "钾", "val", "256", "unit", "mg")
                    )),
            "米饭", new MockNutrition(200.0, 4.0, 45.0, 0.5,
                    "米饭是日常主食，提供充足碳水，建议搭配蛋白质和蔬菜。",
                    List.of(
                            Map.of("name", "钠", "val", "2", "unit", "mg"),
                            Map.of("name", "钾", "val", "30", "unit", "mg")
                    )),
            "苹果", new MockNutrition(95.0, 0.5, 25.0, 0.3,
                    "苹果富含维生素和膳食纤维，适合加餐食用。",
                    List.of(
                            Map.of("name", "维生素C", "val", "8", "unit", "mg"),
                            Map.of("name", "钾", "val", "195", "unit", "mg")
                    )),
            "宫保鸡丁盖饭", new MockNutrition(650.0, 25.0, 80.0, 22.0,
                    "宫保鸡丁盖饭热量较高，建议控制摄入量，搭配蔬菜均衡营养。",
                    List.of(
                            Map.of("name", "钠", "val", "1200", "unit", "mg"),
                            Map.of("name", "胆固醇", "val", "65", "unit", "mg")
                    ))
    );

    private static final MockNutrition DEFAULT_NUTRITION = new MockNutrition(
            300.0, 10.0, 40.0, 10.0,
            "该食物营养数据仅供参考，建议结合专业营养指南合理搭配饮食。",
            List.of(
                    Map.of("name", "钠", "val", "100", "unit", "mg"),
                    Map.of("name", "钾", "val", "100", "unit", "mg")
            )
    );

    private static final Map<String, String> MEAL_NAMES = Map.of(
            "breakfast", "早餐", "lunch", "午餐", "dinner", "晚餐", "snack", "加餐"
    );

    private final AiAnalysisLogMapper aiAnalysisLogMapper;
    private final FoodRecordMapper foodRecordMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.model:deepseek-chat}")
    private String defaultModel;

    @Value("${deepseek.url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    /** 是否使用真实 API */
    private boolean useRealApi() {
        return apiKey != null && !apiKey.isEmpty();
    }

    /** 将前端模型名映射为 API 模型 ID */
    private String resolveModel(String modelFromFront) {
        if (modelFromFront == null || modelFromFront.isEmpty()) return defaultModel;
        return switch (modelFromFront) {
            case "DeepSeek-R1" -> "deepseek-reasoner";
            case "DeepSeek-V3" -> "deepseek-chat";
            default -> defaultModel;
        };
    }

    public AiNutritionServiceImpl(AiAnalysisLogMapper aiAnalysisLogMapper,
                                  FoodRecordMapper foodRecordMapper,
                                  UserMapper userMapper,
                                  ObjectMapper objectMapper) {
        this.aiAnalysisLogMapper = aiAnalysisLogMapper;
        this.foodRecordMapper = foodRecordMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public AiAnalyzeManualVO analyzeManual(Long userId, String foodName, String model) {
        if (useRealApi()) {
            return analyzeManualWithApi(userId, foodName, resolveModel(model));
        }
        return analyzeManualWithMock(userId, foodName);
    }

    @Override
    public AiAnalyzeDietVO analyzeDiet(Long userId, LocalDate date, String model) {
        if (useRealApi()) {
            return analyzeDietWithApi(userId, date, resolveModel(model));
        }
        return analyzeDietWithMock(userId, date);
    }

    // ==================== DeepSeek API 实现 ====================

    /**
     * 调用 DeepSeek API 分析食物营养
     */
    private AiAnalyzeManualVO analyzeManualWithApi(Long userId, String foodName, String apiModel) {
        User user = userMapper.selectById(userId);
        String userContext = buildUserContext(user);

        String prompt = "你是一个熟悉中国人饮食习惯的专业营养师。\n" +
                "第一步：判断用户输入的是否是食物/食材。如果不是食物（如\"桌子\"、\"电脑\"、\"汽车\"等），返回：\n" +
                "{\"isFood\": false, \"message\": \"「xx」不是食物，请输入食物名称\"}\n" +
                "第二步：如果是食物，用户可能只输入了食物名称没有重量，此时按成年人一次常规摄入量估算（肉类约150g、蔬菜约200g、水果约200g、主食约150g），补全份量信息后分析。\n" +
                userContext +
                "用户输入：" + foodName + "\n" +
                "返回严格的JSON格式（不要markdown代码块）：\n" +
                "{\"isFood\": true, \"foodName\": \"补全后的食物名称\", \"calories\": 数值, \"protein\": 数值, \"carbs\": 数值, \"fat\": 数值, " +
                "\"weight\": \"估算份量\", \"advice\": \"结合用户健康目标给出接地气的饮食建议（约50个汉字），推荐贴近中国人日常饮食习惯的搭配方式\", " +
                "\"details\": [{\"name\": \"微量元素名\", \"val\": \"数值\", \"unit\": \"单位\"}]}";

        Map<String, Object> result = callDeepSeek(prompt, apiModel);

        // 检查是否为食物
        if (Boolean.FALSE.equals(result.get("isFood"))) {
            AiAnalyzeManualVO vo = new AiAnalyzeManualVO();
            vo.setIsFood(false);
            vo.setMessage(result.get("message") != null ? result.get("message").toString() : "非食物，请重新输入");
            return vo;
        }

        AiAnalyzeManualVO vo = parseManualResult(result, foodName);

        // 保存分析日志（用原始输入作为 foodName，保证去重准确）
        AiAnalysisLog log = saveManualLog(userId, foodName,
                vo.getCalories(), vo.getProtein(), vo.getCarbs(), vo.getFat(),
                vo.getAdvice(), vo.getDetails());
        vo.setId(log.getId());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }

    /**
     * 调用 DeepSeek API 分析饮食记录
     */
    private AiAnalyzeDietVO analyzeDietWithApi(Long userId, LocalDate date, String apiModel) {
        List<FoodRecord> records = foodRecordMapper.getRecordsByDate(userId, date);
        NutritionSummaryVO summary = calculateSummary(records);
        User user = userMapper.selectById(userId);
        String userContext = buildUserContext(user);

        // 按餐次分组
        Map<String, List<String>> mealFoods = new LinkedHashMap<>();
        Map<String, Double> mealCals = new LinkedHashMap<>();
        String[] allMeals = {"breakfast", "lunch", "dinner", "snack"};
        for (FoodRecord r : records) {
            String mt = r.getMealType() != null ? r.getMealType() : "snack";
            mealFoods.computeIfAbsent(mt, k -> new ArrayList<>())
                    .add(r.getFoodName() + "(" + (r.getCalories() != null ? r.getCalories().intValue() : 0) + "kcal)");
            mealCals.merge(mt, r.getCalories() != null ? r.getCalories() : 0.0, Double::sum);
        }

        // 构建已记录餐次描述
        StringBuilder recorded = new StringBuilder();
        Set<String> presentMeals = new LinkedHashSet<>();
        for (String mt : allMeals) {
            if (mealFoods.containsKey(mt)) {
                presentMeals.add(mt);
                recorded.append("·").append(MEAL_NAMES.get(mt)).append("：");
                recorded.append(String.join("、", mealFoods.get(mt)));
                recorded.append("，共").append(mealCals.get(mt).intValue()).append("kcal\n");
            }
        }

        // 构建缺失餐次列表
        java.util.List<String> missingMeals = new ArrayList<>();
        for (String mt : allMeals) {
            if (!mealFoods.containsKey(mt)) {
                missingMeals.add(MEAL_NAMES.get(mt));
            }
        }

        String scope;
        if (presentMeals.isEmpty()) {
            scope = "用户今日暂无饮食记录。";
        } else if (missingMeals.isEmpty()) {
            scope = "用户今日三餐+加餐已全部记录，具体如下：\n" + recorded;
        } else {
            scope = "用户今日仅记录了部分餐次：\n" + recorded
                    + "尚未记录的餐次：" + String.join("、", missingMeals) + "。";
        }

        String prompt = "你是一个熟悉中国人饮食习惯的专业营养师。" + userContext +
                scope +
                "已摄入营养汇总：总热量" + summary.getTotalCal() + "千卡，蛋白质" + summary.getTotalProtein()
                + "g，碳水" + summary.getTotalCarbs() + "g，脂肪" + summary.getTotalFat() + "g。" +
                "请用贴近中国人日常饮食的方式进行评价和建议，多推荐家常菜、中式食材（如豆腐、鸡蛋、青菜、鱼、瘦肉、杂粮饭、粥、面条等），" +
                "而非西式健身餐（鸡胸肉沙拉、蛋白粉等）。" +
                (missingMeals.isEmpty() && !presentMeals.isEmpty()
                        ? "请评价今日整体饮食质量，返回严格的JSON格式（不要markdown代码块）：\n"
                        : "请先评价已记录餐次的营养质量，然后对尚未记录的餐次给出具体的饮食建议，帮助用户合理搭配。返回严格的JSON格式（不要markdown代码块）：\n") +
                "{\"score\": \"优秀/良好/一般/较差\", \"overallEval\": \"综合评价\", " +
                "\"pros\": [\"优点1\", \"优点2\"], \"suggestions\": [\"建议1\", \"建议2\"]}";

        Map<String, Object> result = callDeepSeek(prompt, apiModel);
        return buildDietVOWithoutLog(date, summary, result);
    }

    /**
     * 调用 DeepSeek API
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callDeepSeek(String prompt, String apiModel) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", apiModel);
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("max_tokens", 2000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(apiUrl, request, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("DeepSeek API 调用失败: " + e.getMessage());
        }

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new RuntimeException("DeepSeek API 返回为空");
        }
        // 检查 API 错误（如 API Key 无效）
        if (body.containsKey("error")) {
            Object err = body.get("error");
            throw new RuntimeException("DeepSeek API 错误: " + err);
        }
        if (!body.containsKey("choices")) {
            throw new RuntimeException("DeepSeek API 返回异常: " + body);
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
        if (choices.isEmpty()) {
            throw new RuntimeException("DeepSeek API 返回空结果");
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");
        if (content == null || content.isEmpty()) {
            throw new RuntimeException("DeepSeek 返回内容为空");
        }

        // 提取 JSON（去掉可能的 markdown 代码块包裹）
        String json = content.trim();
        if (json.startsWith("```")) {
            int start = json.indexOf("\n");
            if (start > 0) {
                json = json.substring(start + 1);
                int end = json.lastIndexOf("```");
                if (end > 0) json = json.substring(0, end);
                json = json.trim();
            }
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("解析 DeepSeek 响应失败，原始内容: " + content.substring(0, Math.min(200, content.length())));
        }
    }

    /**
     * 解析手动分析 API 返回结果
     */
    @SuppressWarnings("unchecked")
    private AiAnalyzeManualVO parseManualResult(Map<String, Object> result, String foodName) {
        AiAnalyzeManualVO vo = new AiAnalyzeManualVO();
        vo.setIsFood(true);
        vo.setFoodName(result.containsKey("foodName") ? getString(result, "foodName", foodName) : foodName);
        vo.setWeight(getString(result, "weight", "100g"));
        vo.setCalories(getDouble(result, "calories", 0.0));
        vo.setProtein(getDouble(result, "protein", 0.0));
        vo.setCarbs(getDouble(result, "carbs", 0.0));
        vo.setFat(getDouble(result, "fat", 0.0));
        vo.setAdvice(getString(result, "advice", ""));
        if (result.containsKey("details")) {
            try {
                List<Map<String, String>> details = (List<Map<String, String>>) result.get("details");
                vo.setDetails(details);
            } catch (Exception ignored) {}
        }
        return vo;
    }

    /**
     * 构建饮食分析 VO
     */
    @SuppressWarnings("unchecked")
    private AiAnalyzeDietVO buildDietVOWithoutLog(LocalDate date, NutritionSummaryVO summary, Map<String, Object> result) {
        String score = getString(result, "score", "良好");
        String overallEval = getString(result, "overallEval", "");
        List<String> pros = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        if (result.containsKey("pros")) pros = (List<String>) result.get("pros");
        if (result.containsKey("suggestions")) suggestions = (List<String>) result.get("suggestions");

        AiAnalyzeDietVO vo = new AiAnalyzeDietVO();
        vo.setDate(date);
        vo.setNutritionSummary(summary);
        vo.setScore(score);
        vo.setOverallEval(overallEval);
        vo.setPros(pros);
        vo.setSuggestions(suggestions);
        return vo;
    }

    // ==================== Mock 实现（降级方案） ====================

    private AiAnalyzeManualVO analyzeManualWithMock(Long userId, String foodName) {
        MockNutrition nutrition = MOCK_DATA.getOrDefault(foodName, DEFAULT_NUTRITION);

        AiAnalyzeManualVO vo = new AiAnalyzeManualVO();
        vo.setFoodName(foodName);
        vo.setWeight("100g");
        vo.setCalories(nutrition.calories());
        vo.setProtein(nutrition.protein());
        vo.setCarbs(nutrition.carbs());
        vo.setFat(nutrition.fat());
        vo.setDetails(nutrition.details());
        vo.setAdvice(nutrition.advice());

        AiAnalysisLog log = saveManualLog(userId, foodName,
                nutrition.calories(), nutrition.protein(), nutrition.carbs(), nutrition.fat(),
                nutrition.advice(), nutrition.details());
        vo.setId(log.getId());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }

    private AiAnalyzeDietVO analyzeDietWithMock(Long userId, LocalDate date) {
        List<FoodRecord> records = foodRecordMapper.getRecordsByDate(userId, date);
        NutritionSummaryVO summary = calculateSummary(records);

        // 检查缺失餐次
        Set<String> presentMeals = new java.util.HashSet<>();
        for (FoodRecord r : records) {
            if (r.getMealType() != null) presentMeals.add(r.getMealType());
        }
        String[] allMeals = {"breakfast", "lunch", "dinner", "snack"};
        Map<String, String> mealNames = Map.of(
                "breakfast", "早餐", "lunch", "午餐", "dinner", "晚餐", "snack", "加餐"
        );
        java.util.List<String> missing = new ArrayList<>();
        for (String mt : allMeals) {
            if (!presentMeals.contains(mt)) missing.add(MEAL_NAMES.get(mt));
        }
        boolean incomplete = !missing.isEmpty() && !presentMeals.isEmpty();

        String score = summary.getTotalCal() > 2000 ? "一般" : summary.getTotalCal() > 1200 ? "良好" : "优秀";
        String overallEval;
        if (summary.getTotalCal() == 0) {
            overallEval = "今日暂无饮食记录。";
        } else if (incomplete) {
            overallEval = "已记录的餐次搭配不错，但" + String.join("、", missing) + "尚未记录，记得按时就餐补充营养。";
        } else {
            overallEval = "今日饮食搭配较为均衡，蛋白质摄入充足。";
        }
        List<String> pros = summary.getTotalCal() > 0
                ? List.of("蛋白质来源优质", "脂肪摄入合理")
                : List.of();
        List<String> suggestions = new ArrayList<>();
        if (summary.getTotalCal() > 2000) {
            suggestions.add("总热量偏高，建议适当减少高热量食物摄入");
        } else if (summary.getTotalCal() > 0) {
            suggestions.add("碳水化合物摄入稍低，可适当增加粗粮");
            suggestions.add("水果摄入不足，建议加餐补充");
        }
        if (incomplete) {
            suggestions.add("记得补充" + String.join("、", missing) + "，保持三餐规律");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("暂无记录，请先添加饮食记录");
        }

        AiAnalyzeDietVO vo = new AiAnalyzeDietVO();
        vo.setDate(date);
        vo.setNutritionSummary(summary);
        vo.setScore(score);
        vo.setOverallEval(overallEval);
        vo.setPros(pros);
        vo.setSuggestions(suggestions);
        return vo;
    }

    // ==================== 公共方法 ====================

    @Override
    public PageResult<AiAnalyzeManualVO> getHistory(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<AiAnalysisLog> logs = aiAnalysisLogMapper.selectPageByUserId(userId, offset, pageSize);
        int total = aiAnalysisLogMapper.countByUserId(userId);

        List<AiAnalyzeManualVO> list = new ArrayList<>();
        for (AiAnalysisLog log : logs) {
            AiAnalyzeManualVO vo = new AiAnalyzeManualVO();
            vo.setId(log.getId());
            vo.setFoodName(log.getFoodName());
            vo.setCalories(log.getCalories());
            vo.setProtein(log.getProtein());
            vo.setCarbs(log.getCarbs());
            vo.setFat(log.getFat());
            vo.setAdvice(log.getAdvice());
            vo.setImageUrl(log.getImageUrl());
            vo.setCreateTime(log.getCreateTime());
            if (log.getDetails() != null) {
                try {
                    List<Map<String, String>> details = objectMapper.readValue(
                            log.getDetails(), new TypeReference<List<Map<String, String>>>() {});
                    vo.setDetails(details);
                } catch (Exception ignored) {}
            }
            list.add(vo);
        }

        return new PageResult<>(list, total, page, pageSize);
    }

    @Override
    public void deleteById(Long userId, Long id) {
        AiAnalysisLog log = aiAnalysisLogMapper.selectById(id);
        if (log == null) throw new RuntimeException("分析记录不存在");
        if (!log.getUserId().equals(userId)) throw new RuntimeException("无权删除他人的分析记录");
        aiAnalysisLogMapper.deleteById(id);
    }

    @Override
    public String generateProfileTip(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return "请先完善个人信息，AI 将为您生成个性化饮食建议。";

        if (useRealApi()) {
            return generateProfileTipWithApi(user);
        }
        return generateProfileTipMock(user);
    }

    @Override
    public void clearHistory(Long userId) {
        aiAnalysisLogMapper.deleteByUserId(userId);
    }

    // ==================== 辅助方法 ====================

    private String buildUserContext(User user) {
        if (user == null) return "";
        StringBuilder sb = new StringBuilder();
        if (user.getHeight() != null) sb.append("用户身高").append(user.getHeight()).append("cm，");
        if (user.getWeight() != null) sb.append("体重").append(user.getWeight()).append("kg，");
        if (user.getAge() != null) {
            sb.append("年龄").append(user.getAge()).append("岁（").append(getAgeGroup(user.getAge())).append("），");
        }
        if (user.getGoal() != null && !user.getGoal().isEmpty()) {
            String goalName = switch (user.getGoal()) {
                case "lose" -> "减脂";
                case "gain" -> "增肌";
                default -> "保持体重";
            };
            sb.append("健康目标为").append(goalName).append("，");
        }
        if (user.getGender() != null && user.getGender() != 0) {
            sb.append("性别为").append(user.getGender() == 1 ? "男" : "女").append("，");
        }
        if (sb.length() == 0) return "";
        return sb.append("请根据用户年龄阶段和身体状况给出个性化饮食建议。").toString();
    }

    private static String getAgeGroup(Integer age) {
        if (age == null || age < 18) return "未知";
        if (age <= 25) return "成长期";
        if (age <= 35) return "青年";
        if (age <= 45) return "壮年";
        return "中老年";
    }

    private String generateProfileTipWithApi(User user) {
        String context = buildUserContext(user);
        String prompt = "你是一个熟悉中国人饮食习惯的专业营养师。根据以下用户信息，用1-2句话给出接地气的个性化饮食建议。" + context
                + "建议要贴近中国人日常饮食，推荐家常菜和中式食材（如豆腐、鸡蛋、绿叶菜、清蒸鱼、杂粮粥等），避免西式健身餐建议。" +
                "返回严格的JSON格式（不要markdown代码块）：\n{\"tip\": \"你的个性化建议\"}";

        Map<String, Object> result = callDeepSeek(prompt, defaultModel.isEmpty() ? "deepseek-chat" : defaultModel);
        return getString(result, "tip", "根据您的身体状况，建议均衡饮食，保持适量运动，定期关注健康指标变化。");
    }

    private String generateProfileTipMock(User user) {
        String ageGroup = getAgeGroup(user.getAge());
        String goal = user.getGoal() != null ? user.getGoal() : "maintain";
        String genderStr = user.getGender() != null && user.getGender() == 1 ? "男性" :
                user.getGender() != null && user.getGender() == 2 ? "女性" : "用户";

        Map<String, String> goalName = Map.of("lose", "减脂", "gain", "增肌", "maintain", "保持体重");
        String goalCN = goalName.getOrDefault(goal, "保持体重");

        Map<String, String> tips = new HashMap<>();
        tips.put("成长期_lose", "成长期代谢旺盛，减脂多吃家常菜即可。建议早餐豆浆配包子，午晚餐一荤一素一汤，少喝奶茶少吃炸鸡，每周打球跑步3-4次。");
        tips.put("成长期_gain", "成长期是增肌黄金窗口，多吃鸡蛋、瘦肉、鱼虾、豆腐，饭后加杯牛奶，保证每天8小时睡眠，搭配力量训练。");
        tips.put("成长期_maintain", "成长期代谢快是天然优势，保持三餐规律，少吃外卖和烧烤，多吃蔬菜水果，养成运动习惯就能维持好身材。");
        tips.put("青年_lose", "青年期工作忙易发胖。建议早餐粥+蛋，午晚餐米饭减半、多吃青菜瘦肉，戒掉宵夜和啤酒，每周快走或跑步3次。");
        tips.put("青年_gain", "青年增肌效果好，每天保证两个蛋、三两瘦肉或鱼虾，饭后加根香蕉或一杯豆浆，力量训练后补充一盒纯牛奶即可。");
        tips.put("青年_maintain", "青年保持体型关键是少喝奶茶少吃零食，尽量自己做饭少点外卖，每周运动2-3次，作息规律不熬夜。");
        tips.put("壮年_lose", "壮年代谢放缓，内脏脂肪易堆积。建议白米饭换成杂粮饭，多吃清蒸鱼、白灼菜，少吃红烧类、油炸类，每周快走4次以上。");
        tips.put("壮年_gain", "壮年增肌需注重训练效率，每餐保证有肉或蛋或豆腐，多吃牛肉、鸡蛋、豆制品，注意关节保护，热身拉伸要做足。");
        tips.put("壮年_maintain", "壮年维持体重建议半年称一次体脂，三餐规律少吃应酬饭，多自己下厨控制油盐，每周保证2-3次力量训练对抗肌肉流失。");
        tips.put("中老年_lose", "中老年减脂宜温和不宜激进。饮食以清蒸、水煮、炖汤为主，少吃红烧和油炸，多吃蔬菜和粗粮，每天散步或打太极40分钟。");
        tips.put("中老年_gain", "中老年增肌以防肌肉流失为目标，每天保证一个蛋、一杯奶、二两瘦肉或鱼虾，搭配弹力带或轻哑铃训练，多晒太阳补钙。");
        tips.put("中老年_maintain", "中老年保持体重需定期体检关注血脂骨密度。饮食清淡少盐少油，多吃当季蔬菜和杂粮粥，每天坚持散步或广场舞。");

        String key = ageGroup + "_" + goal;
        return tips.getOrDefault(key, genderStr + "建议均衡饮食，" + goalCN + "期间注意营养搭配，保持适量运动，定期关注健康指标变化。");
    }

    private NutritionSummaryVO calculateSummary(List<FoodRecord> records) {
        double totalCal = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;
        for (FoodRecord r : records) {
            totalCal += Optional.ofNullable(r.getCalories()).orElse(0.0);
            totalProtein += Optional.ofNullable(r.getProtein()).orElse(0.0);
            totalCarbs += Optional.ofNullable(r.getCarbs()).orElse(0.0);
            totalFat += Optional.ofNullable(r.getFat()).orElse(0.0);
        }
        NutritionSummaryVO summary = new NutritionSummaryVO();
        summary.setTotalCal(totalCal);
        summary.setTotalProtein(totalProtein);
        summary.setTotalCarbs(totalCarbs);
        summary.setTotalFat(totalFat);
        return summary;
    }

    private AiAnalysisLog saveManualLog(Long userId, String foodName,
                                         Double cal, Double pro, Double carb, Double fat,
                                         String advice, List<Map<String, String>> details) {
        // 查重：同用户同食物名则覆盖更新
        AiAnalysisLog log = aiAnalysisLogMapper.findByUserIdAndFoodName(userId, foodName);
        if (log != null) {
            log.setCalories(cal);
            log.setProtein(pro);
            log.setCarbs(carb);
            log.setFat(fat);
            log.setAdvice(advice);
            try {
                log.setDetails(objectMapper.writeValueAsString(details));
            } catch (Exception ignored) {}
            log.setCreateTime(LocalDateTime.now());
            aiAnalysisLogMapper.updateById(log);
            return log;
        }
        log = new AiAnalysisLog();
        log.setUserId(userId);
        log.setFoodName(foodName);
        log.setAnalysisType("manual");
        log.setCalories(cal);
        log.setProtein(pro);
        log.setCarbs(carb);
        log.setFat(fat);
        log.setAdvice(advice);
        try {
            log.setDetails(objectMapper.writeValueAsString(details));
        } catch (Exception ignored) {}
        log.setCreateTime(LocalDateTime.now());
        aiAnalysisLogMapper.insert(log);
        return log;
    }

    private String getString(Map<String, Object> map, String key, String def) {
        return map.containsKey(key) ? map.get(key).toString() : def;
    }

    private Double getDouble(Map<String, Object> map, String key, Double def) {
        if (map.containsKey(key)) {
            Object v = map.get(key);
            if (v instanceof Number) return ((Number) v).doubleValue();
            try { return Double.parseDouble(v.toString()); } catch (Exception ignored) {}
        }
        return def;
    }

    private record MockNutrition(Double calories, Double protein, Double carbs, Double fat,
                                 String advice, List<Map<String, String>> details) {}
}
