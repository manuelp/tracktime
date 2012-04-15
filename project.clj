(defproject tracktime/tracktime "0.1.0-SNAPSHOT" 
  :min-lein-version "2.0.0"
  :plugins [[lein-marginalia "0.7.0"]]
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [joda-time/joda-time "2.1"]
                 [seesaw "1.4.0"]]
  :description "A simple CLI time tracker.")