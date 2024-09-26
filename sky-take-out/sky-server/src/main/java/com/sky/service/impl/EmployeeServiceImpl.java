package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
     private HttpServletRequest request;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //TODO 进行md5加密，然后再进行比对
           password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 员工新增
     * @param employeeDTO
     * @return
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        //创建实体类
        Employee employee=new Employee();
        //对象拷贝
        BeanUtils.copyProperties(employeeDTO,employee);
        //设置员工状态
        employee.setStatus(StatusConstant.ENABLE);
        //设置员工默认密码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //设置创建和更新时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
        //设置创建者
        //或者直接注入HttpServlet
        /*String token=request.getHeader(jwtProperties.getAdminTokenName());
        Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(),token);
        Long id= Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());*/
//        employee.setCreateUser(BaseContext.getCurrentId());
//        //设置更新者
//
//        employee.setUpdateUser(BaseContext.getCurrentId());
        //调用mapper层
        employeeMapper.inset(employee);

    }

    /**
     * 分页查询员工
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult page(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        List<Employee> employeeList= employeeMapper.pageQuery(employeePageQueryDTO);
        Page<Employee> page = (Page<Employee>) employeeList;

        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 修改员工状态
     * @param status1,id
     */
    @Override
    public void update(Integer status1, Long id) {
        //建造对象
        Employee employee=Employee.builder()
                .id(id)
                .status(status1)
                .build();
        employeeMapper.update(employee);

    }

    /**
     * 根据id查询用户信息
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        Employee employee=employeeMapper.getById(id);
        employee.setPassword("*******");
        return employee;
    }

    @Override
    public void updateEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }

    @Override
    public void updatePassword(PasswordEditDTO passwordEditDTO) {
        Employee employee =  employeeMapper.getById(passwordEditDTO.getEmpId());
        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //TODO 进行md5加密，然后再进行比对
        String password = DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        String password1 = DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes());
        if (password1.equals(employee.getPassword())) {
            //修改密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_EDIT_FAILED);
        }
        employeeMapper.update(Employee.builder()
                .id(passwordEditDTO.getEmpId())
                .password(password1)
                .build());


    }

}
