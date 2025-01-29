package app.keycloakdemobackend.controller

import app.keycloakdemobackend.model.Employee
import app.keycloakdemobackend.service.EmployeeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
@RequestMapping("/api/v1/employees")
class EmployeeController(
    private val employeeService: EmployeeService
) {
    @PostMapping("/create")
    fun createEmployee(@RequestBody employee: Employee): ResponseEntity<Employee> {
        val employee = employeeService.createEmployee(employee)
        return ResponseEntity.ok(employee)
    }

    @PostMapping("/update")
    fun updateEmployee(@RequestBody employee: Employee): ResponseEntity<Employee> {
        val employee = employeeService.updateEmployee(employee)
        return ResponseEntity.ok(employee)
    }

    @GetMapping("/get/{id}")
    fun getEmployeeById(@PathVariable id: String): ResponseEntity<Employee> {
        val employee = employeeService.getEmployeeById(id)
        return ResponseEntity.ok(employee)
    }

    @DeleteMapping("/deactivate/{id}")
    fun deactivateEmployee(@PathVariable id: String): ResponseEntity<Employee> {
        val deactivatedEmployee = employeeService.deactivateEmployee(id)
        return ResponseEntity.ok(deactivatedEmployee)
    }
}