package com.carddemo.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "transaction_type_category")
@IdClass(TransactionTypeCategory.TrcKey.class)
public class TransactionTypeCategory {
    @Id @Column(name = "trc_type_code", length = 2) private String trcTypeCode;
    @Id @Column(name = "trc_type_category", length = 4) private String trcTypeCategory;
    @Column(name = "trc_cat_data", length = 50) private String trcCatData;
    public TransactionTypeCategory() {}
    public String getTrcTypeCode() { return trcTypeCode; }
    public void setTrcTypeCode(String v) { this.trcTypeCode = v; }
    public String getTrcTypeCategory() { return trcTypeCategory; }
    public void setTrcTypeCategory(String v) { this.trcTypeCategory = v; }
    public String getTrcCatData() { return trcCatData; }
    public void setTrcCatData(String v) { this.trcCatData = v; }

    public static class TrcKey implements Serializable {
        private String trcTypeCode; private String trcTypeCategory;
        public TrcKey() {}
        public TrcKey(String c, String cat) { trcTypeCode=c; trcTypeCategory=cat; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TrcKey k)) return false;
            return Objects.equals(trcTypeCode, k.trcTypeCode) && Objects.equals(trcTypeCategory, k.trcTypeCategory);
        }
        @Override public int hashCode() { return Objects.hash(trcTypeCode, trcTypeCategory); }
    }
}
