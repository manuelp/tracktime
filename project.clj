(defproject tracktime "0.1.4"
  ;:min-lein-version "2.0.0"
  :description "A simple time tracker."
  :url "https://bitbucket.org/manuelp/tracktime"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repl-retry-limit 30000
  :plugins [[lein-marginalia "0.7.0"]]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [joda-time/joda-time "2.1"]
                 [seesaw "1.4.0"]]
  :main tracktime.gui)