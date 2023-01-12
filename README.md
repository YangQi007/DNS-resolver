# DNS-resolver

## In this project, I'll use Java to write a caching DNS resolver.

This program will listen for incoming DNS requests. When it receives one, it will check its local cache (a hash table) and, if it has a valid response in its cache for the query, will send a result back right away. Otherwise, it will do what we all do, and ask google (forward the request to Google's public DNS server at 8.8.8.8), store Google's response in the local cache, then send back the response.
