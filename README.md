# LuMutator [![Build Status](https://travis-ci.com/ZhongXiLu/LuMutator.svg?token=8ED8fdyNhxKsYhegKEJg&branch=master)](https://travis-ci.com/ZhongXiLu/LuMutator)

*A tool that expands tests to kill survived mutants*
<img src="https://i.imgur.com/NYZ0ZK7.png" width="800">

### Download

Visit the [releases page](https://github.com/ZhongXiLu/LuMutator/releases) to download LuMutator.

### How to use

#### Using PITest

1. First let PITest run the mutation analysis and generate the reports,
make sure there's a directory `/target/pit-reports` present afterwards.
```bash
mvn test -Dfeatures=+EXPORT org.pitest:pitest-maven:mutationCoverage
```
The `pom.xml` file may look something like this:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-maven</artifactId>
            <version>1.4.7</version>
            <configuration>
                <outputFormats>XML</outputFormats>
            </configuration>
            <executions>
                <execution>
                    <id>pitest</id>
                    <goals>
                        <goal>mutationCoverage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

2. Set up the config file for LuMutator; a default one can be downloaded on the [releases page](https://github.com/ZhongXiLu/LuMutator/releases).

3. Now everything is ready to call LuMutator:
```bash
java -jar lumutator-pitest-1.0.jar -c config.xml
```

#### Using another mutation tool

Use the `lumutator-core-1.0.jar` library to call LuMutator and pass the survived mutants. An example of how this is done can be found in the `pitest-example` module.
