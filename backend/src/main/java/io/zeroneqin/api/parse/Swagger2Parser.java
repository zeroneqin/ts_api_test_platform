package io.zeroneqin.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.zeroneqin.api.dto.ApiTestImportRequest;
import io.zeroneqin.api.dto.definition.ApiDefinitionResult;
import io.zeroneqin.api.dto.definition.parse.ApiDefinitionImport;
import io.zeroneqin.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.zeroneqin.api.dto.definition.response.HttpResponse;
import io.zeroneqin.api.dto.scenario.Body;
import io.zeroneqin.api.dto.scenario.KeyValue;
import io.zeroneqin.api.dto.scenario.request.RequestType;
import io.zeroneqin.base.domain.ApiModule;
import io.zeroneqin.commons.constants.SwaggerParameterType;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.*;

public class Swagger2Parser extends SwaggerAbstractParser {

    private Map<String, Model> definitions = null;

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        Swagger swagger;
        String sourceStr = "";
        if (StringUtils.isNotBlank(request.getSwaggerUrl())) {
            swagger = new SwaggerParser().read(request.getSwaggerUrl());
        } else {
            sourceStr = getApiTestStr(source);
            swagger = new SwaggerParser().readWithInfo(sourceStr).getSwagger();
        }

        if (swagger == null || swagger.getSwagger() == null) {
            Swagger3Parser swagger3Parser = new Swagger3Parser();
            return swagger3Parser.parse(sourceStr, request);
        }

