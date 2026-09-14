package com.carddemo.acctupdate.service.edit;

import com.carddemo.acctupdate.api.FieldFlag;
import java.util.LinkedHashMap;
import java.util.Map;

public class EditContext {
    private boolean inputError;
    private String returnMsg;
    private final Map<String, FieldFlag> fieldFlags = new LinkedHashMap<>();

    public void flag(String field, FieldFlag flag, String msg) {
        fieldFlags.put(field, flag);
        inputError |= flag != FieldFlag.VALID;
        if (returnMsg == null && msg != null) {
            returnMsg = msg;
        }
    }

    public boolean isInputError() {
        return inputError;
    }

    public String getReturnMsg() {
        return returnMsg;
    }

    public Map<String, FieldFlag> getFieldFlags() {
        return fieldFlags;
    }
}
