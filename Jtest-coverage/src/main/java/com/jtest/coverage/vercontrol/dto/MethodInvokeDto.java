package com.jtest.coverage.vercontrol.dto;

import com.jtest.coverage.vercontrol.enums.CodeManageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Package: com.dr.code.diff.dto
 * @Description: java类作用描述
 * @Author: rayduan
 * @CreateDate: 2023/3/8 21:22
 * @Version: 1.0
 * <p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MethodInvokeDto {

    /**
     *  远程仓库地址
     */
    private String repoUrl;

    /**
     * 分支或tag
     */
    private String branchName = "";

    /**
     * 代码下载路径
     */
    private String baseLocalDir;
    /**
     * 版本控制类型
     */
    private CodeManageTypeEnum codeManageTypeEnum;

    private String userName;

    private String passWord;

    private String gitSshPrivateKey;


}
