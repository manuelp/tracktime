;; Here we use java.util.concurrent.Executors to create asynchronous
;; actions in a thread-poll.
;;
;; Another option is to use
;; [overtone/at-at](https://github.com/overtone/at-at) as a frontend
;; for more idiomatic thread-pooling.
(ns tracktime.executor
  (:import [java.util.concurrent Executors TimeUnit]))

(defn execute-every
  "Run the given fn every `seconds` seconds starting from now. Returns
  a `java.util.concurrent.ScheduledFuture` that you can use to cancel
  the action."
  [seconds fn]
  (let [executor (Executors/newSingleThreadScheduledExecutor)]
    (.scheduleAtFixedRate executor fn 0 seconds TimeUnit/SECONDS)))

(defn cancel-action
  "Cancel (stops) the given action, returning *false* if it could not
  be cancelled (usually because it has already normally completed)."
  [action]
  (.cancel action true))