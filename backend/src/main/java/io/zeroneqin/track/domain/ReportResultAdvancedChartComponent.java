package io.zeroneqin.track.domain;

import io.zeroneqin.api.dto.automation.ApiScenarioDTO;
import io.zeroneqin.api.dto.automation.ScenarioStatus;
import io.zeroneqin.api.dto.definition.TestPlanApiCaseDTO;
import io.zeroneqin.commons.constants.TestPlanTestCaseStatus;
import io.zeroneqin.track.dto.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportResultAdvancedChartComponent extends ReportComponent {
    Map<String, TestCaseReportStatusResultDTO> functionalStatusResultMap = new HashMap<>();
    Map<String, TestCaseReportStatusResultDTO> apiStatusResultMap = new HashMap<>();
    Map<String, TestCaseReportStatusResultDTO> scenarioStatusResultMap = new HashMap<>();

    private static Map<String, String> apiResultMap = new HashMap<>();
    private static Map<String, String> scenarioResultMap = new HashMap<>();

    static {
        apiResultMap.put("success", TestPlanTestCaseStatus.Pass.name());
        apiResultMap.put("error", TestPlanTestCaseStatus.Failure.name());
        scenarioResultMap.put(ScenarioStatus.Success.name(), TestPlanTestCaseStatus.Pass.name());
        scenarioResultMap.put(ScenarioStatus.Fail.name(), TestPlanTestCaseStatus.Failure.name());
    }

    public ReportResultAdvancedChartComponent(TestPlanDTO testPlan) {
        super(testPlan);
        componentId = "3";
    }

    @Override
    public void readRecord(TestPlanCaseDTO testCase) {
        getStatusResultMap(functionalStatusResultMap, testCase.getStatus());
    }

    @Override
    public void readRecord(TestPlanApiCaseDTO testCase) {
        getStatusResultMap(apiStatusResultMap, apiResultMap.get(testCase.getExecResult()));
    }

    @Override
    public void readRecord(ApiScenarioDTO testCase) {
        getStatusResultMap(scenarioStatusResultMap, scenarioResultMap.get(testCase.getLastResult()));
    }

    @Override
    public void afterBuild(TestCaseReportMetricDTO testCaseReportMetric) {
        testCaseReportMetric.setExecuteResult(getReportStatusResult());
    }

    private TestCaseReportAdvanceStatusResultDTO getReportStatusResult() {
        TestCaseReportAdvanceStatusResultDTO reportStatusResult = new TestCaseReportAdvanceStatusResultDTO();
        buildFunctionalStatusResult(reportStatusResult);
        buildApiStatusResult(reportStatusResult);
        buildScenarioStatusResult(reportStatusResult);
        return reportStatusResult;
    }

    private void buildFunctionalStatusResult(TestCaseReportAdvanceStatusResultDTO reportStatusResult) {
        List<TestCaseReportStatusResultDTO> functionalStatusResult = new ArrayList<>();
        addToReportStatusResultList(functionalStatusResultMap, functionalStatusResult, TestPlanTestCaseStatus.Pass.name());
        addToReportStatusResultList(functionalStatusResultMap, functionalStatusResult, TestPlanTestCaseStatus.Failure.name());
        addToReportStatusResultList(functionalStatusResultMap, functionalStatusResult, TestPlanTestCaseStatus.Blocking.name());
        addToReportStatusResultList(functionalStatusResultMap, functionalStatusResult, TestPlanTestCaseStatus.Skip.name());
        addToReportStatusResultList(functionalStatusResultMap, functionalStatusResult, TestPlanTestCaseStatus.Underway.name());
        addToReportStatusResultList(functionalStatusResultMap, functionalStatusResult, TestPlanTestCaseStatus.Prepare.name());
        reportStatusResult.setFunctionalResult(functionalStatusResult);
    }

    private void buildApiStatusResult(TestCaseReportAdvanceStatusResultDTO reportStatusResult) {
        List<TestCaseReportStatusResultDTO> apiStatusResult = new ArrayList<>();
        addToReportStatusResultList(apiStatusResultMap, apiStatusResult, TestPlanTestCaseStatus.Pass.name());
        addToReportStatusResultList(apiStatusResultMap, apiStatusResult, TestPlanTestCaseStatus.Failure.name());
        addToReportStatusResultList(apiStatusResultMap, apiStatusResult, TestPlanTestCaseStatus.Underway.name());
        reportStatusResult.setApiResult(apiStatusResult);
    }

    private void buildScenarioStatusResult(TestCaseReportAdvanceStatusResultDTO reportStatusResult) {
        List<TestCaseReportStatusResultDTO> scenarioStatusResult = new ArrayList<>();
        addToReportStatusResultList(scenarioStatusResultMap, scenarioStatusResult, TestPlanTestCaseStatus.Pass.name());
        addToReportStatusResultList(scenarioStatusResultMap, scenarioStatusResult, TestPlanTestCaseStatus.Failure.name());
        addToReportStatusResultList(scenarioStatusResultMap, scenarioStatusResult, TestPlanTestCaseStatus.Underway.name());
        reportStatusResult.setScenarioResult(scenarioStatusResult);
    }

    private void addToReportStatusResultList(Map<String, TestCaseReportStatusResultDTO> resultMap, List<TestCaseReportStatusResultDTO> reportStatusResultList, String status) {
        if (resultMap.get(status) != null) {
            reportStatusResultList.add(resultMap.get(status));
        }
    }

    private void getStatusResultMap(Map<String, TestCaseReportStatusResultDTO> reportStatusResultMap, String result) {
        if (StringUtils.isBlank(result)) {
            result = TestPlanTestCaseStatus.Underway.name();
        }
        TestCaseReportStatusResultDTO statusResult = reportStatusResultMap.get(result);
        if (statusResult == null) {
            statusResult = new TestCaseReportStatusResultDTO();
            statusResult.setStatus(result);
            statusResult.setCount(0);
        }
        statusResult.setCount(statusResult.getCount() + 1);
        reportStatusResultMap.put(result, statusResult);
    }
}
