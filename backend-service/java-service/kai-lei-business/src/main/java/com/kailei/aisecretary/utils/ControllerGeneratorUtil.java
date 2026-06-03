package com.kailei.aisecretary.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 控制台输入中文 → 自动翻译英文 → 生成标准Controller
 * 独立main方法运行，不交给Spring，重名不生成
 */
@Slf4j
public class ControllerGeneratorUtil {

    // ====================== 项目配置 ======================
    private static final String CONTROLLER_PACKAGE = "com.kailei.aisecretary.controller";
    private static final String CONTROLLER_SUFFIX = "Controller";
    private static final String API_PREFIX = "/api/v1/";
    // ======================================================

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 主方法：直接运行
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        log.info("请输入模块中文名称（例如：商品购买）：");
        String chineseName = scanner.nextLine().trim();

        if (chineseName.isEmpty()) {
            log.error("输入不能为空！");
            return;
        }

        try {
            // 1. 翻译
            String englishName = translate(chineseName);
            log.info("翻译结果：{} → {}", chineseName, englishName);

            // 2. 生成规范名称
            String urlPath = toDashPath(englishName); // 多单词自动用 - 连接
            String className = toCamelUpper(englishName) + CONTROLLER_SUFFIX;
            String tagName = chineseName + "接口";

            // 3. 获取目录
            File controllerDir = getPackageDir(CONTROLLER_PACKAGE);
            if (!controllerDir.exists()) {
                controllerDir.mkdirs();
                log.info("创建Controller目录：{}", controllerDir.getAbsolutePath());
            }

            // 4. 检查文件是否已存在 → 存在就跳过
            File targetFile = new File(controllerDir, className + ".java");
            if (targetFile.exists()) {
                log.info("{} 已存在，跳过生成！", className);
                return;
            }

            // 5. 生成代码
            String code = buildCode(className, API_PREFIX + urlPath, tagName);

            // 6. 写入文件
            try (FileWriter writer = new FileWriter(targetFile)) {
                writer.write(code);
            }

            log.info("=================== 生成成功 ===================");
            log.info("Controller类名：{}", className);
            log.info("请求路径：{}", API_PREFIX + urlPath);
            log.info("文件路径：{}", targetFile.getAbsolutePath());
            log.info("==============================================");

        } catch (Exception e) {
            log.error("生成Controller失败", e);
        }
    }

    /**
     * 中文翻译英文（修复URL编码BUG）
     */
    private static String translate(String chinese) throws Exception {
        // 编码关键词
        String encodedText = URLEncoder.encode(chinese, StandardCharsets.UTF_8.name());
        // 关键：| 必须编码为 %7C
        String url = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=zh-CN%7Cen";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode node = objectMapper.readTree(resp.body());
        return node.get("responseData").get("translatedText").asText();
    }

    /**
     * 生成Controller代码
     */
    private static String buildCode(String className, String mapping, String tagName) {
        return "package " + CONTROLLER_PACKAGE + ";\n\n" +
                "import io.swagger.v3.oas.annotations.tags.Tag;\n" +
                "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                "import org.springframework.web.bind.annotation.RestController;\n\n" +
                "@RestController\n" +
                "@RequestMapping(\"" + mapping + "\")\n" +
                "@Tag(name = \"" + tagName + "\")\n" +
                "public class " + className + " {\n\n" +
                "}";
    }

    /**
     * 多单词自动转 - 分隔
     * Product Purchase → product-purchase
     */
    private static String toDashPath(String english) {
        return english.trim().replaceAll("\\s+", "-").toLowerCase();
    }

    /**
     * 转大驼峰类名
     * Product Purchase → ProductPurchase
     */
    private static String toCamelUpper(String english) {
        String[] words = english.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            sb.append(Character.toUpperCase(w.charAt(0)))
                    .append(w.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    /**
     * 获取项目源码包路径
     */
    private static File getPackageDir(String packageName) {
        String projectDir = System.getProperty("user.dir");
        String path = packageName.replace(".", File.separator);
        return new File(projectDir, "src/main/java/" + path);
    }
}