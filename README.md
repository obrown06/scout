# scout

**scout** is a WebSocket API for a multithreaded web crawler that identifies a URL's most similar neighbors. Scout performs a concurrent breadth-first search, recording URLs whose on-page text generates the lowest measure of [Cosine Similarity](https://en.wikipedia.org/wiki/Cosine_similarity) with respect to the original URL's text. Scout crawls and processes URLs at a rate of ~ 40 URLs/s on a dual core MacBook Pro.

## API

The API supports the following client actions:

  * **open()** : opens a two-way socket connection
  * **send()** : terminates the current crawl (if there is one) initiates a crawl beginning from the messaged URL
  * **close()** : closes an open connection and terminates the current crawl (if there is one)

Scout updates the client upon each update to its record of most similar URLs by sending a serialized JSON object containing the total number of URLs crawled and JSON lists of 1) the 10 most similar URLs crawled and 2) their measures of Cosine Similarity.

A sample WebSocket message from server to client:

"{ "nURLsVisited": 2, "urls" : ["www.cnn.com", "www.msnbc.com"], "scores" : [0.01, 0.0864]}"

Crawls terminate under one of five conditions:

1) Upon failing to retrieve HTML from the origin URL
2) Upon receiving a new **send()** client action
3) Upon receiving a **close()** client action
4) Upon exhausting the queue of URLs to be crawled
5) Upon crawling the maximum allowed number of distinct URLs (currently 3000)

A working demo of the api can be accessed [here](http://nicholasbrown.io/software)
