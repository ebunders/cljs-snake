; state wordt opgeslagen in @re-frame.db/app-db
(ns snake-game.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frisk.core :refer [enable-re-frisk!]]
            [re-frame.core :refer [reg-event-db reg-event-fx reg-sub subscribe dispatch dispatch-sync]]
            [goog.events :as events]
            [snake-game.game.view :as view]
            [snake-game.game.logic :as logic]
            [snake-game.game.subs]
            [snake-game.game.events]
            [snake-game.keyreg :as keyreg]
            [snake-game.save-state :as save-state]
            [snake-game.game.data :as data]
            [snake-game.util :as util]))

(defn on-js-reload []
  (reagent/force-update-all)
  )


(reg-event-db
  :initialize
  (fn [db _]
    (merge db data/initial-state)))




;;
;; HANDLERS
;;

(reg-event-db
  :change-game-state
  (fn [db [_ new-game-state]]
    (assoc db :game-state new-game-state)))

(reg-event-db :level-up (fn [db _]
                          (do
                            (util/log "level up " (:game-level db))
                            (update db :game-level (partial util/inc-max 3)))))

(reg-event-db :level-down (fn [db _] (do
                                       (util/log "level down " (:game-level db))
                                       (update db :game-level (partial util/dec-min 1)))))





;;
;; GAME BASICS
;;


;;todo: move all the game view fn calls to game/core
(defn game
  "the main rendering funciton"
  []
  [:div#foo
   [view/render-board]
   [view/render-score]
   [view/render-game-over]
   [view/render-pause]
   [save-state/render-saves]
   [view/render-loading]
   ])



(defn ^:export run
  "The main app function"
  []
  (dispatch-sync [:initialize])
  (enable-re-frisk! {:kinde->id->handler? true})
  (reagent/render [game]
                  (.getElementById js/document "app"))
  )


;;
;; KEY EVENTS
;;

(keyreg/reg-key-bindings :state-running 32 :toggle-pause :down)
(keyreg/reg-key-bindings :state-paused 32 :toggle-pause :down)
(keyreg/reg-key-bindings :state-running 37 :key-up :down)
(keyreg/reg-key-bindings :state-running 38 :key-right :down)
(keyreg/reg-key-bindings :state-running 39 :key-down :down)
(keyreg/reg-key-bindings :state-running 40 :key-left :down)


(keyreg/reg-key-bindings :state-loaded 40 :level-up :down)
(keyreg/reg-key-bindings :state-loaded 38 :level-down :down)
(keyreg/reg-key-bindings :state-loaded 32 :start-game :down)





