package com.carddemo.acctupdate.service;

import com.carddemo.acctupdate.api.*;
import com.carddemo.acctupdate.domain.*;
import com.carddemo.acctupdate.repository.*;
import com.carddemo.acctupdate.service.edit.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AccountUpdateService {
    private final AccountRepository accounts;
    private final CustomerRepository customers;
    private final CardXrefRepository xrefs;
    private final FieldEditor editor;

    public AccountUpdateService(AccountRepository accounts, CustomerRepository customers, CardXrefRepository xrefs, Clock clock) {
        this.accounts = accounts; this.customers = customers; this.xrefs = xrefs; this.editor = new FieldEditor(clock);
    }

    public AccountUpdateResponse readAccount(String acctIdText) {
        AccountUpdateResponse r = new AccountUpdateResponse();
        if (acctIdText == null || acctIdText.trim().isEmpty()) return error(r, Messages.ACCOUNT_NOT_PROVIDED, ChangeAction.DETAILS_NOT_FETCHED);
        if (!acctIdText.matches("\\d{11}") || acctIdText.matches("0{11}")) return error(r, Messages.ACCOUNT_INVALID, ChangeAction.DETAILS_NOT_FETCHED);
        Long id = Long.valueOf(acctIdText);
        Optional<CardXrefRecord> x = xrefs.findFirstByAcctId(id);
        if (!x.isPresent()) return error(r, Messages.XREF_NOT_FOUND, ChangeAction.DETAILS_NOT_FETCHED);
        Optional<AccountRecord> a = accounts.findById(id);
        if (!a.isPresent()) return error(r, Messages.ACCOUNT_NOT_FOUND, ChangeAction.DETAILS_NOT_FETCHED);
        Optional<CustomerRecord> c = customers.findById(x.get().getCustId());
        if (!c.isPresent()) return error(r, Messages.CUSTOMER_NOT_FOUND, ChangeAction.DETAILS_NOT_FETCHED);
        r.setAction(ChangeAction.SHOW_DETAILS); r.setInfoMessage(Messages.DETAILS_SHOWN); r.setDetails(from(a.get(), c.get(), x.get())); return r;
    }

    public AccountUpdateResponse editMapInputs(AccountUpdateRequest request) {
        AccountUpdateDetails old = request == null ? null : request.getOriginal(), n = request == null ? null : request.getUpdated();
        AccountUpdateResponse r = new AccountUpdateResponse();
        if (old == null || n == null) return error(r, Messages.NO_CHANGE, ChangeAction.SHOW_DETAILS);
        if (compareOldNew(old, n) == CompareResult.NO_CHANGES_FOUND) return error(r, Messages.NO_CHANGE, ChangeAction.SHOW_DETAILS);
        EditContext c = new EditContext();
        editor.editYesNo("Account Status", n.getActiveStatus(), c);
        editor.editDateCcyymmdd("Open Date", n.getOpenYear(), n.getOpenMon(), n.getOpenDay(), c);
        editor.editSigned9V2("Credit Limit", n.getCreditLimit(), c);
        editor.editDateCcyymmdd("Expiry Date", n.getExpYear(), n.getExpMon(), n.getExpDay(), c);
        editor.editSigned9V2("Cash Credit Limit", n.getCashCreditLimit(), c);
        editor.editDateCcyymmdd("Reissue Date", n.getReissueYear(), n.getReissueMon(), n.getReissueDay(), c);
        editor.editSigned9V2("Current Balance", n.getCurrBal(), c);
        editor.editSigned9V2("Current Cycle Credit Limit", n.getCurrCycCredit(), c);
        editor.editSigned9V2("Current Cycle Debit Limit", n.getCurrCycDebit(), c);
        FieldFlag s1 = editor.editNumReqd("SSN: First 3 chars", n.getSsn1(), 3, c);
        if (s1 == FieldFlag.VALID && Arrays.asList("000", "666").contains(n.getSsn1()) || s1 == FieldFlag.VALID && n.getSsn1() != null && n.getSsn1().compareTo("900") >= 0) c.flag("SSN: First 3 chars", FieldFlag.NOT_OK, "SSN: First 3 chars: should not be 000, 666, or between 900 and 999");
        editor.editNumReqd("SSN 4th & 5th chars", n.getSsn2(), 2, c);
        editor.editNumReqd("SSN Last 4 chars", n.getSsn3(), 4, c);
        DateFlags dob = editor.editDateCcyymmdd("Date of Birth", n.getDobYear(), n.getDobMon(), n.getDobDay(), c);
        if (dob.isValid()) editor.editDateOfBirth("Date of Birth", n.getDobYear(), n.getDobMon(), n.getDobDay(), c);
        FieldFlag fico = editor.editNumReqd("FICO Score", n.getFicoScore(), 3, c);
        if (fico == FieldFlag.VALID) {
            int v = Integer.parseInt(n.getFicoScore());
            if (v < 300 || v > 850) c.flag("FICO Score", FieldFlag.NOT_OK, "FICO Score: should be between 300 and 850");
        }
        editor.editAlphaReqd("First Name", n.getFirstName(), c); editor.editAlphaOpt("Middle Name", n.getMiddleName(), c); editor.editAlphaReqd("Last Name", n.getLastName(), c);
        editor.editMandatory("Address Line 1", n.getAddrLine1(), c);
        FieldFlag state = editor.editAlphaReqd("State", n.getAddrStateCd(), c);
        if (state == FieldFlag.VALID && !editor.stateValid(n.getAddrStateCd())) c.flag("State", FieldFlag.NOT_OK, "State: is not a valid state code");
        FieldFlag zip = editor.editNumReqd("Zip", n.getAddrZip(), 5, c);
        editor.editAlphaReqd("City", n.getAddrLine3(), c); editor.editAlphaReqd("Country", n.getAddrCountryCd(), c);
        editPhone("Phone 1", n.getPhone1A(), n.getPhone1B(), n.getPhone1C(), c);
        editPhone("Phone 2", n.getPhone2A(), n.getPhone2B(), n.getPhone2C(), c);
        editor.editNumReqd("EFT Account Id", n.getEftAccountId(), 10, c); editor.editYesNo("Primary Card Holder", n.getPriHolderInd(), c);
        if (state == FieldFlag.VALID && zip == FieldFlag.VALID && !editor.zipStateValid(n.getAddrStateCd(), n.getAddrZip())) {
            c.flag("State", FieldFlag.NOT_OK, Messages.INVALID_ZIP); c.flag("Zip", FieldFlag.NOT_OK, Messages.INVALID_ZIP);
        }
        r.setAction(c.isInputError() ? ChangeAction.CHANGES_NOT_OK : ChangeAction.CHANGES_OK_NOT_CONFIRMED);
        r.setInfoMessage(c.isInputError() ? null : Messages.CHANGES_VALIDATED); r.setErrorMessage(c.getReturnMsg()); r.setFieldFlags(c.getFieldFlags()); r.setDetails(n); return r;
    }

    private void editPhone(String label, String a, String b, String c, EditContext ctx) {
        if ((a == null || a.trim().isEmpty()) && (b == null || b.trim().isEmpty()) && (c == null || c.trim().isEmpty())) {
            ctx.flag(label + ".A", FieldFlag.VALID, null); ctx.flag(label + ".B", FieldFlag.VALID, null); ctx.flag(label + ".C", FieldFlag.VALID, null); return;
        }
        editor.editPhonePart(label + ".A", a, 3, true, ctx); editor.editPhonePart(label + ".B", b, 3, false, ctx); editor.editPhonePart(label + ".C", c, 4, false, ctx);
    }

    public CompareResult compareOldNew(AccountUpdateDetails a, AccountUpdateDetails b) {
        if (a == null || b == null) return CompareResult.CHANGE_HAS_OCCURRED;
        String[] exact = {a.getAcctId(), b.getAcctId(), a.getActiveStatus(), b.getActiveStatus(), a.getOpenYear(), b.getOpenYear(), a.getOpenMon(), b.getOpenMon(), a.getOpenDay(), b.getOpenDay(), a.getExpYear(), b.getExpYear(), a.getExpMon(), b.getExpMon(), a.getExpDay(), b.getExpDay(), a.getReissueYear(), b.getReissueYear(), a.getReissueMon(), b.getReissueMon(), a.getReissueDay(), b.getReissueDay(), a.getSsn1(), b.getSsn1(), a.getSsn2(), b.getSsn2(), a.getSsn3(), b.getSsn3(), a.getDobYear(), b.getDobYear(), a.getDobMon(), b.getDobMon(), a.getDobDay(), b.getDobDay(), a.getFicoScore(), b.getFicoScore(), a.getPhone1A(), b.getPhone1A(), a.getPhone1B(), b.getPhone1B(), a.getPhone1C(), b.getPhone1C(), a.getPhone2A(), b.getPhone2A(), a.getPhone2B(), b.getPhone2B(), a.getPhone2C(), b.getPhone2C()};
        for (int i = 0; i < exact.length; i += 2) if (!eq(exact[i], exact[i + 1])) return CompareResult.CHANGE_HAS_OCCURRED;
        String[] normalized = {a.getGroupId(),b.getGroupId(),a.getFirstName(),b.getFirstName(),a.getMiddleName(),b.getMiddleName(),a.getLastName(),b.getLastName(),a.getAddrLine1(),b.getAddrLine1(),a.getAddrLine2(),b.getAddrLine2(),a.getAddrLine3(),b.getAddrLine3(),a.getAddrStateCd(),b.getAddrStateCd(),a.getAddrCountryCd(),b.getAddrCountryCd(),a.getAddrZip(),b.getAddrZip(),a.getGovtIssuedId(),b.getGovtIssuedId(),a.getEftAccountId(),b.getEftAccountId(),a.getPriHolderInd(),b.getPriHolderInd()};
        for (int i = 0; i < normalized.length; i += 2) if (!norm(normalized[i]).equals(norm(normalized[i + 1]))) return CompareResult.CHANGE_HAS_OCCURRED;
        String[] money = {a.getCurrBal(),b.getCurrBal(),a.getCreditLimit(),b.getCreditLimit(),a.getCashCreditLimit(),b.getCashCreditLimit(),a.getCurrCycCredit(),b.getCurrCycCredit(),a.getCurrCycDebit(),b.getCurrCycDebit()};
        for (int i = 0; i < money.length; i += 2) if (!moneyEq(money[i], money[i + 1])) return CompareResult.CHANGE_HAS_OCCURRED;
        return CompareResult.NO_CHANGES_FOUND;
    }
    public enum CompareResult { NO_CHANGES_FOUND, CHANGE_HAS_OCCURRED }
    private boolean eq(String a, String b) { return Objects.equals(a == null || a.isEmpty() ? " " : a, b == null || b.isEmpty() ? " " : b); }
    private String norm(String s) { return (s == null ? "" : s.trim()).toUpperCase(); }
    private boolean moneyEq(String a, String b) { try { return CobolNumeric.numvalC(a).compareTo(CobolNumeric.numvalC(b)) == 0; } catch (RuntimeException e) { return eq(a,b); } }

    @Transactional
    public AccountUpdateResponse writeProcessing(AccountUpdateRequest request) {
        AccountUpdateResponse validation = editMapInputs(request);
        if (validation.getAction() != ChangeAction.CHANGES_OK_NOT_CONFIRMED) return validation;
        AccountUpdateDetails old = request.getOriginal(), n = request.getUpdated();
        Long acctId = Long.valueOf(old.getAcctId()), custId = Long.valueOf(old.getCustId());
        Optional<AccountRecord> ao;
        Optional<CustomerRecord> co;
        try { ao = accounts.findByIdForUpdate(acctId); } catch (RuntimeException e) { return error(new AccountUpdateResponse(), Messages.LOCK_ACCOUNT, ChangeAction.CHANGES_OKAYED_LOCK_ERROR); }
        if (!ao.isPresent()) return error(new AccountUpdateResponse(), Messages.LOCK_ACCOUNT, ChangeAction.CHANGES_OKAYED_LOCK_ERROR);
        try { co = customers.findByIdForUpdate(custId); } catch (RuntimeException e) { return error(new AccountUpdateResponse(), Messages.LOCK_CUSTOMER, ChangeAction.CHANGES_OKAYED_LOCK_ERROR); }
        if (!co.isPresent()) return error(new AccountUpdateResponse(), Messages.LOCK_CUSTOMER, ChangeAction.CHANGES_OKAYED_LOCK_ERROR);
        if (!dbMatches(ao.get(), co.get(), old)) return error(new AccountUpdateResponse(), Messages.DATA_CHANGED, ChangeAction.SHOW_DETAILS);
        try {
            AccountRecord a = ao.get(); CustomerRecord c = co.get();
            a.setActiveStatus(n.getActiveStatus()); a.setCurrBal(CobolNumeric.numvalC(n.getCurrBal())); a.setCreditLimit(CobolNumeric.numvalC(n.getCreditLimit())); a.setCashCreditLimit(CobolNumeric.numvalC(n.getCashCreditLimit())); a.setCurrCycCredit(CobolNumeric.numvalC(n.getCurrCycCredit())); a.setCurrCycDebit(CobolNumeric.numvalC(n.getCurrCycDebit()));
            a.setOpenDate(date(n.getOpenYear(),n.getOpenMon(),n.getOpenDay())); a.setExpirationDate(date(n.getExpYear(),n.getExpMon(),n.getExpDay())); a.setReissueDate(date(n.getReissueYear(),n.getReissueMon(),n.getReissueDay())); a.setGroupId(n.getGroupId());
            c.setFirstName(n.getFirstName()); c.setMiddleName(n.getMiddleName()); c.setLastName(n.getLastName()); c.setAddrLine1(n.getAddrLine1()); c.setAddrLine2(n.getAddrLine2()); c.setAddrLine3(n.getAddrLine3()); c.setAddrStateCd(n.getAddrStateCd()); c.setAddrCountryCd(n.getAddrCountryCd()); c.setAddrZip(n.getAddrZip()); c.setPhoneNum1(phone(n.getPhone1A(),n.getPhone1B(),n.getPhone1C())); c.setPhoneNum2(phone(n.getPhone2A(),n.getPhone2B(),n.getPhone2C())); c.setSsn(Long.valueOf(n.getSsn1()+n.getSsn2()+n.getSsn3())); c.setGovtIssuedId(n.getGovtIssuedId()); c.setDobYyyyMmDd(date(n.getDobYear(),n.getDobMon(),n.getDobDay())); c.setEftAccountId(n.getEftAccountId()); c.setPriCardHolderInd(n.getPriHolderInd()); c.setFicoCreditScore(Integer.valueOf(n.getFicoScore()));
            accounts.saveAndFlush(a); customers.saveAndFlush(c);
            AccountUpdateResponse r = new AccountUpdateResponse(); r.setAction(ChangeAction.CHANGES_OKAYED_AND_DONE); r.setInfoMessage(Messages.COMMITTED); r.setDetails(from(a,c,new CardXrefRecord())); return r;
        } catch (RuntimeException e) { TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); return error(new AccountUpdateResponse(), Messages.UPDATE_FAILED, ChangeAction.CHANGES_OKAYED_BUT_FAILED); }
    }
    private boolean dbMatches(AccountRecord a, CustomerRecord c, AccountUpdateDetails o) {
        AccountUpdateDetails db = from(a,c,new CardXrefRecord());
        return eq(a.getActiveStatus(),o.getActiveStatus()) && moneyEq(a.getCurrBal().toString(),o.getCurrBal()) && moneyEq(a.getCreditLimit().toString(),o.getCreditLimit()) && moneyEq(a.getCashCreditLimit().toString(),o.getCashCreditLimit()) && moneyEq(a.getCurrCycCredit().toString(),o.getCurrCycCredit()) && moneyEq(a.getCurrCycDebit().toString(),o.getCurrCycDebit()) && dateParts(a.getOpenDate(),o.getOpenYear(),o.getOpenMon(),o.getOpenDay()) && dateParts(a.getExpirationDate(),o.getExpYear(),o.getExpMon(),o.getExpDay()) && dateParts(a.getReissueDate(),o.getReissueYear(),o.getReissueMon(),o.getReissueDay()) && norm(a.getGroupId()).equals(norm(o.getGroupId())) && norm(c.getFirstName()).equals(norm(o.getFirstName())) && norm(c.getMiddleName()).equals(norm(o.getMiddleName())) && norm(c.getLastName()).equals(norm(o.getLastName())) && norm(c.getAddrLine1()).equals(norm(o.getAddrLine1())) && norm(c.getAddrLine2()).equals(norm(o.getAddrLine2())) && norm(c.getAddrLine3()).equals(norm(o.getAddrLine3())) && norm(c.getAddrStateCd()).equals(norm(o.getAddrStateCd())) && norm(c.getAddrCountryCd()).equals(norm(o.getAddrCountryCd())) && eq(c.getAddrZip(),o.getAddrZip()) && eq(c.getPhoneNum1(),phoneSnapshot(o.getPhone1A(),o.getPhone1B(),o.getPhone1C())) && eq(c.getPhoneNum2(),phoneSnapshot(o.getPhone2A(),o.getPhone2B(),o.getPhone2C())) && eq(c.getSsn()==null?null:String.format("%09d",c.getSsn()),o.getSsn1()+o.getSsn2()+o.getSsn3()) && norm(c.getGovtIssuedId()).equals(norm(o.getGovtIssuedId())) && dateParts(c.getDobYyyyMmDd(),o.getDobYear(),o.getDobMon(),o.getDobDay()) && eq(c.getEftAccountId(),o.getEftAccountId()) && eq(c.getPriCardHolderInd(),o.getPriHolderInd()) && Objects.equals(c.getFicoCreditScore(), Integer.valueOf(o.getFicoScore()));
    }
    private boolean dateParts(String d,String y,String m,String day) { return d != null && d.length() >= 10 && eq(d.substring(0,4),y) && eq(d.substring(5,7),m) && eq(d.substring(8,10),day); }
    private String date(String y,String m,String d) { return y + "-" + m + "-" + d; }
    private String phone(String a,String b,String c) { return "(" + a + ")" + b + "-" + c; }
    private String phoneSnapshot(String a, String b, String c) { return (a == null || a.isEmpty()) && (b == null || b.isEmpty()) && (c == null || c.isEmpty()) ? "" : phone(a, b, c); }
    private AccountUpdateResponse error(AccountUpdateResponse r,String msg,ChangeAction a) { r.setAction(a); r.setErrorMessage(msg); return r; }

    public AccountUpdateDetails from(AccountRecord a, CustomerRecord c, CardXrefRecord x) {
        AccountUpdateDetails d = new AccountUpdateDetails(); d.setAcctId(String.format("%011d",a.getAcctId())); d.setActiveStatus(a.getActiveStatus()); d.setCurrBal(money(a.getCurrBal())); d.setCreditLimit(money(a.getCreditLimit())); d.setCashCreditLimit(money(a.getCashCreditLimit())); d.setCurrCycCredit(money(a.getCurrCycCredit())); d.setCurrCycDebit(money(a.getCurrCycDebit())); splitDate(a.getOpenDate(),d::setOpenYear,d::setOpenMon,d::setOpenDay); splitDate(a.getExpirationDate(),d::setExpYear,d::setExpMon,d::setExpDay); splitDate(a.getReissueDate(),d::setReissueYear,d::setReissueMon,d::setReissueDay); d.setGroupId(a.getGroupId()); d.setCustId(String.format("%09d",c.getCustId())); d.setFirstName(c.getFirstName()); d.setMiddleName(c.getMiddleName()); d.setLastName(c.getLastName()); d.setAddrLine1(c.getAddrLine1()); d.setAddrLine2(c.getAddrLine2()); d.setAddrLine3(c.getAddrLine3()); d.setAddrStateCd(c.getAddrStateCd()); d.setAddrCountryCd(c.getAddrCountryCd()); d.setAddrZip(c.getAddrZip()); phoneParts(c.getPhoneNum1(),d,1); phoneParts(c.getPhoneNum2(),d,2); String ssn=String.format("%09d",c.getSsn()); d.setSsn1(ssn.substring(0,3));d.setSsn2(ssn.substring(3,5));d.setSsn3(ssn.substring(5)); d.setGovtIssuedId(c.getGovtIssuedId()); splitDate(c.getDobYyyyMmDd(),d::setDobYear,d::setDobMon,d::setDobDay); d.setEftAccountId(c.getEftAccountId()); d.setPriHolderInd(c.getPriCardHolderInd()); d.setFicoScore(String.format("%03d",c.getFicoCreditScore())); if(x!=null)d.setCardNum(x.getCardNum()); return d;
    }
    private String money(BigDecimal v) { return v == null ? null : v.setScale(2).toPlainString(); }
    private void splitDate(String v, java.util.function.Consumer<String> y, java.util.function.Consumer<String> m, java.util.function.Consumer<String> d) { if(v!=null && v.matches("\\d{4}-\\d{2}-\\d{2}")) { y.accept(v.substring(0,4));m.accept(v.substring(5,7));d.accept(v.substring(8,10)); } }
    private void phoneParts(String value,AccountUpdateDetails d,int n) { Matcher m=Pattern.compile("^\\((\\d{3})\\)(\\d{3})-(\\d{4})$").matcher(value==null?"":value); String a=m.matches()?m.group(1):value==null?"":value,b=m.matches()?m.group(2):"",c=m.matches()?m.group(3):""; if(n==1){d.setPhone1A(a);d.setPhone1B(b);d.setPhone1C(c);}else{d.setPhone2A(a);d.setPhone2B(b);d.setPhone2C(c);} }
}
