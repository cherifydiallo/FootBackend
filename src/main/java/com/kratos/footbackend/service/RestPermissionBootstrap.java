package com.kratos.footbackend.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RestPermissionBootstrap implements CommandLineRunner {

    @Override
    @Transactional
    public void run(String... args) {
        // Group-based permissions are managed by data inserts/administration.
    }
}
