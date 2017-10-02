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

Add `lein-s3-sync` as a plugin dependency of your project or profiles:

![Clojars Project](http://clojars.org/lein-s3-sync/latest-version.svg)

Add to your project or profile an s3-sync config map, specifying your S3 credentials,
the bucket to upload to and the local directory to sync:
```clojure
:s3-sync {
  :access-key "XXX"
  :secret-key "XXX"
  :local-dir  "./out"
  :bucket     "mybucket"
  :public true
  :metadata {:cache-control "public, max-age=31536000"}
}
```
With the profile map setup, run a sync at the command line:
```bash
    $ lein s3-sync
```
Alternatively, any of the :s3-sync keys can be overriden on the command
line:
```bash
    $ lein s3-sync :local-dir "./out/public" :bucket "www.mywebsite.com"
```

An optional parameter of `:public true` can be set under the `:s3-sync`
map to set uploaded files as readable by all users in S3.

Similarly S3 metadata options against the files can be passed under `:metadata`:

```clojure
:s3-sync {
  ...
  :metadata {:cache-control "public, max-age=31536000"}
}
```

## Development

To run the unit tests:
```bash
    $ lein sub test
```

To run the integration tests:
```bash
    $ lein sub test :integration
```

## License

Copyright © 2017 Kane

Distributed under the Eclipse Public License, the same as Clojure.
