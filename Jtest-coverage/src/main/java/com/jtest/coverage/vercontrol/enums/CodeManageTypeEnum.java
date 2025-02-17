package com.jtest.coverage.vercontrol.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author rui.duan
 * @version 1.0
 * @className WarehousingReceiptStatusEnum
 * @description 代码管理工具枚举
 * @date 2019-06-10 11:39
 */
@Getter
@AllArgsConstructor
public enum  CodeManageTypeEnum {
    //待通知
    GIT(0,"git"),

    SVN(1,"svn"),;

    private Integer code;
    private String value;
    

    /**
     * 根据code获取值
     * @param code
     * @return
     */
    public static String getValueByCode(Integer code) {
        CodeManageTypeEnum[] values = CodeManageTypeEnum.values();
        for (CodeManageTypeEnum type : values) {
            if (type.code.equals(code)) {
                return type.value;
            }
        }
        return null;
    }

    /**
     * 根据value获取code
     * @param value
     * @return
     */
    public static Integer getCodeByValue(String value) {
        CodeManageTypeEnum[] values = CodeManageTypeEnum.values();
        for (CodeManageTypeEnum type : values) {
            if (type.value.equalsIgnoreCase(value)) {
                return type.code;
            }
        }
        return null;
    }
}
