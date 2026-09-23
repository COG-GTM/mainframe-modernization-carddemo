package com.carddemo.web;

import com.carddemo.service.UserAdminService;
import com.carddemo.web.dto.PageResponse;
import com.carddemo.web.dto.UserRequest;
import com.carddemo.web.dto.UserView;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** COUSR00C (list), COUSR01C (add), COUSR02C (update) and COUSR03C (delete). */
@RestController
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public PageResponse<UserView> list(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return userAdminService.list(page, size);
    }

    @GetMapping("/{userId}")
    public UserView view(@PathVariable String userId) {
        return userAdminService.view(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserView add(@RequestBody UserRequest request) {
        return userAdminService.add(request);
    }

    @PutMapping("/{userId}")
    public UserView update(@PathVariable String userId, @RequestBody UserRequest request) {
        return userAdminService.update(userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String userId) {
        userAdminService.delete(userId);
    }
}
