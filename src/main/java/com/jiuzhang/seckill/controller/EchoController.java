package com.jiuzhang.seckill.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("echo") // all methods below are projected under "echo"
public class EchoController {

    @GetMapping("{text}") // access via browser /echo/{text}, text as the Path Variable
    public String echo(
            @PathVariable("text") String text
    ) {
        return text; // return text to client
    }
}
