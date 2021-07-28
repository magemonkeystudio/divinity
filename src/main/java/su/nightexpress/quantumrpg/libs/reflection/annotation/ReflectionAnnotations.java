package su.nightexpress.quantumrpg.libs.reflection.annotation;

import su.nightexpress.quantumrpg.libs.reflection.minecraft.Minecraft;
import su.nightexpress.quantumrpg.libs.reflection.resolver.ClassResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.FieldResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.MethodResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.wrapper.ClassWrapper;
import su.nightexpress.quantumrpg.libs.reflection.resolver.wrapper.FieldWrapper;
import su.nightexpress.quantumrpg.libs.reflection.resolver.wrapper.MethodWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReflectionAnnotations {
    public static final ReflectionAnnotations INSTANCE = new ReflectionAnnotations();

    static final Pattern classRefPattern = Pattern.compile("@Class\\((.*)\\)");

    public void load(Object toLoad) {
        if (toLoad == null)
            throw new IllegalArgumentException("toLoad cannot be null");
        ClassResolver classResolver = new ClassResolver();
        byte b;
        int i;
        java.lang.reflect.Field[] arrayOfField;
        for (i = (arrayOfField = toLoad.getClass().getDeclaredFields()).length, b = 0; b < i; ) {
            java.lang.reflect.Field field = arrayOfField[b];
            Class classAnnotation = field.getAnnotation(Class.class);
            Field fieldAnnotation = field.getAnnotation(Field.class);
            Method methodAnnotation = field.getAnnotation(Method.class);
            if (classAnnotation != null || fieldAnnotation != null || methodAnnotation != null) {
                field.setAccessible(true);
                if (classAnnotation != null) {
                    List<String> nameList = parseAnnotationVersions(Class.class, classAnnotation);
                    if (nameList.isEmpty())
                        throw new IllegalArgumentException("@Class names cannot be empty");
                    String[] names = nameList.<String>toArray(new String[nameList.size()]);
                    for (int j = 0; j < names.length; j++)
                        names[j] = names[j]
                                .replace("{nms}", "net.minecraft.server." + Minecraft.VERSION.name())
                                .replace("{obc}", "org.bukkit.craftbukkit." + Minecraft.VERSION.name());
                    try {
                        if (ClassWrapper.class.isAssignableFrom(field.getType())) {
                            field.set(toLoad, classResolver.resolveWrapper(names));
                        } else if (java.lang.Class.class.isAssignableFrom(field.getType())) {
                            field.set(toLoad, classResolver.resolve(names));
                        } else {
                            throwInvalidFieldType(field, toLoad, "Class or ClassWrapper");
                            return;
                        }
                    } catch (ReflectiveOperationException e) {
                        if (!classAnnotation.ignoreExceptions()) {
                            throwReflectionException("@Class", field, toLoad, e);
                            return;
                        }
                    }
                } else if (fieldAnnotation != null) {
                    List<String> nameList = parseAnnotationVersions(Field.class, fieldAnnotation);
                    if (nameList.isEmpty())
                        throw new IllegalArgumentException("@Field names cannot be empty");
                    String[] names = nameList.<String>toArray(new String[nameList.size()]);
                    try {
                        FieldResolver fieldResolver = new FieldResolver(parseClass(Field.class, fieldAnnotation, toLoad));
                        if (FieldWrapper.class.isAssignableFrom(field.getType())) {
                            field.set(toLoad, fieldResolver.resolveWrapper(names));
                        } else if (Field.class.isAssignableFrom(field.getType())) {
                            field.set(toLoad, fieldResolver.resolve(names));
                        } else {
                            throwInvalidFieldType(field, toLoad, "Field or FieldWrapper");
                            return;
                        }
                    } catch (ReflectiveOperationException e) {
                        if (!fieldAnnotation.ignoreExceptions()) {
                            throwReflectionException("@Field", field, toLoad, e);
                            return;
                        }
                    }
                } else if (methodAnnotation != null) {
                    List<String> nameList = parseAnnotationVersions(Method.class, methodAnnotation);
                    if (nameList.isEmpty())
                        throw new IllegalArgumentException("@Method names cannot be empty");
                    String[] names = nameList.<String>toArray(new String[nameList.size()]);
                    boolean isSignature = names[0].contains(" ");
                    byte b1;
                    int j;
                    String[] arrayOfString1;
                    for (j = (arrayOfString1 = names).length, b1 = 0; b1 < j; ) {
                        String s = arrayOfString1[b1];
                        if (s.contains(" ") != isSignature)
                            throw new IllegalArgumentException("Inconsistent method names: Cannot have mixed signatures/names");
                        b1++;
                    }
                    try {
                        MethodResolver methodResolver = new MethodResolver(parseClass(Method.class, methodAnnotation, toLoad));
                        if (MethodWrapper.class.isAssignableFrom(field.getType())) {
                            if (isSignature) {
                                field.set(toLoad, methodResolver.resolveSignatureWrapper(names));
                            } else {
                                field.set(toLoad, methodResolver.resolveWrapper(names));
                            }
                        } else if (Method.class.isAssignableFrom(field.getType())) {
                            if (isSignature) {
                                field.set(toLoad, methodResolver.resolveSignature(names));
                            } else {
                                field.set(toLoad, methodResolver.resolve(names));
                            }
                        } else {
                            throwInvalidFieldType(field, toLoad, "Method or MethodWrapper");
                            return;
                        }
                    } catch (ReflectiveOperationException e) {
                        if (!methodAnnotation.ignoreExceptions()) {
                            throwReflectionException("@Method", field, toLoad, e);
                            return;
                        }
                    }
                }
            }
            b++;
        }
    }

    <A extends java.lang.annotation.Annotation> List<String> parseAnnotationVersions(java.lang.Class<A> clazz, A annotation) {
        List<String> list = new ArrayList<>();
        try {
            String[] names = (String[]) clazz.getMethod("value", new java.lang.Class[0]).invoke(annotation, new Object[0]);
            Minecraft.Version[] versions = (Minecraft.Version[]) clazz.getMethod("versions", new java.lang.Class[0]).invoke(annotation, new Object[0]);
            if (versions.length == 0) {
                byte b;
                int i;
                String[] arrayOfString;
                for (i = (arrayOfString = names).length, b = 0; b < i; ) {
                    String name = arrayOfString[b];
                    list.add(name);
                    b++;
                }
            } else {
                if (versions.length > names.length)
                    throw new RuntimeException("versions array cannot have more elements than the names (" + clazz + ")");
                for (int i = 0; i < versions.length; i++) {
                    if (Minecraft.VERSION == versions[i]) {
                        list.add(names[i]);
                    } else if (names[i].startsWith(">") && Minecraft.VERSION.newerThan(versions[i])) {
                        list.add(names[i].substring(1));
                    } else if (names[i].startsWith("<") && Minecraft.VERSION.olderThan(versions[i])) {
                        list.add(names[i].substring(1));
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    <A extends java.lang.annotation.Annotation> String parseClass(java.lang.Class<A> clazz, A annotation, Object toLoad) {
        try {
            String className = (String) clazz.getMethod("className", new java.lang.Class[0]).invoke(annotation, new Object[0]);
            Matcher matcher = classRefPattern.matcher(className);
            while (matcher.find()) {
                if (matcher.groupCount() != 1)
                    continue;
                String fieldName = matcher.group(1);
                java.lang.reflect.Field field = toLoad.getClass().getField(fieldName);
                if (ClassWrapper.class.isAssignableFrom(field.getType()))
                    return ((ClassWrapper) field.get(toLoad)).getName();
                if (java.lang.Class.class.isAssignableFrom(field.getType()))
                    return ((java.lang.Class) field.get(toLoad)).getName();
            }
            return className;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    void throwInvalidFieldType(java.lang.reflect.Field field, Object toLoad, String expected) {
        throw new IllegalArgumentException("Field " + field.getName() + " in " + toLoad.getClass() + " is not of type " + expected + ", it's " + field.getType());
    }

    void throwReflectionException(String annotation, java.lang.reflect.Field field, Object toLoad, ReflectiveOperationException exception) {
        throw new RuntimeException("Failed to set " + annotation + " field " + field.getName() + " in " + toLoad.getClass(), exception);
    }
}
