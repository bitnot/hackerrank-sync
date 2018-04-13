# HackerRank Wget - download your sumbitted solutions offline

## What

Download your submitted solutions from HackerRank to your disk

## Why 

To keep track of your performance, reflect on old code and improve

## Why Scala

Because "monad is just a monoid in the category of endofunctors, what's the problem?"
A dynamic language like JavaScript or Python would have been handier for this task, but meh.

## How?

sttp + circe + ... + love

## TODO

 - [x] - Download all (up to 1k) solutions in 'master' and arrange into folders by slug
	In case there are multiple submissions in the same language, take the lates
	In case there are multiple submissions in different languages, take the latest of each
	Name solution files as `solution.lang` where `lang` is appropriate extension for the language
	Cookies to be laded from a file
 - [x] - Logging
 - [] - Async (futures? monix? akka?)
 - [x] - Download description and test cases
 - [x] - Filter by status (Accepted/Failed)
 - [x] - Download all solutions in all contests
 - [] - Filter by date (solutions since date)
 - [] - Filter by language
 - [] - Request pagination, downloading more than 1k submissions
 - [] - Authentication by username and password (as opposed ot loading cookies from file)


## API

List of languages (anonymous):
http://api.hackerrank.com/checker/languages.json

List of all contests (anonymous):
https://www.hackerrank.com/rest/contests/upcoming?offset=0&limit=10&contest_slug=active

List of all submissions (authorized):
https://www.hackerrank.com/rest/contests/master/submissions/?offset=0&limit=1000

Submissions to a problem (authorized):
https://www.hackerrank.com/rest/contests/master/challenges/2d-array/submissions/?offset=0&limit=10

Code for a particular submission (authorized):
https://www.hackerrank.com/rest/contests/master/challenges/2d-array/submissions/14419845

Statement and test cases (anonymous):
https://www.hackerrank.com/rest/contests/master/challenges/2d-array/download_pdf?language=English
https://www.hackerrank.com/rest/contests/master/challenges/2d-array/download_testcases


## Plan 

Cache language codes
-> Get all submissions (limit 1000)
-> Filter by contest id 1 (master) 
-> Filter by status 2 (Accepted)
-> each 
   -> if submission not saved yet
   -> get `/rest/contests/master/challenges/${challenge_slug}/submissions/${submission_id}`
   -> save json
   -> parse json
   -> create directories
   -> write code to file
   -> get problem.pdf
   -> get testcases.zip
   -> unpack test cases