package su.nightexpress.quantumrpg.libs.reflection.resolver;

import su.nightexpress.quantumrpg.libs.reflection.resolver.wrapper.ClassWrapper;

public class ClassResolver extends ResolverAbstract<Class> {
    public ClassWrapper resolveWrapper(String... names) {
        return new ClassWrapper(resolveSilent(names));
    }

    public Class resolveSilent(String... names) {
        try {
            return resolve(names);
        } catch (Exception exception) {
            return null;
        }
    }

    public Class resolve(String... names) throws ClassNotFoundException {
        ResolverQuery.Builder builder = ResolverQuery.builder();
        byte b;
        int i;
        String[] arrayOfString;
        for (i = (arrayOfString = names).length, b = 0; b < i; ) {
            String name = arrayOfString[b];
            builder.with(name);
            b++;
        }
        try {
            return resolve(builder.build());
        } catch (ReflectiveOperationException e) {
            throw (ClassNotFoundException) e;
        }
    }

    protected Class resolveObject(ResolverQuery query) throws ReflectiveOperationException {
        return Class.forName(query.getName());
    }

    protected ClassNotFoundException notFoundException(String joinedNames) {
        return new ClassNotFoundException("Could not resolve class for " + joinedNames);
    }
}
