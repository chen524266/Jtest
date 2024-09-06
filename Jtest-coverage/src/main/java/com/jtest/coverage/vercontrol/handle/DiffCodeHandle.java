package com.jtest.coverage.vercontrol.handle;

import com.Jtest.common.utils.uuid.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtest.coverage.errorcode.BizCode;
import com.jtest.coverage.exception.BaseException;
import com.jtest.coverage.server.domain.CoverageEnvConfig;
import com.jtest.coverage.vercontrol.ProcessHandle;
import com.jtest.coverage.vercontrol.VersionControlHandlerFactory;
import com.jtest.coverage.vercontrol.dto.DiffInfo;
import com.jtest.coverage.vercontrol.dto.DiffMethodParams;
import com.jtest.coverage.vercontrol.dto.VersionControlDto;
import com.jtest.coverage.vercontrol.enums.CodeManageTypeEnum;
import com.jtest.coverage.vercontrol.enums.CoverageType;
import com.jtest.coverage.vercontrol.util.FileUtils;
import com.jtest.coverage.vercontrol.util.FormatUtil;
import com.jtest.coverage.vercontrol.utils.mapper.OrikaMapperUtils;
import org.apache.commons.lang3.StringUtils;

public class DiffCodeHandle extends ProcessHandle {
    public DiffCodeHandle(CoverageEnvConfig envConfig) {
        super(envConfig);
        this.setNext(new JacocoDumpHandle(envConfig));
    }

    @Override
    public void process(CoverageEnvConfig envConfig) {
        if (StringUtils.isNotEmpty(envConfig.getCoverageType()) && envConfig.getCoverageType().equals(CoverageType.INCREAMENT.getValue())) {
            CodeManageTypeEnum codeManageTypeEnum = envConfig.getRepositoryUrl().endsWith(".git") ? CodeManageTypeEnum.GIT : CodeManageTypeEnum.SVN;
            DiffMethodParams diffMethodParams = DiffMethodParams.builder()
                    .repoUrl(StringUtils.trim(envConfig.getRepositoryUrl()))
                    .baseVersion(StringUtils.trim(envConfig.getVersionStart()))
                    .nowVersion(StringUtils.trim(envConfig.getVersionNow()))
                    .codeManageTypeEnum(codeManageTypeEnum)
                    .build();
            VersionControlDto dto = OrikaMapperUtils.map(diffMethodParams, VersionControlDto.class);
            DiffInfo diffInfo = VersionControlHandlerFactory.processHandler(dto);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String diffCodeFile = baseDiffCodeDir + UUID.fastUUID().toString() + ".json";
                FileUtils.setFileText(diffCodeFile, FormatUtil.formatJson(objectMapper.writeValueAsString(diffInfo.getDiffClasses())));
                envConfig.setDiffCodeFile(diffCodeFile);
            } catch (JsonProcessingException e) {
                throw new BaseException(BizCode.GET_DIFF_CLASS_ERROR);
            }
        }
        this.getNext().process(envConfig);
    }
}
