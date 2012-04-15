(ns tracktime.gui
  (:use [seesaw core mig table])
  (:require [tracktime.core :as core]))

(defn today-table-model []
  [:columns [{:key :start :text "Start"}
             {:key :desc :text "Description"}
             {:key :end :text "End"}
             {:key :period :text "Duration"}]
   :rows (map core/transcode-task (core/today-list))])

(def today-table (table :model (today-table-model)))

(defn update-table [table]
  (-> table
      clear!
      (config! :model (today-table-model))))

(def current-task-text (text :columns 30))

(def stop-button (button :text "Stop" :enabled? false))

(def start-button (button :text "Start"))

(defn start-task [e]
  (let [desc (value current-task-text)]
    (if-not (empty? desc)
      (do
        (config! current-task-text :enabled? false)
        (config! start-button :enabled? false)
        (config! stop-button :enabled? true)
        (core/start-task desc))
      (alert "You need to specify a non-void description!"))))

(defn end-task [e]
  (do
    (core/end-task)
    (config! current-task-text :enabled? true)
    (config! start-button :enabled? true)
    (config! stop-button :enabled? false)
    (update-table today-table)))

(listen start-button
        :mouse-clicked start-task)

(listen stop-button
        :mouse-clicked end-task)

(defn -main [& args]
  (invoke-later
    (-> (frame :title "TrackTime"
               :resizable? false
               :content (mig-panel
                          :constraints ["", ""]
                          :items [[(button :text "Aggregated") "wrap"]
                                  [(scrollable today-table) "span 6,grow,shrink 0,wrap"]
                                  [:separator "growx,span 6,wrap"]
                                  [current-task-text "span 4,growx"] 
                                  [start-button]
                                  [stop-button]])
               ;:on-close :exit
               )
      pack!
      show!)))