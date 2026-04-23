package com.logitrack.infrastructure.adapter.in.web;

import com.logitrack.application.dto.*;
import com.logitrack.application.service.PackageService;
import com.logitrack.domain.model.PackageStatus;
import com.logitrack.infrastructure.adapter.in.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/packages")
@RequiredArgsConstructor
@Tag(name = "Package Management", description = "Endpoints for managing packages")
@SecurityRequirement(name = "Bearer Authentication")
public class PackageController {

    private final PackageService packageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Create a new package", description = "Creates a new package with the provided details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Package created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<PackageResponse>> createPackage(
            @Valid @RequestBody CreatePackageCommand command) {
        log.info("Creating package for recipient: {}", command.getRecipientEmail());

        PackageResponse response = packageService.createPackage(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PackageResponse>builder()
                        .success(true)
                        .message("Package created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/{packageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Get package by ID", description = "Retrieves package details by its tracking ID")
    public ResponseEntity<ApiResponse<PackageResponse>> getPackage(
            @Parameter(description = "Package tracking ID", example = "LT-123456789")
            @PathVariable String packageId) {
        log.debug("Fetching package: {}", packageId);

        PackageResponse response = packageService.getByIdOrThrow(packageId);

        return ResponseEntity.ok(ApiResponse.<PackageResponse>builder()
                .success(true)
                .message("Package retrieved successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{packageId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Update package status", description = "Updates the status of an existing package")
    public ResponseEntity<ApiResponse<PackageResponse>> updateStatus(
            @PathVariable String packageId,
            @Parameter(description = "New package status")
            @RequestParam PackageStatus status) {
        log.info("Updating package {} status to {}", packageId, status);

        PackageResponse response = packageService.updateStatus(packageId, status);

        return ResponseEntity.ok(ApiResponse.<PackageResponse>builder()
                .success(true)
                .message("Package status updated successfully")
                .data(response)
                .build());
    }

    @PostMapping("/{packageId}/locations")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Add location to package", description = "Adds a new location entry to package tracking history")
    public ResponseEntity<ApiResponse<PackageResponse>> addLocation(
            @PathVariable String packageId,
            @Valid @RequestBody AddLocationCommand command) {
        log.info("Adding location to package: {}", packageId);

        // Ensure packageId matches
        command.setPackageId(packageId);
        PackageResponse response = packageService.addLocation(command);

        return ResponseEntity.ok(ApiResponse.<PackageResponse>builder()
                .success(true)
                .message("Location added successfully")
                .data(response)
                .build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Search packages", description = "Search packages with optional filters and pagination")
    public ResponseEntity<ApiResponse<Page<PackageResponse>>> searchPackages(
            @Parameter(description = "Recipient name (partial match)")
            @RequestParam(required = false) String recipientName,
            @Parameter(description = "Recipient email (exact match)")
            @RequestParam(required = false) String recipientEmail,
            @Parameter(description = "Package status filter")
            @RequestParam(required = false) PackageStatus status,
            @Parameter(description = "Created from date (ISO format)")
            @RequestParam(required = false) String dateFrom,
            @Parameter(description = "Created to date (ISO format)")
            @RequestParam(required = false) String dateTo,
            @Parameter(description = "Include deleted packages")
            @RequestParam(defaultValue = "false") Boolean includeDeleted,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Searching packages with filters - status: {}, recipient: {}", status, recipientEmail);

        PackageService.SearchCriteria criteria = new PackageService.SearchCriteria(
                recipientName, recipientEmail, status, dateFrom, dateTo, includeDeleted
        );

        Page<PackageResponse> response = packageService.searchPackages(criteria, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<PackageResponse>>builder()
                .success(true)
                .message("Search completed successfully")
                .data(response)
                .build());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Get packages by status", description = "Retrieves all packages with the specified status")
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getByStatus(
            @PathVariable PackageStatus status) {
        log.debug("Fetching packages with status: {}", status);

        List<PackageResponse> response = packageService.findByStatus(status);

        return ResponseEntity.ok(ApiResponse.<List<PackageResponse>>builder()
                .success(true)
                .message(String.format("Found %d packages with status %s", response.size(), status))
                .data(response)
                .build());
    }

    @DeleteMapping("/{packageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete package", description = "Soft deletes a package (marks as deleted)")
    public ResponseEntity<ApiResponse<Void>> deletePackage(
            @PathVariable String packageId) {
        log.info("Deleting package: {}", packageId);

        packageService.deletePackage(packageId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Package deleted successfully")
                .build());
    }
}