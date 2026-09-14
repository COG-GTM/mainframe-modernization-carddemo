package com.carddemo.acctupdate.service.edit;

import com.carddemo.acctupdate.api.FieldFlag;

public class DateFlags {
    public final FieldFlag year;
    public final FieldFlag month;
    public final FieldFlag day;

    public DateFlags(FieldFlag year, FieldFlag month, FieldFlag day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public boolean isValid() {
        return year == FieldFlag.VALID
            && month == FieldFlag.VALID
            && day == FieldFlag.VALID;
    }
}
