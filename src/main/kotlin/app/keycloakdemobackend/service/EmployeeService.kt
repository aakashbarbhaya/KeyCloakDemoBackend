package app.keycloakdemobackend.service

import app.keycloakdemobackend.model.Employee
import app.keycloakdemobackend.repository.EmployeeRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository
) {
    fun createEmployee(employee: Employee): Employee {
        val employeeToCreate = employee.copy(
            id = UUID.randomUUID().toString(),
            createdAt = Instant.now().epochSecond
        )
        return employeeRepository.createEmployee(employeeToCreate)
    }

    fun updateEmployee(employee: Employee): Employee {
        if (employee.id == null) {
            throw IllegalArgumentException("Employee ID cannot be null or blank.")
        }
        return employeeRepository.updateEmployee(employee)
    }

    fun getEmployeeById(id: String): Employee {
        val employee = employeeRepository.getEmployeeById(id) ?: throw IllegalArgumentException("Employee ID cannot be null or blank.")
        return employee
    }
}