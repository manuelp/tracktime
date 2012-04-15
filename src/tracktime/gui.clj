(ns tracktime.gui
  (:use [seesaw core mig table])
  (:require [tracktime.core :as core]))

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

(def aggregated-table (table :model (aggregated-table-model)))

(def aggregated-dialog (dialog :modal? true
                               :title "Today's aggregated tasks"
                               :minimum-size [400 :by 200]
                               :content (mig-panel
                                         :constraints ["", ""]
                                         :items [[(scrollable aggregated-table) "span 6 6,wrap"]])))

;; Here comes the handlers...

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
    (update-table today-table (today-table-model))))

(defn open-aggregated [e]
  (show! aggregated-dialog))

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
                                  [current-task-text "span 4,growx"] 
                                  [start-button]
                                  [stop-button]])
               ;:on-close :exit
               )
      pack!
      show!)))