package com.czintercity.icsec_app.runtime;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom Spring MVC error controller that intercepts all {@code /error} requests and renders
 * the application error template with the HTTP status code.
 */
@Controller
public class AppErrorController implements ErrorController {

    /**
     * Handles unhandled errors forwarded by the servlet container.
     * Adds the HTTP status code (or {@code "500"} as a fallback) to the model.
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model){
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if(status != null){
            model.addAttribute("error", status.toString());
        }
        else
            model.addAttribute("error", "500");

        return "error";
    }
}
