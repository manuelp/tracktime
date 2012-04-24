(ns tracktime.gui
  (:use [seesaw core mig table]
        tracktime.executor)
  (:require [tracktime.core :as core])
  (:import [org.joda.time DateTime Period])
  (:gen-class))

;; Let's define some components.

(defn today-table-model []
  [:columns [{:key :start :text "Start"}
             {:key :desc :text "Description"}
             {:key :end :text "End"}
             {:key :period :text "Duration"}]
   :rows (map core/transcode-task (core/today-list))])

(def today-table (table :model (today-table-model)))

(defn update-table [table model]
  (-> table
      clear!
      (config! :model model)))

(def current-task-text (text :columns 30))

(def stop-button (button :text "Stop" :enabled? false))

(def start-button (button :text "Start"))

(def aggregated-button (button :text "Aggregated"))

(defn aggregated-table-model []
  [:columns [{:key :desc :text "Description"}
             {:key :duration :text "Duration"}]
   :rows (core/aggregate-today)])

(defn aggregated-dialog []
  (let [table (scrollable (table :model (aggregated-table-model)))]
    (dialog :title "Today's aggregated tasks"
            :minimum-size [400 :by 200]
            :on-close :dispose
            :content (mig-panel
                      :constraints ["", ""]
                      :items [[table "span 6 6,wrap"]]))))

(def elapsed-label (label "00h 00m 00s") )

;; We need a reference to the running timer so that we can effectively
;; stop it when the current unterminated task is closed.
(def timer-executor (atom nil))

(defn update-timer
  "Updates the current elapsed time in the corresponding label since
the unterminated task has been created."
  []
  (config! elapsed-label :text (core/calculate-elapsed-time)))

(defn start-timer
  "We need a function that updates the timer every second, but only if
there is an unterminated task."
  []
  (reset! timer-executor (execute-every 1 update-timer)))

(defn stop-timer
  []
  (cancel-action @timer-executor))

;; Here comes the handlers...

(defn start-task [e]
  (let [desc (value current-task-text)]
    (if-not (empty? desc)
      (do
        (config! current-task-text :enabled? false)
        (config! start-button :enabled? false)
        (config! stop-button :enabled? true)
        (core/start-task desc)
        (start-timer))
      (alert "You need to specify a non-void description!"))))

(defn end-task [e]
  (do
    (core/end-task)
    (stop-timer)
    (config! current-task-text :enabled? true)
    (config! start-button :enabled? true)
    (config! stop-button :enabled? false)
    (update-table today-table (today-table-model))))

(defn open-aggregated [e]
  (show! (aggregated-dialog)))

;; ...and the listen bindings.

(listen start-button
        :mouse-clicked start-task)

(listen stop-button
        :mouse-clicked end-task)

(listen aggregated-button
        :mouse-clicked open-aggregated)

(defn -main [& args]
  (invoke-later
   (-> (frame :title "TrackTime"
              :resizable? false
              :content (mig-panel
                        :constraints ["", ""]
                        :items [[aggregated-button "wrap"]
                                [(scrollable today-table) "span 6,grow,shrink 0,wrap"]
                                [:separator "growx,span 6,wrap"]
                                [current-task-text "span 3,growx"]
                                [elapsed-label]
                                [start-button]
                                [stop-button]])
              :on-close :exit)
       pack!
       show!)))