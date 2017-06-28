; state wordt opgeslagen in @re-frame.db/app-db
(ns snake-game.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [reg-event-db reg-event-fx reg-sub-raw subscribe dispatch dispatch-sync]]
            [goog.events :as events]
            [snake-game.view :as view]
            [snake-game.logic :as logic]
            [re-frisk.core :refer [enable-re-frisk!]]
            [engine.game :as game]))

(enable-console-print!)

(defn log [& strs]
  (.log js/console (reduce str (interleave strs (repeat " ")))))

(defn on-js-reload []
  (reagent/force-update-all)
  )

; game-state [:game-running :gamek-paused :game-finished]
; snake-state [:snake-moving :snake-collided]

(def board [35 25])
(def snake {:direction   [1 0]
            :body        [[3 2] [2 2] [1 2] [0 2]]
            :snake-state :snake-moving
            :blink-head  false})




(def initial-state {
                    :board      board
                    :snake      snake
                    :point      (logic/random-free-position snake board)
                    :points     0
                    :game-state :game-running
                    :saves  []})



;;
;; model utilities
;;

(defn snake-is-moving? [db] (= :snake-moving (-> db :snake :snake-state)))
(defn game-is-running? [db] (= :game-running (:game-state db)))
(defn game-is-paused? [db] (= :game-paused (:game-state db)))
(defn game-is-finished? [db] (= :game-finished (:game-state db)))


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
          snake (:snake db)
          snake-state (:snake-state snake)
          running-and-moving (and (= (:game-state db) :game-running) (= snake-state :snake-moving))
          update-game (fn [db]
                        (-> db
                            (update-in [:snake] logic/move-snake)
                            (as-> after-move
                                  (logic/process-move after-move))))]
      (if running-and-moving
        ;if there is a collision we switch to :snake-collided
        (if (logic/collisions snake board)
          {:db             (assoc-in db [:snake :snake-state] :snake-collided)
           :dispatch-later [{:ms 200 :dispatch [:blink 5 200]}]}
          {:db (update-game db)})                           ;; no collisions - move the game forward
        {:db db}                                            ;; not running and moving- no change
        ))))


(reg-event-fx
  :blink
  (fn [cofx [_ counter interval]]
    (let [db (:db cofx)]
      (if (> counter 0)
        {:db             (update-in db [:snake :blink-head] not)
         :dispatch-later [{:ms interval :dispatch [:blink (dec counter) interval]}]}
        {:db (assoc-in db [:game-state] :game-finished)}
        ))))


(reg-event-db
  :change-direction
  (fn  [db [_ new-direction]]

    (if (and (game-is-running? db) (snake-is-moving? db))

      (update-in db
                 [:snake :direction]
                 (partial logic/change-snake-direction new-direction))
      db)))


(reg-event-db
  :toggle-pause
  (fn [db _]
    (let [new-state (if (game-is-paused? db) :game-running :game-paused)]
      (log ":toggel-pause. new state is:" new-state "game-is-paused:" (game-is-paused? db) "db:" db)
      (assoc db :game-state new-state))))

(reg-event-db
  :save-state
  (fn [db _]
    (update db :saves #(conj % ["save" (dissoc db :saves)]))))

(reg-event-db
  :load-state
  (fn [db [_ state]]
    (let [saves (:saves db)]
      (assoc state :saves saves))))


(reg-event-db :key-up (fn [db _] (assoc-in db [:snake :direction] [-1 0])))
(reg-event-db :key-right (fn [db _] (assoc-in db [:snake :direction] [0 -1])))
(reg-event-db :key-down (fn [db _] (assoc-in db [:snake :direction] [1 0])))
(reg-event-db :key-left (fn [db _] (assoc-in db [:snake :direction] [0 1])))

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
   [view/render-saves]
   ])


(defn start []
  (js/setInterval #(dispatch [:next-state]) 150))


(defn ^:export run
  "The main app function"
  []
  (dispatch-sync [:initialize])
  (game/update-game-state :running)
  (enable-re-frisk! {:kind->id->handler? true})
  (reagent/render [game]
                  (.getElementById js/document "app"))
  (start))


;;
;; KEY EVENTS
;;

(game/reg-key-bindings :running 32 :toggle-pause)
(game/reg-key-bindings :running 37 :key-up)
(game/reg-key-bindings :running 38 :key-right)
(game/reg-key-bindings :running 39 :key-down)
(game/reg-key-bindings :running 40 :key-left)


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
  :game-state
  (fn [db _]
    (reaction (:game-state @db))))

(reg-sub-raw
  :saves
  (fn [db _]
    (reaction (:saves @db))))