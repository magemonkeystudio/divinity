[![Build](https://github.com/promcteam/prorpgitems/actions/workflows/publish.yml/badge.svg?branch=dev)](https://github.com/promcteam/promccore/packages/1203729)

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