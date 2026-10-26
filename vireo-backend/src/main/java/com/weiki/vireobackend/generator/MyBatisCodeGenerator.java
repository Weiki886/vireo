package com.weiki.vireobackend.generator;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

/**
 * MyBatis-Flex 代码生成器
 * 详细配置见：https://mybatis-flex.com/zh/others/codegen.html
 */
public class MyBatisCodeGenerator {

    // 需要生成的表名
    private static final String[] TABLE_NAMES = {"user"};

    /**
     * 读取数据源配置并执行代码生成。
     *
     * @param args 启动参数（未使用）
     */
    public static void main(String[] args) {
        // 获取数据源信息（直接复用 application.yaml，避免维护两份配置）
        Dict dict = YamlUtil.loadByPath("application.yaml");
        Map<String, Object> dataSourceConfig = dict.getByPath("spring.datasource");
        String url = String.valueOf(dataSourceConfig.get("url"));
        String username = String.valueOf(dataSourceConfig.get("username"));
        String password = resolvePlaceholder(String.valueOf(dataSourceConfig.get("password")));

        // 配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // 创建配置内容
        GlobalConfig globalConfig = createGlobalConfig();

        // 通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        // 生成代码
        generator.generate();
    }

    /**
     * 创建代码生成配置：生成包路径、目标表、逻辑删除字段、雪花主键、
     * 以及 entity / mapper / service / controller 各层开关。
     *
     * @return 全局生成配置
     */
    public static GlobalConfig createGlobalConfig() {
        // 创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        // 设置根包，建议先生成到一个临时目录下，生成代码后，再移动到项目目录下
        globalConfig.getPackageConfig()
                .setBasePackage("com.weiki.vireobackend.genresult");

        // 设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setGenerateTable(TABLE_NAMES)
                // 设置逻辑删除的默认字段名称
                .setLogicDeleteColumn("isDelete")
                // user 表主键由应用层雪花算法生成，避免自增 ID 暴露数据规模
                .setColumnConfig("user", ColumnConfig.create()
                        .setColumnName("id")
                        .setPrimaryKey(true)
                        .setKeyType(KeyType.Generator)
                        .setKeyValue(KeyGenerators.snowFlakeId));

        // 设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        // 设置生成 mapper
        globalConfig.enableMapper();
        globalConfig.enableMapperXml();

        // 设置生成 service
        globalConfig.enableService();
        globalConfig.enableServiceImpl();

        // 设置生成 controller
        globalConfig.enableController();

        // 设置作者和生成时间，避免多余的代码改动
        globalConfig.getJavadocConfig()
                .setAuthor("<a href=\"https://github.com/Weiki886\">weiki</a>")
                .setSince("");
        return globalConfig;
    }

    /**
     * 解析 Spring 占位符配置：application.yaml 中 password 写为 ${DB_PASSWORD:123456} 时，
     * 优先取环境变量 DB_PASSWORD，未设置时使用默认值 123456
     */
    private static String resolvePlaceholder(String value) {
        if (value != null && value.startsWith("${") && value.endsWith("}")) {
            String inner = value.substring(2, value.length() - 1);
            String[] parts = inner.split(":", 2);
            String envValue = System.getenv(parts[0]);
            return envValue != null ? envValue : (parts.length > 1 ? parts[1] : "");
        }
        return value;
    }
}
