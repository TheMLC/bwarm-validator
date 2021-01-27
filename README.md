# bwarm-validator

Validator for the BWARM Standard.
Details of the Standard: https://kb.ddex.net/display/BWARM

## Current Version
1.0.1

## Current Validations 
* AVS Validation
* AVS Multi-Value Validations
* Number of Fields in each record is as expected
* Mandatory Fields are filled out
* Mandatory Fields are of correct type
	* String
	* Numbers
	* ISO 8601 Date
	* ISO 8601 Duration
* Conditional Fields are present

## Usage 

```
java com.themlc.bwarm.BWARMValidator -d /path/to/snapshots/ -s BWARM_PADPIDA12345678901_20210101010101010
	-d,--snapshot-directory <arg>   BWARM Snapshot Base Folder
	-s,--snapshot <arg>             Snapshot Reference
```
