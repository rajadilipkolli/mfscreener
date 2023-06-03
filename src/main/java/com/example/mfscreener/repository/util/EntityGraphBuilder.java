package com.example.mfscreener.repository.util;

import com.example.mfscreener.entities.Identifiable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityGraphBuilder {

    private final Map<Class, EntityVisitor> visitorsMap;

    private final EntityContext entityContext;

    public EntityGraphBuilder(EntityVisitor[] entityVisitors) {
        visitorsMap = new HashMap<>();
        for (EntityVisitor entityVisitor : entityVisitors) {
            visitorsMap.put(entityVisitor.getTargetClazz(), entityVisitor);
        }
        entityContext = new EntityContext(new HashMap<>());
    }

    public EntityContext getEntityContext() {
        return entityContext;
    }

    public EntityGraphBuilder build(List<? extends Identifiable> objects) {
        for (Identifiable object : objects) {
            visit(object);
        }
        return this;
    }

    private <T extends Identifiable, P extends Identifiable> void visit(T object) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) object.getClass();
        @SuppressWarnings("unchecked")
        EntityVisitor<T, P> entityVisitor = visitorsMap.get(clazz);
        if (entityVisitor == null) {
            throw new IllegalArgumentException("Class " + clazz + " has no entityVisitor!");
        }
        entityVisitor.visit(object, entityContext);
        P parent = entityVisitor.getParent(object);
        if (parent != null) {
            visit(parent);
        }
    }
}
