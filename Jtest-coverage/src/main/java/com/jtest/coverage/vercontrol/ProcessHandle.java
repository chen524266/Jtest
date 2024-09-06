package com.jtest.coverage.vercontrol;

import com.jtest.coverage.server.domain.CoverageEnvConfig;
import lombok.Data;
import java.io.File;

@Data
public abstract class ProcessHandle {

    private ProcessHandle next;

    private CoverageEnvConfig envConfig;

    public static String basePath = System.getProperty("user.dir") + File.separator;

    public static String baseCodeCloneDir = basePath + "code_source" + File.separator;

    public static String baseReportDir =basePath+ "jacoco_report" + File.separator;

    public static String baseExecDir = basePath+"jacoco_exec" + File.separator;

    public static String baseDiffCodeDir = basePath+"diff_code" + File.separator;

    public ProcessHandle(CoverageEnvConfig envConfig) {
        this.envConfig = envConfig;
    }

    public abstract void process(CoverageEnvConfig envConfig);

}
