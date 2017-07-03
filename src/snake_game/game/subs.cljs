(ns snake-game.game.subs
  (:require [re-frame.core :refer [reg-sub]]))

;;
;; SUBSCRIPTIONS
;;
(reg-sub
  :board
  (fn
    [db _]
    (:board db)))

(reg-sub
  :snake
  (fn [db _]
    (:snake db)))

(reg-sub
  :point
  (fn [db _]
    (:point db)))

(reg-sub
  :points
  (fn [db _]
    (:points db)))

(reg-sub
  :game-state
  (fn [db _]
    (:game-state db)))

(reg-sub
  :saves
  (fn [db _]
    (:saves db)))
