(ns snake-game.util)

(enable-console-print!)

(defn log [& strs]
  (.log js/console (reduce str (interleave strs (repeat " ")))))

(defn inc-max [max i](if (< i max) (inc i) max))
(defn dec-min [min i](if (> i min) (dec i) min))
