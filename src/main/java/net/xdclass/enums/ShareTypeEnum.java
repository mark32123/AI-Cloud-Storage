package net.xdclass.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum ShareTypeEnum {

    /**
     * 无码
     */
    NO_CODE,

    /**
     * 需要码
     */
    NEED_CODE;

    @JsonCreator
    public static ShareTypeEnum fromValue(String value) {
        if (value == null) {
            return null;
        }
        // 忽略大小写匹配
        for (ShareTypeEnum type : ShareTypeEnum.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
