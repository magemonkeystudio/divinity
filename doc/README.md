[![Build](https://github.com/promcteam/${project.artifactId}/actions/workflows/release.yml/badge.svg?branch=main)](https://s01.oss.sonatype.org/content/repositories/releases/com/promcteam/${project.artifactId}/${project.version})
[![Build](https://github.com/promcteam/${project.artifactId}/actions/workflows/devbuild.yml/badge.svg?branch=dev)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/promcteam/${project.artifactId}/${project.version})

# ${project.name}

If you wish to use ${project.name} as a dependency in your projects, ${project.name} is available through Maven Central
or snapshots through Sonatype.

```xml
<repository>
    <id>sonatype</id>
    <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
</repository>
...
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>${project.artifactId}</artifactId>
    <version>${project.version}</version>
</dependency>
```