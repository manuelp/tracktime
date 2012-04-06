;; The intent of this small tool is to track time spent on arbitrary
;; everyday tasks, and storing all the data into an accessible form
;; (useful for search and analysis).
;;
;; Currently it has to be used through the Clojure REPL.
(ns tracktime.core
  (require [clojure.string :as s]
           [tracktime.conf :as conf])
  (import [org.joda.time DateTime Period]
          [org.joda.time.format DateTimeFormat]
          [java.text SimpleDateFormat]
          [java.io File]))

(defn parse-date-time
  "Parse a date-time string into a `DateTime` value, with the
  following format string: `dd/MM/yyyy HH:mm`"
  [date-time]
  (let [format-string "dd/MM/yyyy HH:mm"
        parser (. DateTimeFormat forPattern format-string)]
    (. DateTime parse date-time parser)))

(defn parse-row
  "Parse a string representing a CSV row into a lazy seq of strings."
  [csv-row]
  (letfn [(string-field? [field]
            (not (nil? (re-matches #"\".*\"" field))))
          (extract-string [field]
            (apply str (drop-last (rest field))))]
    (map #(if (string-field? %)
            (extract-string %)
            %)
         (s/split csv-row #","))))

(defn parse-task
  "Parse a string representing a task in CSV form into a... task map."
  [csv-task]
  (let [row (parse-row csv-task)
        start (parse-date-time (nth row 0))
        end (parse-date-time (nth row 2))]
    {:start start
     :desc (nth row 1)
     :end end
     :period (new Period start end)}))

(defn read-tasks
  "Read all the tasks stored in the CSV file and converts them into a seq of maps."
  [filename]
  (if (.exists (File. filename))
    (let [data (slurp filename)]
      (if-not (empty? data)
        (map parse-task (s/split data #"\n"))
        []))
    []))

;; The description is mandatory for every task.
;;
;; The atom (uncoordinated synchronous state), is initialized with all
;; the tasks stored in the CSV file.
(def tasks (atom (read-tasks conf/csv-file)
                 :validator (partial every? :desc)))

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
            (map #(if-not (:end %) (terminate %) %) tasks))]
    (do
      (swap! tasks terminate-open-task)
      (write-csv conf/csv-file))))

(defn start-task
  "Starts a new open task, terminating the eventual previous open one.
  This is useful when passing from a task to another without any
  pause."
  [description]
  (do
    (end-task)
    (swap! tasks conj {:desc description :start (new DateTime)})
    nil))

(defn calculate-minutes
  "Calculate the total number of minutes of a given time period."
  [period]
  (.getMinutes (.toStandardMinutes period)))

(defn format-csv-task
  "Formats a single task in a CSV row with the following fields:

- **start date-time**: `dd/MM/yyyy HH:mm`
- **description**
- **end date-time**: `dd/MM/yyyy HH:mm`
- **duration**: `<HH>h <mm>m`
- **total number of minutes**

This function supports both terminated and unterminated tasks. For
unterminated tasks, it calculates the elapsed time from start until
now."
  [task]
  (letfn [(format-period [period]
            (str (.getHours period) "h " (.getMinutes period) "m"))
          (format-date-time [date]
            (. (new SimpleDateFormat "dd/MM/yyyy HH:mm") format (. date toDate)))
          (start-to-now [{:keys [start]}]
            (new Period start (new DateTime)))]
    (format "\"%s\",\"%s\",\"%s\",\"%s\",\"%d\""
            (format-date-time (:start task))
            (:desc task)
            (if (:end task)
              (format-date-time (:end task))
              "")
            (if (:end task)
              (format-period (:period task))
              (format-period (start-to-now task)))
            (if (:end task)
              (calculate-minutes (:period task))
              (calculate-minutes (start-to-now task))))))

(defn write-csv
  "Writes to file all *terminated* tasks in the atom formatted into CSV rows."
  [filename]
  (letfn [(format-csv []
            (s/join \newline (map format-csv-task (filter :end @tasks))))]
    (spit filename (format-csv))))

(defn today?
  "Check if the datetime is today or not."
  [datetime]
  (let [now (DateTime.)]
    (and (= (.getYear datetime) (.getYear now))
         (= (.getMonthOfYear datetime) (.getMonthOfYear now))
         (= (.getDayOfMonth datetime) (.getDayOfMonth now)))))

(defn today-list
  "Extract the list of tasks that started today, sorted by start date and time."
  []
  (filter #(today? (:start %)) (sort-by :start @tasks)))

(defn list-today
  "Prints all today's tasks, sorted by start date-time to *out* in CSV format."
  []
  (println (s/join \newline
                   (map format-csv-task (today-list)))))

(defn aggregate
  "Calculate the total number of minutes of all the tasks in the list."
  [list]
  (letfn [(add-periods [t1 t2]
            (hash-map :period (.plus (:period t1) (:period t2))))]
    (calculate-minutes (:period (reduce add-periods list)))))

(defn aggregate-all-today
  "Returns the total number of minutes for all the today's closed tasks."
  []
  (aggregate (today-list)))

;;(hash-map :desc (key (first (group-by :desc (today-list)))) :minutes (aggregate (val (first (group-by :desc (today-list))))))

(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))