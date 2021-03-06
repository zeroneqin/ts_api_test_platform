package io.zeroneqin.controller;

import io.zeroneqin.base.domain.ServiceIntegration;
import io.zeroneqin.controller.request.IntegrationRequest;
import io.zeroneqin.service.IntegrationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("service/integration")
@RestController
public class ServiceIntegrationController {

    @Resource
    private IntegrationService integrationService;

    @PostMapping("/save")
    public ServiceIntegration save(@RequestBody ServiceIntegration service) {
        return integrationService.save(service);
    }

    @PostMapping("/type")
    public ServiceIntegration getByPlatform(@RequestBody IntegrationRequest request) {
        return integrationService.get(request);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody IntegrationRequest request) {
        integrationService.delete(request);
    }

    @GetMapping("/all/{orgId}")
    public List<ServiceIntegration> getAll(@PathVariable String orgId) {
        return integrationService.getAll(orgId);
    }

}
