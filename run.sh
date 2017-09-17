#!/bin/bash
#date=`date --iso-8601=seconds`
date=`date -u +"%Y-%m-%dT%H:%M:%S"`
kill -9 `pgrep java`
bash ./gradlew run -x test && java -jar build/libs/sara-with-dependencies-2.0.jar  > >(tee stdout.$date) 2> >(tee stderr.$date >&2)
