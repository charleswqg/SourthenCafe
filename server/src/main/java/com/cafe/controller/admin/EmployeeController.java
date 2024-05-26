package com.cafe.controller.admin;

import com.cafe.constant.JwtClaimsConstant;
import com.cafe.dto.EmployeeDTO;
import com.cafe.dto.EmployeeLoginDTO;
import com.cafe.dto.EmployeePageQueryDTO;
import com.cafe.entity.Employee;
import com.cafe.properties.JwtProperties;
import com.cafe.result.PageResult;
import com.cafe.result.Result;
import com.cafe.service.EmployeeService;
import com.cafe.utils.JwtUtil;
import com.cafe.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录") //生成接口文档的注解
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation(value = "员工退出")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工,路径已经对上了，不用额外加
     *前端传过来的是json格式数据，要加requestbody
     * @return
     */
    @PostMapping()
    @ApiOperation(value = "新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工:{}",employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }
    /**
     * 分页查询员工信息
     *
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工查询:{}",employeePageQueryDTO);
        PageResult pg=employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pg);
    }

    /**
     * 启用禁用员工账号，路径参数加pathvariable，地址栏参数不用管
     *status 1或0，id员工id
     */
    @PostMapping("/status/{status}")
    @ApiOperation(value = "启用禁用员工账号")
    public Result startOrStop(@PathVariable("status") Integer status, Long id){
        employeeService.startOrStop(status,id);
        return  Result.success();
    }

    /**
     * 根据id查询员工信息
     *id员工id
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询员工信息")
    public Result<Employee> getById(@PathVariable Long id){
       Employee employee=employeeService.getById(id);
       return Result.success(employee);
    }

    @PutMapping()
    @ApiOperation(value = "编辑员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO){
        employeeService.update(employeeDTO);
        return Result.success();
    }


}
