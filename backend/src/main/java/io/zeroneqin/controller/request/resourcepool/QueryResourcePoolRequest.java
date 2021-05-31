package io.zeroneqin.controller.request.resourcepool;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryResourcePoolRequest {
    private String name;
    private String status;
}
