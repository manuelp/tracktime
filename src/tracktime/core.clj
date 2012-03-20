(ns tracktime.core
  (require [clojure.string :as s])
  (import [org.joda.time DateTime Period]
           [java.text SimpleDateFormat]))

(def validate-tasks (partial every? :desc))

(def tasks (atom [] :validator validate-tasks))

(defn start-task [description]
  (do
    (end-task)
    (swap! tasks conj {:desc description :start (new DateTime)})))

(defn terminate [task]
  (let [end (new DateTime)
        period (new Period (:start task) end)]
    (assoc task :end end :period period)))

(defn end-task []
  (letfn [(terminate-open-task [tasks]
            (map #(if (not (:end %))
                    (terminate %)
                    %) tasks))]
    (swap! tasks terminate-open-task)))

(defn format-csv [task]
  (letfn [(format-period [period]
            (str (.getHours period) "h " (.getMinutes period) "m"))
          (format-date-time [date]
            (. (new SimpleDateFormat "dd/MM/yyyy HH:mm") format (. date toDate)))]
    (format "\"%s\",\"%s\",\"%s\",\"%s\""
            (format-date-time (:start task))
            (:desc task)
            (format-date-time (:end task))
            (format-period (:period task)))))

(defn write-csv [filename]
  (letfn [(format-csv-tasks []
            (s/join \newline (map format-csv @tasks)))]
    (spit filename (format-csv-tasks))))

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