[![Build](https://github.com/magemonkeystudio/${project.artifactId}/actions/workflows/release.yml/badge.svg?branch=main)](https://repo1.maven.org/maven2/studio/magemonkey/${project.artifactId}/${project.version})
[![Build](https://github.com/magemonkeystudio/${project.artifactId}/actions/workflows/devbuild.yml/badge.svg?branch=dev)](https://central.sonatype.com/repository/maven-snapshots/studio/magemonkey/${project.artifactId}/${project.version})
[![Discord](https://dcbadge.limes.pink/api/server/6UzkTe6RvW?style=flat)](https://discord.gg/6UzkTe6RvW)

# ${project.name}

If you wish to use ${project.name} as a dependency in your projects, ${project.name} is available through Maven Central
or snapshots through Sonatype.

```xml
<repository>
    <id>sonatype</id>
    <url>https://central.sonatype.com/repository/maven-snapshots/</url>
</repository>
...
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>${project.artifactId}</artifactId>
    <version>${project.version}</version>
</dependency>
```

### A huge thanks to our contributors

<a href="https://github.com/magemonkeystudio/${project.artifactId}/graphs/contributors">
<img src="https://contrib.rocks/image?repo=magemonkeystudio/${project.artifactId}" />
</a>