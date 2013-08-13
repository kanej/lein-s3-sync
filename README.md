# lein-s3-sync [![Build Status](https://travis-ci.org/kanej/lein-s3-sync.png)](https://travis-ci.org/kanej/lein-s3-sync)

A Leiningen plugin to synchronise the file contents of a local folder
to Amazon's S3 service.

Currently only local push to S3 is supported, with upload happening if
the file doesn't exist on S3 or its MD5 hash doesn't match.

## Usage

Put `[lein-s3-sync "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your
`:user` profile, or if you are on Leiningen 1.x do `lein plugin install
lein-s3-sync 0.1.0-SNAPSHOT`.

Add to your profile a s3-sync map, specifying your S3 credential, the 
bucket to upload to and the local directory to synce:

    :s3-sync {
      :access-key "..."
      :secret-key "..."
      :local-dir  "./out"
      :bucket     "mybucket"
    }

With the profile map setup, run a sync at the command line:

    $ lein s3-sync

## License

Copyright © 2013 John Kane

Distributed under the Eclipse Public License, the same as Clojure.
