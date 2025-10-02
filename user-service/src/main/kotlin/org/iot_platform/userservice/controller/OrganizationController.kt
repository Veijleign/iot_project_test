package org.iot_platform.userservice.controller

import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import org.iot_platform.userservice.payload.user.OrganizationDto
import org.iot_platform.userservice.service.OrganizationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/organizations")
class OrganizationController(
    private val organizationService: OrganizationService
) {

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    suspend fun createOrganization(
        @Valid @RequestBody orgDto: OrganizationDto
    ): ResponseEntity<OrganizationDto> {
        val organization = organizationService.createOrganization(orgDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(organization)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin') or hasRole('operator')")
    suspend fun getOrganizationById(
        @PathVariable id: UUID
    ): ResponseEntity<OrganizationDto> {
        val organization = organizationService.getDtoById(id)

        return if (organization != null) {
            ResponseEntity.ok(organization)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    suspend fun getAllOrganizations(): ResponseEntity<List<OrganizationDto>> {
        val organizations = organizationService.getAllOrganizationDtos()
        return ResponseEntity.ok(organizations)
    }
}