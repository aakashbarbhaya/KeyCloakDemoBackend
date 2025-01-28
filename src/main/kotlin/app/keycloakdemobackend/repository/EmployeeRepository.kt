package app.keycloakdemobackend.repository

import app.keycloakdemobackend.model.Employee

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class EmployeeRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    private fun rowMapper(rs: java.sql.ResultSet): Employee {
        return Employee(
            id = rs.getString("id"),
            firstName = rs.getString("first_name"),
            lastName = rs.getString("last_name"),
            email = rs.getString("email"),
            contactNumber = rs.getString("contact_number"),
            roles = rs.getArray("roles").let { array ->
                (array?.array as? Array<*>)?.filterIsInstance<String>() ?: emptyList()
            },
            createdAt = rs.getLong("created_at"),
            status = rs.getString("status")
        )
    }

    fun createEmployee(employee: Employee): Employee {
        return try {
            val sql = """
            INSERT INTO employees
            (id, first_name, last_name, email, contact_number, roles, created_at, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """
            jdbcTemplate.update(
                sql,
                employee.id,
                employee.firstName,
                employee.lastName,
                employee.email,
                employee.contactNumber,
                employee.roles.toTypedArray(), // Convert List<String> to Array for PostgreSQL TEXT[]
                employee.createdAt,
                employee.status
            )
            employee
        } catch (ex: Exception) {
            throw RuntimeException("Failed to create employee with ID: ${employee.id}", ex)
        }
    }

    fun updateEmployee(employee: Employee): Employee {
        return try {
            val sql = """
            UPDATE employees 
            SET first_name = ?, 
                last_name = ?, 
                email = ?, 
                contact_number = ?, 
                roles = ?, 
                status = ?, 
                created_at = ?
            WHERE id = ?
        """
            jdbcTemplate.update(
                sql,
                employee.firstName,
                employee.lastName,
                employee.email,
                employee.contactNumber,
                employee.roles.toTypedArray(), // Convert List<String> to Array for PostgreSQL TEXT[]
                employee.status,
                employee.createdAt,
                employee.id
            )
            employee
        } catch (ex: Exception) {
            throw RuntimeException("Failed to update employee with ID: ${employee.id}", ex)
        }
    }

    fun getEmployeeById(id: String): Employee? {
        val sql = "SELECT * FROM employees WHERE id = ?"
        val employee = jdbcTemplate.queryForObject(sql, { rs, _ -> rowMapper(rs) }, id)
        return employee
    }
}