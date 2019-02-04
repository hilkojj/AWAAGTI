# .awaagti database format


## Introduction

The .awaagti database format is used in to store weather data in a binary form.
One .awaagti file represents one second of weather data of all stations that reported data for that second.
Stations that do not report data for that specific second are not included in the file.

The binary format is divided into chunks, one chunk per weather station.
In the first byte of the file, the chunk length in bytes is specified.
Following the chunk length byte are the chunks, until the end of the file.

The reason for having separated files is to make it faster to read specific data.
We have experimented with larger files and having a read method that only reads specific bytes instead of the whole file, with the help of indexes so the reader knows where the requested data is located in a file.
We have learned that, in Java, this doesn't provide a speed boost overall, when executing the queries our customer desires.
The effectiveness of this method is also dependant on the Java version, and the method to read the data, as the standard Java library has multiple competing libraries for opening files and reading the contents.
One advantage of having larger database files is that less storage space is required, as in the chosen method of data storage, every database file also contains the weather station number.

Once a .awaagti file it stored, it should not be modified.
Therefore it is required to store a .awaagti file only after all required data to save in the file has been received.

## Data format

There are two kind of data formats supported by the .awaagti files.
In a regular weather data database file, one chunk is composed of 6 bytes per chunk, in the format:

 - 3 bytes for the ID of the weather station. This allows for station IDs from 0 to 16777215.
 - 2 bytes for the temperature, measured by the weather station. This is a positive number in tenths degrees Celsius, on which 1000 is added. This allows for temperatures from -100.0 to 6543.5 degrees Celsius.
 - 1 byte for the wind speed, in meters per second. This is a positive number in tenths of m/s, which allows to store wind speeds from 0 to 25.5 meters per second.

The usage of chunks, and having specified a chunk length in every database file, makes it possible to extend the chunk data to store more variables, while keeping it backwards compatible on both sides.
For instance; the .awaagti database file reader does not only support the chunk scheme as shown above, but also chunks which do not contain the wind speed, and are therefore 5 bytes in size.
The software will ignore chunk data after after the expected 6 bytes, so new variables can be added without breaking the current behaviour of the current version of the software.

The chunks in the database file are sorted ascendantly on the client ID.

Regular .awaagti files have the time in seconds in unix format as the file name, which is the amount of seconds since the start of 1970 in the Universal Coordinated Time.
For example 1549055732.awaagti, which contains data measured at the time 1549055732.

### Summaries

A second chunk format supported.
This format stores summaries, in which a variable of every weather station in a given time frame is stored as one value.
Currently, summaries are supported for the minimum and maximum value of the temperature and wind speed.
Meaning, there are four different summary files supported.

A summary file is generated for every 100 seconds, and of every 100 summary files, a new summary file is generated, recursively..
The summary file 15490557 summarises the 100 or less (if data is missing) database files from 1549055700 to 1549055799..

The chunk format of a summary file is as follows:

 - 3 bytes for the ID of the weather station.
 - 2 bytes for the grouped value.
   - The min or max temperature uses both these bytes, in the same way as in a regular chunk.
   - The min or max wind speed uses only the first byte, in the same way as in a regular chunk.
 - 3 bytes for the unix time in seconds, of which the value of the station was measured.

Like with regular database files, the file name of summary files are also standardised.
There are in the format: '{variable}_{type summary}_sum.awaagti', for example 'temp_max_sum.awaagti'.

It is the responsibility of the reader to know if the file contains regular chunks, or summary chunks, and also what the summary types are.

## Directory structure

The database is stored in the file system of the operating system kernel, and it known to work on the ext4 file system.
In the database, the files are stored in groups of (maximum) hundred.
Because of this, there are a maximum of 100 database files in a directory, or 100 directories in a directory.
This is to make it faster to request a directory listing from the operating system, which makes it faster to read data from the database.

Following is an example of a schematic drawing of the directory structure:

```
15
    48
        00
            00
                 1548000000.awaagti
                 ...
                 1548000099.awaagti
                 temp_max_sum.awaagti
                 temp_min_sum.awaagti
                 wind_max_sum.awaagti
                  wind_min_sum.awaagti
             ...
             99
                 1548009901.awaagti
                 1548009999.awaagti
                 temp_max_sum.awaagti
                 temp_min_sum.awaagti
                 wind_max_sum.awaagti
                 wind_min_sum.awaagti
             temp_max_sum.awaagti
             temp_min_sum.awaagti
             wind_max_sum.awaagti
             wind_min_sum.awaagti
        ...
        99
	    ...
        temp_max_sum.awaagti
        temp_min_sum.awaagti
        wind_max_sum.awaagti
        wind_min_sum.awaagti
```

You can see how each directory contains either a maximum of 100 database files, or a maximum of 100 directories.
Every directory which time frame is in the past may contain summary files.
It is not required that every directory contains 100 files or directories, if the time frame is in the past.
Missing data is allowed.

## Alternatives

### Compressing directories

We have done research on measured to save on storage space.
One method is to compress database files.
That could be done by compressing the directories containing (a maximum of) 100 database files, and to delete the original directory.
If the reader needs to read the data in the compressed directory, it could decompress the compressed file into /tmp.
The directory '15/48/01/54/' would be replaced by '15/48/01/54.zstd', but only after the unix time 1548015500 is reached.

Compressing directories of summary files, instead of compressing the files separately would allow to compression algorithm to cut down on more redundant data.

If this idea would be realised, the compression algorithm to be chosen should comply with a few goals.
 - The compressed file should be reasonably smaller than the contents of the directory which is compressed, to make it worthwhile.
 - The decompression progress needs to be really fast, to not slow down the reader which is executing a query from the client.

We have chosen not to implement compression in the database.
Our binary format is already efficient enough to meet the requirement of the client.
We have not come across issues regarding storage space, and it would take too much development time to implement compression and decompression.
