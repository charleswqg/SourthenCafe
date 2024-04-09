package com.cafe.service;

import com.cafe.dto.EmployeeLoginDTO;
import com.cafe.entity.Employee;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

}
