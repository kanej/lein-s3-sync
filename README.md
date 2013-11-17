# lein-s3-sync [![Build Status](https://travis-ci.org/kanej/lein-s3-sync.png)](https://travis-ci.org/kanej/lein-s3-sync)

A Leiningen plugin to synchronise the contents of a local folder
to a bucket on Amazon's S3 service.

The sync operates recursively within the local file directory.
Files are compared by MD5 hash with their remote equivalent and
pushed if it does not exist or has been changed locally.

Useful for pushing the html/js/css output files of a leiningen project
to S3 for hosting.

The synchronisation functionality is available as a separate lib `s3-sync`, please see the [README](https://github.com/kanej/lein-s3-sync/blob/master/s3-sync) in this repo's subfolder.

## Usage

Put `[lein-s3-sync "0.2.0"]` into the `:plugins` vector of your
`:user` profile, or if you are on Leiningen 1.x do `lein plugin install
lein-s3-sync 0.1.0`.

Add to your profile an s3-sync config map, specifying your S3 credentials,
the bucket to upload to and the local directory to sync:

    :s3-sync {
      :access-key "XXX"
      :secret-key "XXX"
      :local-dir  "./out"
      :bucket     "mybucket"
    }

With the profile map setup, run a sync at the command line:

    $ lein s3-sync

Alternatively, any of the :s3-sync keys can be overriden on the command
line:

    $ lein s3-sync :local-dir "./out/public" :bucket "www.mywebsite.com"

## License

Copyright © 2013 John Kane

Distributed under the Eclipse Public License, the same as Clojure.
