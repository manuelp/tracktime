(ns tracktime.gui
  (:use [seesaw core mig]
        tracktime.core))

(def today-table (table :model [
                                :columns [{:key :start :text "Start"}
                                          {:key :desc :text "Description"}
                                          {:key :end :text "End"}
                                          {:key :period :text "Duration"}]
                                :rows @tasks]))

(defn -main [& args]
  (invoke-later
    (-> (frame :title "TrackTime"
               :resizable? false
               :content (mig-panel
                          :constraints ["", ""]
                          :items [[(button :text "Aggregated") "wrap"]
                                  [(scrollable today-table) "span 6,grow,shrink 0,wrap"]
                                  [:separator "growx,span 6,wrap"]
                                  [(text :columns 30) "span 4,growx"] 
                                  [(button :text "Start")]
                                  [(button :text "Stop")]])
               ;:on-close :exit
               )
      pack!
      show!)))