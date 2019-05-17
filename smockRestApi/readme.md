## REST API that demonstrates account transfers ##

This is a REST API that runs in memory and demonstrates account transfers.  The transfers can occur:
* across accounts of a single user
* across accounts from different users

Each user that gets created has the following set of accounts:
* A savings account with a balance of $100.
* A checking account with a balance of $100.
* A money manager account with a balance of $100.

REST response codes that have been included are as follows:
* 200 - indicates a successful REST call.
* 400 - indicates invalid input provided on REST request.

A test class has been provided to demonstrate the capabilities built into this example project.