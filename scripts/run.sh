#!/usr/bin/env bash

usage()
{
	echo ""
	echo "Script to run some tests"
	echo "Usage: $0 <type>"
	echo -e "	\033[33mtype\033[0m : nr, load, perf"
	echo "				  * nr: non-regression tests"
	echo "				  * laod: load tests"
	echo "				  * perf: performance tests"
	echo ""
	echo "Examples of usages:"
	echo -e "\033[33mFor NR tests\033[0m          : $0 nr"
	echo -e "\033[33mFor load tests\033[0m        : $0 load"
	echo -e "\033[33mFor performance  test\033[0m : $0 perf"
	echo ""
}

TEST_TYPE=$1
CUR_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )

if [ -z "$TEST_TYPE" ]; then
	usage
	exit 1
fi

run_nr()
{
	$CUR_DIR/apache-jmeter-4.0/bin/jmeter.sh -n -t $CUR_DIR/plan.jmx
}

run_load()
{
	sed -i.back 's@<stringProp name="LoopController\.loops">1</stringProp>@<stringProp name="LoopController.loops">-1</stringProp>@g' $CUR_DIR/plan.jmx
	sed -i.back 's@<stringProp name="ThreadGroup.num_threads">1</stringProp>@<stringProp name="ThreadGroup.num_threads">100</stringProp>@g' $CUR_DIR/plan.jmx
	sed -i.back 's@<stringProp name="ThreadGroup.ramp_time">1</stringProp>@<stringProp name="ThreadGroup.ramp_time">120</stringProp>@g' $CUR_DIR/plan.jmx
	sed -i.back 's@<boolProp name="TestPlan.serialize_threadgroups">true</boolProp>@<boolProp name="TestPlan.serialize_threadgroups">false</boolProp>>@g' $CUR_DIR/plan.jmx
	run_nr
	git checkout $CUR_DIR/plan.jmx
	rm CUR_DIR/*.back
}

run_perf()
{
	sed -i.back 's@<stringProp name="LoopController\.loops">1</stringProp>@<stringProp name="LoopController.loops">-1</stringProp>@g' $CUR_DIR/plan.jmx
	sed -i.back 's@<boolProp name="TestPlan.serialize_threadgroups">true</boolProp>@<boolProp name="TestPlan.serialize_threadgroups">false</boolProp>@g' $CUR_DIR/plan.jmx
	run_nr
	git checkout $CUR_DIR/plan.jmx
	rm $CUR_DIR/*.back
}

if [ "$TEST_TYPE" = "nr" ]; then
	rm $CUR_DIR/output.csv
	run_nr
elif [ "$TEST_TYPE" = "load" ]; then
	rm $CUR_DIR/output.csv
	run_load
elif [ "$TEST_TYPE" = "perf" ]; then
	rm $CUR_DIR/output.csv
	run_perf
else
	echo -e "\033[31m Invalid test type\033[0m"
	usage
	exit 2
fi
