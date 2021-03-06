package io.zeroneqin.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.zeroneqin.base.domain.Organization;
import io.zeroneqin.commons.constants.RoleConstants;
import io.zeroneqin.commons.utils.PageUtils;
import io.zeroneqin.commons.utils.Pager;
import io.zeroneqin.controller.request.OrganizationRequest;
import io.zeroneqin.dto.OrganizationMemberDTO;
import io.zeroneqin.service.OrganizationService;
import io.zeroneqin.service.UserService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("organization")
@RestController
public class OrganizationController {

    @Resource
    private OrganizationService organizationService;
    @Resource
    private UserService userService;

    @PostMapping("/add")
    @RequiresRoles(RoleConstants.ADMIN)
    public Organization addOrganization(@RequestBody Organization organization) {
        return organizationService.addOrganization(organization);
    }

    @GetMapping("/list")
    @RequiresRoles(value = {RoleConstants.ADMIN, RoleConstants.ORG_ADMIN}, logical = Logical.OR)
    public List<Organization> getOrganizationList() {
        return organizationService.getOrganizationList(new OrganizationRequest());
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresRoles(value = {RoleConstants.ADMIN, RoleConstants.ORG_ADMIN}, logical = Logical.OR)
    public Pager<List<Organization>> getOrganizationList(@RequestBody OrganizationRequest request, @PathVariable int goPage, @PathVariable int pageSize) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, organizationService.getOrganizationList(request));
    }

    @GetMapping("/delete/{organizationId}")
    @RequiresRoles(RoleConstants.ADMIN)
    public void deleteOrganization(@PathVariable(value = "organizationId") String organizationId) {
        userService.refreshSessionUser("organization", organizationId);
        organizationService.deleteOrganization(organizationId);
    }

    @PostMapping("/update")
    @RequiresRoles(value = {RoleConstants.ADMIN, RoleConstants.ORG_ADMIN}, logical = Logical.OR)
    public void updateOrganization(@RequestBody Organization organization) {
        organizationService.updateOrganization(organization);
    }

    @GetMapping("/list/userorg/{userId}")
    public List<Organization> getOrganizationListByUserId(@PathVariable String userId) {
        return organizationService.getOrganizationListByUserId(userId);
    }

    @PostMapping("/member/update")
    @RequiresRoles(value = {RoleConstants.ADMIN, RoleConstants.ORG_ADMIN}, logical = Logical.OR)
    public void updateOrgMember(@RequestBody OrganizationMemberDTO memberDTO) {
        organizationService.updateOrgMember(memberDTO);
    }
}
