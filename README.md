# LuMutator [![Build Status](https://travis-ci.com/ZhongXiLu/LuMutator.svg?token=8ED8fdyNhxKsYhegKEJg&branch=master)](https://travis-ci.com/ZhongXiLu/LuMutator)

*A tool that expands tests to kill survived mutants*


### Prerequisites

- JDK version 9 or 10
- Maven
- PITest

### Build

- Run the tests: `mvn test`
- Build the jar file: `mvn package`

### How to use

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

2. Set up a config file for LuMutator; a default one can be found in `config.xml`.

3. Now everything is ready to call LuMutator:
```bash
java -jar lumutator-1.0-SNAPSHOT.jar -c config.xml
```
