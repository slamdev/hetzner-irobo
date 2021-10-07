package com.github.slamdev.hetzner.irobo.integration;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@UtilityClass
public class ErrorModels {

    public ModelAndView clientError(HttpStatus status) {
        return new ModelAndView(
                "error/4xx",
                Map.of("status", status.value(), "error", status.getReasonPhrase()),
                status
        );
    }
}
