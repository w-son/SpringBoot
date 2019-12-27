package jpabook.jpashop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// 1. 관련된 annotation을 달아줘야 한다
@Controller
public class HelloController {

    // 2. hello 라는 url로 오면 이 컨트롤러가 호출이 된다는 말이다
    @GetMapping("hello")
    public String hello(Model model) {
        // 3. 컨트롤러를 통해서 모델에 데이터를 실어서 뷰로 넘길 수 있다

        model.addAttribute("data" , "hello");

        // 4. 리턴되는 값에 자동으로 .html이 붙는다
        return "hello";
    }
}
