package com.czintercity.icsec_app.runtime;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for top-level application navigation routes.
 */
@Controller
public class AppRuntimeController {

    /** Renders the application home page. */
    @GetMapping("/")
    public String index() {
        return "app/index";
    }
}
