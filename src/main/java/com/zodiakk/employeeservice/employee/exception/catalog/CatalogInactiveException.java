package com.zodiakk.employeeservice.employee.exception.catalog;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class CatalogInactiveException extends BusinessException {

    public CatalogInactiveException(String catalogName, UUID id) {
        super(ErrorMessages.CATALOG_INACTIVE.formatted(catalogName, id));
    }
}
