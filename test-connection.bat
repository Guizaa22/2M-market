@echo off
echo Testing MySQL Connection...
C:\Users\tAkkini\maven\apache-maven-3.9.6\bin\mvn.cmd compile exec:java "-Dexec.mainClass=util.TestConnection"
pause

