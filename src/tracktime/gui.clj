(ns tracktime.gui
  (:use [seesaw core mig]))

(defn -main [& args]
  (invoke-later
    (-> (frame :title "TrackTime"
               :resizable? false
               :content (mig-panel
                          :constraints ["", ""]
                          :items [[(button :text "Aggregated") "wrap"]
                                  ["TODO: list of today's tasks" "span 6,growx,wrap"]
                                  [:separator "growx,span 6,wrap"]
                                  [(text :columns 30) "span 4,growx"] 
                                  [(button :text "Start")]
                                  [(button :text "Stop")]])
               ;:on-close :exit
               )
      pack!
      show!)))