package com.czintercity.icsec_app.runtime;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AppErrorController implements ErrorController {

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
