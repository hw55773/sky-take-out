package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
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
    @ApiOperation(value = "员工登录")
    @PostMapping("/login")
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
    @ApiOperation("员工退出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 员工新增
     * @param employeeDTO
     * @return
     */
    @PostMapping
    @ApiOperation("员工新增")
    public Result save(@RequestBody EmployeeDTO employeeDTO){

        log.info("新增员工  {}",employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询员工")
    public Result page(EmployeePageQueryDTO employeePageQueryDTO){

        log.info("分页查询 {}",employeePageQueryDTO);
      PageResult pageResult= employeeService.page(employeePageQueryDTO);

        return  Result.success(pageResult);
    }

    /**
     * 修改员工状态
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用员工")
    public  Result startOrOff(@PathVariable("status")Integer status1, Long id){
        log.info("修改员工状态，{}，{}",status1,id);
        employeeService.update(status1,id);

        return Result.success();
    }

    /**
     * 根据id查询用户信息
     * @param id
     * @return
     */
    @ApiOperation("根据id查询用户信息")
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("根据id查询用户信息: {}",id);
        Employee employee=employeeService.getById(id);

        return Result.success(employee);
    }
    @PutMapping
    @ApiOperation("修改员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO){

        log.info("修改员工信息：{}",employeeDTO);
        employeeService.updateEmployee(employeeDTO);
        return  Result.success();
    }
    @PutMapping("/editPassword")
    @ApiOperation("员工修改密码")
    public Result updatePassWord(@RequestBody PasswordEditDTO passwordEditDTO){
            log.info("员工修改密码  {}",passwordEditDTO);
            employeeService.updatePassword(passwordEditDTO);
        return Result.success();
    }

}
