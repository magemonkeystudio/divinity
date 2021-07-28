package su.nightexpress.quantumrpg.libs.reflection.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResolverQuery {
    private String name;

    private Class<?>[] types;

    public ResolverQuery(String name, Class... types) {
        this.name = name;
        this.types = types;
    }

    public ResolverQuery(String name) {
        this.name = name;
        this.types = new Class[0];
    }

    public ResolverQuery(Class... types) {
        this.types = types;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return this.name;
    }

    public Class<?>[] getTypes() {
        return this.types;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ResolverQuery that = (ResolverQuery) o;
        if ((this.name != null) ? !this.name.equals(that.name) : (that.name != null))
            return false;
        return Arrays.equals((Object[]) this.types, (Object[]) that.types);
    }

    public int hashCode() {
        int result = (this.name != null) ? this.name.hashCode() : 0;
        result = 31 * result + ((this.types != null) ? Arrays.hashCode((Object[]) this.types) : 0);
        return result;
    }

    public String toString() {
        return "ResolverQuery{name='" +
                this.name + '\'' +
                ", types=" + Arrays.toString((Object[]) this.types) +
                '}';
    }

    public static class Builder {
        private List<ResolverQuery> queryList = new ArrayList<>();

        private Builder() {
        }

        public Builder with(String name, Class[] types) {
            this.queryList.add(new ResolverQuery(name, types));
            return this;
        }

        public Builder with(String name) {
            this.queryList.add(new ResolverQuery(name));
            return this;
        }

        public Builder with(Class[] types) {
            this.queryList.add(new ResolverQuery(types));
            return this;
        }

        public ResolverQuery[] build() {
            return this.queryList.<ResolverQuery>toArray(new ResolverQuery[this.queryList.size()]);
        }
    }
}
