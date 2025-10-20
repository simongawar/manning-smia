package com.optimagrowth.license.service;

import org.springframework.stereotype.Service;
import com.optimagrowth.license.model.Organization;

@Service
public class OrganizationService {

    public Organization getOrganization(String organizationId) {
        Organization org = new Organization();
        org.setId(organizationId);
        org.setName("Demo Organization");
        org.setContactName("Jane Doe");
        org.setContactEmail("jane.doe@demo.org");
        org.setContactPhone("+211-999-123456");
        return org;
    }
}