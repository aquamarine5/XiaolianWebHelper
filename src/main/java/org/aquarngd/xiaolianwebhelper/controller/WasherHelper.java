package org.aquarngd.xiaolianwebhelper.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WasherHelper {

    @RequestMapping("/wash")
    public String GetWash(){
        return "";
    }
}
