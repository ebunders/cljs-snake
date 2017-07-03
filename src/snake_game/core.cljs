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
            [snake-game.game.data :as data]))

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



;;
;; GAME BASICS
;;



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


(defn start []
  (js/setInterval #(dispatch [:next-state]) 150))


(defn ^:export run
  "The main app function"
  []
  (dispatch-sync [:initialize])
  (enable-re-frisk! {:kinde->id->handler? true})
  (reagent/render [game]
                  (.getElementById js/document "app"))
  (start))


;;
;; KEY EVENTS
;;

(keyreg/reg-key-bindings :state-running 32 :toggle-pause)
(keyreg/reg-key-bindings :state-paused 32 :toggle-pause)
(keyreg/reg-key-bindings :state-running 37 :key-up)
(keyreg/reg-key-bindings :state-running 38 :key-right)
(keyreg/reg-key-bindings :state-running 39 :key-down)
(keyreg/reg-key-bindings :state-running 40 :key-left)


