package com.czintercity.icsec_app.runtime;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppRuntimeController {

    @GetMapping("/")
    public String index() {
        return "app/index";
    }
}
