package com.jtest.coverage.vercontrol.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CoverageType {
    FULL(1,"全量"),

    INCREAMENT(2,"增量"),;

    private Integer code;
    private String value;
    

    /**
     * 根据code获取值
     * @param code
     * @return
     */
    public static String getValueByCode(Integer code) {
        CoverageType[] values = CoverageType.values();
        for (CoverageType type : values) {
            if (type.code.equals(code)) {
                return type.value;
            }
        }
        return null;
    }

    /**
     * 根据code获取值
     * @param code
     * @returngetTypeEnumByCode
     */
    public static CoverageType getTypeEnumByCode(Integer code) {
        CoverageType[] values = CoverageType.values();
        for (CoverageType type : values) {
            if (type.code.equals(code)) {
                return type;
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
        CoverageType[] values = CoverageType.values();
        for (CoverageType type : values) {
            if (type.value.equalsIgnoreCase(value)) {
                return type.code;
            }
        }
        return null;
    }
}
