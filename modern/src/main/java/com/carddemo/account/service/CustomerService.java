package com.carddemo.account.service;

import com.carddemo.account.dto.AccountResponse.CustomerData;
import com.carddemo.account.dto.AccountUpdateRequest.CustomerFields;
import com.carddemo.account.entity.CustomerEntity;
import com.carddemo.account.exception.ResourceNotFoundException;
import com.carddemo.account.repository.CustomerRepository;
import org.springframework.stereotype.Service;

/**
 * Service for customer data access.
 * Migrated from: EXEC CICS READ/REWRITE FILE('CUSTDAT') in COACTVWC.cbl / COACTUPC.cbl.
 * VSAM file: CUSTDAT (KSDS, keyed by CUST-ID)
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerEntity findById(Long custId) {
        return customerRepository.findById(custId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + custId));
    }

    public CustomerEntity updateCustomer(Long custId, CustomerFields fields) {
        CustomerEntity entity = findById(custId);
        if (fields.firstName() != null) entity.setFirstName(fields.firstName());
        if (fields.middleName() != null) entity.setMiddleName(fields.middleName());
        if (fields.lastName() != null) entity.setLastName(fields.lastName());
        if (fields.addrLine1() != null) entity.setAddrLine1(fields.addrLine1());
        if (fields.addrLine2() != null) entity.setAddrLine2(fields.addrLine2());
        if (fields.addrLine3() != null) entity.setAddrLine3(fields.addrLine3());
        if (fields.addrStateCd() != null) entity.setAddrStateCd(fields.addrStateCd());
        if (fields.addrCountryCd() != null) entity.setAddrCountryCd(fields.addrCountryCd());
        if (fields.addrZip() != null) entity.setAddrZip(fields.addrZip());
        if (fields.phoneNum1() != null) entity.setPhoneNum1(fields.phoneNum1());
        if (fields.phoneNum2() != null) entity.setPhoneNum2(fields.phoneNum2());
        if (fields.ssn() != null) entity.setSsn(fields.ssn());
        if (fields.govtIssuedId() != null) entity.setGovtIssuedId(fields.govtIssuedId());
        if (fields.dob() != null) entity.setDob(fields.dob());
        if (fields.eftAccountId() != null) entity.setEftAccountId(fields.eftAccountId());
        if (fields.primaryCardHolderInd() != null) entity.setPrimaryCardHolderInd(fields.primaryCardHolderInd());
        if (fields.ficoCreditScore() != null) entity.setFicoCreditScore(fields.ficoCreditScore());
        return customerRepository.save(entity);
    }

    public CustomerData toDto(CustomerEntity entity) {
        return new CustomerData(
                entity.getCustId(),
                entity.getFirstName(),
                entity.getMiddleName(),
                entity.getLastName(),
                entity.getAddrLine1(),
                entity.getAddrLine2(),
                entity.getAddrLine3(),
                entity.getAddrStateCd(),
                entity.getAddrCountryCd(),
                entity.getAddrZip(),
                entity.getPhoneNum1(),
                entity.getPhoneNum2(),
                entity.getSsn(),
                entity.getGovtIssuedId(),
                entity.getDob(),
                entity.getEftAccountId(),
                entity.getPrimaryCardHolderInd(),
                entity.getFicoCreditScore()
        );
    }
}
