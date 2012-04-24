;; This namespace manages a timer that executes an action every
;; second. This timer can be stopped.
(ns tracktime.timer
  (:use tracktime.executor))

;; We need a reference to the running timer so that we can effectively
;; stop it when the current unterminated task is closed.
(def timer-executor (atom nil))

(defn start-timer
  "We need a function that updates the timer every second, but only if
there is an unterminated task."
  [action]
  (reset! timer-executor (execute-every 1 action)))

(defn stop-timer
  []
  (cancel-action @timer-executor))