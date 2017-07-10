(ns snake-game.game.data
  (:require [snake-game.game.logic :as logic]))

(def board [35 25])
(def snake {:direction   [1 0]
            :body        [[3 2] [2 2] [1 2] [0 2]]
            :snake-state :snake-moving
            :blink-head  false})



;dohere maybe this is not the place for adding the :saves entry?
(def initial-state {
                    :board      board
                    :snake      snake
                    :point      (logic/random-free-position snake board)
                    :points     0
                    :game-state :state-loaded
                    :game-level 1
                    :saves      []})



;;
;; model utilities
;;

(defn snake-is-moving? [db] (= :snake-moving (-> db :snake :snake-state)))
(defn game-is-running? [db] (= :state-running (:game-state db)))
(defn game-is-paused? [db] (= :state-paused (:game-state db)))
(defn game-is-finished? [db] (= :state-finished (:game-state db)))

