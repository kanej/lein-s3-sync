# s3-sync

A Clojure library for synchronising the contents of a local folder
to a bucket on Amazon's S3 service.

The sync operates recursively within the local file directory.
Files are compared by MD5 hash with their remote equivalent and
pushed if it does not exist or has been changed locally.

Useful for pushing the html/js/css output files of a leiningen project
to S3 for hosting.

## Usage

FIXME

## Artifacts

With Leiningen:

    [me.kanej/s3-sync "0.1.0-SNAPSHOT"]

With Maven:

    <dependency>
      <groupId>me.kanej</groupId>
      <artifactId>s3-sync</artifactId>
      <version>0.1.0-SNAPSHOT</version>
    </dependency>

## License

Copyright © 2013 John Kane

Distributed under the Eclipse Public License, the same as Clojure.
