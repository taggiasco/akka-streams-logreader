[![Build Status](https://travis-ci.org/taggiasco/akka-streams-logreader.svg?branch=master)](https://travis-ci.org/taggiasco/akka-streams-logreader)

# Akka Streams sample

This project is a simple example of Akka Streams usage. It allows to read log files, apply a "reducer" function (i.e. split logs in different groups), and then apply a specific sink (sum or average).



## Usage

$ sbt run sample.log sample_post.log
