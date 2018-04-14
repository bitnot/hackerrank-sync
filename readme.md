# HackerRank Wget - download your sumbitted solutions offline

## What

Downloads your solutions from HackerRank to your computer. [Like this.](https://github.com/bitnot/hackerrank-solutions)

## Why 

To keep track of your performance, reflect on old code and improve.

## Why Scala

A dynamic language like JavaScript or Python could have been handier for this task, but why not?


## How to use

At the moment authentication is done via logging in into browser and storing cookies to a file.
You can ignore `login` and `password` settings in `reference.conf`.

Add a `cookies.txt` to `/src/main/resources/` containing the value of `Cookie` header 
(1 line formatted as `cookie1_name: cookie1_value; cookie2_name: cookie2_value; ...`) 
after you have logged into website in your browser.

Example:
```txt
default_cdn_url=hrcdn.net; _hrank_session=12ghv3123kh1v2k3ghv1k2h3gv12hg3v12gh3v1; cdn_url=hrcdn.net; cdn_set=true; __utma=74197771.123123.1523563402.1523563402.1523563402.1; __utmc=123123123; ...
```

Compile project with sbt as you usually would and run it.

Note:

It is pretty slow right now as all the downloads are synchronous, 
on the bright side it does not DDOS HackerRank API. 

So plug in your laptop and go do something useful.

## TODO

 - [x] - Download all (up to 1k) solutions in 'master' and arrange into folders by slug
	In case there are multiple submissions in the same language, take the lates
	In case there are multiple submissions in different languages, take the latest of each
	Name solution files as `solution.lang` where `lang` is appropriate extension for the language
	Cookies to be laded from a file
 - [x] - Logging
 - [x] - Download description and test cases
 - [x] - Filter by status (Accepted/Failed)
 - [x] - Download all solutions in all contests
 - [] - Async (futures? monix? akka?)
 - [] - Filter by date (solutions since date)
 - [] - Filter by language
 - [] - Request pagination, downloading more than 1k submissions
 - [] - Authentication by username and password (as opposed ot loading cookies from file)
