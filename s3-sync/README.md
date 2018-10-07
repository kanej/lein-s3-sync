# s3-sync

A Clojure library for synchronising the contents of a local folder
to a bucket on Amazon's S3 service.

The sync operates recursively within the local file directory.
Files are compared by MD5 hash with their remote equivalent and
pushed if it does not exist or has been changed locally.

Useful for pushing the html/js/css output files of a leiningen project
to S3 for hosting.

## Usage

A local directory of html files is to be copied to S3:
```clojure
(use 'me.kanej.s3-sync)

(def aws-credentials
  {:access-key "XXXXX"
   :secret-key "XXXXX"})

(sync-to-s3 aws-credentials "example/html_dir" "s3-bucket")
```

The `sync-to-s3` function will recursively go
through the files in the given local directory and upload them to S3
if they don't exist in the bucket or are different (by MD5 hash).
Hence another call to `sync-to-s3` would upload no files until
a change is made in the local directory.


## Artifacts

With Leiningen:
```clojure
[me.kanej/s3-sync "0.5.1"]
```

With Maven:
```xml
<dependency>
  <groupId>me.kanej</groupId>
  <artifactId>s3-sync</artifactId>
  <version>0.5.1</version>
</dependency>
```
## License

Copyright © 2018 John Kane

Distributed under the Eclipse Public License, the same as Clojure.
