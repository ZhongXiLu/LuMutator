# LuMutator
[![Build Status](https://travis-ci.com/ZhongXiLu/LuMutator.svg?token=8ED8fdyNhxKsYhegKEJg&branch=master)](https://travis-ci.com/ZhongXiLu/LuMutator)
[![](https://github.com/ZhongXiLu/LuMutator/workflows/Java%20CI/badge.svg)](https://github.com/ZhongXiLu/LuMutator/actions?query=workflow%3A%22Java+CI%22)

*A tool that expands tests to kill survived mutants*

```diff
Mutator: MathMutator (Replaced integer subtraction with addition)
src/main/java/bank/Bank.java:
  96                Customer toCustomer = customers.get(toCustomerId);
  97
  98                if (fromCustomer.getBalance() >= amount) {
! 99                    fromCustomer.setBalance(fromCustomer.getBalance() - amount);
  100                    toCustomer.setBalance(toCustomer.getBalance() + amount);
  101                    return true;
  102                }


src/test/java/bank/BankTest.java:
  58
  59        boolean success = bank.internalTransfer(customer1.getAccountNumber(), customer2.getAccountNumber(), balanceCustomer1);
  60
+ 61        assertEquals(0, customer1.getBalance());
  62        assertEquals(balanceCustomer1 + balanceCustomer2, customer2.getBalance());
  63    }
  64

(4/5) Add this new assertion? (Y/N):
```

## Download

Either visit the [releases page](https://github.com/ZhongXiLu/LuMutator/releases) to directly download the jar files or visit [packages page](https://github.com/ZhongXiLu/LuMutator/packages) to add the jar files to your `pom.xml`.

## How to use

### Using [PITest](http://pitest.org/)

1. First let PITest run the mutation analysis and generate the reports,
make sure there's a directory `/target/pit-reports` present afterwards.
```bash
mvn test -Dfeatures=+EXPORT org.pitest:pitest-maven:mutationCoverage
```
To include PITest into your project, add the following plugin in your `pom.xml`:
```xml
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
```

2. Set up the [config file](https://github.com/ZhongXiLu/LuMutator/releases/download/v1.0/config.xml) for LuMutator; a default one can be downloaded on the [releases page](https://github.com/ZhongXiLu/LuMutator/releases).

3. Now everything is ready to call LuMutator:
```bash
java -jar pitest-example-1.0.jar -c config.xml
```

### Using another mutation tool

Use the [`lumutator-core-1.0.jar`](https://github.com/ZhongXiLu/LuMutator/packages/57327) library to call LuMutator and pass the survived mutants. An example of how this is done can be found in the [`pitest-example` module](https://github.com/ZhongXiLu/LuMutator/tree/master/pitest-example/src/main/java/pitest-example).
