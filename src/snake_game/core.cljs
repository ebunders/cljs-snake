; state wordt opgeslagen in @re-frame.db/app-db
(ns snake-game.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [reg-event-db reg-event-fx reg-sub-raw subscribe dispatch dispatch-sync]]
            [goog.events :as events]
            [snake-game.view :as view]
            [snake-game.logic :as logic]
            [re-frisk.core :refer [enable-re-frisk!]]))

(enable-console-print!)

(println "This text is printed from src/snake-game/core.cljs. Go ahead and edit it and see reloading in action.")

(defn on-js-reload []
  (reagent/force-update-all)
  )


(def board [35 25])
(def snake {:direction  [1 0]
            :body       [[3 2] [2 2] [1 2] [0 2]]
            :ismoving?  true
            :blink-head false})




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
    (not (:game-paused? db))
    (:ismoving? (:snake db))))

;;
;; HANDLERS
;;


(reg-event-db
  :initialize
  (fn [db _]
    (merge db initial-state)))


(reg-event-fx
  :next-state
  (fn [cofx event]
    (let [db (:db cofx)
          {snake :snake world :world} db
          update-game (fn [db]
                        (-> db
                            (update-in [:snake] logic/move-snake)
                            (as-> after-move
                                  (logic/process-move after-move))))]
      (if (game-active? db)
        (if (logic/collisions snake board)
          {:db db
           :dispatch-later [{:ms 200 :dispatch [:blink 5]}]}
          {:db (update-game db)})
        {:db db}
        ))))


(reg-event-fx
  :blink
  (fn [cofx [_ counter]]
    (let [db (:db cofx)]
      (if (> counter 0)
        {:db             (update-in db [:snake :blink-head] not)
         :dispatch-later [{:ms 200 :dispatch [:blink (dec counter)]}]}
        {:db (assoc-in db [:game-running?] false)}
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
   [view/render-pause]
   ])


(defn ^:export run
  "The main app function"
  []
  (dispatch-sync [:initialize])
  (enable-re-frisk! {:kind->id->handler? true})
  (reagent/render [game]
                  (.getElementById js/document "app")))


;;
;; Js Listeners
;;


(defonce snake-moving
         (js/setInterval #(dispatch [:next-state]) 150))

(defonce key-handler
         (events/listen js/window "keydown"
                        (fn [e]
                          (let [key-code (.-keyCode e)]

                            (if (contains? logic/key-code->move key-code)
                              (do
                                (dispatch [:change-direction (logic/key-code->move key-code)])
                                (.preventDefault e)))

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
    (reaction (:snake @db))))

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