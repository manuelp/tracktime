;; The intent of this small tool is to track time spent on arbitrary
;; everyday tasks, and storing all the data into an accessible form
;; (useful for search and analysis).
;;
;; Currently it has to be used through the Clojure REPL. For
;; reference, the most useful functions from the point of view of a
;; user are:
;;
;; - (start-task desc)
;; - (end-task)
;; - (print-today)
;; - (print-today-aggregated)
;; - (calculate-today-total)
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

(defn format-period
  "Format the given period in a string in this form: `HHh MMm`."
  [period]
  (str (.getHours period) "h " (.getMinutes period) "m"))

(defn transcode-task
  "Transform a task by returning a new map with the values changed in this way:

- **start date-time**: `dd/MM/yyyy HH:mm`
- **description**
- **end date-time**: `dd/MM/yyyy HH:mm`
- **duration**: `<HH>h <mm>m`

This function supports both terminated and unterminated tasks. For
unterminated tasks, it calculates the elapsed time from start until
now."
  [task]
  (letfn [(format-date-time [date]
            (. (new SimpleDateFormat "dd/MM/yyyy HH:mm") format (. date toDate)))
          (start-to-now [{:keys [start]}]
            (new Period start (new DateTime)))]
    (assoc {}
      :start (format-date-time (:start task))
      :desc (:desc task)
      :end (if (:end task)
             (format-date-time (:end task))
             "")
      :period (if (:end task)
                (format-period (:period task))
                (format-period (start-to-now task))))))

(defn format-csv-task
  "Formats a single task in a CSV row using the *transcode-task* function, plus an additional field (useful for calculations in a spreadsheet application:

- **total number of minutes**"
  [task]
  (let [t-task (transcode-task task)
        start-to-now (fn [{:keys [start]}]
                       (new Period start (new DateTime)))]
    (format "\"%s\",\"%s\",\"%s\",\"%s\",\"%d\""
            (:start t-task)
            (:desc t-task)
            (:end t-task)
            (:period t-task)
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

(defn print-today
  "Prints all today's tasks, sorted by start date-time to *out* in CSV format."
  []
  (println (s/join \newline
                   (map format-csv-task (today-list)))))

(defn aggregate
  "Calculate the total `Period` of time of all the terminated tasks in the list."
  [list]
  (letfn [(terminated? [task] (:end task))
          (add-periods [t1 t2]
            (hash-map :period (.plus (:period t1) (:period t2))))]
    (:period (reduce add-periods (filter terminated? list)))))

(defn calculate-today-total
  "Returns the total time logged for all the today's closed tasks."
  []
  (format-period (aggregate (today-list))))

(defn aggregate-today
  "Returns a seq of maps that contains aggregated information about
   today's *completed* tasks. Each map contains:

- *desc*: the textual description of the task
- *duration*: the aggregated time worked for that task,
            even if splitted between several separated entries."
  []
  (letfn [(aggregate-info [agg]
            (hash-map :desc (key agg) :duration (format-period (aggregate (val agg)))))]
    (map aggregate-info (group-by :desc (today-list)))))

(defn print-today-aggregated
  "Prints in a readable format the aggregated data for today's
completed tasks."
  []
  (letfn [(format-info-entry [entry]
            (str (:desc entry) ": " (:duration entry)))]
    (println (s/join \newline
                     (map format-info-entry (aggregate-today))))))