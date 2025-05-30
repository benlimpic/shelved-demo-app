package com.authentication.demo.Controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Profile("demo")
public class DemoSplashController {


    @GetMapping("/")
    public String splash() {
        return "splash";
    }
}