        ApiDefinitionImport definitionImport = new ApiDefinitionImport();
        this.projectId = request.getProjectId();
        definitionImport.setData(parseRequests(swagger, request));
        return definitionImport;
    }

    private List<ApiDefinitionResult> parseRequests(Swagger swagger, ApiTestImportRequest importRequest) {
        Map<String, Path> paths = swagger.getPaths();
        Set<String> pathNames = paths.keySet();

        this.definitions = swagger.getDefinitions();

        List<ApiDefinitionResult> results = new ArrayList<>();

        ApiModule parentNode = getSelectModule(importRequest.getModuleId());

        for (String pathName : pathNames) {
            Path path = paths.get(pathName);
            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            Set<HttpMethod> httpMethods = operationMap.keySet();
            for (HttpMethod method : httpMethods) {
                Operation operation = operationMap.get(method);
                MsHTTPSamplerProxy request = buildRequest(operation, pathName, method.name());
                ApiDefinitionResult apiDefinition = buildApiDefinition(request.getId(), operation, pathName, method.name());
                parseParameters(operation, request);
                apiDefinition.setRequest(JSON.toJSONString(request));
                apiDefinition.setResponse(JSON.toJSONString(parseResponse(operation.getResponses())));
                buildModule(parentNode, apiDefinition, operation.getTags(), importRequest.isSaved());
                results.add(apiDefinition);
            }
        }

        this.definitions = null;
        return results;
    }

    private ApiDefinitionResult buildApiDefinition(String id, Operation operation, String path, String method) {
        String name = "";
        if (StringUtils.isNotBlank(operation.getSummary())) {
            name = operation.getSummary();
        } else  if (StringUtils.isNotBlank(operation.getOperationId())) {
            name = operation.getOperationId();
        } else {
            name = path;
        }
        return buildApiDefinition(id, name, path, method);
    }

    private MsHTTPSamplerProxy buildRequest(Operation operation, String path, String method) {
        String name = "";
        if (StringUtils.isNotBlank(operation.getSummary())) {
            name = operation.getSummary();
        } else {
            name = operation.getOperationId();
        }
        return buildRequest(name, path, method);
    }

    private void parseParameters(Operation operation, MsHTTPSamplerProxy request) {

        List<Parameter> parameters = operation.getParameters();
        request.getBody().setType(getBodyType(operation));

        // todo ???????????? {xxx} ???????????????

        for (Parameter parameter : parameters) {
            switch (parameter.getIn()) {
                case SwaggerParameterType.PATH:
                    parsePathParameters(parameter, request.getRest());
                    break;
                case SwaggerParameterType.QUERY:
                    parseQueryParameters(parameter, request.getArguments());
                    break;
                case SwaggerParameterType.FORM_DATA:
                    parseFormDataParameters((FormParameter) parameter, request.getBody());
                    break;
                case SwaggerParameterType.BODY:
                    parseRequestBodyParameters(parameter, request.getBody());
                    break;
                case SwaggerParameterType.HEADER:
                    parseHeaderParameters(parameter, request.getHeaders());
                    break;
                case SwaggerParameterType.COOKIE:
                    parseCookieParameters(parameter, request.getHeaders());
                    break;
//                case SwaggerParameterType.FILE:
//                    parsePathParameters(parameter, request);
//                    break;
            }
        }
    }

    private String getBodyType(Operation operation) {
        if (CollectionUtils.isEmpty(operation.getConsumes())) {
            return Body.RAW;
        }
        String contentType = operation.getConsumes().get(0);
        return getBodyType(contentType);
    }

    private void parsePathParameters(Parameter parameter, List<KeyValue> rests) {
        PathParameter pathParameter = (PathParameter) parameter;
        rests.add(new KeyValue(pathParameter.getName(), "", getDefaultStringValue(parameter.getDescription())));
    }

    private String getDefaultStringValue(String val) {
        return StringUtils.isBlank(val) ? "" : val;
    }

    private void parseCookieParameters(Parameter parameter, List<KeyValue> headers) {
        CookieParameter cookieParameter = (CookieParameter) parameter;
        addCookie(headers, cookieParameter.getName(), "", getDefaultStringValue(cookieParameter.getDescription()), parameter.getRequired());
    }

    private void parseHeaderParameters(Parameter parameter, List<KeyValue> headers) {
        HeaderParameter headerParameter = (HeaderParameter) parameter;
        addHeader(headers, headerParameter.getName(), "", getDefaultStringValue(headerParameter.getDescription()),
                "", parameter.getRequired());
    }

    private HttpResponse parseResponse(Map<String, Response> responses) {
        HttpResponse msResponse = new HttpResponse();
        msResponse.setBody(new Body());
        msResponse.setHeaders(new ArrayList<>());
        msResponse.setType(RequestType.HTTP);
        // todo ?????????????????????
        msResponse.setStatusCode(new ArrayList<>());
        if (responses != null) {
            responses.forEach((responseCode, response) -> {
                msResponse.getStatusCode().add(new KeyValue(responseCode, responseCode));
                parseResponseHeader(response, msResponse.getHeaders());
                parseResponseBodyParameters(response, msResponse.getBody());
            });
        }
        return msResponse;
    }

    private void parseResponseHeader(Response response, List<KeyValue> msHeaders) {
        Map<String, Property> headers = response.getHeaders();
        if (headers != null) {
            headers.forEach((k, v) -> {
                msHeaders.add(new KeyValue(k, "", v.getDescription()));
            });
        }
    }

    private void parseResponseBodyParameters(Response response, Body body) {
        body.setRaw(parseSchema(response.getResponseSchema()));
    }

    private void parseRequestBodyParameters(Parameter parameter, Body body) {
        BodyParameter bodyParameter = (BodyParameter) parameter;
        body.setRaw(parseSchema(bodyParameter.getSchema()));
    }

    private String parseSchema(Model schema) {
        // ????????????
        if (schema instanceof RefModel) {
            String simpleRef = "";
            RefModel refModel = (RefModel) schema;
            String originalRef = refModel.getOriginalRef();
            if (refModel.getOriginalRef().split("/").length > 3) {
                simpleRef = originalRef.replace("#/definitions/", "");
            } else {
                simpleRef = refModel.getSimpleRef();
            }
            Model model = this.definitions.get(simpleRef);
            HashSet<String> refSet = new HashSet<>();
            refSet.add(simpleRef);
            if (model != null) {
                JSONObject bodyParameters = getBodyParameters(model.getProperties(), refSet);
                return bodyParameters.toJSONString();
            }
        } else if (schema instanceof ArrayModel) {
            //????????????
            ArrayModel arrayModel = (ArrayModel) schema;
            Property items = arrayModel.getItems();
            JSONArray propertyList = new JSONArray();
            if (items instanceof RefProperty) {
                RefProperty refProperty = (RefProperty) items;
                String simpleRef = refProperty.getSimpleRef();
                HashSet<String> refSet = new HashSet<>();
                refSet.add(simpleRef);
                Model model = definitions.get(simpleRef);
                if (model != null) {
                    propertyList.add(getBodyParameters(model.getProperties(), refSet));
                } else {
                    propertyList.add(new JSONObject());
                }
            }
            return propertyList.toString();
        }
        return "";
    }

    private JSONObject getBodyParameters(Map<String, Property> properties, HashSet<String> refSet) {
        JSONObject jsonObject = new JSONObject();
        if (properties != null) {
            properties.forEach((key, value) -> {
                if (value instanceof ObjectProperty) {
                    ObjectProperty objectProperty = (ObjectProperty) value;
                    jsonObject.put(key, getBodyParameters(objectProperty.getProperties(), refSet));
                } else if (value instanceof ArrayProperty) {
                    ArrayProperty arrayProperty = (ArrayProperty) value;
                    Property items = arrayProperty.getItems();
                    if (items instanceof RefProperty) {
                        RefProperty refProperty = (RefProperty) items;
                        String simpleRef = refProperty.getSimpleRef();
                        if (refSet.contains(simpleRef)) {
                            //?????????????????????
                            jsonObject.put(key, new JSONArray());
                            return;
                        }
                        refSet.add(simpleRef);
                        Model model = this.definitions.get(simpleRef);
                        JSONArray propertyList = new JSONArray();
                        propertyList.add(getBodyParameters(model.getProperties(), refSet));
                        jsonObject.put(key, propertyList);
                    } else {
                        jsonObject.put(key, new ArrayList<>());
                    }
                } else if (value instanceof RefProperty) {
                    RefProperty refProperty = (RefProperty) value;
                    String simpleRef = refProperty.getSimpleRef();
                    if (refSet.contains(simpleRef)) {
                        //?????????????????????
                        jsonObject.put(key, new JSONArray());
                        return;
                    }
                    refSet.add(simpleRef);
                    Model model = definitions.get(simpleRef);
                    jsonObject.put(key, getBodyParameters(model.getProperties(), refSet));
                } else {
                    jsonObject.put(key, getDefaultValueByPropertyType(value));
                }
            });
        }
        return jsonObject;
    }

    private Object getDefaultValueByPropertyType(Property value) {
        if (value instanceof LongProperty || value instanceof IntegerProperty
                || value instanceof BaseIntegerProperty) {
            return 0;
        } else if (value instanceof FloatProperty || value instanceof DoubleProperty
                || value instanceof DecimalProperty) {
            return 0.0;
        } else {// todo ?????????????
            return getDefaultStringValue(value.getDescription());
        }
    }

    private void parseFormDataParameters(FormParameter parameter, Body body) {
        List<KeyValue> keyValues = Optional.ofNullable(body.getKvs()).orElse(new ArrayList<>());
        KeyValue kv = new KeyValue(parameter.getName(), "", getDefaultStringValue(parameter.getDescription()), parameter.getRequired());
        if (StringUtils.equals(parameter.getType(), "file")) {
            kv.setType("file");
        }
        keyValues.add(kv);
        body.setKvs(keyValues);
    }

    private void parseQueryParameters(Parameter parameter, List<KeyValue> arguments) {
        QueryParameter queryParameter = (QueryParameter) parameter;
        arguments.add(new KeyValue(queryParameter.getName(), "", getDefaultStringValue(queryParameter.getDescription()), queryParameter.getRequired()));
    }
}
