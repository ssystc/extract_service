package com.geovis.extract_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HtmlController {

	@RequestMapping("/index")
    public String hhh() {
        return "/index";
    }
	
}
