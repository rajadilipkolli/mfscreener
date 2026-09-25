/* Licensed under Apache-2.0 2022-2024. */
package com.learning.mfscreener.archunit;

public class ArchitectureConstants {
    // Suffixes
    public static final String CONTROLLER_SUFFIX = "Controller";
    public static final String ENTITY_SUFFIX = "Entity";
    public static final String MAPPER_SUFFIX = "Mapper";
    public static final String REPOSITORY_SUFFIX = "Repository";
    public static final String SERVICE_SUFFIX = "Service";

    // Packages
    public static final String CONTROLLER_PACKAGE = "..controllers..";
    public static final String ENTITIES_PACKAGE = "..entities..";
    public static final String MAPPER_PACKAGE = "..mapper..";
    public static final String MODEL_PACKAGE = "..models..";
    public static final String REPOSITORY_PACKAGE = "..repository..";
    public static final String REPOSITORY_IMPL_PACKAGE = "..repository.impl..";
    public static final String SERVICE_PACKAGE = "..service..";

    // Package to scan
    public static final String DEFAULT_PACKAGE = "com.learning.mfscreener";

    // Explanations
    public static final String ANNOTATED_EXPLANATION = "Classes in %s package should be annotated with %s";

    private ArchitectureConstants() {}
}
