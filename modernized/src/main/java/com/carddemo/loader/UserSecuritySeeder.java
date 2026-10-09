package com.carddemo.loader;

import com.carddemo.repository.SecUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** DUSRSECJ equivalent: seeds USRSEC on first start so an administrator can sign on and run jobs. */
@Component
public class UserSecuritySeeder implements ApplicationRunner {

    private final SecUserRepository users;
    private final LegacyDataLoader loader;

    public UserSecuritySeeder(SecUserRepository users, LegacyDataLoader loader) {
        this.users = users;
        this.loader = loader;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() == 0) {
            loader.loadUsers();
        }
    }
}
