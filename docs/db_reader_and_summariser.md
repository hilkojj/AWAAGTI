# DB Writer

DB Writer is a TCP server which is responsible from taking the weather data from the Raspberry Pi and storing it in the database.
DB Writer is a daemon process, which listens on a configured TCP port for Pi's to connect to.

TCP is used to make sure no data is lost during transport.
The extra overhead and delay of TCP is of no burden to the user.
The format of the TCP stream is divided into a few states.
```
START
{date in yyyy-mm-dd},{time in hh:mm:ss}
{station id},{temperature in C, 1 decimal},{wind speed in m/s, 1 decimal}
END
```

The 'end' signals that no more data is coming for the second, and the received data for the second can be written to the database.

For example, a second in which only two weather stations reported their data to the Pi.

```
START
2019-02-01,15:09:33
1337,25.5,5.5
1338,22.5,1.3
END
```

The data format allows for adding more variables, while keeping the software backwards compatible with older versions.
A new variable can be added by adding an extra comma after the station data, with the new value after it.

DB Writer allows for multiple Pi's to connect simultaneously.

On the VM server, DB Writer is running as a daemon configured to be run by systemd, the default init system on Debian.

## Usage

```
$ db_writer {tcp port to listen to}
```

# DB Summariser

DB Summariser makes summary files in the format described in the documentation of the .awaagti database files.

On the VM server, DB Summariser is running as a systemd timer, the default init system on Debian.
The reason for using systemd, instead of cron, is that systemd is installed by default on Debian, and as it is the init system, it is the preferred choise for running daemons.
Systemd's timer functionality has been developed to replace cron on modern distributions of GNU/Linux.

## Usage

When executing db_summariser, the user should configure the time period to summarise, which is supplied as the command line arguments.
The from and end period are supplied in unix time in seconds.
The end time is optional, and when not specified, it defaults to the current system time. 

```
$ db_summariser {start of period} [optional, end of period]
```
