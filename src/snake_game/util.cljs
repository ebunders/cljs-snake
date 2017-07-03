(ns snake-game.util)

(enable-console-print!)

(defn log [& strs]
  (.log js/console (reduce str (interleave strs (repeat " ")))))
