[![Release](https://img.shields.io/github/v/release/magemonkeystudio/${project.artifactId}?label=release)](https://github.com/magemonkeystudio/${project.artifactId}/releases/latest)
[![Release Build](https://github.com/magemonkeystudio/${project.artifactId}/actions/workflows/release.yml/badge.svg?branch=dev)](https://github.com/magemonkeystudio/${project.artifactId}/actions/workflows/release.yml)
[![Dev](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.travja.dev%2Fsnapshots%2Fstudio%2Fmagemonkey%2F${project.artifactId}%2Fmaven-metadata.xml&strategy=highestVersion&label=dev)](https://repo.travja.dev/#/snapshots/studio/magemonkey/${project.artifactId})
[![Dev Build](https://github.com/magemonkeystudio/${project.artifactId}/actions/workflows/devbuild.yml/badge.svg?branch=dev)](https://github.com/magemonkeystudio/${project.artifactId}/actions/workflows/devbuild.yml)
[![Discord](https://dcbadge.limes.pink/api/server/mQrkW4htUA?style=flat)](https://discord.gg/mQrkW4htUA)

# ${project.name}

If you wish to use ${project.name} as a dependency in your projects, ${project.name} is available through our repository.

Use the latest release version shown above, or `X.Y.Z-SNAPSHOT` (the next release) for the weekly dev build.

```xml
<repository>
    <id>magemonkey-snapshots</id>
    <url>https://repo.travja.dev/snapshots</url>
</repository>
...
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>${project.artifactId}</artifactId>
    <version>VERSION</version>
</dependency>
```

### A huge thanks to our contributors

<a href="https://github.com/magemonkeystudio/${project.artifactId}/graphs/contributors">
<img src="https://contrib.rocks/image?repo=magemonkeystudio/${project.artifactId}" />
</a>