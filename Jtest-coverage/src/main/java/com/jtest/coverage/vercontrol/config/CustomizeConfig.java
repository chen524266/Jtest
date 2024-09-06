package com.jtest.coverage.vercontrol.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @ProjectName: cmdb
 * @Package: com.dr.cmdb.application.config
 * @Description: 自定义参数
 * @Author: duanrui
 * @CreateDate: 2021/3/18 9:49
 * @Version: 1.0
 * <p>
 * Copyright: Copyright (c) 2021
 */
@Data
@Configuration
public class CustomizeConfig {
    /**
     * maven的地址
     */
    @Value(value = "${maven.home}")
    private String mavenHome;
}
