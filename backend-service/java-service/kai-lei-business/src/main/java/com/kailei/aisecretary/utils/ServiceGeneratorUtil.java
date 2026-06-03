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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * 控制台输入中文 → 自动翻译英文
 * 自动扫描所有 Mapper → 可多选 → 生成 Service + Impl
 * 使用 @Autowired 自动注入
 */
@Slf4j
public class ServiceGeneratorUtil {

    // ====================== 项目配置 ======================
    private static final String SERVICE_PACKAGE = "com.kailei.aisecretary.service";
    private static final String SERVICE_IMPL_PACKAGE = "com.kailei.aisecretary.service.impl";
    private static final String MAPPER_PACKAGE = "com.kailei.aisecretary.mapper";
    private static final String SERVICE_PREFIX = "I";
    private static final String IMPL_SUFFIX = "ServiceImpl";
    // ======================================================

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        log.info("请输入业务模块中文名称（例如：用户、商品、订单）：");
        String chineseName = scanner.nextLine().trim();
        if (chineseName.isEmpty()) {
            log.error("输入不能为空！");
            return;
        }

        try {
            // 1. 翻译
            String englishName = translate(chineseName);
            log.info("翻译结果：{} → {}", chineseName, englishName);

            // 2. 生成名称
            String upperCamel = toCamelUpper(englishName);
            String serviceInterfaceName = SERVICE_PREFIX + upperCamel + "Service";
            String serviceImplName = upperCamel + IMPL_SUFFIX;

            // 3. 扫描所有 Mapper + 展示选择
            List<String> allMappers = scanAllMappers();
            if (allMappers.isEmpty()) {
                log.warn("未扫描到任何 Mapper！");
            } else {
                log.info("====== 已扫描到 {} 个 Mapper，请输入序号多选（空格分隔）======", allMappers.size());
                for (int i = 0; i < allMappers.size(); i++) {
                    log.info("{} → {}", i + 1, allMappers.get(i));
                }
                log.info("======================================================");
            }

            // 4. 选择 Mapper
            List<String> selectedMappers = new ArrayList<>();
            if (!allMappers.isEmpty()) {
                String selectInput = scanner.nextLine().trim();
                if (!selectInput.isBlank()) {
                    String[] indexArr = selectInput.split("\\s+");
                    for (String indexStr : indexArr) {
                        try {
                            int idx = Integer.parseInt(indexStr) - 1;
                            if (idx >= 0 && idx < allMappers.size()) {
                                selectedMappers.add(allMappers.get(idx));
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
            log.info("已选择 Mapper：{}", selectedMappers);

            // 5. 创建目录
            File serviceDir = getPackageDir(SERVICE_PACKAGE);
            File serviceImplDir = getPackageDir(SERVICE_IMPL_PACKAGE);
            checkAndMkdirs(serviceDir, serviceImplDir);

            // 6. 文件路径
            File serviceFile = new File(serviceDir, serviceInterfaceName + ".java");
            File serviceImplFile = new File(serviceImplDir, serviceImplName + ".java");

            // 7. 重名跳过
            if (serviceFile.exists() || serviceImplFile.exists()) {
                log.info("Service 已存在，跳过生成！");
                return;
            }

            // 8. 生成代码
            String serviceCode = buildServiceCode(serviceInterfaceName);
            String implCode = buildServiceImplCode(serviceInterfaceName, serviceImplName, selectedMappers);

            // 9. 写入文件
            writeFile(serviceFile, serviceCode);
            writeFile(serviceImplFile, implCode);

            log.info("=================== 生成成功 ===================");
            log.info("接口：{}", serviceInterfaceName);
            log.info("实现类：{}", serviceImplName);
            log.info("注入Mapper：{}", selectedMappers);
            log.info("==============================================");

        } catch (Exception e) {
            log.error("生成失败", e);
        }
    }

    /**
     * 【核心】自动扫描 Mapper 包下所有类
     */
    private static List<String> scanAllMappers() throws Exception {
        List<String> mappers = new ArrayList<>();
        String basePath = System.getProperty("user.dir") + "/src/main/java/" + MAPPER_PACKAGE.replace(".", "/");
        Path path = Paths.get(basePath);

        if (!Files.exists(path)) return mappers;

        try (Stream<Path> stream = Files.walk(path)) {
            stream.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(file -> file.getName().endsWith(".java"))
                    .forEach(file -> {
                        String fileName = file.getName();
                        String mapperName = fileName.substring(0, fileName.lastIndexOf("."));
                        mappers.add(mapperName);
                    });
        }
        Collections.sort(mappers);
        return mappers;
    }

    /**
     * 翻译
     */
    private static String translate(String chinese) throws Exception {
        String encoded = URLEncoder.encode(chinese, StandardCharsets.UTF_8);
        String url = "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=zh-CN%7Cen";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = objectMapper.readTree(resp.body());
        return root.get("responseData").get("translatedText").asText();
    }

    /**
     * 大驼峰
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
     * 生成 Service 接口
     */
    private static String buildServiceCode(String interfaceName) {
        return "package " + SERVICE_PACKAGE + ";\n\npublic interface " + interfaceName + " {\n\n}";
    }

    /**
     * 生成 Impl（使用 @Autowired 注入）
     */
    private static String buildServiceImplCode(String interfaceName, String implName, List<String> mappers) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(SERVICE_IMPL_PACKAGE).append(";\n\n");
        sb.append("import ").append(SERVICE_PACKAGE).append(".").append(interfaceName).append(";\n");

        // 导入 Mapper
        for (String mapper : mappers) {
            sb.append("import ").append(MAPPER_PACKAGE).append(".").append(mapper).append(";\n");
        }

        // 导入 @Autowired
        sb.append("import org.springframework.stereotype.Service;\n");
        sb.append("import org.springframework.beans.factory.annotation.Autowired;\n\n");

        sb.append("@Service\n");
        sb.append("public class ").append(implName).append(" implements ").append(interfaceName).append(" {\n\n");

        // 注入 Mapper → @Autowired
        for (String mapper : mappers) {
            String field = Character.toLowerCase(mapper.charAt(0)) + mapper.substring(1);
            sb.append("    @Autowired\n");
            sb.append("    private ").append(mapper).append(" ").append(field).append(";\n\n");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * 获取包目录
     */
    private static File getPackageDir(String packageName) {
        String projectDir = System.getProperty("user.dir");
        String path = packageName.replace(".", File.separator);
        return new File(projectDir, "src/main/java/" + path);
    }

    /**
     * 创建目录
     */
    private static void checkAndMkdirs(File... dirs) {
        for (File dir : dirs) {
            if (!dir.exists()) {
                dir.mkdirs();
                log.info("创建目录：{}", dir.getAbsolutePath());
            }
        }
    }

    /**
     * 写入文件
     */
    private static void writeFile(File file, String content) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}