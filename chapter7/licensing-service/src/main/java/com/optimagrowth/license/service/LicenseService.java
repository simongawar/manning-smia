package com.optimagrowth.license.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.optimagrowth.license.config.ServiceConfig;
import com.optimagrowth.license.model.License;
import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.repository.LicenseRepository;
import com.optimagrowth.license.service.client.OrganizationDiscoveryClient;
import com.optimagrowth.license.service.client.OrganizationFeignClient;
import com.optimagrowth.license.service.client.OrganizationRestTemplateClient;
import com.optimagrowth.license.utils.UserContextHolder;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.bulkhead.annotation.Bulkhead.Type;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class LicenseService {

    @Autowired
    private MessageSource messages;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private ServiceConfig config;

    @Autowired
    private OrganizationFeignClient organizationFeignClient;

    @Autowired
    private OrganizationRestTemplateClient organizationRestClient;

    @Autowired
    private OrganizationDiscoveryClient organizationDiscoveryClient;

    private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

    public License getLicense(String licenseId, String organizationId, String clientType) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        if (license == null) {
            throw new IllegalArgumentException(String.format(
                messages.getMessage("license.search.error.message", null, null),
                licenseId, organizationId));
        }

        Organization organization = retrieveOrganizationInfo(organizationId, clientType);
        if (organization != null) {
            license.setOrganizationName(organization.getName());
            license.setContactName(organization.getContactName());
            license.setContactEmail(organization.getContactEmail());
            license.setContactPhone(organization.getContactPhone());
        }

        return license.withComment(config.getProperty());
    }

    private Organization retrieveOrganizationInfo(String organizationId, String clientType) {
        switch (clientType) {
            case "feign":
                logger.info("Using Feign client");
                return organizationFeignClient.getOrganization(organizationId);
            case "rest":
                logger.info("Using RestTemplate client");
                return organizationRestClient.getOrganization(organizationId);
            case "discovery":
                logger.info("Using Discovery client");
                return organizationDiscoveryClient.getOrganization(organizationId);
            default:
                logger.info("Using default RestTemplate client");
                return organizationRestClient.getOrganization(organizationId);
        }
    }

    public License createLicense(License license) {
        license.setLicenseId(UUID.randomUUID().toString());
        licenseRepository.save(license);
        return license.withComment(config.getProperty());
    }

    public License updateLicense(License license) {
        licenseRepository.save(license);
        return license.withComment(config.getProperty());
    }

    public String deleteLicense(String licenseId) {
        License license = new License();
        license.setLicenseId(licenseId);
        licenseRepository.delete(license);
        return String.format(messages.getMessage("license.delete.message", null, null), licenseId);
    }

    @CircuitBreaker(name = "licenseService", fallbackMethod = "customFallbackLicenseList")
    @RateLimiter(name = "licenseService", fallbackMethod = "customFallbackLicenseList")
    @Retry(name = "retryLicenseService", fallbackMethod = "customFallbackLicenseList")
    @Bulkhead(name = "bulkheadLicenseService", type = Type.THREADPOOL, fallbackMethod = "customFallbackLicenseList")
    public List<License> getLicensesByOrganization(String organizationId) throws TimeoutException {
        logger.debug("getLicensesByOrganization Correlation id: {}", UserContextHolder.getContext().getCorrelationId());
        randomlyRunLong();
        return licenseRepository.findByOrganizationId(organizationId);
    }

    public List<License> customFallbackLicenseList(String organizationId, Throwable throwable) {
        logger.error("CUSTOM FALLBACK ACTIVATED for organizationId: {}", organizationId);
        logger.error("Reason: {}", throwable.getMessage(), throwable);
        logger.warn("Returning default license list due to service failure or timeout.");

        License fallbackLicense = new License();
        fallbackLicense.setLicenseId("CUSTOM-FALLBACK-000");
        fallbackLicense.setOrganizationId(organizationId);
        fallbackLicense.setProductName("Fallback License");
        fallbackLicense.setLicenseType("Trial");
        fallbackLicense.setComment("This is a custom fallback response. Please try again later.");

        List<License> fallbackList = new ArrayList<>();
        fallbackList.add(fallbackLicense);

        logger.info("Fallback license list prepared for organizationId: {}", organizationId);
        return fallbackList;
    }

    private void randomlyRunLong() throws TimeoutException {
        Random rand = new Random();
        int randomNum = rand.nextInt(3) + 1;
        if (randomNum == 3) sleep();
    }

    private void sleep() throws TimeoutException {
        try {
            logger.info("Simulating delay...");
            Thread.sleep(5000);
            throw new TimeoutException("Simulated timeout");
        } catch (InterruptedException e) {
            logger.error("Sleep interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}