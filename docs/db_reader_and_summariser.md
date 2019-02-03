# DB Writer

DB Writer is a TCP server which is responsible from taking the weather data from the Raspberry Pi and storing it in the database.
DB Writer is a daemon process, which listens on a configured TCP port for Pi's to connect to.

Documentation regarding the code can be found by reading the comments of the source code, and the comments in the version control systemd which was used during development.

TCP is used to make sure no data is lost during transport.
The extra overhead and delay of TCP is of no burden to the user.
The format of the TCP stream is divided into a few states:

 - a state signalling the start of period and waiting for the time stamp,
 - a state in which the time stamp is known and data is expected,
 - a state in which data is received,
 - and a state in which data is received and the received data can be persisted.

The data format has been designed with these four states in mind.
It is as follows:

```
START
{date in yyyy-mm-dd},{time in hh:mm:ss}
{station id},{temperature in C, 1 decimal},{wind speed in m/s, 1 decimal}
END
```

The 'end' signals that no more data is coming for the second, and the received data for the second can be written to the database.

For example, for a second in which only two weather stations reported their data to the Pi, the data stream would be as follows:

```
START
2019-02-01,15:09:33
1337,25.5,5.5
1338,22.5,1.3
END
```

The data format allows for adding more variables received from weather stations, while keeping the software backwards compatible with older versions.
This has been particularly useful during development.
A new variable can be added by adding an extra comma after the station data, with the new value after it.

DB Writer allows for multiple Pi's to connect simultaneously.

## Usage

```

$ db_writer {tcp port to listen to}
```
## On the production server

On the VM server, DB Writer is running as a daemon configured to be run by systemd, the default init system on Debian.

The systemd service file is as follows:

```
[Unit]
Description=db_writer

[Service]
WorkingDirectory=/home/ITV2E03/vmdb/db_writer
Restart=always
ExecStart=/home/ITV2E03/vmdb/db_writer/run.sh
Type=simple
User=ITV2E03

[Install]
WantedBy=multi-user.target
```

run.sh is as follows:

```
#!/bin/bash

cd ../data

java -jar ../db_writer/db_writer.jar 2049
```

Having a separate run.sh method allows for configuring the TCP port and data storage directory, without having to change the service file.

# DB Summariser

DB Summariser makes summary files in the format described in the documentation of the .awaagti database files.
The program will not summarise data which is already summarised, as database files should be immutable.

Documentation regarding the code can be found by reading the comments of the source code, and the comments in the version control systemd which was used during development.

## Usage

When executing db_summariser, the user should configure the time period to summarise, which is supplied as the command line arguments.
The from and end period are supplied in unix time in seconds.
The end time is optional, and when not specified, it defaults to the current system time. 

```
$ db_summariser {start of period} [optional, end of period]
```

## On the production server

On the VM server, DB Summariser is running as a systemd timer, the default init system on Debian.
The reason for using systemd, instead of cron, is that systemd is installed by default on Debian, and as it is the init system, it is the preferred choise for running daemons.
Systemd's timer functionality has been developed to replace cron on modern distributions of GNU/Linux.

The systemd service file and systemd timer are as follows:

```
[Unit]
Description=db_writer

[Service]
WorkingDirectory=/home/ITV2E03/vmdb/db_summariser
ExecStart=/home/ITV2E03/vmdb/db_summariser/run.sh
Type=simple
User=ITV2E03

[Install]
WantedBy=multi-user.target
```

```
[Unit]
Description=DBSummariser every minute

[Timer]
OnCalendar=*-*-* *:*:00
 
[Install]
WantedBy=timers.target
```

This executes db_summariser every start of the minute. Ideally, db_summariser would be executed at the start of every 100 seconds, but this is not possible with systemd. And neither with cron.

run.sh is as follows:

```
#!/bin/bash

cd ../data

java -jar ../db_summariser/db_summariser.jar $(date -d "10 minutes ago" +%s) $(date +%s)
```

This requests db_summariser to create summary files for every 100th database files or directories, in the last 10 minutes.
This 10 minutes, instead of 100 seconds, is a safety measure to allow a bit of down time.
If down time greater than 10 minutes happens, db_summariser can be started manually and be configured to summarise the period of down time manually.



