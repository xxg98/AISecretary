package com.kailei.aisecretary.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * MyBatis-Plus Mapper 自动生成工具
 * 扫描entity包，自动生成对应Mapper接口
 */
@Slf4j
@Component
public class MapperGeneratorUtil {

    // ====================== 配置项（可根据项目修改）======================
    // 实体类包路径
    private static final String ENTITY_PACKAGE = "com.kailei.aisecretary.entity";
    // Mapper接口包路径
    private static final String MAPPER_PACKAGE = "com.kailei.aisecretary.mapper";
    // Mapper文件前缀（IXXXMapper）
    private static final String MAPPER_PREFIX = "I";
    // Mapper文件后缀
    private static final String MAPPER_SUFFIX = "Mapper";
    // 实体类后缀（Entity）
    private static final String ENTITY_SUFFIX = "Entity";
    // ==================================================================

    /**
     * 执行自动生成
     */
    public void generateMappers() {
        try {
            // 1. 获取【源码目录】的实体类包
            File entityDir = getSourcePackageDir(ENTITY_PACKAGE);
            log.info("正在扫描实体目录：{}", entityDir.getAbsolutePath());

            if (!entityDir.exists()) {
                log.error("实体目录不存在！路径：{}", entityDir.getAbsolutePath());
                return;
            }

            File[] entityFiles = entityDir.listFiles((dir, name) -> name.endsWith(".java"));

            if (entityFiles == null || entityFiles.length == 0) {
                log.info("实体类包下没有找到任何实体文件");
                return;
            }

            // 2. 获取Mapper包目录
            File mapperDir = getSourcePackageDir(MAPPER_PACKAGE);
            if (!mapperDir.exists()) {
                mapperDir.mkdirs();
                log.info("创建Mapper目录：{}", mapperDir.getAbsolutePath());
            }

            // 3. 遍历实体类，生成对应Mapper
            for (File entityFile : entityFiles) {
                String entityFileName = entityFile.getName().replace(".java", "");
                generateSingleMapper(entityFileName, mapperDir);
            }

            log.info("✅ Mapper自动生成完成！");

        } catch (Exception e) {
            log.error("❌ Mapper生成失败：", e);
        }
    }

    /**
     * 生成单个Mapper文件
     */
    private void generateSingleMapper(String entityFileName, File mapperDir) throws IOException {
        String entityName = entityFileName.replace(ENTITY_SUFFIX, "");
        String mapperName = MAPPER_PREFIX + entityName + MAPPER_SUFFIX;
        File mapperFile = new File(mapperDir, mapperName + ".java");

        if (mapperFile.exists()) {
            log.info("⏭️ {} 已存在，跳过生成", mapperName);
            return;
        }

        String mapperContent = buildMapperContent(entityFileName, mapperName);

        try (FileWriter writer = new FileWriter(mapperFile)) {
            writer.write(mapperContent);
            writer.flush();
            log.info("✅ 成功生成：{}", mapperFile.getAbsolutePath());
        }
    }

    /**
     * 构建Mapper文件代码内容
     */
    private String buildMapperContent(String entityFileName, String mapperName) {
        return "package " + MAPPER_PACKAGE + ";\n\n" +
                "import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n" +
                "import " + ENTITY_PACKAGE + "." + entityFileName + ";\n" +
                "import org.apache.ibatis.annotations.Mapper;\n\n" +
                "@Mapper\n" +
                "public interface " + mapperName + " extends BaseMapper<" + entityFileName + "> {\n" +
                "}";
    }

    /**
     * 【修复核心】直接获取 Java 源码目录（绝对不会找不到）
     */
    private File getSourcePackageDir(String packageName) {
        // 项目根目录
        String projectDir = System.getProperty("user.dir");
        // 拼接源码路径
        String packagePath = packageName.replace(".", File.separator);
        String fullPath = projectDir + File.separator + "src/main/java/" + packagePath;
        return new File(fullPath);
    }

}