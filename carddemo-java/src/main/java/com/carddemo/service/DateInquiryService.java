package com.carddemo.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class DateInquiryService {
    public Map<String, String> getCurrentDate() {
        LocalDate now = LocalDate.now();
        return Map.of(
            "date", now.format(DateTimeFormatter.ISO_DATE),
            "year", String.valueOf(now.getYear()),
            "month", String.format("%02d", now.getMonthValue()),
            "day", String.format("%02d", now.getDayOfMonth())
        );
    }
}
