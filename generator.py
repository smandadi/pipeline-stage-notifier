# COPYRIGHT: (c) 2018 Daimler AG and Robert Bosch GmbH
# The reproduction, distribution and utilization of this file as
# well as the communication of its contents to others without express
# authorization is prohibited. Offenders will be held liable for the
# payment of damages and can be prosecuted. All rights reserved
# particularly in the event of the grant of a patent, utility model
# or design.

#!/usr/bin/env python3

from sys import argv,exit
from json import dump

stages = argv[3:]
results = ["INPROGRESS", "FAILED", "SUCCESSFUL"]

if len(argv) < 4:
    print("ERROR: Please enter atleast '3' variables to the script \n")
    print("EX: generate.py <JOB_BASE_NAME> <BUILD_URL> stage1 stage2")
    exit(1)


for stage in stages:
    for result in results:
        f_name = result + '-' + stage + '.json'
        description = result + " :: stage :: " + stage
        data = {
            "state" : result,
            "key"   : 0,
            "name"  : argv[1],
            "url"   : argv[2],
            "description" : description
        }
        with open(f_name, 'w' ) as f:
            dump(data, f, indent = 4)
