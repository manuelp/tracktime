;; The intent of this small tool is to track time spent on arbitrary
;; everyday tasks, and storing all the data into an accessible form
;; (useful for search and analysis).
;;
;; Currently it has to be used through the Clojure REPL.
(ns tracktime.core
  (require [clojure.string :as s])
  (import [org.joda.time DateTime Period]
           [java.text SimpleDateFormat]))

;; The description is mandatory for every task.
(def tasks (atom [] :validator (partial every? :desc)))

(defn terminate
  "When a task is terminated, we memorize the end time
   and the period between the start and end of it."
  [task]
  (let [end (new DateTime)
        period (new Period (:start task) end)]
    (assoc task :end end :period period)))

;; Since when every time a task is terminated we store the entire
;; list, we need a function to do it (both formatting and actual writing).
(declare write-csv)

(defn end-task
  "At any given time there could be only one unterminated task.
   This function terminates that task and writes the entire tasks list
   into a CSV file."
  []
  (letfn [(terminate-open-task [tasks]
            (map #(if (not (:end %))
                    (terminate %)
                    %) tasks))]
    (do
      (swap! tasks terminate-open-task)
      (write-csv "tasks.csv"))))

(defn start-task
  "Starts a new open task, terminating the eventual previous open one.
  This is useful when passing from a task to another without any
  pause."
  [description]
  (do
    (end-task)
    (swap! tasks conj {:desc description :start (new DateTime)})))

(defn format-csv-task
  "Formats a single task in a CSV row with the following fields:

- **start date-time**: `dd/MM/yyyy HH:mm`
- **description**
- **end date-time**: `dd/MM/yyyy HH:mm`
- **duration**: `<HH>h <mm>m`
- **total number of minutes**"
  [task]
  (letfn [(format-period [period]
            (str (.getHours period) "h " (.getMinutes period) "m"))
          (calculate-minutes [period]
            (.getMinutes (.toStandardMinutes period)))
          (format-date-time [date]
            (. (new SimpleDateFormat "dd/MM/yyyy HH:mm") format (. date toDate)))]
    (format "\"%s\",\"%s\",\"%s\",\"%s\",\"%d\""
            (format-date-time (:start task))
            (:desc task)
            (format-date-time (:end task))
            (format-period (:period task))
            (calculate-minutes (:period task)))))

(defn write-csv
  "Writes to file all *terminated* tasks formatted into CSV rows."
  [filename]
  (letfn [(format-csv []
            (s/join \newline (map format-csv-task (filter :end @tasks))))]
    (spit filename (format-csv))))

;; TODO
(defn read-tasks [filename]
  (let [data (slurp filename)]
    (if (not (empty? data))
      (read-string data)
      [])))

(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))