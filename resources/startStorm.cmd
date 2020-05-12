cd C:\soft\zookeeper\bin
start "Zookeeper" zkServer.cmd
cd %STORM_HOME%\bin
start "Nimbus" powershell storm.ps1 nimbus
start "Supervisior" powershell storm.ps1 supervisor
start "UI" powershell storm.ps1 ui
exit