#!/bin/bash

usage()
{
	echo ""
	echo "Script to plot the response time from jmeter"
	echo -e "\033[33mThe script assumes you have GNUPlot installed\033[0m"
	echo ""
	echo "Example of usage:"
	echo "	$0"
	echo ""
}

# How to install gnuplot (MacOS)
#
# brew cask install xquartz
# brew install gnuplot --with-x11
#

REFERENCE_FILE=plot-reference/jmeter_output.csv
FILE=jmeter_output.csv
CLOUDWATCH_FILE=vm_output.csv
REFERENCE_CLOUDWATCH_FILE=plot-reference/vm_output.csv
FORMATTED_FILE=formatted_output.csv
PLOT_INPUT=points.dat
DATE=$(date "+%Y-%m-%d")

if [ ! -f $FILE ] || [ ! -f $CLOUDWATCH_FILE ]; then
	echo "Missing data files jmeter_output.csv or vm_output.csv"
	usage
	exit 1
fi

filter_unused_requests()
{
	cat $FILE |grep -v ".js" |grep -v ".css" |grep -v ".png" |grep -v ".svg" |grep -v ".jpg" |grep -v ".woff" |grep -v ",/," > $FORMATTED_FILE
}

plot_requests_over_time()
{
	TITLE=$1-$DATE
	OUTPUT_FILE=output/$TITLE-time.png
	FILTER=$2

	echo "*** Plotting time for $TITLE"

	cat $FORMATTED_FILE |grep ",true," > OK-$FORMATTED_FILE
	cat $FORMATTED_FILE |grep ",false," > KO-$FORMATTED_FILE
	cat OK-$FORMATTED_FILE |grep $FILTER | cut -d , -f 1,2 | awk -F'\,' '{print substr($1,1,10) "," $2}' > OK-$PLOT_INPUT
	cat KO-$FORMATTED_FILE |grep $FILTER | cut -d , -f 1,2 | awk -F'\,' '{print substr($1,1,10) "," $2}' > KO-$PLOT_INPUT
	
gnuplot <<EOF
	set term png;
	set datafile sep ',';
	set output "$OUTPUT_FILE";
	set xlabel "Date/Time";
	set ylabel "Response time (ms)";
	set xdata time;
	set timefmt "%s";
	set format x "%H:%M:%S";
	set xtics rotate by 45 offset -0.8,-2.5;
	set logscale y;
	set grid;
	set title "$TITLE"
	plot "OK-$PLOT_INPUT" using 1:2 with points lc rgb "blue" title "OK", "KO-$PLOT_INPUT" using 1:2 with points lc rgb "red" title "KO";
EOF
}

plot_cloudwatch_metric()
{
	TITLE=$1-$DATE
	OUTPUT_FILE=output/$TITLE-metric.png
	FILTER=$2

	echo "*** Plotting cloudwatch for $TITLE"

	cat $CLOUDWATCH_FILE |grep $FILTER | cut -d ';' -f 3 > $PLOT_INPUT
	cat $REFERENCE_CLOUDWATCH_FILE |grep $FILTER | cut -d ';' -f 3 > REF-$PLOT_INPUT

gnuplot <<EOF
	set term png;
	set datafile sep ';';
	set output "$OUTPUT_FILE";
	set ylabel "Percent";
	set style line 100 lt 1 lc rgb "gray";
	set style line 101 lt 0 lc rgb "gray";
	set grid xtics ytics mxtics mytics ls 100, ls 101;
	set mxtics 4;
	set mytics;
	set title "$TITLE"
	plot "$PLOT_INPUT" with line lc rgb "blue" title "Actual", "REF-$PLOT_INPUT" with line lc rgb "orange" title "Reference";
EOF
}

plot_percentile_requests()
{
	TITLE=$1-$DATE
	OUTPUT_FILE=output/$TITLE-percentile.png
	FILTER=$2

	echo "*** Plotting percentile for $TITLE"

	cat $FORMATTED_FILE | grep ",true," |grep $FILTER | cut -d , -f 2 |sort -n > tmp.dat
	COUNT=$(cat tmp.dat |awk 'END{print NR}')
	cat -n tmp.dat | awk -v c=$COUNT '{print ($1 * 100 / c) " " $2}' > $PLOT_INPUT
	rm tmp.dat

	cat $REFERENCE_FILE | grep ",true," |grep $FILTER | cut -d , -f 2 |sort -n > tmp.dat
	COUNT=$(cat tmp.dat |awk 'END{print NR}')
	cat -n tmp.dat | awk -v c=$COUNT '{print ($1 * 100 / c) " " $2}' > REF-$PLOT_INPUT
	rm tmp.dat

gnuplot <<EOF
	set term png;
	set output "$OUTPUT_FILE";
	set xlabel "Percentile";
	set ylabel "Response time (ms)";
	set logscale y;
	set style line 100 lt 1 lc rgb "gray";
	set style line 101 lt 0 lc rgb "gray";
	set mxtics 4;
	set mytics;
	set grid xtics ytics mxtics mytics ls 100, ls 101;
	set title "$TITLE"
	plot "$PLOT_INPUT" using 1:2 with line lc rgb "blue" title "Actual", "REF-$PLOT_INPUT" using 1:2 with line lc rgb "orange" title "Reference";
EOF
}

clean_files()
{
	rm $FORMATTED_FILE
	rm OK-$FORMATTED_FILE
	rm KO-$FORMATTED_FILE
	rm $PLOT_INPUT
	rm REF-$PLOT_INPUT
	rm OK-$PLOT_INPUT
	rm KO-$PLOT_INPUT
}

if [ -d output ] ; then
	rm -rf output
fi
mkdir output

filter_unused_requests

## TODO add the regexes for the application graphs
plot_requests_over_time homepage "www.homepage.com/,"
plot_percentile_requests homepage "www.homepage.com/,"


## TODO add the regexes for the VP graphs
plot_cloudwatch_metric service-client-CPUUtilization "service_client_CPUUtilization"
plot_cloudwatch_metric service-client-MemoryUtilization "service_client_MemoryUtilization"

clean_files
