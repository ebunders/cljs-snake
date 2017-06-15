(ns snake-game.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [reg-event-db reg-sub-raw subscribe dispatch dispatch-sync]]
            [goog.events :as events]
            [snake-game.view :as view]
            [snake-game.logic :as logic]))

(enable-console-print!)

(println "This text is printed from src/snake-game/core.cljs. Go ahead and edit it and see reloading in action.")

(defn on-js-reload []
  (reagent/force-update-all)
  )


(def board [35 25])
(def snake {:direction [1 0]
            :body      [[3 2] [2 2] [1 2] [0 2]]})




(def initial-state {
                    :board         board
                    :snake         snake
                    :point         (logic/random-free-position snake board)
                    :points        0
                    :game-running? true
                    :game-paused?  false})


(defn game-active?
  [db]
  (and
    (:game-running? db)
    (not (:game-paused? db))))

;;
;; HANDLERS
;;


(reg-event-db
  :initialize
  (fn [db _]
    (merge db initial-state)))

(reg-event-db
  :next-state
  (fn [{:keys [snake board] :as db} _]
    (let [update-game (fn [db]
                        (-> db
                            (update-in [:snake] logic/move-snake)
                            (as-> after-move
                                  (logic/process-move after-move))))]
      (if (game-active? db)
        (if (logic/collisions snake board)
          (assoc-in db [:game-running?] false)
          (update-game db))
        db
        ))))




(reg-event-db
  :change-direction
  (fn [db [_ new-direction]]
    (if (game-active? db)
      (update-in db
                 [:snake :direction]
                 (partial logic/change-snake-direction new-direction)))))


(reg-event-db
  :toggle-pause
  (fn [db _]
    (update db :game-paused? not)))

(defn game
  "the main rendering funciton"
  []
  [:div#foo
   [view/render-board]
   [view/render-score]
   [view/render-game-over]
   [view/render-state]
   [view/render-pause]])


(defn ^:export run
  "The main app function"
  []
  (dispatch-sync [:initialize])
  (reagent/render [game]
                  (.getElementById js/document "app")))

(defonce snake-moving
         (js/setInterval #(dispatch [:next-state]) 150))

(defonce key-handler
         (events/listen js/window "keydown"
                        (fn [e]
                          (let [key-code (.-keyCode e)]
                            (if (contains? logic/key-code->move key-code)
                              (dispatch [:change-direction (logic/key-code->move key-code)]))
                            (if (= key-code 32)
                              (dispatch [:toggle-pause]))))))

;;
;; SUBSCRIPTIONS
;;


(reg-sub-raw
  :board
  (fn
    [db _]
    (reaction (:board @db))))

(reg-sub-raw
  :snake
  (fn [db _]
    (reaction (:body (:snake @db)))))

(reg-sub-raw
  :point
  (fn [db _]
    (reaction (:point @db))))

(reg-sub-raw
  :points
  (fn [db _]
    (reaction (:points @db))))

(reg-sub-raw
  :game-running?
  (fn [db _]
    (reaction (:game-running? @db))))

(reg-sub-raw
  :game-paused?
  (fn [db _]
    (reaction (:game-paused? @db))))