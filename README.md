# tracktime

This is an (unobtrusive) desktop application that can help you track the time you spend on tasks every day. If you are interested, download the JAR of the last version or clone this repo. If you feel like criticize or better yet send "patches", feel free to fork this repo and send pull requests.

## Usage
The simples way to use this application is to download the latest JAR from the *"Downloads"* section up here and run it, for example from the command line:

    java -jar tracktime-<VERSION>-standalone.jar
    
That's it. All your **completed** tasks will be stored in the *tasks.csv* file that will be created in the same directory of the JAR file.

## Documentation
See the [documentation](http://manuelp.bitbucket.org/tracktime.html) for what functions you can use. Otherwise, you can generate it yourself with [Marginalia](https://github.com/fogus/marginalia) using lein:

    lein marg
    
This way you'll find the freshly compiled documentation into the *docs* directory.
    
You can also use [kibit](https://github.com/jonase/kibit) to check the code:

    lein plugin install jonase/kibit 0.0.2
    lein kibit

## License

Copyright © 2012 Manuel Paccagnella

Distributed under the Eclipse Public License, the same as Clojure.
