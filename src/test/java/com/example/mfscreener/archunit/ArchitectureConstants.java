/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.archunit;

public class ArchitectureConstants {
    // Suffixes
    public static final String CONTROLLER_SUFFIX = "Controller";
    public static final String REPOSITORY_SUFFIX = "Repository";
    public static final String SERVICE_SUFFIX = "Service";

    // Packages
    public static final String CONTROLLER_PACKAGE = "..controller..";
    public static final String MODEL_PACKAGE = "..models..";
    public static final String DTO_PACKAGE = "..dto..";
    public static final String REPOSITORY_PACKAGE = "..repository..";
    public static final String SERVICE_PACKAGE = "..service.impl..";

    // Package to scan
    public static final String DEFAULT_PACKAGE = "com.example.mfscreener";

    // Explanations
    public static final String ANNOTATED_EXPLANATION =
            "Classes in %s package should be annotated with %s";

    private ArchitectureConstants() {}
}
