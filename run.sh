#!/bin/bash
date=`date --iso-8601=seconds`
bash ./gradlew build -x test && java -jar build/libs/sara-with-dependencies-2.0.jar  > >(tee stdout.$date) 2> >(tee stderr.$date >&2)
