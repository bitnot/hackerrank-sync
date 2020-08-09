# HackerRank Wget - download your sumbitted solutions offline

## What

Downloads your solutions from HackerRank to your computer. [Like this.](https://github.com/bitnot/hackerrank-solutions)

## Why 

To keep track of your performance, reflect on old code and improve.

## Why Scala

A dynamic language like JavaScript or Python could have been handier for this task, but why not?


## How to use

Copy `application.conf.template` to `application.conf`, add any additional settins from `reference.conf`.
You need to log into Hackerrank in your browser and copy the _value_ of your `hrank_session` cookie.

Note:

It is pretty slow right now as all the downloads are synchronous, 
on the bright side it does not DDOS HackerRank API. 

So plug in your laptop and go do something useful.

1. Install sbt: https://www.scala-sbt.org/1.x/docs/Setup.html
2. Run `sbt` in the root folder
3. Wait for sbt to download all the sbt dependencies…
4. Inside sbt console run `compile` command
5. Wait for sbt to download all the project dependencies and compile the source code…
6. Inside sbt console run `run` command

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
 - [x] - Filter by date (solutions since date, last x days)
 - [x] - Merge indexes (readme.md files) to allow partial updates (last x days)
 - [ ] - Async (futures? monix? akka?)
 - [ ] - Exclude contests in progress (?)
 - [ ] - Filter by language
 - [ ] - Request pagination, downloading more than 1k submissions
 - [ ] - Authentication by username and password (as opposed ot loading cookies from file)
