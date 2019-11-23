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

## Caveats

- Needs a green test suite
- No random tests (or they need to produce the same results everytime)
- No infinite numbers
- Test cases need to be independent of each other
- Poor optimization (and poor results)

## Case Study

Some results of LuMutator running on real-world projects (thanks to [awesome-java](https://github.com/akullpp/awesome-java) for providing a list):

- **Project**: name of the project as well as the link to the repository (and the right commit)
- **#Tests**: how many tests were ran (excluding skipped ones)
- **#Mutants**: how many mutants were generated (using PITest)
- **#Survived Mutants**: how many mutants survived out of all mutants
- **#Killed Mutants**: how many mutants (+percentage) got killed by LuMutator of the remaining survived mutants

#### Cases where LuMutator produced some results

<table>
    <tr>
        <th>Project</th>
        <th>#Tests</th>
        <th>#Mutants</th>
        <th>#Survived Mutants</th>
        <th>#Killed Mutants</th>
    </tr>
    <tr>
        <td><a href="https://github.com/uklimaschewski/EvalEx/tree/613ae2934b1117d8debbea5356f76d7d0f10279f">EvalEx</a></td>
        <td>131</td>
        <td>535</td>
        <td>132</td>
        <td>8 (~6%)</td>
    </tr>
    <tr>
        <td><a href="https://github.com/romankh3/image-comparison/tree/2035e84e604d5d7c842295b69c233e5674efeb9c">image-comparison</a></td>
        <td>68</td>
        <td>325</td>
        <td>71</td>
        <td>2 (~3%)</td>
    </tr>
    <tr>
        <td><a href="https://github.com/jhy/jsoup/tree/8d1d503913a68e549b5c4a94717c62cf3f64507a">jsoup</a></td>
        <td>695</td>
        <td>4759</td>
        <td>1664</td>
        <td>14 (~1%)</td>
    </tr>
    <tr>
        <td><a href="https://github.com/square/moshi/tree/3c0e3edff3b03c455a1f4c70652c6c58d0dbed7c">moshi</a></td>
        <td>933</td>
        <td>1886</td>
        <td>372</td>
        <td>11 (~3%)</td>
    </tr>
    <tr>
        <td><a href="https://github.com/slub/urnlib/tree/36146fe818244ddc9a6b6bc74285598eedf88698">urnlib</a></td>
        <td>161</td>
        <td>178</td>
        <td>52</td>
        <td>3 (~6%)</td>
    </tr>
</table>

#### And some cases where it didn't... 

<table>
    <tr>
        <th>Project</th>
        <th>#Tests</th>
        <th>#Mutants</th>
        <th>#Survived Mutants</th>
        <th>#Killed Mutants</th>
    </tr>
    <tr>
        <td><a href="https://github.com/awaitility/awaitility/tree/9864c1bfc1b233f24555ceffe117c87ab2414e67">awaitility</a></td>
        <td>113</td>
        <td>571</td>
        <td>270</td>
        <td>0 (0%)</td>
    </tr>
    <tr>
        <td><a href="https://github.com/google/jimfs/tree/989957df7e7fce1f025818e4ccd5a416636dcd51">jimfs</a></td>
        <td>5833</td>
        <td>1944</td>
        <td>452</td>
        <td>0 (0%)</td>
    </tr>
    <tr>
        <td><a href="https://github.com/JodaOrg/joda-money/tree/892ab01c7f63cfff267ba1fbc5ef097a7c01ce45">joda-money</a></td>
        <td>1478</td>
        <td>915</td>
        <td>177</td>
        <td>0 (0%)</td>
    </tr>
    <tr>
        <td><a href="https://github.com/poetix/protonpack/tree/00c55a05a4779926d02d5f4e6c820560a773f9f1">protonpack</a></td>
        <td>87</td>
        <td>605</td>
        <td>254</td>
        <td>0 (0%)</td>
    </tr>
    <tr>
        <td><a href="https://github.com/remondis-it/remap/tree/e172c4f6124fefd97acf26adbc3a8c6cdc52780c">remap</a></td>
        <td>148</td>
        <td>563</td>
        <td>151</td>
        <td>0 (0%)</td>
    </tr>
</table>
