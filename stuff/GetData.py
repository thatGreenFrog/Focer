import random
import csv
import solr

s = solr.SolrConnection("http://192.168.0.129:8983/solr/crawler_final")
data = [['Class', 'url']]


def get_data():
    queries = ["Class:Negative", "-Class:Negative"]
    for q in queries:
        response = s.query(q, sort='Class asc, id asc', rows=10000)
        result = response.results
        max_docs = response.numFound
        seen = []
        while len(seen) < 100:
            r = random.randint(0, max_docs - 1)
            print(f"max docs: {max_docs}, r: {r}")
            if r in seen:
                continue
            seen.append(r)
            data.append([result[r]['Class'], result[r]['url']])
    my_file = open('D:\\crawl_result.csv', 'w')
    with my_file:
        writer = csv.writer(my_file)
        writer.writerows(data)


get_data()
