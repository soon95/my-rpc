package org.leon.myrpc.demo;

import org.leon.myrpc.annotation.RPCConsumer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Leon Song
 * @date 2020-12-06
 */
@RestController
@RequestMapping("/rpc")
public class HelloController {

    @RPCConsumer
    private HelloService helloService;

    @RequestMapping("/{name}")
    public String hello(@PathVariable String name) {
        return helloService.hello(name);
    }
}
