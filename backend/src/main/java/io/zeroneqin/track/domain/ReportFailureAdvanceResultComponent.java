package io.zeroneqin.track.domain;

import io.zeroneqin.api.dto.automation.ApiScenarioDTO;
import io.zeroneqin.api.dto.automation.ScenarioStatus;
import io.zeroneqin.api.dto.definition.TestPlanApiCaseDTO;
import io.zeroneqin.commons.constants.TestPlanTestCaseStatus;
import io.zeroneqin.track.dto.FailureTestCasesAdvanceDTO;
import io.zeroneqin.track.dto.TestCaseReportMetricDTO;
import io.zeroneqin.track.dto.TestPlanCaseDTO;
import io.zeroneqin.track.dto.TestPlanDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ReportFailureAdvanceResultComponent extends ReportComponent {
    private List<TestPlanCaseDTO> functionalTestCases = new ArrayList<>();
    private List<TestPlanApiCaseDTO> apiTestCases = new ArrayList<>();
    private List<ApiScenarioDTO> scenarioTestCases = new ArrayList<>();

    public ReportFailureAdvanceResultComponent(TestPlanDTO testPlan) {
        super(testPlan);
        componentId = "4";
    }

    @Override
    public void readRecord(TestPlanCaseDTO testCase) {
        if (StringUtils.equals(testCase.getStatus(), TestPlanTestCaseStatus.Failure.name())) {
            this.functionalTestCases.add(testCase);
        }
    }

    @Override
    public void readRecord(TestPlanApiCaseDTO testCase) {
        if (StringUtils.equals(testCase.getExecResult(), "error")) {
            this.apiTestCases.add(testCase);
        }
    }

    @Override
    public void readRecord(ApiScenarioDTO testCase) {
        if (StringUtils.equals(testCase.getLastResult(), ScenarioStatus.Fail.name())) {
            this.scenarioTestCases.add(testCase);
        }
    }

    @Override
    public void afterBuild(TestCaseReportMetricDTO testCaseReportMetric) {
        FailureTestCasesAdvanceDTO failureTestCasesAdvanceDTO = new FailureTestCasesAdvanceDTO();
        failureTestCasesAdvanceDTO.setFunctionalTestCases(functionalTestCases);
        failureTestCasesAdvanceDTO.setApiTestCases(apiTestCases);
        failureTestCasesAdvanceDTO.setScenarioTestCases(scenarioTestCases);
        testCaseReportMetric.setFailureTestCases(failureTestCasesAdvanceDTO);
    }
}
