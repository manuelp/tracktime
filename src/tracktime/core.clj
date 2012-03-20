(ns tracktime.core
  (:import [org.joda.time DateTime]
           [java.text SimpleDateFormat])))

(defn format-date-time [date]
  (. (new SimpleDateFormat "dd/MM/yyyy") format (. date toDate)))

(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))