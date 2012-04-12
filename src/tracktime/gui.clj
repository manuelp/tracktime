(ns tracktime.gui
  (:use [seesaw core mig]))

(defn -main [& args]
  (invoke-later
   (-> (frame :title "TrackTime"
              :resizable? false
              ;:minimum-size [500 :by 350]
              :content (mig-panel
                        :constraints ["wrap 2", "[fill]5px[shrink 0]"]
                        :items [["TODO: list of today's tasks" "growx,span"]
                                [:separator "growx,span"]
                                [(text :columns 30) "grow"] [(button :text "Start")]])
              ;:on-close :exit
              )
       pack!
       show!)))

