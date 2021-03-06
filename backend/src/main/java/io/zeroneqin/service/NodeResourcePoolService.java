package io.zeroneqin.service;

import com.alibaba.fastjson.JSON;
import io.zeroneqin.base.domain.TestResource;
import io.zeroneqin.base.domain.TestResourceExample;
import io.zeroneqin.base.mapper.TestResourceMapper;
import io.zeroneqin.commons.constants.ResourceStatusEnum;
import io.zeroneqin.commons.exception.MSException;
import io.zeroneqin.commons.utils.LogUtil;
import io.zeroneqin.dto.NodeDTO;
import io.zeroneqin.dto.TestResourcePoolDTO;
import io.zeroneqin.i18n.Translator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.zeroneqin.commons.constants.ResourceStatusEnum.VALID;

@Service
public class NodeResourcePoolService {
    private final static String nodeControllerUrl = "http://%s:%s/status";

    @Resource(name = "restTemplateWithTimeOut")
    private RestTemplate restTemplateWithTimeOut;
    @Resource
    private TestResourceMapper testResourceMapper;

    public boolean validate(TestResourcePoolDTO testResourcePool) {
        if (CollectionUtils.isEmpty(testResourcePool.getResources())) {
            MSException.throwException(Translator.get("no_nodes_message"));
        }

        deleteTestResource(testResourcePool.getId());
        List<String> nodeIps = testResourcePool.getResources().stream()
                .map(resource -> {
                    NodeDTO nodeDTO = JSON.parseObject(resource.getConfiguration(), NodeDTO.class);
                    return nodeDTO.getIp();
                })
                .distinct()
                .collect(Collectors.toList());
        if (nodeIps.size() < testResourcePool.getResources().size()) {
            MSException.throwException(Translator.get("duplicate_node_ip"));
        }
        testResourcePool.setStatus(VALID.name());
        boolean isValid = true;
        for (TestResource resource : testResourcePool.getResources()) {
            NodeDTO nodeDTO = JSON.parseObject(resource.getConfiguration(), NodeDTO.class);
            boolean isValidate = validateNode(nodeDTO);
            if (!isValidate) {
                testResourcePool.setStatus(ResourceStatusEnum.INVALID.name());
                resource.setStatus(ResourceStatusEnum.INVALID.name());
                isValid = false;
            } else {
                resource.setStatus(VALID.name());
            }
            resource.setTestResourcePoolId(testResourcePool.getId());
            updateTestResource(resource);
        }
        return isValid;
    }


    private boolean validateNode(NodeDTO node) {
        try {
            ResponseEntity<String> entity = restTemplateWithTimeOut.getForEntity(String.format(nodeControllerUrl, node.getIp(), node.getPort()), String.class);
            return HttpStatus.OK.equals(entity.getStatusCode());
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return false;
        }
    }

    private void updateTestResource(TestResource testResource) {
        testResource.setUpdateTime(System.currentTimeMillis());
        testResource.setCreateTime(System.currentTimeMillis());
        if (StringUtils.isBlank(testResource.getId())) {
            testResource.setId(UUID.randomUUID().toString());
        }
        // ??????????????????????????????????????????ID???????????????
        testResourceMapper.insertSelective(testResource);
    }

    private void deleteTestResource(String testResourcePoolId) {
        TestResourceExample testResourceExample = new TestResourceExample();
        testResourceExample.createCriteria().andTestResourcePoolIdEqualTo(testResourcePoolId);
        testResourceMapper.deleteByExample(testResourceExample);
    }
}
