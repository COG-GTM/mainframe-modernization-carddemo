package com.carddemo.web;

import com.carddemo.service.SignonService;
import com.carddemo.web.dto.SignonRequest;
import com.carddemo.web.dto.SignonResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** COSGN00C - signon. */
@RestController
@RequestMapping("/api/v1/signon")
public class SignonController {

    private final SignonService signonService;

    public SignonController(SignonService signonService) {
        this.signonService = signonService;
    }

    @PostMapping
    public SignonResponse signon(@RequestBody SignonRequest request) {
        return signonService.signon(request);
    }
}
