(ns tracktime.core
  (:import [org.joda.time DateTime Period]
           [java.text SimpleDateFormat]))

(defn format-date-time [date]
  (. (new SimpleDateFormat "dd/MM/yyyy") format (. date toDate)))

(def validate-tasks (partial every? :desc))

(def tasks (atom [] :validator validate-tasks))

(defn start-task [description]
  (swap! tasks conj {:desc description :start (new DateTime)}))

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

(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))