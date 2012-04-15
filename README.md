# tracktime

I'm an app. Or maybe I'm a library? I haven't decided yet. However, I'm an (unobtrusive) thing that can help you track the time you spend on tasks every day. If you are interested, clone this repo and use me.

If you feel like criticize or better yet send "patches", feel free to fork this repo and send pull requests.

## Usage

You have to use the REPL for now, see the [documentation](http://manuelp.bitbucket.org/tracktime.html) for what functions you can use. Otherwise, you can generate it yourself with [Marginalia](https://github.com/fogus/marginalia) using lein:

    lein marg
    
This way you'll find the freshly compiled documentation into the *docs* directory.
    
You can also use [kibit](https://github.com/jonase/kibit) to check the code:

    lein plugin install jonase/kibit 0.0.2
    lein kibit

## TODO
Bold items are done.

- Implement a desktop GUI (with [seesaw](https://github.com/daveray/seesaw#readme)?)
- End task with optional :desc editing
- Extract CSV lib
- Separate functional and mutable models

## License

Copyright © 2012 Manuel Paccagnella

Distributed under the Eclipse Public License, the same as Clojure.
