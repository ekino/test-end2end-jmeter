# End to end tests

Jmeter plans for some end to end tests and the associated scripts to run them.
Those tests must be run in a controlled platform which won't be disturbed
during the tests execution.

[![GitHub license](https://img.shields.io/github/license/ekino/test-end2end-jmeter.svg)](https://github.com/ekino/test-end2end-jmeter/blob/master/LICENSE.md)

## Tests type

The project can run the same JMeter plan against multiple scenarios:
 * Non-regression tests: executed everyday to detect regressions
 * Performance tests: to test the nominal case and obtain the nominal response times
 * Load tests: to test how much load the platform can handle
 
## How to adapt this project?

- [ ] Fork this project to make all your modifications
- [ ] Add into the folder `scripts/jmx_plans/` all the JMeter plans you want to test
- [ ] Link the JMeter plans into the file `scripts/plan.jmx`
- [ ] Add all the different workflows at `src/main/kotlin/com/com/end2end/serivce/RequestsResultsAnalyzer.kt`
- [ ] Implement the way to retrieve the VM metrics at add the output at `scripts/vm_output.csv`
- [ ] Run the first time to obtain the reference data, and move the reference data to the folder `scripts/plot-reference/`
- [ ] Add the regexes to plot the different graphs at `scripts/plot.sh`
- [ ] Implement the way the results are uploaded into your favorite storage system and use it at `src/main/kotlin/com/com/end2end/Application.kt`
- [ ] List all the VM services you want to check at `src/main/kotlin/com/com/end2end/serivce/VMResultsAnalyzer.kt`
- [ ] Implement the continuous delivery pipeline script

# How to rut the non-regression tests script?

`scripts/run.sh nr`

The load used for those kind of tests is just one user at a time. 
All the workflows will be run sequentially.

# How to rut the performance tests script?

```
scripts/run.sh perf
## obtain the VM metrics at this point
scripts/plot.sh
gradle run
```

The load used for those kind of tests is the nominal (adapt it at `scripts/run.sh`)
All the workflows will be run in parallel.

# How to rut the load tests script?

```
scripts/run.sh load
## obtain the VM metrics at this point
scripts/plot.sh
gradle run
``` 

The load used for those kind of tests is very high (adapt it at `scripts/run.sh`)
All the workflows will be run in parallel.