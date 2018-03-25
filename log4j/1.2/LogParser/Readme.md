Log4J Log file parser
=========
Parse already generated unstructured log files programmatically (as Apache chainsaws does in GUI).


### Constraint

Using deprecated Log4J 1.2 jar files.

### Example log lines
```
2017-12-02 16:53:42,618 DEBUG [main] LogParser (LogParser.java:15) - message 1
2017-12-02 16:53:42,624 DEBUG [main] LogParser (LogParser.java:16) - message 2
```

### Output example

Show the time and the message.
```
1512194022618 (epoc time)
message 1

1512194022624 (epoc time)
message 2
---

