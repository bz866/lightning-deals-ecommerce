package com.jiuzhang.seckill.services;

import com.jiuzhang.seckill.db.dao.SeckillActivityDao;
import com.jiuzhang.seckill.db.dao.SeckillCommodityDao;
import com.jiuzhang.seckill.db.po.SeckillActivity;
import com.jiuzhang.seckill.db.po.SeckillCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ActivityHtmlPageService {
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private SeckillActivityDao seckillActivityDao;
    @Autowired
    private SeckillCommodityDao seckillCommodityDao;
    /**
     * create the html page
     *
     * @throws Exception */
    public void createActivityHtml(long seckillActivityId) {
        PrintWriter writer = null;
        try {
            SeckillActivity seckillActivity =
                    seckillActivityDao.querySeckillActivityById(seckillActivityId);
            SeckillCommodity seckillCommodity =
                    seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
            // get page information
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("seckillActivity", seckillActivity);
            resultMap.put("seckillCommodity", seckillCommodity);
            resultMap.put("seckillPrice", seckillActivity.getSeckillPrice());
            resultMap.put("oldPrice", seckillActivity.getOldPrice());
            resultMap.put("commodityId", seckillActivity.getCommodityId());
            resultMap.put("commodityName", seckillCommodity.getCommodityName());
            resultMap.put("commodityDesc", seckillCommodity.getCommodityDesc());
            // create thymeleaf context
            Context context = new Context();
            // set variables in the Context
            context.setVariables(resultMap);
            // create the output file
            File file = new File("src/main/resources/templates/" +
                    "seckill_item_" + seckillActivityId + ".html");
            writer = new PrintWriter(file);
            // render the page
            templateEngine.process("seckill_item", context, writer);
        } catch (Exception e) {
            log.error(e.toString());
            log.error("页面静态化出错:" + seckillActivityId);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
