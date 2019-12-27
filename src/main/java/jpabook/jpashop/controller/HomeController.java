package jpabook.jpashop.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class HomeController {

    // slf4j 의 로거를 사용한다

    @RequestMapping("/")
    public String home() {
        log.info("home controller");
        // home.html 을 찾아가서 타임리프에서 실행시키게 된다
        return "home";
    }


}
