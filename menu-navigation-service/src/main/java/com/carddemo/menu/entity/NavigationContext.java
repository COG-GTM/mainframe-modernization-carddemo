package com.carddemo.menu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * JPA entity representing navigation context, mapping to CARDDEMO-COMMAREA
 * from COCOM01Y.cpy. Preserves from-program/to-program routing context
 * across navigation actions in a session-based model.
 */
@Entity
@Table(name = "navigation_context")
public class NavigationContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 64)
    private String sessionId;

    @Column(name = "user_id", length = 8)
    private String userId;

    @Column(name = "user_type", length = 1)
    private String userType;

    @Column(name = "from_tranid", length = 4)
    private String fromTranid;

    @Column(name = "from_program", length = 8)
    private String fromProgram;

    @Column(name = "to_tranid", length = 4)
    private String toTranid;

    @Column(name = "to_program", length = 8)
    private String toProgram;

    @Column(name = "pgm_context", nullable = false)
    private int pgmContext = 0;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    protected NavigationContext() {
    }

    public NavigationContext(String sessionId) {
        this.sessionId = sessionId;
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getFromTranid() {
        return fromTranid;
    }

    public void setFromTranid(String fromTranid) {
        this.fromTranid = fromTranid;
    }

    public String getFromProgram() {
        return fromProgram;
    }

    public void setFromProgram(String fromProgram) {
        this.fromProgram = fromProgram;
    }

    public String getToTranid() {
        return toTranid;
    }

    public void setToTranid(String toTranid) {
        this.toTranid = toTranid;
    }

    public String getToProgram() {
        return toProgram;
    }

    public void setToProgram(String toProgram) {
        this.toProgram = toProgram;
    }

    public int getPgmContext() {
        return pgmContext;
    }

    public void setPgmContext(int pgmContext) {
        this.pgmContext = pgmContext;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
