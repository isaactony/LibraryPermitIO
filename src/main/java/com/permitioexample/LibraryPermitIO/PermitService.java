package com.permitioexample.LibraryPermitIO;

import io.permit.sdk.Permit;
import io.permit.sdk.PermitConfig;
import io.permit.sdk.api.PermitApiError;
import io.permit.sdk.api.PermitContextError;
import io.permit.sdk.api.models.CreateOrUpdateResult;
import io.permit.sdk.enforcement.Resource;
import io.permit.sdk.enforcement.User;
import io.permit.sdk.openapi.models.UserRead;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;

@Service
public class PermitService {

    private final Permit permit;

    public PermitService(@Value("${permit.pdp.url}") String pdpUrl, @Value("${permit.sdk.key}") String sdkKey) {
        // Initialize the Permit SDK
        this.permit = new Permit(
                new PermitConfig.Builder(sdkKey)
                        .withPdpAddress(pdpUrl)
                        .withDebugMode(true)
                        .build()
        );
    }

    // Sync a user with Permit.io and assign roles
    public UserRead syncUser(String userId, String email, String firstName, String lastName, String role) {
        try {
            // Optionally define ABAC attributes
            HashMap<String, Object> userAttributes = new HashMap<>();
            userAttributes.put("role", role);

            // Sync the user to Permit.io
            CreateOrUpdateResult<UserRead> response = permit.api.users.sync(
                    new io.permit.sdk.enforcement.User.Builder(userId)
                            .withEmail(email)
                            .withFirstName(firstName)
                            .withLastName(lastName)
                            .withAttributes(userAttributes)
                            .build()
            );

            // Assign the role to the user
            permit.api.users.assignRole(userId, role, "default");

            return response.getResult();

        } catch (IOException | PermitApiError | PermitContextError e) {
            throw new RuntimeException("Failed to sync user with Permit.io", e);
        }
    }

    // Check if a user has permission for a specific action on a resource
    public boolean checkPermission(String userId, String action, String resourceType) throws IOException, PermitApiError, PermitContextError {
        User user = User.fromString(userId);
        Resource resource = new Resource.Builder(resourceType).withTenant("default").build();

        return permit.check(user, action, resource);
    }
}
