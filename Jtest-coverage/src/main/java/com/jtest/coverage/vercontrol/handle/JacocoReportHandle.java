package com.jtest.coverage.vercontrol.handle;

import com.Jtest.common.utils.uuid.UUID;
import com.jtest.coverage.errorcode.BizCode;
import com.jtest.coverage.exception.BaseException;
import com.jtest.coverage.server.domain.CoverageEnvConfig;
import com.jtest.coverage.vercontrol.ProcessHandle;
import com.jtest.coverage.vercontrol.enums.CoverageType;
import com.jtest.coverage.vercontrol.util.JacocoCommandUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JacocoReportHandle extends ProcessHandle {
    public JacocoReportHandle(CoverageEnvConfig envConfig) {
        super(envConfig);
    }

    @Override
    public void process(CoverageEnvConfig envConfig) {
        String reportDir = baseReportDir + UUID.fastUUID();
        envConfig.setLastReportDir(reportDir);
        try {
            List<String> args = new ArrayList<>();
            args.add("report");
            args.add(envConfig.getExecFile());
            if (envConfig.getCoverageType().equals(CoverageType.INCREAMENT.getValue())) {
                args.add("--diffCodeFiles");
                args.add(envConfig.getDiffCodeFile());
            }
            args.add("--html");
            args.add(reportDir);
            addpend(args, envConfig.getSourceFiles(), "sourcefiles", envConfig.getNowCodeFile());
            addpend(args, envConfig.getClassFiles(), "classfiles", envConfig.getNowCodeFile());
            String[] array = args.stream().toArray(String[]::new);
            JacocoCommandUtil.execute(array);
        } catch (Exception e) {
            throw new BaseException(BizCode.JACOCO_REPORT_FAIL);
        }
    }

    public static void addpend(List<String> args, String source, String paramterName, String codeFile) {
        String[] sources = source.split(",");
        for (String code : sources) {
            args.add("--" + paramterName);
            args.add(codeFile + File.separator + code);
        }
    }
}
