package com.jtest.coverage.vercontrol.handle;

import com.Jtest.common.utils.uuid.UUID;
import com.jtest.coverage.errorcode.BizCode;
import com.jtest.coverage.exception.BaseException;
import com.jtest.coverage.server.domain.CoverageEnvConfig;
import com.jtest.coverage.vercontrol.ProcessHandle;
import com.jtest.coverage.vercontrol.util.JacocoCommandUtil;

public class JacocoDumpHandle extends ProcessHandle {
    public JacocoDumpHandle(CoverageEnvConfig envConfig) {
        super(envConfig);
        this.setNext(new JacocoReportHandle(envConfig));
    }

    @Override
    public void process(CoverageEnvConfig envConfig) {
        String execFile = baseExecDir + UUID.fastUUID() + ".exec";
        try {
            JacocoCommandUtil.execute("dump", "--destfile", execFile, "--port", String.valueOf(envConfig.getPort()),
                    "--retry", "1", "--address", envConfig.getIp());
            envConfig.setExecFile(execFile);
        } catch (Exception e) {
            throw new BaseException(BizCode.JACOCO_DUMP_FAIL);
        }
        this.getNext().process(envConfig);
    }
}
