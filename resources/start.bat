cd C:\crawler
start "Focer" powershell %STORM_HOME%\bin\storm.ps1 jar focer-0.1.jar lv.greenfrog.crawler.FocerCrawlTopology -conf config.yaml
exit