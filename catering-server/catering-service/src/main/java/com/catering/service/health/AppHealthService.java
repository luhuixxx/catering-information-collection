package com.catering.service.health;

import org.springframework.stereotype.Service;

@Service
public class AppHealthService {

    public String status() {
        return "catering-service-ready";
    }
}
