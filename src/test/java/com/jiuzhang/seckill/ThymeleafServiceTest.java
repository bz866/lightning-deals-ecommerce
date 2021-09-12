package com.jiuzhang.seckill;

import com.jiuzhang.seckill.services.ActivityHtmlPageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ThymeleafServiceTest {

    @Autowired
    private ActivityHtmlPageService activityHtmlPageService;

    @Test
    public void createHtmlTest(){
        activityHtmlPageService.createActivityHtml(19);
    }
}
