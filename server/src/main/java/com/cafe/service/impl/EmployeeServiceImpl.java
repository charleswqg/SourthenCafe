package com.cafe.service.impl;

import com.cafe.constant.MessageConstant;
import com.cafe.constant.PasswordConstant;
import com.cafe.constant.StatusConstant;
import com.cafe.context.BaseContext;
import com.cafe.dto.EmployeeDTO;
import com.cafe.dto.EmployeeLoginDTO;
import com.cafe.dto.EmployeePageQueryDTO;
import com.cafe.entity.Employee;
import com.cafe.exception.AccountLockedException;
import com.cafe.exception.AccountNotFoundException;
import com.cafe.exception.PasswordErrorException;
import com.cafe.mapper.EmployeeMapper;
import com.cafe.result.PageResult;
import com.cafe.service.EmployeeService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.LocalTaskExecutorThreadPool;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

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

        // 把前端的明文密码进行md5加密，然后再与数据库中加密后的密码进行比对.
        password=DigestUtils.md5DigestAsHex(password.getBytes());
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
     * 新增员工
     *
     * @param employeeDTO
     * @return
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee=new Employee();
        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO,employee);
        //设置账号状态
        employee.setStatus(StatusConstant.ENABLE);
        //设置密码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //设置当前记录的创建和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //设置当前用户创建人的id，使用threadlocal从jwt获取当前正在进行操作的用户的id
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        //TODO 后期修改成当前登录用户的id就行
        employeeMapper.insert(employee);

    }
    /**
     * 分页查询员工信息
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee>page=employeeMapper.pageQuery(employeePageQueryDTO);
        long total=page.getTotal();
        List<Employee> records=page.getResult();
        return new PageResult(total,records);
    }

    /**
     * 启用禁用员工账号
     * @param status, id
     * 持久层使用通用update方法，传入employee对象，动态更新重复使用。
     * employee实体类有构建器注解builder
     * @return
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee=Employee.builder().status(status).id(id).build();
        employeeMapper.update(employee);
    }

    /**
     * 根据员工id查询员工信息
     * @param  id
     * @return
     */
    @Override
    public Employee getById(Long id) {
       Employee employee=employeeMapper.getById(id);
       employee.setPassword("****");
       return employee;
    }

    /**
     * 修改员工信息
     * @param  employeeDTO
     * @return
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee=new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }

}
