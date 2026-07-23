package com.checkspace.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignLeadRequest {
    @NotNull private Long propertyId;
    @NotNull private Long employeeId;
}