package com.lagou.page.controller;

import com.lagou.common.pojo.Products;
import com.lagou.page.fegin.ProductFeign;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.ribbon.proxy.annotation.Hystrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/page")
public class PageController {

    @Autowired
    private ProductFeign productFeign;

    @GetMapping("/getProduct/{id}")
    public Products getProduct(@PathVariable Integer id) {
        //RT降级演示
       /** try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
       //异常比例数
        int i = 1/0;
        return productFeign.queryById(id);
    }

    @GetMapping("/loadProductServicePort")
    public String getProductServerPort() {
        try {Thread.sleep(5000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return productFeign.getPort();
    }

    /**
     * 模拟服务超时，熔断处理
     * 针对熔断处理，Hystrix默认维护一个线程池，默认大小为10。
     *
     * @return
     */
    @HystrixCommand(
            //只有是在@HystrixCommand中定义了threadPoolKey，就意味着开启了舱壁模式（线程隔离），该方法就会自己维护一个线程池。
            threadPoolKey = "getProductServerPort2", //默认所有的请求共同维护一个线程池，实际开发：每个方法维护一个线程池
            //每一个属性对应的都是一个HystrixProperty
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "1"),//并发线程数
                    @HystrixProperty(name = "maxQueueSize", value = "20")//默认线程队列值是-1，默认不开启
            },
            //超时时间的设置
            commandProperties = {
                    //设置请求的超时时间，一旦请求超过此时间那么都按照超时处理，默认超时时间是1S
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000")
            }
    )
    @GetMapping("/loadProductServicePort2")
    public String getProductServerPort2() {
        return productFeign.getPort();
    }

    /**
     * 服务降级演示：是在服务熔断之后的兜底操作
     */

    @HystrixCommand(
            //超时时间的设置
            commandProperties = {
                    //设置请求的超时时间，一旦请求超过此时间那么都按照超时处理，默认超时时间是1S
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000"),
                    //统计窗口时间的设置
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds",value = "8000"),
                    //统计窗口内的最小请求数
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "2"),
                    //统计窗口内错误请求阈值的设置  50%
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "50"),
                    //自我修复的活动窗口时间
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds",value = "3000")

            },//设置回退方法
            fallbackMethod = "getProductServerPortFallBack"
    )
    @GetMapping("/loadProductServicePort3")
    public String getProductServerPort3() {
        return productFeign.getPort();
    }

    /**
     * 定义回退方法，当请求出发熔断后执行，补救措施
     * 注意：
     * 1.方法形参和原方法保持一致
     * 2.方法的返回值与原方法保持一致
     */
    public String getProductServerPortFallBack(){
        return "-1";
    }
}
