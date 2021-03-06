package io.zeroneqin.service;

import com.alibaba.fastjson.JSON;
import io.zeroneqin.api.dto.datacount.response.TaskInfoResult;
import io.zeroneqin.base.domain.Schedule;
import io.zeroneqin.base.domain.ScheduleExample;
import io.zeroneqin.base.domain.User;
import io.zeroneqin.base.domain.UserExample;
import io.zeroneqin.base.mapper.ScheduleMapper;
import io.zeroneqin.base.mapper.UserMapper;
import io.zeroneqin.base.mapper.ext.ExtScheduleMapper;
import io.zeroneqin.commons.exception.MSException;
import io.zeroneqin.commons.utils.DateUtils;
import io.zeroneqin.commons.utils.LogUtil;
import io.zeroneqin.commons.utils.ServiceUtils;
import io.zeroneqin.commons.utils.SessionUtils;
import io.zeroneqin.controller.request.OrderRequest;
import io.zeroneqin.controller.request.QueryScheduleRequest;
import io.zeroneqin.dto.ScheduleDao;
import io.zeroneqin.job.sechedule.ApiTestJob;
import io.zeroneqin.job.sechedule.ScheduleManager;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ScheduleService {

    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private ScheduleManager scheduleManager;
    @Resource
    private ExtScheduleMapper extScheduleMapper;
    @Resource
    private UserMapper userMapper;

    public void addSchedule(Schedule schedule) {
        schedule.setId(UUID.randomUUID().toString());
        schedule.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        schedule.setCreateTime(System.currentTimeMillis());
        schedule.setUpdateTime(System.currentTimeMillis());
        scheduleMapper.insert(schedule);
    }

    public Schedule getSchedule(String ScheduleId) {
        return scheduleMapper.selectByPrimaryKey(ScheduleId);
    }

    public int editSchedule(Schedule schedule) {
        schedule.setUpdateTime(System.currentTimeMillis());
        return scheduleMapper.updateByPrimaryKeySelective(schedule);
    }

    public Schedule getScheduleByResource(String resourceId, String group) {

        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andResourceIdEqualTo(resourceId).andGroupEqualTo(group);
        List<Schedule> schedules = scheduleMapper.selectByExample(example);
        if (schedules.size() > 0) {
            return schedules.get(0);
        }
        return null;
    }

    public int deleteSchedule(String scheduleId) {
        Schedule schedule = scheduleMapper.selectByPrimaryKey(scheduleId);
        removeJob(schedule.getResourceId());
        return scheduleMapper.deleteByPrimaryKey(scheduleId);
    }

    public int deleteByResourceId(String resourceId) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andResourceIdEqualTo(resourceId);
        removeJob(resourceId);
        return scheduleMapper.deleteByExample(scheduleExample);
    }

    public List<Schedule> listSchedule() {
        ScheduleExample example = new ScheduleExample();
        return scheduleMapper.selectByExample(example);
    }

    public List<Schedule> getEnableSchedule() {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andEnableEqualTo(true);
        return scheduleMapper.selectByExample(example);
    }

    public void startEnableSchedules() {
        List<Schedule> Schedules = getEnableSchedule();
        Schedules.forEach(schedule -> {
            try {
                if (schedule.getEnable()) {
                    LogUtil.error("??????????????????" + JSON.toJSONString(schedule));
                    scheduleManager.addOrUpdateCronJob(new JobKey(schedule.getKey(), schedule.getGroup()),
                            new TriggerKey(schedule.getKey(), schedule.getGroup()), Class.forName(schedule.getJob()), schedule.getValue(),
                            scheduleManager.getDefaultJobDataMap(schedule.getResourceId(), schedule.getValue(), schedule.getUserId()));
                }
            } catch (Exception e) {
                LogUtil.error("?????????????????????", e);
                e.printStackTrace();
            }
        });
    }

    public Schedule buildApiTestSchedule(Schedule request) {
        Schedule schedule = new Schedule();
        schedule.setResourceId(request.getResourceId());
        schedule.setEnable(request.getEnable());
        schedule.setValue(request.getValue().trim());
        schedule.setKey(request.getResourceId());
        schedule.setUserId(SessionUtils.getUser().getId());
        return schedule;
    }

    public void removeJob(String resourceId) {
        scheduleManager.removeJob(ApiTestJob.getJobKey(resourceId), ApiTestJob.getTriggerKey(resourceId));
    }

    public void addOrUpdateCronJob(Schedule request, JobKey jobKey, TriggerKey triggerKey, Class clazz) {
        Boolean enable = request.getEnable();
        String cronExpression = request.getValue();
        if (enable != null && enable && StringUtils.isNotBlank(cronExpression)) {
            try {
                scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, clazz, cronExpression, scheduleManager.getDefaultJobDataMap(request.getResourceId(), cronExpression, SessionUtils.getUser().getId()));
            } catch (SchedulerException e) {
                LogUtil.error(e.getMessage(), e);
                MSException.throwException("????????????????????????");
            }
        } else {
            try {
                scheduleManager.removeJob(jobKey, triggerKey);
            } catch (Exception e) {
                MSException.throwException("????????????????????????");
            }
        }
    }

    public List<ScheduleDao> list(QueryScheduleRequest request) {
        List<OrderRequest> orderList = ServiceUtils.getDefaultOrder(request.getOrders());
        request.setOrders(orderList);
        return extScheduleMapper.list(request);
    }

    public void build(Map<String, String> resourceNameMap, List<ScheduleDao> schedules) {
        List<String> userIds = schedules.stream()
                .map(Schedule::getUserId)
                .collect(Collectors.toList());
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(userIds);
        Map<String, String> userMap = userMapper.selectByExample(example).stream().collect(Collectors.toMap(User::getId, User::getName));
        schedules.forEach(schedule -> {
            schedule.setResourceName(resourceNameMap.get(schedule.getResourceId()));
            schedule.setUserName(userMap.get(schedule.getUserId()));
        });
    }

    public long countTaskByProjectId(String projectId) {
        return  extScheduleMapper.countTaskByProjectId(projectId);
    }

    public long countTaskByProjectIdInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if(firstTime==null || lastTime == null){
            return  0;
        }else {
            return extScheduleMapper.countTaskByProjectIdAndCreateTimeRange(projectId,firstTime.getTime(),lastTime.getTime());
        }
    }

    public List<TaskInfoResult> findRunningTaskInfoByProjectID(String projectID) {
        List<TaskInfoResult> runningTaskInfoList = extScheduleMapper.findRunningTaskInfoByProjectID(projectID);
        return  runningTaskInfoList;
    }
}
