#!/bin/bash
#date=`date --iso-8601=seconds`
date=`date -u +"%Y-%m-%dT%H:%M:%S"`
kill -9 `pgrep java` # kill left over java processes
#find ~/.gradle/caches -type f -name '*multiuser*' -exec rm {} \; # reload the multiuser-JARs on every invocation
bash ./gradlew run -x test > >(tee stdout.$date) 2> >(tee stderr.$date >&2)
echo "finished running" `date`
