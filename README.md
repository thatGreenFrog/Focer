# Disclaimer 
## Not for use in production.
This is just a prototype (proof of concept) for focused web-crawler using text classification algorithms.  
# Focer
Focer is Focused web-crawler based on [StormCrawler](https://github.com/DigitalPebble/storm-crawler).
It was developed as part of Bachelor's thesis Development of focused web-crawler by Mārtiņš Trubiņš, Riga Technical university.
## Requirements
- Java 11
- Apache Storm cluster (can be run in -local mode without Storm) tested with version 1.2.3
- Apache Solr tested with version 8.5.1
## Limitations
As mentioned before this just prototype, so limitations apply:
- Only Latvian language is supported
- Only HTML mime-type is supported
## Classifiers
[Weka](https://waikato.github.io/weka-wiki/) trained models are used as classifiers.
Crawler consists of two classifiers primary(binary) and secondary(multi-class).
1. Primary classifier is used to determin outliers.
2. Multi-class classifier is used to determine exact class of page before saving it to solr. Included classifier classify pages in following classes:
   * Auto
   * Culture
   * Finance
   * Lifestyle
   * Politics
   * Sports
   * Technology
## Run crawler
Crawler was tested on Ubuntu 18.04 VM.
1. Make sure you have Java 11 installed.
2. Configure [Storm cluster](https://storm.apache.org/releases/current/Setting-up-a-Storm-cluster.html)
3. Configure [Solr](https://lucene.apache.org/solr/guide/8_5/installing-solr.html)
4. Compile code with `oneJar` gradle task. This will copy all necessary libraries and scripts to `output` folder.
5. Configure `config.yaml`
   * focer.resourceFolder - location of crawlers resource folder that contains classification models and queue. Default is located in project [resources/resources](resources/resources).
   * focer.solr - solr index url
   * focer.maxNgramBinary and focer.maxNgramMulti - max n-gram size for binary and multi-class classifier.
   * focer.binaryDocCount and focer.multiDocCount - document count in classifier training + testing datasets. This parameter is related to [bug](https://weka.8497.n7.nabble.com/StringToWordVector-with-new-documents-and-TF-IDF-help-needed-td46531.html) in Weka.
   * focer.cleanDb - if set to `true` queue will be cleared on start-up and filled with seed urls. If set to `false` crawling will continue from last stop.
   * focer.seeds - seed urls
   * focer.blacklist - blacklisted domains. Will not be added to queue if found.
   * Every other parameter is related to [StormCrawler configuration](https://github.com/DigitalPebble/storm-crawler/wiki/Configuration).
6. Add your classification models to corresponding folder in resources folder. You also have to add prepared Weka dictionary generated with `StringToWordVector` filter and tokenized arff file. From arff file only structure is required, so everything after `@data` tag can be deleted. Or you can use default classifiers included with project.
7. If you want to run crawler in Storm cluster copy [extlibs](extlibs) contents to {storm_home}/extlib folder.
8. Run [startWithStorm.sh](resources/startWithStorm.sh) to run in Storm cluster or [startLocal.sh](resources/startLocal.sh) to start locally without Storm cluster. If you are starting crawler in Storm cluster edit [startWithStorm.sh](resources/startWithStorm.sh) script so it points to correct Storm folder.
